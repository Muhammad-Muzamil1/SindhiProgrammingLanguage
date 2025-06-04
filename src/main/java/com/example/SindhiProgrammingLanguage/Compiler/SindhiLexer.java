package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SindhiLexer {
    // Update the keyword map with proper ordering
    private static final Map<String, SindhiToken.Type> KEYWORDS = new LinkedHashMap<>() {{
        put("لکيوَ", SindhiToken.Type.PRINT);  // Must come before DECLARE
        put("جيڪڏ", SindhiToken.Type.IF);
        put("ته", SindhiToken.Type.ELSE);
        put("ته جيڪڏ", SindhiToken.Type.ELSE_IF);
        put("جيستائين", SindhiToken.Type.WHILE);
        put("لک", SindhiToken.Type.DECLARE);
        put("عددي", SindhiToken.Type.NUMERIC_TYPE);
        put("لکت", SindhiToken.Type.STRING_TYPE);
        put("ڪر", SindhiToken.Type.DO);
        put("۽", SindhiToken.Type.AND_OPERATOR);
        put("يا", SindhiToken.Type.OR_OPERATOR);
    }};

    // Update token pattern to properly handle all cases
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(" +
                    "//[^\\n]*|" +                          // Comments
                    "\"(?:\\\\[\"\\\\tnr]|[^\"\\\\])*\"|" + // Strings
                    "ته\\s+جيڪڏ|" +                         // ELSE IF
                    "لکيوَ|" +                              // PRINT
                    "لک|" +                                 // DECLARE
                    "عددي|" +                               // NUMERIC_TYPE
                    "لکت|" +                                // STRING_TYPE
                    "جيڪڏ|" +                               // IF
                    "ته|" +                                 // ELSE
                    "جيستائين|" +                           // WHILE
                    "ڪر|" +                                 // DO
                    "۽|" +                                  // AND
                    "يا|" +                                 // OR
                    "\\d+|" +                               // Numbers
                    "[{}]|" +                               // Braces
                    "==|!=|<=|>=|[=<>+\\-*/%()]|" +        // Operators
                    "[\\p{InArabic}a-zA-Z_][\\p{InArabic}a-zA-Z0-9_]*" + // Identifiers
                    ")"
    );

    public List<SindhiToken> tokenize(String input) throws SindhiLexerException {
        List<SindhiToken> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        int pos = 0;
        int line = 1;
        int lineStart = 0;

        while (pos < input.length()) {
            if (!matcher.find(pos) || matcher.start() != pos) {
                // Handle unmatched characters
                int col = pos - lineStart + 1;
                throw new SindhiLexerException(
                        String.format("Unexpected character '%c' at line %d, column %d",
                                input.charAt(pos), line, col));
            }

            String token = matcher.group(1);
            if (token == null || token.isEmpty()) {
                pos = matcher.end();
                continue;
            }

            // Handle newlines for line counting
            for (int i = pos; i < matcher.start(); i++) {
                if (input.charAt(i) == '\n') {
                    line++;
                    lineStart = i + 1;
                }
            }

            int column = matcher.start() - lineStart + 1;
            tokens.add(createToken(token, line, column));
            pos = matcher.end();
        }

        tokens.add(new SindhiToken(SindhiToken.Type.EOF, "", line, input.length() - lineStart + 1));
        return tokens;
    }

    private SindhiToken createToken(String value, int line, int column) {
        if (KEYWORDS.containsKey(value)) {
            return new SindhiToken(KEYWORDS.get(value), value, line, column);
        } else if (value.matches("\\d+")) {
            return new SindhiToken(SindhiToken.Type.NUMBER, value, line, column);
        } else if (value.startsWith("\"")) {
            String unescaped = value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
            return new SindhiToken(SindhiToken.Type.STRING, unescaped, line, column);
        } else if (value.matches("[=<>+\\-*/%(){}]|==|!=|<=|>=")) {
            return new SindhiToken(SindhiToken.Type.OPERATOR, value, line, column);
        } else {
            return new SindhiToken(SindhiToken.Type.IDENTIFIER, value, line, column);
        }
    }

class SindhiLexerException extends Exception {
    public SindhiLexerException(String message) {
        super(message);
    }
}
}