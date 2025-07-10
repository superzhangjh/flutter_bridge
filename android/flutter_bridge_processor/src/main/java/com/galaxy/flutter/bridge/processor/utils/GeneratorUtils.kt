package com.galaxy.flutter.bridge.processor.utils

import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier


internal object GeneratorUtils {

    /**
     * 判断方法是否是public且已实现（非抽象）
     */
    fun isPublicConcreteMethod(method: ExecutableElement): Boolean {
        val modifiers: Set<Modifier> = method.modifiers
        return modifiers.contains(Modifier.PUBLIC) &&
                !modifiers.contains(Modifier.ABSTRACT) &&
                !modifiers.contains(Modifier.NATIVE)
    }

}