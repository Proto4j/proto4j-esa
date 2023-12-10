package org.proto4j.crypto.android; //@date 28.01.2023

import android.content.Context;
import dalvik.system.DexClassLoader;
import io.github.proto4j.crypto.ICipher;
import io.github.proto4j.esa.ESAFile;
import io.github.proto4j.esa.JarConfiguration;
import io.github.proto4j.esa.ESABase;
import io.github.proto4j.crypto.provider.KeyProvider;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class AndroidESA extends ESABase {

    private final Map<String, Class<?>> loadedClasses = new HashMap<>();

    AndroidESA(KeyProvider provider, ICipher cipher,
               JarConfiguration configuration) {
        this(provider, cipher, configuration, null);
    }

    AndroidESA(KeyProvider provider, ICipher cipher,
               JarConfiguration configuration, ClassLoader classLoader) {
        super(provider, cipher,
              configuration instanceof AndroidJarConfiguration ? configuration : null,
              classLoader instanceof DexClassLoader ? classLoader : null);
    }

    @Override
    protected Class<?> getClass(String className) {
        if (loadedClasses.containsKey(className)) {
            return loadedClasses.get(className);
        }

        try {
            Class<?> cls = getClassLoader().loadClass(className);
            if (cls != null) {
                loadedClasses.put(className, cls);
            }
            return cls;
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void load(ESAFile aesContent) throws GeneralSecurityException, IOException {
        if (isLoaded()) return;
        if (aesContent == null) {
            return;
        }

        Context context = getConfiguration().getContext();
        byte[] encryptedJar = aesContent.getEncoded().getBytes();
        File cacheDir = context.getCacheDir();
        if ((cacheDir == null) && (cacheDir = context.getDir("dex", 0)) == null) {
            throw new IOException("Could not create cache directory");
        }

        String filename = formatFilename(cacheDir, aesContent.getFilename(), "jar");
        File jarFile = new File(filename);
        if (!jarFile.exists()) {
            if (!jarFile.createNewFile()) {
                throw new IOException("Could not create cached file!");
            }

            prepareCipher(Cipher.DECRYPT_MODE);
            try (FileOutputStream fos = new FileOutputStream(jarFile)) {
                fos.write(cipher.doFinal(encryptedJar));
            }
        }

        File dexFile = new File(formatFilename(cacheDir, aesContent.getFilename(), "dex"));
        if (!dexFile.exists()) {
            if (!dexFile.createNewFile()) {
                throw new IOException("Could not create cache DEX file");
            }

            byte[] dexContent = null;
            try (FileInputStream fis = new FileInputStream(jarFile);
                 JarInputStream jis = new JarInputStream(fis)) {

                JarEntry entry = null;
                while ((entry = jis.getNextJarEntry()) != null) {
                    File file = new File(entry.getName());
                    if (file.getName().equals("classes.dex")) {
                        dexContent = getZipEntryContent(jis);
                    }
                }
            }

            if (dexContent == null) {
                throw new IOException("Invalid jar file");
            }

            try (FileOutputStream fos = new FileOutputStream(dexFile)) {
                fos.write(dexContent);
            }
        }

        classLoader = new DexClassLoader(
                dexFile.getAbsolutePath(), cacheDir.getAbsolutePath(),
                null, context.getClassLoader());

        // cleanup
        context.deleteFile(dexFile.getAbsolutePath());
        context.deleteFile(jarFile.getAbsolutePath());

        setLoaded();
        System.gc();
    }


    @Override
    public DexClassLoader getClassLoader() {
        return (DexClassLoader) super.getClassLoader();
    }

    @Override
    public AndroidJarConfiguration getConfiguration() {
        return (AndroidJarConfiguration) super.getConfiguration();
    }

    private String formatFilename(File dir, String name, String suffix) {
        return String.format("%s/%s.%s", dir, name, suffix);
    }
}
