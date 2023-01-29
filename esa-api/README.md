# ESA-API

This directory contains the platform-independent module with all main functionalities. To integrate a new `SharedJar` to your project, a few steps have to be done:

1. Specify an output class where the encrypted JAR file will be stored:

    ```java
    @Output
    class JarStorage {
        // Put your code here except from two static fields that will
        // be added, named 'filename' and 'encodedJar'. 
    }
    ```
   The class will be transformed to the following structure (make sure there is a default constructor):

    ```java
    @Output
    class JarStorage implements ESAFile {
        public static final String filename = "<Your filename>";
        public static final String encodedJar = "<encrypted content>";
   
        @Override
        public String getFilename() { return filename; }
   
        @Override
        public String getEncoded() { return encodedJar; }
    }
    ```

2. Annotate classes that should be included in the shadowed file with `@Shadow`. Add the `@Relocate` annotation the place the class into another directory within the jar file:

   ```java
    @Shadow
    class Foo {
        public static String getMessage() {
            return "Hello World";
        } 
    }
    ```