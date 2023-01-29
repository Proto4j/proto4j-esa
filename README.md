Proto4j-ESA (Embedded Shared Archive)
=====================================

<a href="https://github.com/Proto4j/proto4j-esa">
<img src="https://github.com/Proto4j/proto4j-esa/blob/master/docs/esa_logo1.svg" alt="" height="180px" align="right" />
</a>

Runtime execution of embedded shared archive (ESA) files. 

Shields...
 
Proto4j-ESA is a code generation and transformation library that can create embedded shared archive (ESA) files that can be loaded and executed during the runtime of a Java application. This project is not limited to Java applications but can also be integrated in the building process of an Android app. Furthermore, Proto4j-ESA offers a convenient API to interact with runtime-defined classes (invoke methods, query fields and create instances).

In order to use/create an embedded shared archive, one does not require an understanding of Java byte code or the class file format. To include classes in a generated embedded JAR file, they just have to be annotated with an annotation. In addition, the destination class, where all contents of the embedded JAR file will be stored, can be chosen by annotating it with a special annotation.

Proto4j-ESA is written in Java 8 and can be marked as a leigh-weight library, which only depends on the visitor API of the Java byte code parser library [ASM](https://asm.ow2.io/) and the [Proto4j-Dx](https://github.com/Proto4j/proto4j-dx/)-API in order to create DEX-files for Android projects.

The main goal of this repository was to create a simple API that can generate an encrypted JAR file with classes that have been marked to be included in the output JAR file. If the compilation process has finished, the marked classes will be transformed and finally wrapped into the final JAR. As this step is part of the general building process, a simple [Gradle](https://gradle.org/)-Plugin has been developed with [Groovy](https://groovy-lang.org/). 

## Embedded Shared Archive (ESA)

When speaking of an embedded shared archive (ESA), an encrypted JAR file that is stored inside a small Java class file is referenced. Although, an ESA does not contain any other resources than the standard manifest file by default, it is possible that a generated DEX-file is stored inside. It is required when working on Android projects. Generated and encrypted JAR files will be stored in Java classes that implement the `ESAFile` interface.

To encrypt generated JAR files, a custom implementation of an AES-Cipher will be used. It is also possible to use your own cipher implementations, because the default one does symmetric crypto operations (one key). Make sure, the `ICipher` implementation is available for both, runtime environment and gradle-plugin.

## Hello World

Receiving _Hello World_ by invoking a shared method is rather simple. The first step of each project is to decide which classes are _shared_, so they will be invisible at runtime. In this case, the class `MessageUtil` will be shared:  

```java
// This annotation makes the class invisible at runtime
@Shadow 
class MessageUtil {
    public static String getMessage() {
        return "Hello World!";
    }
}
```

