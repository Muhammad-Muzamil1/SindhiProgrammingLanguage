package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.Objects;

/**
 * Represents a token in the Sindhi programming language.
 * Contains type, value, and position information for error reporting.
 */
public class SindhiToken {
    /**
     * Enumeration of all possible token types in the Sindhi language.
     */
    public enum Type {
        // Control structures
        IF("جيڪڏ"),
        ELSE("ته"),
        ELSE_IF("ته جيڪڏ"),
        WHILE("جيستائين"),

        // I/O
        PRINT("لکيوَ"),

        // Declaration keywords
        DECLARE("لک"),
        NUMERIC_TYPE("عددي"),
        STRING_TYPE("لکت"),
        DO("ڪر"),

        // Literals
        STRING,
        NUMBER,

        // Identifiers
        IDENTIFIER,

        // Operators
        OPERATOR,
        AND_OPERATOR("۽"),
        OR_OPERATOR("يا"),

        // Special
        EOF;

        private final String sindhiKeyword;

        Type() {
            this.sindhiKeyword = null;
        }

        Type(String sindhiKeyword) {
            this.sindhiKeyword = sindhiKeyword;
        }

        public String getSindhiKeyword() {
            return sindhiKeyword;
        }

        public boolean isKeyword() {
            return sindhiKeyword != null;
        }
    }

    private final Type type;
    private final String value;
    private final int line;
    private final int column;

    /**
     * Creates a new token with position information.
     *
     * @param type The token type
     * @param value The token value
     * @param line The line number where the token appears (1-based)
     * @param column The column number where the token starts (1-based)
     */
    public SindhiToken(Type type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    /**
     * Creates a new token without position information (for testing).
     */
    public SindhiToken(Type type, String value) {
        this(type, value, -1, -1);
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Returns true if this token has position information.
     */
    public boolean hasPosition() {
        return line > 0 && column > 0;
    }

    /**
     * Returns true if this token matches the given type and value.
     */
    public boolean is(Type type, String value) {
        return this.type == type && this.value.equals(value);
    }

    /**
     * Returns true if this token matches the given type.
     */
    public boolean is(Type type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        String pos = hasPosition() ? String.format(" [%d:%d]", line, column) : "";
        return String.format("SindhiToken{type=%s, value='%s'%s}", type, value, pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SindhiToken)) return false;
        SindhiToken other = (SindhiToken) obj;
        return type == other.type &&
                Objects.equals(value, other.value) &&
                line == other.line &&
                column == other.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, line, column);
    }
}