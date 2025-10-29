package com.kidcode.core.ast;

public record BooleanLiteral(boolean value) implements Expression {
    @Override
    public String tokenLiteral() { return String.valueOf(value); }
}
