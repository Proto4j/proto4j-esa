package org.proto4j.crypto.android;//@date 28.01.2023

import android.content.Context;
import io.github.proto4j.esa.JarConfiguration;

public interface AndroidJarConfiguration extends JarConfiguration {

    // Required to save the DEX-File
    Context getContext();
}
