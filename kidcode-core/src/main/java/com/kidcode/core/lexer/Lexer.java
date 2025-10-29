package com.kidcode.core.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String input;
    private int position = 0; // current position in input (points to current char)
    private int readPosition = 0; // current reading position (after current char)
    private char ch; // current char under examination
    private int lineNumber = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("move", TokenType.MOVE);
        keywords.put("forward", TokenType.FORWARD);
        keywords.put("turn", TokenType.TURN);
        keywords.put("left", TokenType.LEFT);
        keywords.put("right", TokenType.RIGHT);
        keywords.put("say", TokenType.SAY);
        keywords.put("repeat", TokenType.REPEAT);
        keywords.put("end", TokenType.END);
        keywords.put("set", TokenType.SET);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("pen", TokenType.PEN);
        keywords.put("up", TokenType.UP);
        keywords.put("down", TokenType.DOWN);
        keywords.put("color", TokenType.COLOR);
        keywords.put("define", TokenType.DEFINE);
        keywords.put("home", TokenType.HOME);
        keywords.put("clear", TokenType.CLEAR);
    }

    public Lexer(String input) {
        this.input = input;
        readChar(); // Initialize the first character
    }

    public List<Token> allTokens() {
        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = nextToken();
            tokens.add(t);
        } while (t.type() != TokenType.EOF);
        return tokens;
    }

    private void readChar() {
        if (readPosition >= input.length()) {
            ch = 0; // Use 0 (NUL character) to signify end of input
        } else {
            ch = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition += 1;
    }

    private char peekChar() {
        if (readPosition >= input.length()) {
            return 0;
        }
        return input.charAt(readPosition);
    }

    public Token nextToken() {
        // This loop is the key. It will run as many times as needed to
        // skip over any combination of whitespace and full-line comments.
        while (true) {
            // First, skip any leading whitespace characters.
            while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                if (ch == '\n') {
                    lineNumber++;
                }
                readChar();
            }

            // After skipping whitespace, check if we've landed on a comment.
            if (ch == '#') {
                // If it's a comment, consume characters until the line ends.
                while (ch != '\n' && ch != 0) {
                    readChar();
                }
                // Use 'continue' to restart the main loop. This is crucial
                // because the line after a comment might be empty or another comment.
                continue;
            }

            // If we get here, 'ch' is not whitespace and not a comment.
            // It's the start of a real token, so we can break the loop.
            break;
        }

        Token token;

        // The rest of the switch statement is the same. It is now guaranteed
        // to only see real tokens.
        switch (ch) {
            case '=':
                if (peekChar() == '=') {
                    readChar();
                    token = new Token(TokenType.EQ, "==", lineNumber);
                } else {
                    token = new Token(TokenType.ASSIGN, String.valueOf(ch), lineNumber);
                }
                break;
            case '+': token = new Token(TokenType.PLUS, String.valueOf(ch), lineNumber); break;
            case '-': token = new Token(TokenType.MINUS, String.valueOf(ch), lineNumber); break;
            case '*': token = new Token(TokenType.STAR, String.valueOf(ch), lineNumber); break;
            case '/': token = new Token(TokenType.SLASH, String.valueOf(ch), lineNumber); break;
            case '(': token = new Token(TokenType.LPAREN, String.valueOf(ch), lineNumber); break;
            case ')': token = new Token(TokenType.RPAREN, String.valueOf(ch), lineNumber); break;
            case '[': token = new Token(TokenType.LBRACKET, String.valueOf(ch), lineNumber); break;
            case ']': token = new Token(TokenType.RBRACKET, String.valueOf(ch), lineNumber); break;
            case ',': token = new Token(TokenType.COMMA, String.valueOf(ch), lineNumber); break;
            case '!':
                if (peekChar() == '=') {
                    readChar();
                    token = new Token(TokenType.NOT_EQ, "!=", lineNumber);
                } else {
                    token = new Token(TokenType.ILLEGAL, String.valueOf(ch), lineNumber);
                }
                break;
            case '"':
                return new Token(TokenType.STRING, readString(), lineNumber);
            case 0:
                token = new Token(TokenType.EOF, "", lineNumber);
                break;
            default:
                if (isLetter(ch)) {
                    String literal = readIdentifier();
                    TokenType type = keywords.getOrDefault(literal.toLowerCase(), TokenType.IDENTIFIER);
                    return new Token(type, literal, lineNumber);
                } else if (isDigit(ch)) {
                    return new Token(TokenType.NUMBER, readNumber(), lineNumber);
                } else {
                    token = new Token(TokenType.ILLEGAL, String.valueOf(ch), lineNumber);
                }
        }

        readChar();
        return token;
    }

    private String readIdentifier() {
        int startPosition = position;
        while (isLetter(ch)) {
            readChar();
        }
        // `readChar` leaves `ch` on the first non-letter character,
        // so we don't need to advance again.
        return input.substring(startPosition, position);
    }

    private String readNumber() {
        int startPosition = position;
        while (isDigit(ch)) {
            readChar();
        }
        // `readChar` leaves `ch` on the first non-digit character.
        return input.substring(startPosition, position);
    }

    private String readString() {
        int startPosition = position + 1; // Skip the opening '"'
        do {
            readChar();
        } while (ch != '"' && ch != 0);
        
        String literal = input.substring(startPosition, position);
        readChar(); // Skip the closing '"'
        return literal;
    }

    private boolean isLetter(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_';
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }
} 