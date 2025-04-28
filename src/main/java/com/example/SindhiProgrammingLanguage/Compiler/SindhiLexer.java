package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SindhiLexer {
    private static final Map<String, SindhiToken.Type> KEYWORDS = Map.of(
            "لکيوَ", SindhiToken.Type.PRINT,
            "جيڪڏ", SindhiToken.Type.IF,
            "پو", SindhiToken.Type.ELSE,
            "جيستائين", SindhiToken.Type.WHILE,
            "لک", SindhiToken.Type.DECLARE,
            "عددي", SindhiToken.Type.NUMERIC_TYPE,
            "لکت", SindhiToken.Type.STRING_TYPE,
            "ڪر", SindhiToken.Type.DO
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(" +
                    // Comments (single line)
                    "//[^\\n]*" +
                    "|" +
                    // Keywords
                    String.join("|", KEYWORDS.keySet()) +
                    "|" +
                    // Numeric literals
                    "\\d+" +
                    "|" +
                    // String literals with escape sequences
                    "\"(?:\\\\[\"\\\\tnr]|[^\"\\\\])*\"" +
                    "|" +
                    // Operators and punctuation
                    "==|!=|<=|>=|[=<>+\\-*/%()]" +
                    "|" +
                    // Identifiers (Sindhi + English)
                    "[\\p{InArabic}a-zA-Z_][\\p{InArabic}a-zA-Z0-9_]*" +
                    ")"
    );

    public List<SindhiToken> tokenize(String input) throws SindhiLexerException {
        List<SindhiToken> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        int lastEnd = 0;
        int lineNumber = 1;
        int lineStart = 0;

        while (matcher.find()) {
            // Track line numbers for better error reporting
            String between = input.substring(lastEnd, matcher.start());
            for (int i = 0; i < between.length(); i++) {
                if (between.charAt(i) == '\n') {
                    lineNumber++;
                    lineStart = lastEnd + i + 1;
                }
            }

            if (matcher.start() > lastEnd) {
                String gap = input.substring(lastEnd, matcher.start()).trim();
                if (!gap.isEmpty() && !gap.startsWith("//")) {
                    int col = lastEnd - lineStart + 1;
                    throw new SindhiLexerException(
                            String.format("Unexpected characters '%s' at line %d, column %d",
                                    gap, lineNumber, col)
                    );
                }
            }

            String token = matcher.group(1).trim();
            if (token.isEmpty() || token.startsWith("//")) {
                lastEnd = matcher.end();
                continue;
            }

            try {
                int column = matcher.start() - lineStart + 1;

                if (KEYWORDS.containsKey(token)) {
                    tokens.add(new SindhiToken(KEYWORDS.get(token), token, lineNumber, column));
                } else if (token.matches("\\d+")) {
                    tokens.add(new SindhiToken(SindhiToken.Type.NUMBER, token, lineNumber, column));
                } else if (token.startsWith("\"")) {
                    String stringVal = token.substring(1, token.length() - 1)
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .replace("\\n", "\n")
                            .replace("\\r", "\r")
                            .replace("\\t", "\t");
                    tokens.add(new SindhiToken(SindhiToken.Type.STRING, stringVal, lineNumber, column));
                } else if (token.matches("==|!=|<=|>=|[=<>+\\-*/%()]")) {
                    tokens.add(new SindhiToken(SindhiToken.Type.OPERATOR, token, lineNumber, column));
                } else if (token.matches("[\\p{InArabic}a-zA-Z_][\\p{InArabic}a-zA-Z0-9_]*")) {
                    tokens.add(new SindhiToken(SindhiToken.Type.IDENTIFIER, token, lineNumber, column));
                } else {
                    throw new SindhiLexerException("Cannot classify token: " + token);
                }
            } catch (Exception e) {
                throw new SindhiLexerException(
                        String.format("Error processing token '%s' at line %d: %s",
                                token, lineNumber, e.getMessage())
                );
            }

            lastEnd = matcher.end();
        }

        // Check for trailing characters
        if (lastEnd < input.length()) {
            String remaining = input.substring(lastEnd).trim();
            if (!remaining.isEmpty() && !remaining.startsWith("//")) {
                int col = lastEnd - lineStart + 1;
                throw new SindhiLexerException(
                        String.format("Unexpected trailing characters '%s' at line %d, column %d",
                                remaining, lineNumber, col)
                );
            }
        }

        tokens.add(new SindhiToken(SindhiToken.Type.EOF, "", lineNumber, input.length() + 1));
        return tokens;
    }

class SindhiLexerException extends Exception {
    public SindhiLexerException(String message) {
        super(message);
    }
}
}