package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SindhiLexer {
    // Update the keyword map with proper ordering
    private static final Map<String, SindhiToken.Type> KEYWORDS = Map.ofEntries(
            Map.entry("لکيوَ", SindhiToken.Type.PRINT),
            Map.entry("جيڪڏ", SindhiToken.Type.IF),
            Map.entry("ته", SindhiToken.Type.ELSE),
            Map.entry("جيستائين", SindhiToken.Type.WHILE),
            Map.entry("عددي", SindhiToken.Type.NUMERIC_TYPE),
            Map.entry("لکت", SindhiToken.Type.STRING_TYPE),
            Map.entry("ڪر", SindhiToken.Type.DO),
            Map.entry("۽", SindhiToken.Type.AND_OPERATOR),
            Map.entry("يا", SindhiToken.Type.OR_OPERATOR)
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(?:" +
                    "//[^\\n]*|" +                          // Comments
                    "\"(?:\\\\[\"\\\\tnr]|[^\"\\\\])*\"|" + // Strings
                    "لک(?!يوَ)|" +                          // DECLARE (not followed by يوَ)
                    String.join("|", KEYWORDS.keySet()) + "|" +
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
                throw new SindhiLexerException("Unexpected character at line " + line);
            }

            String token = matcher.group();
            int column = matcher.start() - lineStart + 1;

            if (token.trim().isEmpty() || token.startsWith("//")) {
                pos = matcher.end();
                continue;
            }

            tokens.add(createToken(token, line, column));
            pos = matcher.end();
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
            return new SindhiToken(SindhiToken.Type.STRING,
                    value.substring(1, value.length()-1), line, column);
        }
        if (value.matches("[=<>+\\-*/%(){}]|==|!=|<=|>=")) {
            return new SindhiToken(SindhiToken.Type.OPERATOR, value, line, column);
        }
        return new SindhiToken(SindhiToken.Type.IDENTIFIER, value, line, column);
    }

class SindhiLexerException extends Exception {
    public SindhiLexerException(String message) {
        super(message);
    }
}
}