package com.example.SindhiProgrammingLanguage.Compiler;

public class SindhiToken {
    public enum Type {
        // Added DO keyword
        DECLARE, NUMERIC_TYPE, STRING_TYPE, PRINT, IF, ELSE, WHILE, DO,
        AND_OPERATOR, OR_OPERATOR, OPERATOR,
        NUMBER, STRING, COMMENT,
        IDENTIFIER, EOF
    }

    private final Type type;
    private final String value;
    private final int line;
    private final int column;

    public SindhiToken(Type type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
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

    public boolean is(Type type) {
        return this.type == type;
    }

    public boolean is(Type type, String value) {
        return this.type == type && this.value.equals(value);
    }

    @Override
    public String toString() {
        return "SindhiToken{type=" + type + ", value='" + value + "' [" + line + ":" + column + "]}";
    }
}