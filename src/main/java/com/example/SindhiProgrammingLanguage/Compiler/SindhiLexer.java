package com.example.SindhiProgrammingLanguage.Compiler;
import java.util.*;
import java.util.regex.*;
import java.text.Normalizer;

public class SindhiLexer {
    private static final List<TokenRule> TOKEN_RULES = Arrays.asList(
            // 1. Comments
            new TokenRule(SindhiToken.Type.COMMENT, "//.*"),

            // 2. Strings (must come before keywords)
            new TokenRule(SindhiToken.Type.STRING, "\"([^\"\\\\]|\\\\[\"\\\\tnr])*\"", true),

            // 3. Keywords (using negative lookahead for word boundaries)
            new TokenRule(SindhiToken.Type.DECLARE, "لک(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.NUMERIC_TYPE, "عددي(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.STRING_TYPE, "لکت(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.PRINT, "لکيوَ(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.IF, "جيڪڏ(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.ELSE, "ته(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.WHILE, "جيستائين(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.DO, "ڪر(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.AND_OPERATOR, "۽(?!\\p{L})"),
            new TokenRule(SindhiToken.Type.OR_OPERATOR, "يا(?!\\p{L})"),

            // 4. Numbers
            new TokenRule(SindhiToken.Type.NUMBER, "\\d+"),

            // 5. Operators
            new TokenRule(SindhiToken.Type.OPERATOR, "\\?\\:|\\?|:|\\.\\.|==|!=|<=|>=|\\(|\\)|[=<>+\\-*/%{}]"),

            // 6. Identifiers (last to avoid keyword conflicts)
            new TokenRule(SindhiToken.Type.IDENTIFIER, "[\\p{L}\\p{M}_][\\p{L}\\p{M}0-9_]*")
    );

    private static class TokenRule {
        final SindhiToken.Type type;
        final Pattern pattern;
        final boolean unescape;

        TokenRule(SindhiToken.Type type, String regex) {
            this(type, regex, false);
        }

        TokenRule(SindhiToken.Type type, String regex, boolean unescape) {
            this.type = type;
            this.pattern = Pattern.compile("^(" + regex + ")", Pattern.UNICODE_CHARACTER_CLASS);
            this.unescape = unescape;
        }
    }

    public List<SindhiToken> tokenize(String input) {
        // Normalize Unicode and handle BOM
        input = Normalizer.normalize(input, Normalizer.Form.NFC).replace("\uFEFF", "");

        List<SindhiToken> tokens = new ArrayList<>();
        int line = 1;
        int column = 1;
        int pos = 0;
        int inputLength = input.length();

        while (pos < inputLength) {
            char current = input.charAt(pos);

            // Handle all whitespace types
            if (Character.isWhitespace(current)) {
                if (current == '\n') {
                    line++;
                    column = 1;
                    pos++;
                    continue;
                } else if (current == '\r') {
                    if (pos + 1 < inputLength && input.charAt(pos + 1) == '\n') {
                        pos += 2;
                    } else {
                        pos++;
                    }
                    line++;
                    column = 1;
                    continue;
                }
                column++;
                pos++;
                continue;
            }

            boolean matched = false;
            String substr = input.substring(pos);
            for (TokenRule rule : TOKEN_RULES) {
                Matcher matcher = rule.pattern.matcher(substr);
                if (matcher.find()) {
                    String value = matcher.group(1);

                    // Skip comments
                    if (rule.type == SindhiToken.Type.COMMENT) {
                        // Handle newlines in comments
                        int newlineCount = 0;
                        int lastNewlinePos = -1;
                        for (int i = 0; i < value.length(); i++) {
                            if (value.charAt(i) == '\n') {
                                newlineCount++;
                                lastNewlinePos = i;
                            }
                        }

                        if (newlineCount > 0) {
                            line += newlineCount;
                            column = value.length() - lastNewlinePos;
                        } else {
                            column += value.length();
                        }
                        pos += value.length();
                        matched = true;
                        break;
                    }

                    // Unescape strings
                    if (rule.unescape) {
                        value = unescapeString(value);
                    }

                    tokens.add(new SindhiToken(rule.type, value, line, column));

                    // Update position
                    int tokenLength = matcher.group(1).length();
                    int lastNewlinePos = value.lastIndexOf('\n');
                    if (lastNewlinePos >= 0) {
                        line += countOccurrences(value, '\n');
                        column = tokenLength - lastNewlinePos;
                    } else {
                        column += tokenLength;
                    }

                    pos += tokenLength;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                // Enhanced diagnostic information
                String context = substr.length() > 20 ? substr.substring(0, 20) + "..." : substr;
                throw new RuntimeException("Unexpected token at line " + line + ", column " + column +
                        ": '" + escapeChar(current) + "' (0x" +
                        Integer.toHexString(current) + "), near: '" + context + "'");
            }
        }

        tokens.add(new SindhiToken(SindhiToken.Type.EOF, "", line, column));
        return tokens;
    }

    private String unescapeString(String value) {
        return value.substring(1, value.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r");
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    private String escapeChar(char c) {
        switch (c) {
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\t': return "\\t";
            case '\"': return "\\\"";
            case '\\': return "\\\\";
            default: return String.valueOf(c);
        }
    }
}