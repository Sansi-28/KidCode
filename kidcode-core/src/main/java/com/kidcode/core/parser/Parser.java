package com.kidcode.core.parser;

import com.kidcode.core.ast.*;
import com.kidcode.core.lexer.*;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int position = 0;
    private final List<String> errors = new ArrayList<>();

    // Precedence enum and map remain the same
    private enum Precedence {
        LOWEST, EQUALS, LESSGREATER, SUM, PRODUCT, PREFIX, INDEX
    }
    private static final Map<TokenType, Precedence> precedences = new HashMap<>();
    static {
        precedences.put(TokenType.EQ, Precedence.EQUALS);
        precedences.put(TokenType.NOT_EQ, Precedence.EQUALS);
        precedences.put(TokenType.LT, Precedence.LESSGREATER);
        precedences.put(TokenType.GT, Precedence.LESSGREATER);
        precedences.put(TokenType.PLUS, Precedence.SUM);
        precedences.put(TokenType.MINUS, Precedence.SUM);
        precedences.put(TokenType.STAR, Precedence.PRODUCT);
        precedences.put(TokenType.SLASH, Precedence.PRODUCT);
        precedences.put(TokenType.LBRACKET, Precedence.INDEX);
    }

    public Parser(Lexer lexer) {
        this.tokens = lexer.allTokens();
    }

    public List<String> getErrors() {
        return errors;
    }

    private Token currentToken() {
        return (position < tokens.size()) ? tokens.get(position) : new Token(TokenType.EOF, "", -1);
    }

    private Token peekToken() {
        return (position + 1 < tokens.size()) ? tokens.get(position + 1) : new Token(TokenType.EOF, "", -1);
    }

    private void nextToken() {
        position++;
    }

    // MODIFIED: The main program loop NO LONGER calls nextToken().
    public List<Statement> parseProgram() {
        List<Statement> statements = new ArrayList<>();
        while (currentToken().type() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return statements;
    }

    // NEW: A helper to advance the token only if it's not EOF.
    // This simplifies the end of all parse...Statement methods.
    private void advanceToNextStatement() {
        // Most statements are one per line. We advance until we find the next meaningful token.
        // This is not strictly necessary for this parser, but is good practice.
        // For our current grammar, a simple nextToken() is sufficient.
        nextToken();
    }

   private Statement parseStatement() {
    int line = currentToken().lineNumber(); // capture line number at start
    int startPos = position; // remember position to detect no-progress errors

    Statement inner = switch (currentToken().type()) {
        case MOVE -> parseMoveStatement();
        case TURN -> parseTurnStatement();
        case SAY -> parseSayStatement();
        case REPEAT -> parseRepeatStatement();
        case SET -> parseSetStatement();
        case IF -> parseIfStatement();
        case PEN -> parsePenStatement();
        case COLOR -> parseSetColorStatement();
        case DEFINE -> parseFunctionDefinitionStatement();
        case IDENTIFIER -> parseFunctionCallStatement();
        default -> {
            errors.add("Error line " + currentToken().lineNumber() + ": Invalid start of a statement: '" + currentToken().literal() + "'");
            advanceToNextStatement();
            yield null;
        }
        };

    if (inner == null) {
        // Some parse... methods return null on error without consuming tokens.
        // Advance at least one token to avoid infinite loops.
        if (position == startPos) {
            advanceToNextStatement();
        }
        return null;
    }
    return new LocatedStatement(inner, line); // wrap it with line number
}


    private MoveStatement parseMoveStatement() {
        // ... (parsing logic is the same)
        nextToken(); // Consume 'forward'
        nextToken(); // Move to the expression
        Expression steps = parseExpression(Precedence.LOWEST);
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new MoveStatement(steps);
    }

    private TurnStatement parseTurnStatement() {
        // ... (parsing logic is the same)
        nextToken(); // Consume 'left' or 'right'
        String direction = currentToken().literal();
        nextToken(); // Move to the expression
        Expression degrees = parseExpression(Precedence.LOWEST);
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new TurnStatement(direction, degrees);
    }

    private SayStatement parseSayStatement() {
        nextToken(); // Consume 'say', move to string
        Expression message = parseExpression(Precedence.LOWEST);
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new SayStatement(message);
    }

    private SetStatement parseSetStatement() {
        nextToken(); // Consume 'set', move to identifier
        Identifier name = new Identifier(currentToken().literal());
        nextToken(); // Consume identifier, move to '='
        if (currentToken().type() != TokenType.ASSIGN) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected '=' after variable name");
            advanceToNextStatement();
            return null;
        }
        nextToken(); // Consume '=', move to expression
        Expression value = parseExpression(Precedence.LOWEST);
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new SetStatement(name, value);
    }

    private PenStatement parsePenStatement() {
        nextToken(); // Consume 'pen'
        if (currentToken().type() != TokenType.UP && currentToken().type() != TokenType.DOWN) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'up' or 'down' after 'pen'");
            return null;
        }
        String state = currentToken().literal();
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new PenStatement(state);
    }

    private SetColorStatement parseSetColorStatement() {
        nextToken(); // Consume 'color'
        Expression colorName = parseExpression(Precedence.LOWEST);
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new SetColorStatement(colorName);
    }
    
    // Function and control flow statements manage their own token advancement because they are complex.

    private FunctionCallStatement parseFunctionCallStatement() {
        Identifier function = new Identifier(currentToken().literal());
        List<Expression> arguments = new ArrayList<>();
        while (peekToken().type() != TokenType.EOF && peekToken().type() != TokenType.END && peekToken().type() != TokenType.ELSE && isArgument(peekToken().type())) {
            nextToken();
            arguments.add(parseExpression(Precedence.LOWEST));
        }
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new FunctionCallStatement(function, arguments);
    }

    // NEW: Helper to check if a token can be the start of an argument.
    private boolean isArgument(TokenType type) {
        return type == TokenType.NUMBER || type == TokenType.IDENTIFIER ||
               type == TokenType.STRING || type == TokenType.LPAREN || type == TokenType.LBRACKET;
    }

    private FunctionDefinitionStatement parseFunctionDefinitionStatement() {
        nextToken(); // Consume 'define'
        Identifier name = new Identifier(currentToken().literal());
        
        List<Identifier> parameters = new ArrayList<>();
        while (peekToken().type() == TokenType.IDENTIFIER) {
            nextToken();
            parameters.add(new Identifier(currentToken().literal()));
        }
        
        List<Statement> body = parseBlock();
        if (currentToken().type() != TokenType.END) { /* error */ return null; }
        
        if (peekToken().type() == TokenType.DEFINE) {
            nextToken(); // consume 'define' from 'end define'
        }
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new FunctionDefinitionStatement(name, parameters, body);
    }

    private RepeatStatement parseRepeatStatement() {
        nextToken(); // Consume 'repeat'
        Expression times = parseExpression(Precedence.LOWEST);
        List<Statement> body = parseBlock();
        if (currentToken().type() != TokenType.END) { /* error */ return null; }
        
        if (peekToken().type() == TokenType.REPEAT) {
            nextToken(); // consume 'repeat' from 'end repeat'
        }
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new RepeatStatement(times, body);
    }

    private IfStatement parseIfStatement() {
        nextToken(); // Consume 'if'
        Expression condition = parseExpression(Precedence.LOWEST);
        List<Statement> consequence = parseBlock();
        List<Statement> alternative = null;

        if (currentToken().type() == TokenType.ELSE) {
            alternative = parseBlock();
        }

        if (currentToken().type() != TokenType.END) { /* error */ return null; }

        if (peekToken().type() == TokenType.IF) {
            nextToken(); // consume 'if' from 'end if'
        }
        advanceToNextStatement(); // MODIFIED: Advance token stream
        return new IfStatement(condition, consequence, alternative);
    }

    // MODIFIED: The block parsing loop NO LONGER calls nextToken().
    private List<Statement> parseBlock() {
        List<Statement> block = new ArrayList<>();
        nextToken(); // Consume the keyword that started the block (or 'else')
        
        while (currentToken().type() != TokenType.END && currentToken().type() != TokenType.ELSE && currentToken().type() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                block.add(stmt);
            }
        }
        return block;
    }

    // Expression parsing logic (this part was correct in the previous fix)
    private Precedence getPeekPrecedence() {
        return precedences.getOrDefault(peekToken().type(), Precedence.LOWEST);
    }

    private Expression parseExpression(Precedence precedence) {
        Expression left;
        switch (currentToken().type()) {
            case IDENTIFIER: left = new Identifier(currentToken().literal()); break;
            case NUMBER: left = new IntegerLiteral(Integer.parseInt(currentToken().literal())); break;
            case STRING: left = new StringLiteral(currentToken().literal()); break;
            case LPAREN: left = parseGroupedExpression(); break;
            case LBRACKET: left = parseListLiteral(); break;
            default:
                errors.add("Error line " + currentToken().lineNumber() + ": Unexpected token " + currentToken().literal() + " in expression");
                return null;
        }

        while (precedence.ordinal() < getPeekPrecedence().ordinal()) {
            switch (peekToken().type()) {
                case PLUS, MINUS, STAR, SLASH, EQ, NOT_EQ, LT, GT:
                    nextToken();
                    left = parseInfixExpression(left);
                    break;
                case LBRACKET:
                    nextToken();
                    left = parseIndexExpression(left);
                    break;
                default:
                    return left;
            }
        }
        return left;
    }
    
    private Expression parseInfixExpression(Expression left) {
        String operator = currentToken().literal();
        Precedence p = precedences.get(currentToken().type());
        nextToken();
        Expression right = parseExpression(p);
        return new InfixExpression(left, operator, right);
    }
    
    private Expression parseIndexExpression(Expression left) {
        // currentToken is '['
        nextToken(); // move to index
        Expression index = parseExpression(Precedence.LOWEST);
        if (peekToken().type() != TokenType.RBRACKET) { /* error */ return null; }
        nextToken(); // consume ']'
        return new IndexExpression(left, index);
    }
    
    private Expression parseListLiteral() {
        List<Expression> elements = new ArrayList<>();
        if (peekToken().type() == TokenType.RBRACKET) {
            nextToken();
            return new ListLiteral(elements);
        }
        nextToken();
        elements.add(parseExpression(Precedence.LOWEST));
        while (peekToken().type() == TokenType.COMMA) {
            nextToken();
            nextToken();
            elements.add(parseExpression(Precedence.LOWEST));
        }
        if (peekToken().type() != TokenType.RBRACKET) { /* error */ return null; }
        nextToken();
        return new ListLiteral(elements);
    }
    
    private Expression parseGroupedExpression() {
        nextToken();
        Expression exp = parseExpression(Precedence.LOWEST);
        if (peekToken().type() != TokenType.RPAREN) { /* error */ return null; }
        nextToken();
        return exp;
    }
}