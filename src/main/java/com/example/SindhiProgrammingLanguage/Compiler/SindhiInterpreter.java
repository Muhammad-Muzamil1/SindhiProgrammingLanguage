package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.*;

public class SindhiInterpreter {
    private final List<SindhiToken> tokens;
    private int pos = 0;
    private final Map<String, Object> variables = new HashMap<>();

    public SindhiInterpreter(List<SindhiToken> tokens) {
        this.tokens = tokens;
    }

    public String interpret() {
        StringBuilder output = new StringBuilder();
        try {
            while (!match(SindhiToken.Type.EOF)) {
                String statementOutput = statement();
                if (statementOutput != null && !statementOutput.isEmpty()) {
                    output.append(statementOutput);
                    if (!statementOutput.endsWith("\n")) {
                        output.append("\n");
                    }
                }
            }
        } catch (Exception e) {
            SindhiToken currentToken = pos < tokens.size() ? tokens.get(pos) : null;
            String errorLocation = currentToken != null && currentToken.hasPosition() ?
                    String.format(" at line %d, column %d", currentToken.getLine(), currentToken.getColumn()) :
                    " at position " + pos;

            System.err.println("Runtime Error" + errorLocation + ": " + e.getMessage());
            return output.toString() + "\nError occurred during interpretation.";
        }
        return output.toString();
    }

    private String statement() {
        if (match(SindhiToken.Type.EOF)) return "";

        SindhiToken current = peek();

        if (current.is(SindhiToken.Type.DECLARE)) {
            return declaration();
        } else if (current.is(SindhiToken.Type.PRINT)) {
            return print();
        } else if (current.is(SindhiToken.Type.IF)) {
            return ifCondition();
        } else if (current.is(SindhiToken.Type.WHILE)) {
            return whileLoop();
        } else if (current.is(SindhiToken.Type.DO)) {
            return doStatement();
        }

        throw new RuntimeException("Unknown statement starting with: " + current);
    }

    private String declaration() {
        consume(SindhiToken.Type.DECLARE); // consume 'لک'
        SindhiToken typeToken = peek();

        if (!typeToken.is(SindhiToken.Type.NUMERIC_TYPE) && !typeToken.is(SindhiToken.Type.STRING_TYPE)) {
            throw new RuntimeException("Expected type (عددي or لکت) after لک");
        }
        consume(typeToken.getType()); // consume type

        String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
        consume(SindhiToken.Type.OPERATOR, "="); // consume '='

        Object value = evaluateExpression();

        // Type checking
        if (typeToken.is(SindhiToken.Type.NUMERIC_TYPE)) {
            if (!(value instanceof Integer)) {
                try {
                    value = Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Expected integer for variable '" + varName + "', got: " + value);
                }
            }
        } else if (typeToken.is(SindhiToken.Type.STRING_TYPE)) {
            value = value.toString();
        }

        variables.put(varName, value);
        return "";
    }

    private String print() {
        consume(SindhiToken.Type.PRINT); // consume 'لکيوَ'
        Object value = evaluateExpression();
        String output = value != null ? value.toString() : "null";
        return output;
    }

    private Object evaluateExpression() {
        SindhiToken token = peek();

        if (token.is(SindhiToken.Type.STRING)) {
            return consume(SindhiToken.Type.STRING).getValue();
        } else if (token.is(SindhiToken.Type.NUMBER)) {
            return Integer.parseInt(consume(SindhiToken.Type.NUMBER).getValue());
        } else if (token.is(SindhiToken.Type.IDENTIFIER)) {
            String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(varName)) {
                throw new RuntimeException("Variable '" + varName + "' not found");
            }
            return variables.get(varName);
        } else if (token.is(SindhiToken.Type.OPERATOR, "(")) {
            consume(SindhiToken.Type.OPERATOR, "(");
            Object result = evaluateExpression();
            consume(SindhiToken.Type.OPERATOR, ")");
            return result;
        }

