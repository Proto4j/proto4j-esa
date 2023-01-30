package io.github.proto4j.esa.gradle

interface ESAPluginSpec {

    public static final String EXTENSION_NAME = "esaConfig"
    public static final String CONFIG_NAME = EXTENSION_NAME

    public static final String GROUP_NAME = "esa"
    public static final String JAR_TASK_NAME = EXTENSION_NAME

    public static final String TEMP_DIR_NAME = "esa/"

    public static final String JAR_BASE_NAME = "esa-compact"

    public static final String DX_EXTENSION_NAME = "dxOpts"
    public static final String DX_FILENAME = "classes.dex"
    public static final String DX_ATTRIBUTE_KEY = "Dex-Location"

}