package com.galaxy.flutter.bridge.processor.utils

import java.io.IOException
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

internal object SpiFileGenerator {

    /**
     * 生成SPI，Generate的Service才能读取到
     */
    @Throws(IOException::class)
    fun generateSpiFile(
        filer: Filer,
        interfaceName: String,
        vararg implementationNames: String
    ) {
        // 创建 META-INF/services 文件
        val file = filer.createResource(
            StandardLocation.CLASS_OUTPUT,
            "",
            "META-INF/services/$interfaceName"
        )

        file.openWriter().use { writer ->
            implementationNames.forEach { name ->
                writer.write(name + "\n")
            }
        }
    }

}