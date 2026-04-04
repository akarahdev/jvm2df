package dev.akarah.jvm2df.generator.gen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CodeBlocksAPIGenerator implements ResourceGenerator {
    public static CodeBlocksAPIGenerator INSTANCE = new CodeBlocksAPIGenerator();

    List<String> codeBlocks = List.of(
            "control",
            "playerAction",
            "ifPlayer",
            "ifVar",
            "setVar",
            "selectObject",
            "entityAction",
            "ifEntity",
            "gameAction"
    );

    List<String> variableYieldingCodeBlocks = List.of(
            "setVar"
    );

    Map<String, String> typesToAccountFor = Map.ofEntries(
            Map.entry("I", "int"),
            Map.entry("L", "long"),
            Map.entry("Z", "boolean"),
            Map.entry("F", "float"),
            Map.entry("D", "double"),
            Map.entry("C", "char"),
            Map.entry("R", "Object")
    );

    @Override
    public void generate() throws IOException {
        var sb = new StringBuilder();
        sb.append("package diamondfire.internal;");
        sb.append("\n");
        sb.append("\n");
        sb.append("public class CodeBlocks {");
        for (var codeBlock : codeBlocks) {
            for (int args = 0; args < 10; args++) {
                generateMethod(
                        sb,
                        codeBlock,
                        "void",
                        "",
                        args
                );
                if (variableYieldingCodeBlocks.contains(codeBlock)) {
                    for (var typeEntry : typesToAccountFor.entrySet()) {
                        generateMethod(
                                sb,
                                codeBlock,
                                typeEntry.getValue(),
                                typeEntry.getKey(),
                                args
                        );
                    }
                }
            }
        }
        sb.append("""
                    public static native void openNormal();
                
                    public static native void closeNormal();
                
                    public static native <T> void ret(T obj);
                """);
        sb.append("}");

        Files.writeString(
                Path.of("../stdlib/src/patched/java/diamondfire/internal/CodeBlocks.java"),
                sb
        );
    }

    private void generateMethod(StringBuilder sb, String codeBlock, String returnType, String typeTag, int args) {
        if (!returnType.equals("Object")) {
            sb.append("    public static native ")
                    .append(returnType)
                    .append(" ")
                    .append(codeBlock)
                    .append(typeTag)
                    .append("(");
            for (int argCount = 0; argCount < args; argCount++) {
                sb.append("Object arg").append(argCount);
                if (argCount != args - 1) {
                    sb.append(",");
                }
            }
            sb.append(");\n");
        } else {
            sb.append("    public static native <T> T ")
                    .append(codeBlock)
                    .append("R(");
            for (int argCount = 0; argCount < args; argCount++) {
                sb.append("Object arg").append(argCount);
                if (argCount != args - 1) {
                    sb.append(",");
                }
            }
            sb.append(");\n");
        }
    }
}
