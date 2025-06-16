package com.example.SindhiProgrammingLanguage.Compiler;
import java.util.*;

public class SindhiInterpreter {
    private final List<SindhiToken> tokens;
    private int pos = 0;
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, SindhiToken.Type> varTypes = new HashMap<>();

    public SindhiInterpreter(List<SindhiToken> tokens) {
        this.tokens = tokens;
    }

    public String interpret() {
        StringBuilder output = new StringBuilder();
        try {
            while (!match(SindhiToken.Type.EOF)) {
                String result = statement();
                if (result != null && !result.isEmpty()) {
                    output.append(result);
                    if (!result.endsWith("\n")) output.append("\n");
                }
            }
        } catch (Exception e) {
            return "Runtime Error: " + e.getMessage();
        }
        return output.toString().trim();
    }

    private String statement() {
        if (match(SindhiToken.Type.PRINT)) {
            return print();
        } else if (match(SindhiToken.Type.DECLARE)) {
            return declaration();
        } else if (match(SindhiToken.Type.IF)) {
            return ifCondition();
        } else if (match(SindhiToken.Type.WHILE)) {
            return whileLoop();
        } else if (match(SindhiToken.Type.DO)) {
            return doWhileLoop();
        } else if (match(SindhiToken.Type.OPERATOR, "{")) {
            return block();
        } else if (match(SindhiToken.Type.IDENTIFIER)) {
            return assignment();
        }
        throw new RuntimeException("Unknown statement at " + positionStr());
    }

    private String positionStr() {
        SindhiToken token = peek();
        return "line " + token.getLine() + ", column " + token.getColumn();
    }

    private String declaration() {
        consume(SindhiToken.Type.DECLARE);
        SindhiToken typeToken = consumeType();
        String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
        consume(SindhiToken.Type.OPERATOR, "=");
        Object value = evaluateExpression();

        // Type conversion
        if (typeToken.is(SindhiToken.Type.NUMERIC_TYPE)) {
            try {
                value = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Expected number for variable '" + varName + "' at " + positionStr());
            }
        } else {
            value = value.toString();
        }

        variables.put(varName, value);
        varTypes.put(varName, typeToken.getType());
        return "";
    }

    private SindhiToken consumeType() {
        if (match(SindhiToken.Type.NUMERIC_TYPE)) {
            return consume(SindhiToken.Type.NUMERIC_TYPE);
        } else if (match(SindhiToken.Type.STRING_TYPE)) {
            return consume(SindhiToken.Type.STRING_TYPE);
        }
        SindhiToken token = peek();
        throw new RuntimeException("Expected type (عددي or لکت) but found '" +
                token.getValue() + "' at " + positionStr());
    }

    private String print() {
        consume(SindhiToken.Type.PRINT);
        Object result = evaluateExpression();
        return result.toString();
    }

    private String assignment() {
        String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
        consume(SindhiToken.Type.OPERATOR, "=");
        Object value = evaluateExpression();

        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Variable '" + varName + "' not declared at " + positionStr());
        }

        // Type conversion
        if (varTypes.get(varName) == SindhiToken.Type.NUMERIC_TYPE) {
            try {
                value = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Expected number for variable '" + varName + "' at " + positionStr());
            }
        } else {
            value = value.toString();
        }

