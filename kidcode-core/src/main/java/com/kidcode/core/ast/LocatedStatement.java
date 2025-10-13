package com.kidcode.core.ast;

public class LocatedStatement implements Statement {
    private final Statement inner;
    private final int lineNumber;

    public LocatedStatement(Statement inner, int lineNumber) {
        this.inner = inner;
        this.lineNumber = lineNumber;
    }

    public Statement getInner() {
        return inner;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String tokenLiteral() {
        return inner.tokenLiteral();
    }
}
