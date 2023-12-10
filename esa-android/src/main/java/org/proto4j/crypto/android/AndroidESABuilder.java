package org.proto4j.crypto.android; //@date 28.01.2023

import io.github.proto4j.esa.ESAFile;
import io.github.proto4j.esa.AbstractESABuilder;
import io.github.proto4j.esa.JarConfiguration;
import io.github.proto4j.esa.ESA;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

public final class AndroidESABuilder
        extends AbstractESABuilder<AndroidESABuilder> {

    @Override
    public AndroidESABuilder configure(JarConfiguration configuration) {
        this.configuration = configuration instanceof AndroidJarConfiguration
                ? (AndroidJarConfiguration) configuration : null;
        return this;
    }

    @Override
    public ESA finish() {
        Objects.requireNonNull(configuration, "config");
        Objects.requireNonNull(keyProvider, "provider");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(cipher, "cipher");

        ESA jar = new AndroidESA(keyProvider, cipher, configuration);

        if (!(content instanceof ESAFile)) {
            throw new ClassFormatError("Invalid output object of type " + content.getClass().getName());
        }

        try {
            jar.load((ESAFile) content);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return jar;
    }

    @Override
    protected AndroidESABuilder this0() {
        return this;
    }
}
