package com.kidcode.core.lexer;

public enum TokenType {
    // Commands & Keywords
    MOVE, FORWARD, TURN, LEFT, RIGHT, SAY, HOME,

    // Values
    NUMBER, STRING,

    // Special
    ILLEGAL, // Unknown token
    EOF,      // End of File

    // New additions
    REPEAT, END,
    SET, IDENTIFIER, ASSIGN, PLUS, MINUS, STAR, SLASH, LPAREN, RPAREN,
    IF, ELSE, // Keywords
    GT, LT, GTE, LTE, EQ, NOT_EQ, // Comparison operators
    PEN, UP, DOWN, COLOR, // Pen and color commands

    // NEW for functions and lists
    DEFINE,
    LBRACKET, RBRACKET, COMMA

    // Additional commands
    // Add any other commands or keywords here
} 