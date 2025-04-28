package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.Objects;

public class SindhiValue {

    public enum Type {
        STRING, NUMBER
    }

    private final Object value;
    private final Type type;

    private SindhiValue(Object value, Type type) {
        this.value = value;
        this.type = type;
    }

    // Factory methods
    public static SindhiValue string(String val) {
        return new SindhiValue(val, Type.STRING);
    }

    public static SindhiValue number(int val) {
        return new SindhiValue(val, Type.NUMBER);
    }

    // Type checks
    public boolean isNumber() {
        return type == Type.NUMBER;
    }

    public boolean isString() {
        return type == Type.STRING;
    }

    // Get as number
    public int asInt() {
        if (!isNumber()) {
            throw new RuntimeException("Type Error: Expected a NUMBER but got " + type);
        }
        return (int) value;
    }

    // Get as string
    public String asString() {
        return value.toString();
    }

    // Boolean conversion logic
    public boolean asBoolean() {
        if (isNumber()) return asInt() != 0;
        return !asString().isEmpty();
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("SindhiValue{type=%s, value=%s}", type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SindhiValue)) return false;
        SindhiValue other = (SindhiValue) obj;
        return type == other.type && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
