package io.github.proto4j.esa.gradle

/**
 * Common names and resources used across different classes.
 */
interface ESAPluginSpec {

    /**
     * The extension name used when configuring the ESA file. They can
     * be declared in the {@code build.gradle} file:
     * <pre>
     * esaConfig {
     *     // The key used for encryption
     *     key = 'encryptionKey'
     *
     *     // Android projects might use this property
     *     // to generate DEX-files that will be used instead
     *     createDexFile = true
     * }
     * </pre>
     * @see ESAPluginExtension
     */
    public static final String EXTENSION_NAME = "esaConfig"

    /**
     * @see #EXTENSION_NAME
     */
    public static final String CONFIG_NAME = EXTENSION_NAME

    /**
     * The default task group name
     */
    public static final String GROUP_NAME = "esa-tasks"

    /**
     * The name used to configure the JAR creation task
     */
    public static final String JAR_TASK_NAME = "esa"

    /**
     * @deprecated Used in previous versions of this plugin
     */
    @Deprecated
    public static final String TEMP_DIR_NAME = "esa/"

    /**
     * The default name when storing the archive temporarily.
     */
    public static final String JAR_BASE_NAME = "esa-compact"

    /**
     * The extension name used to configure the DEX-file options. By default,
     * the following options can be controlled in the {@code build.gradle}
     * file:
     * <pre>
     * dxOpts {
     *     // class file version check (to prevent errors, this
     *     // option should be declared as false
     *     useStrict = false
     *
     *     // Use this property when the class files should be
     *     // optimized on translation
     *     optimize = false
     *
     *     // The minimum Android SDK version number
     *     minimumSdkVersion = 26
     * }
     * </pre>
     *
     * @see DexOptionsExtension
     */
    public static final String DX_EXTENSION_NAME = "dxOpts"

    /**
     * The DEX-filename to use.
     */
    public static final String DX_FILENAME = "classes.dex"

    /**
     * Special JAR manifest property that will be added when a DEX-file
     * should be generated.
     */
    public static final String DX_ATTRIBUTE_KEY = "Dex-Location"



}