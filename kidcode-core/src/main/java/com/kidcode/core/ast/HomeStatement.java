package com.kidcode.core.ast;

public record HomeStatement() implements Statement {
      @Override
      public String tokenLiteral() { return "home"; }
  }
