package dev.akarah.jvm2df.bytecode;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;

public class JarToClasses {

    public static List<ClassModel> convert(Path jarFile) {
        var classFiles = new ArrayList<ClassModel>();
        try (var jarStream = new JarInputStream(new FileInputStream(jarFile.toFile()))) {
            var entry = jarStream.getNextJarEntry();
            while (entry != null) {
                var classBytes = jarStream.readAllBytes();
                if (classBytes.length == 0) {
                    entry = jarStream.getNextJarEntry();
                    continue;
                }
                var cf = ClassFile.of();
                classFiles.add(cf.parse(classBytes));
                entry = jarStream.getNextJarEntry();
            }
            return classFiles;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
