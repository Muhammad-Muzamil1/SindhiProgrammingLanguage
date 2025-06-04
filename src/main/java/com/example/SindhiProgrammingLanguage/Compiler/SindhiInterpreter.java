package com.example.SindhiProgrammingLanguage.Compiler;

import java.util.*;

import static org.apache.el.lang.ELArithmetic.*;

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
                String result = statement();
                if (result != null) {
                    output.append(result);
                }
            }
        } catch (Exception e) {
            return "Runtime Error: " + e.getMessage();
        }
        return output.toString();
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
            return doStatement();
        } else if (match(SindhiToken.Type.OPERATOR, "{")) {
            return block();
        }
        throw new RuntimeException("Unknown statement at line " + peek().getLine());
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
        consume(SindhiToken.Type.IF);
        boolean condition = evaluateBooleanCondition();
        String thenResult = statement();
        String elseResult = null;

        if (match(SindhiToken.Type.ELSE)) {
            consume(SindhiToken.Type.ELSE);
            elseResult = statement();
        }

        return condition ? thenResult : (elseResult != null ? elseResult : "");
    }

    private String handleElseBlocks() {
        if (match(SindhiToken.Type.ELSE_IF)) {
            consume(SindhiToken.Type.ELSE_IF);
            boolean elseIfCondition = evaluateBooleanCondition();
            if (elseIfCondition) {
                return statement();
            } else {
                skipStatement();
                return handleElseBlocks();
            }
        } else if (match(SindhiToken.Type.ELSE)) {
            consume(SindhiToken.Type.ELSE);
            return statement();
        }
        return "";
    }

    private void skipRemainingElseBlocks() {
        while (match(SindhiToken.Type.ELSE_IF) || match(SindhiToken.Type.ELSE)) {
            skipStatement();
            if (match(SindhiToken.Type.ELSE_IF)) {
                consume(SindhiToken.Type.ELSE_IF);
                evaluateBooleanCondition(); // Skip the condition
            } else if (match(SindhiToken.Type.ELSE)) {
                consume(SindhiToken.Type.ELSE);
            }
            skipStatement();
        }
    }

    private void skipStatement() {
        if (match(SindhiToken.Type.OPERATOR, "{")) {
            // Skip entire block
            int braceCount = 1;
            consume(SindhiToken.Type.OPERATOR, "{");
            while (braceCount > 0 && pos < tokens.size()) {
                if (match(SindhiToken.Type.OPERATOR, "{")) {
                    braceCount++;
                } else if (match(SindhiToken.Type.OPERATOR, "}")) {
                    braceCount--;
                }
                pos++;
            }
        } else {
            // Skip single statement
            if (match(SindhiToken.Type.IF) || match(SindhiToken.Type.WHILE)) {
                // Skip condition
                evaluateBooleanCondition();
            }
            // Skip the statement token itself
            if (pos < tokens.size()) pos++;
        }
    }

    private String whileLoop() {
        consume(SindhiToken.Type.WHILE);
        int conditionStart = pos;
        boolean condition = evaluateBooleanCondition();
        int bodyStart = pos;

        StringBuilder output = new StringBuilder();
        while (condition) {
            pos = bodyStart;
            String statementOutput = statement();
            if (statementOutput != null) {
                output.append(statementOutput);
            }

            // Re-evaluate condition
            pos = conditionStart;
            condition = evaluateBooleanCondition();
        }

        // Skip past loop body
        pos = bodyStart;
        skipStatement();
        return output.toString();
    }

    private String doStatement() {
        consume(SindhiToken.Type.DO);
        String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
        consume(SindhiToken.Type.OPERATOR, "=");
        Object value = evaluateExpression();

        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Variable '" + varName + "' not declared");
        }

        // Type checking
        Object existing = variables.get(varName);
        if (existing instanceof Integer && !(value instanceof Integer)) {
            throw new RuntimeException("Type mismatch for variable '" + varName + "'");
        }

        variables.put(varName, value);
        return "";
    }


    private String block() {
        consume(SindhiToken.Type.OPERATOR, "{");
        StringBuilder output = new StringBuilder();
        while (!check(SindhiToken.Type.OPERATOR, "}") && !check(SindhiToken.Type.EOF)) {
            String stmtOutput = statement();
            if (stmtOutput != null && !stmtOutput.isEmpty()) {
                output.append(stmtOutput);
            }
        }
        consume(SindhiToken.Type.OPERATOR, "}");
        return output.toString();
    }

    private String declaration() {
        consume(SindhiToken.Type.DECLARE);
        SindhiToken typeToken = consumeType();
        SindhiToken.Type type = typeToken.getType();
        String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
        consume(SindhiToken.Type.OPERATOR, "=");
        Object value = evaluateExpression();

        if (type == SindhiToken.Type.NUMERIC_TYPE && !(value instanceof Integer)) {
            try {
                value = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Expected number for variable '" + varName + "'");
            }
        } else if (type == SindhiToken.Type.STRING_TYPE) {
            value = value.toString();
        }

        variables.put(varName, value);
        return "";
    }

    private SindhiToken consumeType() {
        if (match(SindhiToken.Type.NUMERIC_TYPE)) {
            return consume(SindhiToken.Type.NUMERIC_TYPE);
        } else if (match(SindhiToken.Type.STRING_TYPE)) {
            return consume(SindhiToken.Type.STRING_TYPE);
        }
        throw new RuntimeException("Expected type (عددي or لکت)");
    }


    private String print() {
        consume(SindhiToken.Type.PRINT);
        return evaluateExpression().toString();
    }

    // ---------------- Expression Evaluation ----------------

    private Object evaluateExpression() {
        Object left = evaluateTerm();
        while (match(SindhiToken.Type.OPERATOR, "+") || match(SindhiToken.Type.OPERATOR, "-")) {
            String op = consume(SindhiToken.Type.OPERATOR).getValue();
            Object right = evaluateTerm();
            left = op.equals("+") ? add(left, right) : subtract(left, right);
        }
        return left;
    }

    private Object evaluateLogical() {
        Object left = evaluateTerm();
        while (match(SindhiToken.Type.AND_OPERATOR) || match(SindhiToken.Type.OR_OPERATOR)) {
            String op = consume(peek().getType()).getValue();
            Object right = evaluateTerm();
            left = evaluateLogicalOp(left, op, right);
        }
        return left;
    }

    private Object evaluateTerm() {
        Object left = evaluateFactor();
        while (match(SindhiToken.Type.OPERATOR, "*") ||
                match(SindhiToken.Type.OPERATOR, "/") ||
                match(SindhiToken.Type.OPERATOR, "%")) {
            String op = consume(SindhiToken.Type.OPERATOR).getValue();
            Object right = evaluateFactor();
            switch (op) {
                case "*" -> left = multiply(left, right);
                case "/" -> left = divide(left, right);
                case "%" -> {
                    if (!(left instanceof Integer) || !(right instanceof Integer)) {
                        throw new RuntimeException("Modulo requires integers");
                    }
                    left = (Integer) left % (Integer) right;
                }
            }
        }
        return left;
    }

    private Object evaluateFactor() {
        if (match(SindhiToken.Type.NUMBER)) {
            return Integer.parseInt(consume(SindhiToken.Type.NUMBER).getValue());
        } else if (match(SindhiToken.Type.STRING)) {
            return consume(SindhiToken.Type.STRING).getValue();
        } else if (match(SindhiToken.Type.IDENTIFIER)) {
            String varName = consume(SindhiToken.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(varName)) {
                throw new RuntimeException("Variable '" + varName + "' not found");
            }
            return variables.get(varName);
        } else if (match(SindhiToken.Type.OPERATOR, "(")) {
            consume(SindhiToken.Type.OPERATOR, "(");
            Object result = evaluateExpression();
            consume(SindhiToken.Type.OPERATOR, ")");
            return result;
        }
        throw new RuntimeException("Invalid expression");
    }

    private Object evaluateLogicalOp(Object left, String op, Object right) {
        if (left == null || right == null) {
            throw new RuntimeException("Null values in logical operation");
        }
        boolean leftBool = toBoolean(left);
        boolean rightBool = toBoolean(right);

        return switch (op) {
            case "۽" -> leftBool && rightBool;
            case "يا" -> leftBool || rightBool;
            default -> throw new RuntimeException("Unknown logical operator: " + op);
        };
    }

    private Object subtract(Object left, Object right) {
        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            throw new RuntimeException("Subtraction requires integers");
        }
        return (Integer) left - (Integer) right;
    }

    private Object evaluateMathOp(Object left, String op, Object right) {
        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            throw new RuntimeException("Math operations require numbers");
        }
        int l = (Integer) left;
        int r = (Integer) right;

        return switch (op) {
            case "*" -> l * r;
            case "/" -> l / r;
            case "%" -> l % r;
            default -> throw new RuntimeException("Unknown math operator: " + op);
        };
    }

    private Object concatenateOrAdd(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left + (Integer) right;
        }
        return left.toString() + right.toString();
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Integer) return (Integer) value != 0;
        if (value instanceof String) return !((String) value).isEmpty();
        return false;
    }

    // ---------------- Token Handling Helpers ----------------

    private boolean match(SindhiToken.Type type) {
        return pos < tokens.size() && tokens.get(pos).is(type);
    }

    private boolean match(SindhiToken.Type type, String value) {
        return pos < tokens.size() && tokens.get(pos).is(type, value);
    }

    private boolean check(SindhiToken.Type type) {
        return pos < tokens.size() && tokens.get(pos).is(type);
    }

    private boolean check(SindhiToken.Type type, String value) {
        return pos < tokens.size() && tokens.get(pos).is(type, value);
    }

    private SindhiToken consume(SindhiToken.Type type) {
        if (pos >= tokens.size()) {
            throw new RuntimeException("Unexpected end of input");
        }
        SindhiToken token = tokens.get(pos++);
        if (!token.is(type)) {
            throw new RuntimeException("Expected " + type + " but got " + token.getType());
        }
        return token;
    }


    private SindhiToken consume(SindhiToken.Type type, String value) {
        if (!check(type, value)) {
            throw new RuntimeException("Expected token: " + value);
        }
        return tokens.get(pos++);
    }

    private SindhiToken peek() {
        if (pos >= tokens.size()) {
            throw new RuntimeException("Unexpected end of input");
        }
        return tokens.get(pos);
    }

}
