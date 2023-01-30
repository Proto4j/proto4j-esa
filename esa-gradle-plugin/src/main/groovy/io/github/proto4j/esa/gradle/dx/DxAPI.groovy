package io.github.proto4j.esa.gradle.dx

import com.android.dx.dex.cf.CfOptions
import com.android.dx.dex.file.DexFile
import io.github.proto4j.esa.gradle.DexOptionsExtension
import org.gradle.api.GradleException
import org.proto4j.dx.ClassParser
import org.proto4j.dx.DexFactory
import org.proto4j.dx.file.DexOutputStream

import java.nio.file.Files

/**
 * Internal API that stores one central method to create the DEX-file that is
 * equivalent to the generated JAR file.
 *
 * @see io.github.proto4j.esa.gradle.tasks.SharedJarCopyAction
 */
final class DxAPI {

    // no instance allowed
    private DxAPI() {}

    /**
     * Uses the information provided by the {@code DexOptionsExtension} to generate a DEX-file
     * with all classes annotated ith {@code @Shadow}.
     * <p>
     * This method takes the following execution flow:
     * <ol>
     *     <li>Creating all necessary variables (contexts and so on) and the output DexFile. The
     *        DxContext and CfContext variables are needed to translate/optimize the underlying
     *        class file.</li>
     *     <li>Process each file and parse it with DxAPI.createClassFile(). It is important to
     *        set the --no-strict flag, otherwise a ParseException would be thrown. (this option
     *        is set to true by default)</li>
     *     <li>Translate the class file by optimizing it if possible. This action also adds the
     *        returned ClassDefItem to the DexFile</li>
     *     <li>By calling DexFile.toDex(), an array of bytes is created that contains the raw
     *        dex-file. This content is transferred into the JAR-file that will be placed into
     *        the source code</li>
     * </ol>
     *
     * @param dxoe the options to apply when creating the DEX-file
     * @return the generated DEX-file as raw bytes
     */
    static byte[] toDex(DexOptionsExtension dxoe) {
        if (dxoe == null) {
            throw new GradleException("DexExtension == null")
        }
        DexFactory factory = DexFactory.getDefault()

        DexFile dexFile = factory.createDexFile(dxoe.getMinimumSdkVersion())
        ClassParser parser = factory.createClassParser()
        CfOptions cfOptions = new CfOptions()

        cfOptions.optimize = dxoe.shouldOptimize()
        parser.setUseStrictMode(dxoe.getUseStrict())
        try (DexOutputStream dos = factory.newOutputStream(dexFile, parser)) {
            for (DxClassInfo classInfo in dxoe.getClasses()) {
                byte[] content = Files.readAllBytes(classInfo.getFilePath())

                dos.putNextClass(classInfo.getName(), cfOptions)
                dos.write(content)
                dos.closeClass()
            }
            return dos.toByteArray()
        }
    }
}
