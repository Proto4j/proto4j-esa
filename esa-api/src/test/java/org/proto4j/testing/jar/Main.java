package org.proto4j.testing.jar; //@date 24.01.2023

import org.objectweb.asm.Type;
import io.github.proto4j.crypto.provider.PlainTextKeyProvider;
import io.github.proto4j.esa.DefaultBuilder;
import io.github.proto4j.esa.JarConfiguration;
import io.github.proto4j.esa.SharedJar;
import io.github.proto4j.esa.api.SharedJarClassWriter;
import io.github.proto4j.esa.api.EncryptedFieldInfo;
import io.github.proto4j.esa.api.IFieldInfo;
import io.github.proto4j.esa.annotation.Output;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;

public class Main {

    public static void main(String[] args) throws Exception {
        try (FileInputStream fis = new FileInputStream(
                "aesjar-api/build/classes/java/test/org/proto4j/testing/aes/jar/TestClass.class")) {

            FileOutputStream fos = new FileOutputStream("TestClass.class");

            SharedJarClassWriter cw = new SharedJarClassWriter();
            EncryptedFieldInfo efi = IFieldInfo.getEncryptedInstance(
                    Type.getType(String.class), SharedJar.ENCODED,
                    "aovhtvpniuhetort", Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC);

            cw.setClassInfo(new Output.OutputClassInfo(TestClass.class, "1234", efi));
            cw.setExists(true);

            cw.transferTo(fos);
            fos.close();
        }

        SharedJar sharedJar = new DefaultBuilder()
                .setOutputClass(TestClass.class)
                .setProvider(new PlainTextKeyProvider("HelloWorld", "AES"))
                .configure((JarConfiguration) null)
                .finish();


    }
}
