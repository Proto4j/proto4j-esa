package org.proto4j.testing;

import io.github.proto4j.esa.api.ISharedClassInfo;
import io.github.proto4j.esa.api.asm.ClassInfoCollector;
import io.github.proto4j.esa.api.asm.ClassInfoWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ClassPrintTest {

    public static void main(String[] args) throws Exception {
        String path = "build/classes/java/main/Test.class";

        try (FileInputStream fis = new FileInputStream(path)) {
            ClassInfoCollector collector = ClassInfoCollector.collect(new ClassReader(fis));

            ISharedClassInfo info = collector.getClassInfo();
            if (info.isShadowed() || info.hasEncryptedFields()) {
                System.out.println("=== Class: " + info.getSimpleName() + " ===");

                try (FileOutputStream fos = new FileOutputStream("build/shjar/Test.class")) {
                    fis.close();
                    FileInputStream fis2 = new FileInputStream(path);
                    ClassReader reader = new ClassReader(fis2);

                    ClassInfoWriter writer = new ClassInfoWriter(info, new ClassWriter(0));
                    reader.accept(writer, 0);

                    fos.write(writer.getBytes());
                }
            }
        }


    }
}
