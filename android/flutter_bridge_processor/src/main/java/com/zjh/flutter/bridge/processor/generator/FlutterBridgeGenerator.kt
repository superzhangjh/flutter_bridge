package com.zjh.flutter.bridge.processor.generator

import com.zjh.flutter.bridge.processor.model.BridgeModel
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.zjh.flutter.bridge.core.base.FlutterBridgeDelegate
import com.zjh.flutter.bridge.core.model.ThreadMode
import com.zjh.flutter.bridge.core.registry.IFlutterBridgeRegistry
import com.zjh.flutter.bridge.core.cache.FlutterBridgeService
import com.zjh.flutter.bridge.core.cache.FlutterBridgeCacheManager
import java.io.IOException
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement


internal object FlutterBridgeGenerator {

    @Throws(IOException::class)
    fun generate(model: BridgeModel, env: ProcessingEnvironment) {
        // 生成服务实现类
        val serviceImpl = generateServiceImpl(model)

        // 写入文件
        JavaFile.builder(model.packageName, serviceImpl)
            .build()
            .writeTo(env.filer)

        // 生成注册类
        val registry = generateRegistryClass(model, serviceImpl)

        JavaFile.builder(model.packageName, registry)
            .build()
            .writeTo(env.filer)

        // 生成Delegate接口
        if (model.flutterMethods.isNotEmpty()) {
            val delegate = generateDelegateInterface(model)

            JavaFile.builder(model.packageName, delegate)
                .build()
                .writeTo(env.filer)
        }

        // 生成抽象实现类，避免报错
        if (model.needGenerateImplClass) {
            val delegateImpl = generateBridgeImpl(model)

            JavaFile.builder(model.packageName, delegateImpl)
                .build()
                .writeTo(env.filer)
        }
    }

    private fun generateServiceImpl(model: BridgeModel): TypeSpec {
        val builder = TypeSpec.classBuilder(model.generatedName)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(FlutterBridgeService::class.java))
            .addField(
                FieldSpec.builder(String::class.java, "CHANNEL_NAME")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", model.channelName)
                    .build()
            )

        //添加成员变量
        builder.addField(generateServiceImplField(model))

        //添加构造方法
        builder.addMethod(generateServiceImplConstructor(model))

        //添加通道方法
        builder.addMethod(generateChannelNameMethod())

        //添加方法路由逻辑
        builder.addMethod(generateOnCallMethod(model))

        //添加线程调度
        builder.addMethod(generateResolveThreadModelMethod(model))

        return builder.build()
    }

    private fun generateRegistryClass(model: BridgeModel, serviceImpl: TypeSpec): TypeSpec {
        val bridgeClass = ClassName.get(model.packageName, if (model.needGenerateImplClass) model.implClassName else model.className)

        return TypeSpec.classBuilder(model.registryClassName)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(IFlutterBridgeRegistry::class.java))
            .addMethod(
                MethodSpec.methodBuilder("register")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement(
                        "\$T delegate = new \$T()",
                        bridgeClass,
                        bridgeClass
                    )
                    .addStatement(
                        "\$T.addService(new \$L(delegate))",
                        ClassName.get(FlutterBridgeCacheManager::class.java),
                        serviceImpl.name
                    )
                    .build()
            )
            .build()
    }

    private fun generateDelegateInterface(model: BridgeModel): TypeSpec {
        val builder = TypeSpec.interfaceBuilder(model.delegateInterfaceName)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(FlutterBridgeDelegate::class.java))

        for (method in model.flutterMethods) {
            builder.addMethod(
                MethodSpec.methodBuilder(method.methodName)
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .addParameters(method.parameters.map {
                        ParameterSpec.get(it)
                    })
                    .build()
            )
        }

        return builder.build()
    }

    private fun generateBridgeImpl(model: BridgeModel): TypeSpec {
        val builder = TypeSpec.classBuilder(model.implClassName)
            .addModifiers(Modifier.PUBLIC)
            .superclass(ClassName.get(model.packageName, model.className))

        for (method in model.flutterMethods) {
            builder.addMethod(
                MethodSpec.methodBuilder(method.methodName)
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(method.parameters.map {
                        ParameterSpec.get(it)
                    })
                    //禁止直接调用flutterMethod方法
                    .addStatement("throw new RuntimeException(\"Please do not call the flutter method named '${method.methodName}', " +
                            "instead use the method corresponding to '${model.delegateInterfaceName}' \")")
                    .build()
            )
        }
        return builder.build()
    }

    private fun generateServiceImplField(model: BridgeModel): FieldSpec {
        val builder = FieldSpec.builder(ClassName.get(model.packageName, model.className), model.implInstanceName)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        return builder.build()
    }

    private fun generateServiceImplConstructor(model: BridgeModel): MethodSpec {
        val builder: MethodSpec.Builder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get(model.packageName, model.className), model.implInstanceName)
            .addStatement("this.${model.implInstanceName} = ${model.implInstanceName}")

        return builder.build()
    }

    private fun generateChannelNameMethod(): MethodSpec {
        val builder: MethodSpec.Builder = MethodSpec.methodBuilder("channelName")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(ClassName.get(String::class.java))
            .addStatement("return CHANNEL_NAME")

        return builder.build()
    }

    private fun generateOnCallMethod(model: BridgeModel): MethodSpec {
        val builder: MethodSpec.Builder = MethodSpec.methodBuilder("onCallNativeMethod")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String::class.java, "methodName")
            .addParameter(Array<Any>::class.java, "args")
            .returns(TypeName.OBJECT)

        builder.beginControlFlow("switch (methodName)")

        for (method in model.nativeMethods) {
            if (method.isReturnVoid()) {
                builder.addStatement(
                    "case \$S:\nthis.${model.implInstanceName}.\$L(\$L);\nreturn Void.TYPE",
                    method.methodName,
                    method.originalName,
                    generateMethodArgs(method.parameters)
                )
            } else {
                builder.addStatement(
                    "case \$S:\nreturn this.${model.implInstanceName}.\$L(\$L)",
                    method.methodName,
                    method.originalName,
                    generateMethodArgs(method.parameters)
                )
            }
        }

        builder.addStatement(
            "default:\nthrow new \$T(\$S + methodName)",
            IllegalArgumentException::class.java,
            "Unknown method: "
        )
            .endControlFlow()

        return builder.build()
    }

    private fun generateResolveThreadModelMethod(model: BridgeModel): MethodSpec {
        val threadModeClass = ClassName.get(ThreadMode::class.java)

        val builder: MethodSpec.Builder = MethodSpec.methodBuilder("resolveThreadMode")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String::class.java, "methodName")
            .returns(threadModeClass)

        builder.beginControlFlow("switch (methodName)")

        for (method in model.nativeMethods) {
            builder.addStatement(
                "case \$S:\nreturn \$T.${method.threadMode.name}",
                method.methodName,
                threadModeClass
            )
        }

        builder.addStatement(
            "default:\nthrow new \$T(\$S + methodName)",
            IllegalArgumentException::class.java,
            "Unknown method: "
        )
            .endControlFlow()

        return builder.build()
    }

    private fun generateMethodArgs(parameters: List<VariableElement>): String {
        return IntStream.range(0, parameters.size)
            .mapToObj { i: Int ->
                val type = parameters[i].asType()
                val typeName = type.toString()
                val arg = "args[$i]"

                // 基本类型需要特殊处理
                if (type.kind.isPrimitive) {
                    return@mapToObj "($typeName)$arg"
                }
                "($typeName)$arg"
            }
            .collect(Collectors.joining(", "))
    }

}