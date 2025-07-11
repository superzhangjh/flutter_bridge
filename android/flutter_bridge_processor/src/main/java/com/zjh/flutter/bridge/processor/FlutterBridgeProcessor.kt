package com.zjh.flutter.bridge.processor

import com.zjh.flutter.bridge.processor.model.BridgeModel
import com.zjh.flutter.bridge.processor.model.NativeMethodModel
import com.zjh.flutter.bridge.processor.utils.GeneratorUtils
import com.zjh.flutter.bridge.processor.utils.toSnakeCase
import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.zjh.flutter.bridge.core.annotation.FlutterBridge
import com.zjh.flutter.bridge.core.annotation.FlutterMethod
import com.zjh.flutter.bridge.core.annotation.NativeMethod
import com.zjh.flutter.bridge.core.registry.IFlutterBridgeRegistry
import com.zjh.flutter.bridge.processor.generator.FlutterBridgeGenerator
import com.zjh.flutter.bridge.processor.generator.SpiFileGenerator
import com.zjh.flutter.bridge.processor.model.MethodModel
import java.io.IOException
import java.util.stream.Collectors
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
internal class FlutterBridgeProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
            FlutterBridge::class.java.name,
            NativeMethod::class.java.name,
            FlutterMethod::class.java.name
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: Set<TypeElement?>?, env: RoundEnvironment): Boolean {
        // 1. 收集所有带@FlutterBridge注解的类
        val bridgeClasses: MutableSet<TypeElement> = HashSet()
        for (element in env.getElementsAnnotatedWith(FlutterBridge::class.java)) {
            if (element.kind === ElementKind.CLASS) {
                bridgeClasses.add(element as TypeElement)
            }
        }

        val bridgeModels = mutableListOf<BridgeModel>()

        // 2. 为每个类生成桥接代码
        for (classElement in bridgeClasses) {
            processBridgeClass(classElement)?.let {
                bridgeModels.add(it)
            }
        }

        // 3. 生成注册类的 SPI
        if (bridgeModels.isNotEmpty()) {
            SpiFileGenerator.generateSpiFile(
                processingEnv.filer,
                ClassName.get(IFlutterBridgeRegistry::class.java).canonicalName(),
                *bridgeModels.map { "${it.packageName}.${it.registryClassName}" }.toTypedArray()
            )
        }

        return true
    }

    private fun processBridgeClass(classElement: TypeElement): BridgeModel? {
        // 获取类上的@FlutterBridge注解
        val classAnnotation = classElement.getAnnotation(FlutterBridge::class.java)
        val channelName = classAnnotation.channelName.ifEmpty { classElement.simpleName.toString().toSnakeCase() }

        // 收集该类中所有@NativeMethod方法
        val nativeMethods: MutableList<ExecutableElement> = ArrayList()
        val flutterMethods: MutableList<ExecutableElement> = ArrayList()
        for (enclosed in classElement.enclosedElements) {
            if (enclosed.kind === ElementKind.METHOD) {
                when {
                    enclosed.getAnnotation(NativeMethod::class.java) != null -> {
                        nativeMethods.add(enclosed as ExecutableElement)
                    }
                    enclosed.getAnnotation(FlutterMethod::class.java) != null -> {
                        flutterMethods.add(enclosed as ExecutableElement)
                    }
                }
            }
        }

        if (nativeMethods.isNotEmpty()) {
            try {
                return generateBridgeClasses(classElement, channelName, nativeMethods, flutterMethods)
            } catch (e: IOException) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to generate bridge for " + classElement + ": " + e.message
                )
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun generateBridgeClasses(
        classElement: TypeElement,
        channelName: String,
        nativeMethods: List<ExecutableElement>,
        flutterMethods: List<ExecutableElement>,
    ) : BridgeModel {
        val packageName = (classElement.enclosingElement as PackageElement).qualifiedName.toString()
        val className = classElement.simpleName.toString()

        // 转换方法模型
        val nativeMethodModels = nativeMethods.stream()
            .map { method ->
                if (GeneratorUtils.isPublicConcreteMethod(method)) {
                    this.createNativeMethodModel(method)
                } else {
                    throw IllegalArgumentException("method `${method.simpleName}` is no public concrete !")
                }
            }
            .collect(Collectors.toList())

        val flutterMethodModels = flutterMethods.stream()
            .map { method ->
                if (method.modifiers.contains(Modifier.ABSTRACT)) {
                    this.createFlutterMethodModel(method)
                } else {
                    throw IllegalArgumentException("method `${method.simpleName}` is no abstract !")
                }
            }
            .collect(Collectors.toList())

        // 创建桥接模型
        val model = BridgeModel(
            classElement.modifiers.contains(Modifier.ABSTRACT),
            packageName,
            className,
            channelName,
            nativeMethodModels,
            flutterMethodModels
        )

        // 生成代码
        FlutterBridgeGenerator.generate(model, processingEnv)

        return model
    }

    private fun createNativeMethodModel(method: ExecutableElement): NativeMethodModel {
        val annotation = method.getAnnotation(
            NativeMethod::class.java
        )
        val methodName = annotation.methodName.ifEmpty { method.simpleName.toString() }

        return NativeMethodModel(
            methodName,
            method.simpleName.toString(),
            method.returnType,
            method.parameters,
            annotation.threadMode
        )
    }

    private fun createFlutterMethodModel(method: ExecutableElement): MethodModel {
        val annotation = method.getAnnotation(
            FlutterMethod::class.java
        )
        val methodName = annotation.methodName.ifEmpty { method.simpleName.toString() }

        return MethodModel(
            methodName,
            method.simpleName.toString(),
            method.returnType,
            method.parameters
        )
    }
}