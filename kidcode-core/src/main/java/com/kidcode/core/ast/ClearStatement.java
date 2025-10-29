package com.kidcode.core.ast;

public record ClearStatement() implements Statement {
    @Override
    public String tokenLiteral() { return "clear"; }
}
