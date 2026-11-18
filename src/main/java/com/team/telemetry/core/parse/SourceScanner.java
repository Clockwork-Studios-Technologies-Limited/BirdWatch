package com.team.telemetry.core.parse;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Gabriella contributed this code: added the source-cleaning and method-declaration patterns
/** Lightweight scan of Java source text for method/constructor declarations (no BlueJ needed). */
public class SourceScanner {
    private static final Set<String> NOT_METHODS = Set.of(
            "if", "for", "while", "switch", "catch", "synchronized", "try", "do", "else", "return",
            "new", "super", "this", "throw");
    private static final Set<String> NOT_RETURN_TYPES = Set.of("new", "return", "else", "throw");

    private static final Pattern STRING_LITERAL = Pattern.compile("\"(?:\\\\.|[^\"\\\\\\n])*\"");
    private static final Pattern BLOCK_COMMENT = Pattern.compile("(?s)/\\*.*?\\*/");
    private static final Pattern LINE_COMMENT = Pattern.compile("//[^\\n]*");

    // [modifiers] [<generics>] [returnType] name(params) [throws ...] {   -- brace may be on the next line
    private static final Pattern METHOD = Pattern.compile(
            "(?m)^[ \\t]*(?:(?:public|protected|private|static|final|abstract|synchronized|native|default)\\s+)*"
                    + "(?:<[^>]+>\\s*)?(?:([\\w<>\\[\\]?,.]+)\\s+)?(\\w+)\\s*\\([^;{}()]*\\)\\s*"
                    + "(?:throws\\s+[\\w.,\\s]+)?\\{");

    // Gabriella contributed this code: added method name extraction from source text
    /**
     * Finds likely method and constructor declarations in Java source text.
     *
     * @param source source text to scan
     * @return unique declaration names in first-seen order, or an empty set for null or empty input
     */
    public Set<String> findMethodNames(String source) {
        Set<String> names = new LinkedHashSet<>();
        if (source == null || source.isEmpty()) {
            return names;
        }
        String cleaned = LINE_COMMENT.matcher(BLOCK_COMMENT.matcher(
                STRING_LITERAL.matcher(source).replaceAll("\"\"")).replaceAll(" ")).replaceAll(" ");
        Matcher m = METHOD.matcher(cleaned);
        while (m.find()) {
            String returnType = m.group(1);
            String name = m.group(2);
            if (NOT_METHODS.contains(name) || (returnType != null && NOT_RETURN_TYPES.contains(returnType))) {
                continue;
            }
            names.add(name);
        }
        return names;
    }
}