        variables.put(varName, value);
        return "";
    }

    private String ifCondition() {
        consume(SindhiToken.Type.IF);
        boolean condition = toBoolean(evaluateExpression());
        String thenResult = statement();
        String elseResult = "";

        if (match(SindhiToken.Type.ELSE)) {
            consume(SindhiToken.Type.ELSE);
            elseResult = statement();
        }

        return condition ? thenResult : elseResult;
    }

    private String whileLoop() {
        consume(SindhiToken.Type.WHILE);
        int conditionStart = pos;
        boolean condition = toBoolean(evaluateExpression());
        int bodyStart = pos;

        StringBuilder output = new StringBuilder();
        while (condition) {
            // Execute body
            pos = bodyStart;
            String result = statement();
            if (result != null) {
                output.append(result);
            }

            // Re-evaluate condition
            pos = conditionStart;
            condition = toBoolean(evaluateExpression());
        }

        // Skip loop body
        pos = bodyStart;
        skipStatement();
        return output.toString();
    }

    private String doWhileLoop() {
        consume(SindhiToken.Type.DO);
        int bodyStart = pos;  // Save start position of body
        int conditionStart = -1;

        // Execute body at least once
        String body = statement();
        StringBuilder output = new StringBuilder();
        if (body != null) {
            output.append(body);
        }

        // Parse the WHILE keyword and condition
        if (!match(SindhiToken.Type.WHILE)) {
            throw new RuntimeException("Expected WHILE after DO loop body at " + positionStr());
        }
        consume(SindhiToken.Type.WHILE);
        conditionStart = pos;  // Save start position of condition
        boolean condition = toBoolean(evaluateExpression());

        // Execute loop repeatedly while condition is true
        while (condition) {
            // Execute body again
            pos = bodyStart;
            String result = statement();
            if (result != null) {
                output.append(result);
            }

            // Re-evaluate condition
            pos = conditionStart;
            condition = toBoolean(evaluateExpression());
        }

        return output.toString();
    }

    private String block() {
        consume(SindhiToken.Type.OPERATOR, "{");
        StringBuilder output = new StringBuilder();
        while (!match(SindhiToken.Type.OPERATOR, "}") && !match(SindhiToken.Type.EOF)) {
            String stmt = statement();
            if (stmt != null) {
                output.append(stmt);
            }
        }
        consume(SindhiToken.Type.OPERATOR, "}");
        return output.toString();
    }

    private void skipStatement() {
        if (match(SindhiToken.Type.OPERATOR, "{")) {
            skipBlock();
        } else if (pos < tokens.size()) {
            pos++;
        }
    }

    private void skipBlock() {
        consume(SindhiToken.Type.OPERATOR, "{");
        int depth = 1;
        while (depth > 0 && pos < tokens.size()) {
            if (match(SindhiToken.Type.OPERATOR, "{")) {
                depth++;
            } else if (match(SindhiToken.Type.OPERATOR, "}")) {
                depth--;
            }
            pos++;
        }
    }

    private Object evaluateExpression() {
        return evaluateTernary();
    }

    private Object evaluateTernary() {
        Object condition = evaluateLogicalOr();
        if (match(SindhiToken.Type.OPERATOR, "?")) {
            consume(SindhiToken.Type.OPERATOR, "?");
            Object trueValue = evaluateExpression();
            if (match(SindhiToken.Type.OPERATOR, ":")) {
                consume(SindhiToken.Type.OPERATOR, ":");
            } else if (match(SindhiToken.Type.OPERATOR, "?:")) {
                consume(SindhiToken.Type.OPERATOR, "?:");
            }
            Object falseValue = evaluateExpression();
            return toBoolean(condition) ? trueValue : falseValue;
        }
        return condition;
    }

    private Object evaluateLogicalOr() {
        Object left = evaluateLogicalAnd();
        while (match(SindhiToken.Type.OR_OPERATOR)) {
            consume(SindhiToken.Type.OR_OPERATOR);
            Object right = evaluateLogicalAnd();
            left = toBoolean(left) || toBoolean(right);
        }
        return left;
    }

    private Object evaluateLogicalAnd() {
        Object left = evaluateEquality();
        while (match(SindhiToken.Type.AND_OPERATOR)) {
            consume(SindhiToken.Type.AND_OPERATOR);
            Object right = evaluateEquality();
            left = toBoolean(left) && toBoolean(right);
        }
        return left;
    }

    private Object evaluateEquality() {
        Object left = evaluateComparison();
        while (match(SindhiToken.Type.OPERATOR, "==") || match(SindhiToken.Type.OPERATOR, "!=")) {
            String op = consume(SindhiToken.Type.OPERATOR).getValue();
            Object right = evaluateComparison();
            left = op.equals("==") ? Objects.equals(left, right) : !Objects.equals(left, right);
        }
        return left;
    }

    private Object evaluateComparison() {
        Object left = evaluateAddition();
        while (match(SindhiToken.Type.OPERATOR, "<") || match(SindhiToken.Type.OPERATOR, ">") ||
                match(SindhiToken.Type.OPERATOR, "<=") || match(SindhiToken.Type.OPERATOR, ">=")) {
            String op = consume(SindhiToken.Type.OPERATOR).getValue();
            Object right = evaluateAddition();
            left = compareValues(left, right, op);
        }
        return left;
    }

    private Object evaluateAddition() {
        Object left = evaluateMultiplication();
        while (match(SindhiToken.Type.OPERATOR, "+") || match(SindhiToken.Type.OPERATOR, "-")) {
            String op = consume(SindhiToken.Type.OPERATOR).getValue();
            Object right = evaluateMultiplication();
            left = op.equals("+") ? addValues(left, right) : subtractValues(left, right);
        }
        return left;
    }

    private Object evaluateMultiplication() {
        Object left = evaluatePrimary();
        while (match(SindhiToken.Type.OPERATOR, "*") || match(SindhiToken.Type.OPERATOR, "/") ||
                match(SindhiToken.Type.OPERATOR, "%")) {
            String op = consume(SindhiToken.Type.OPERATOR).getValue();
            Object right = evaluatePrimary();
            left = mathOperation(left, right, op);
        }
        return left;
    }

    private Object evaluatePrimary() {
        if (match(SindhiToken.Type.NUMBER)) {
            return Integer.parseInt(consume(SindhiToken.Type.NUMBER).getValue());
        } else if (match(SindhiToken.Type.STRING)) {
            return consume(SindhiToken.Type.STRING).getValue();
        } else if (match(SindhiToken.Type.IDENTIFIER)) {
            String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(varName)) {
                throw new RuntimeException("Variable '" + varName + "' not found at " + positionStr());
            }
            return variables.get(varName);
        } else if (match(SindhiToken.Type.OPERATOR, "(")) {
            consume(SindhiToken.Type.OPERATOR, "(");
            Object result = evaluateExpression();
            consume(SindhiToken.Type.OPERATOR, ")");
            return result;
        }
        throw new RuntimeException("Invalid expression at " + positionStr());
    }

    // Helper methods
    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Integer) return (Integer) value != 0;
        if (value instanceof String) return !((String) value).isEmpty();
        throw new RuntimeException("Cannot convert to boolean: " + value);
    }

    private boolean compareValues(Object left, Object right, String op) {
        if (left instanceof Integer && right instanceof Integer) {
            int a = (Integer) left, b = (Integer) right;
            switch (op) {
                case "<": return a < b;
                case ">": return a > b;
                case "<=": return a <= b;
                case ">=": return a >= b;
            }
        } else if (left instanceof String && right instanceof String) {
            int cmp = ((String) left).compareTo((String) right);
            switch (op) {
                case "<": return cmp < 0;
                case ">": return cmp > 0;
                case "<=": return cmp <= 0;
                case ">=": return cmp >= 0;
            }
        }
        throw new RuntimeException("Cannot compare " + left + " and " + right + " at " + positionStr());
    }

    private Object addValues(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left + (Integer) right;
        }
        return left.toString() + right.toString();
    }

    private Object subtractValues(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left - (Integer) right;
        }
        throw new RuntimeException("Cannot subtract non-numeric values at " + positionStr());
    }

    private Object mathOperation(Object left, Object right, String op) {
        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            throw new RuntimeException("Math operation requires numbers at " + positionStr());
        }
        int a = (Integer) left, b = (Integer) right;
        switch (op) {
            case "*": return a * b;
            case "/":
                if (b == 0) throw new RuntimeException("Division by zero at " + positionStr());
                return a / b;
            case "%": return a % b;
            default: throw new RuntimeException("Unsupported operator: " + op);
        }
    }

    // Token consumption utilities
    private boolean match(SindhiToken.Type type) {
        return pos < tokens.size() && tokens.get(pos).is(type);
    }

    private boolean match(SindhiToken.Type type, String value) {
        return pos < tokens.size() && tokens.get(pos).is(type, value);
    }

    private SindhiToken consume(SindhiToken.Type type) {
        if (pos >= tokens.size()) throw new RuntimeException("Unexpected end of input at " + positionStr());
        SindhiToken token = tokens.get(pos++);
        if (!token.is(type)) {
            throw new RuntimeException("Expected " + type + " but got " + token.getType() +
                    " ('" + token.getValue() + "') at " + positionStr());
        }
        return token;
    }

    private SindhiToken consume(SindhiToken.Type type, String value) {
        if (pos >= tokens.size()) throw new RuntimeException("Unexpected end of input at " + positionStr());
        SindhiToken token = tokens.get(pos++);
        if (!token.is(type, value)) {
            throw new RuntimeException("Expected " + type + " '" + value + "' but got " +
                    token.getType() + " ('" + token.getValue() + "') at " + positionStr());
        }
        return token;
    }

    private SindhiToken peek() {
        if (pos >= tokens.size()) throw new RuntimeException("Unexpected end of input");
        return tokens.get(pos);
    }
}