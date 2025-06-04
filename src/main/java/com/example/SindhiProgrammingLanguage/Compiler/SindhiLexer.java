// ✅ FIXED SindhiLexer.java
package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SindhiLexer {
    private static final Map<String, SindhiToken.Type> KEYWORDS;

    static {
        // Sort keywords by descending length to avoid partial match (e.g. لک vs لکيوَ)
        Map<String, SindhiToken.Type> map = new LinkedHashMap<>();
        map.put("لکيوَ", SindhiToken.Type.PRINT);
        map.put("جيستائين", SindhiToken.Type.WHILE);
        map.put("جيڪڏ", SindhiToken.Type.IF);
        map.put("ته", SindhiToken.Type.ELSE);
        map.put("عددي", SindhiToken.Type.NUMERIC_TYPE);
        map.put("لکت", SindhiToken.Type.STRING_TYPE);
        map.put("ڪر", SindhiToken.Type.DO);
        map.put("۽", SindhiToken.Type.AND_OPERATOR);
        map.put("يا", SindhiToken.Type.OR_OPERATOR);
        KEYWORDS = Collections.unmodifiableMap(map);
    }

    private static final Pattern TOKEN_PATTERN;

    static {
        // Build regex pattern dynamically
        String keywordsPattern = String.join("|", KEYWORDS.keySet());
        TOKEN_PATTERN = Pattern.compile(
                "\\s*(?:" +
                        "//[^\\n]*|" +                      // Comments
                        "\\\"(?:\\\\[\\\"\\\\tnr]|[^\\\"\\\\])*\\\"|" + // Strings
                        keywordsPattern + "|" +
                        "لک(?!يوَ)|" +                        // DECLARE keyword with lookahead
                        "\\d+|" +                            // Numbers
                        "[{}]|" +                            // Braces
                        "==|!=|<=|>=|[=<>+\\-*/%()]|" +     // Operators
                        "[\\p{InArabic}a-zA-Z_][\\p{InArabic}a-zA-Z0-9_]*" + // Identifiers
                        ")"
        );
    }

    public List<SindhiToken> tokenize(String input) throws SindhiLexerException {
        List<SindhiToken> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        int line = 1;
        int lineStart = 0;
        int lastMatchEnd = 0;

        while (matcher.find()) {
            if (matcher.start() != lastMatchEnd) {
                throw new SindhiLexerException("Unrecognized token at line " + line + ", column " + (matcher.start() - lineStart + 1));
            }

            String tokenValue = matcher.group().trim();
            if (tokenValue.isEmpty()) {
                lastMatchEnd = matcher.end();
                continue;
            }

            for (int i = lastMatchEnd; i < matcher.start(); i++) {
                if (input.charAt(i) == '\n') {
                    line++;
                    lineStart = i + 1;
                }
            }

            int column = matcher.start() - lineStart + 1;
            tokens.add(createToken(tokenValue, line, column));
            lastMatchEnd = matcher.end();
        }

        tokens.add(new SindhiToken(SindhiToken.Type.EOF, "", line, input.length() - lineStart + 1));
        return tokens;
    }

    private SindhiToken createToken(String value, int line, int column) {
        if (value.equals("لک")) {
            return new SindhiToken(SindhiToken.Type.DECLARE, value, line, column);
        }
        if (KEYWORDS.containsKey(value)) {
            return new SindhiToken(KEYWORDS.get(value), value, line, column);
        }
        if (value.matches("\\d+")) {
            return new SindhiToken(SindhiToken.Type.NUMBER, value, line, column);
        }
        if (value.startsWith("\"")) {
            String raw = value.substring(1, value.length() - 1);
            String decoded = raw
                    .replace("\\\\", "\\")
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\t", "\t");
            return new SindhiToken(SindhiToken.Type.STRING, decoded, line, column);
        }
        if (value.matches("[=<>+\\-*/%(){}]|==|!=|<=|>=")) {
            return new SindhiToken(SindhiToken.Type.OPERATOR, value, line, column);
        }
        return new SindhiToken(SindhiToken.Type.IDENTIFIER, value, line, column);
    }

    public static class SindhiLexerException extends Exception {
        public SindhiLexerException(String message) {
            super(message);
        }
    }
}