        throw new RuntimeException("Invalid expression starting with: " + token);
    }

    private boolean evaluateBooleanCondition() {
        Object left = evaluateExpression();
        String operator = consume(SindhiToken.Type.OPERATOR).getValue();
        Object right = evaluateExpression();

        if (!(left instanceof Comparable) || !(right instanceof Comparable)) {
            throw new RuntimeException("Cannot compare values: " + left + " and " + right);
        }

        @SuppressWarnings("unchecked")
        int compareResult = ((Comparable<Object>)left).compareTo(right);

        return switch (operator) {
            case ">" -> compareResult > 0;
            case "<" -> compareResult < 0;
            case ">=" -> compareResult >= 0;
            case "<=" -> compareResult <= 0;
            case "==" -> compareResult == 0;
            case "!=" -> compareResult != 0;
            default -> throw new RuntimeException("Unsupported operator: " + operator);
        };
    }

    private String ifCondition() {
        consume(SindhiToken.Type.IF); // consume 'جيڪڏ'
        boolean condition = evaluateBooleanCondition();
        consume(SindhiToken.Type.ELSE); // consume 'پو'

        if (condition) {
            return statement();
        } else {
            // Skip the statement if condition is false
            int currentPos = pos;
            try {
                statement();
            } catch (Exception e) {
                pos = currentPos; // Restore position if skipping fails
            }
            return "";
        }
    }

    private String whileLoop() {
        consume(SindhiToken.Type.WHILE); // consume 'جيستائين'
        int conditionStart = pos;
        boolean condition = evaluateBooleanCondition();
        consume(SindhiToken.Type.ELSE); // consume 'پو'
        int bodyStart = pos;

        StringBuilder output = new StringBuilder();
        while (condition) {
            // Execute body
            pos = bodyStart;
            String statementOutput = statement();
            if (statementOutput != null) {
                output.append(statementOutput);
            }

            // Re-evaluate condition
            pos = conditionStart;
            condition = evaluateBooleanCondition();
            consume(SindhiToken.Type.ELSE); // consume 'پو' again
        }

        // Skip past the loop body
        pos = bodyStart;
        try {
            statement();
        } catch (Exception e) {
            // Ignore errors while skipping
        }

        return output.toString();
    }

    private String doStatement() {
        consume(SindhiToken.Type.DO); // consume 'ڪر'
        // Implementation for DO statements
        // (Add your specific implementation here)
        return "";
    }

    private boolean match(SindhiToken.Type type) {
        return pos < tokens.size() && tokens.get(pos).is(type);
    }

    private boolean match(SindhiToken.Type type, String value) {
        return pos < tokens.size() && tokens.get(pos).is(type, value);
    }

    private SindhiToken consume(SindhiToken.Type type) {
        if (pos >= tokens.size()) {
            throw new RuntimeException("Unexpected end of input, expected: " + type);
        }
        SindhiToken token = tokens.get(pos);
        if (!token.is(type)) {
            throw new RuntimeException("Expected " + type + " but got " + token.getType() +
                    (token.hasPosition() ?
                            String.format(" at line %d, column %d",
                                    token.getLine(), token.getColumn()) : ""));
        }
        pos++;
        return token;
    }

    private SindhiToken consume(SindhiToken.Type type, String value) {
        if (pos >= tokens.size()) {
            throw new RuntimeException("Unexpected end of input, expected: " + type + " '" + value + "'");
        }
        SindhiToken token = tokens.get(pos);
        if (!token.is(type, value)) {
            throw new RuntimeException("Expected " + type + " '" + value + "' but got " +
                    token.getType() + " '" + token.getValue() + "'" +
                    (token.hasPosition() ?
                            String.format(" at line %d, column %d",
                                    token.getLine(), token.getColumn()) : ""));
        }
        pos++;
        return token;
    }

    private SindhiToken peek() {
        if (pos >= tokens.size()) {
            throw new RuntimeException("Unexpected end of input");
        }
        return tokens.get(pos);
    }
}