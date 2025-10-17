package com.kidcode.core.ast;

import java.util.List;

public record FunctionCallExpression(
    Identifier function,
    List<Expression> arguments
) implements Expression {
    @Override
    public String tokenLiteral() { return function.tokenLiteral(); }
}