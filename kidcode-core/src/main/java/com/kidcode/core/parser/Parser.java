package com.kidcode.core.parser;

import com.kidcode.core.ast.*;
import com.kidcode.core.lexer.*;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int position = 0;
    private final List<String> errors = new ArrayList<>();

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
        precedences.put(TokenType.LPAREN, Precedence.INDEX); // Function calls
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

    private void advanceToNextStatement() {
        nextToken();
    }

    private Statement parseStatement() {
        switch (currentToken().type()) {
            case MOVE: return parseMoveStatement();
            case TURN: return parseTurnStatement();
            case SAY: return parseSayStatement();
            case REPEAT: return parseRepeatStatement();
            case SET: return parseSetStatement();
            case IF: return parseIfStatement();
            case PEN: return parsePenStatement();
            case COLOR: return parseSetColorStatement();
            case DEFINE: return parseFunctionDefinitionStatement();
            case IDENTIFIER: return parseFunctionCallStatement();
            default:
                errors.add("Error line " + currentToken().lineNumber() + ": Invalid start of a statement: '" + currentToken().literal() + "'");
                advanceToNextStatement();
                return null;
        }
    }

    private MoveStatement parseMoveStatement() {
        nextToken(); // Consume 'forward'
        nextToken(); // Move to the expression
        Expression steps = parseExpression(Precedence.LOWEST);
        advanceToNextStatement();
        return new MoveStatement(steps);
    }

    private TurnStatement parseTurnStatement() {
        nextToken(); // Consume 'left' or 'right'
        String direction = currentToken().literal();
        nextToken(); // Move to the expression
        Expression degrees = parseExpression(Precedence.LOWEST);
        advanceToNextStatement();
        return new TurnStatement(direction, degrees);
    }

    private SayStatement parseSayStatement() {
        nextToken(); // Consume 'say', move to string
        Expression message = parseExpression(Precedence.LOWEST);
        advanceToNextStatement();
        return new SayStatement(message);
    }

    private SetStatement parseSetStatement() {
        nextToken(); // Consume 'set', move to identifier
        Identifier name = new Identifier(currentToken().literal());
        nextToken(); // Consume identifier, move to '='
        if (currentToken().type() != TokenType.ASSIGN) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected '=' after variable name");
            return null;
        }
        nextToken(); // Consume '=', move to expression
        Expression value = parseExpression(Precedence.LOWEST);
        advanceToNextStatement();
        return new SetStatement(name, value);
    }

    private PenStatement parsePenStatement() {
        nextToken();
        if (currentToken().type() != TokenType.UP && currentToken().type() != TokenType.DOWN) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'up' or 'down' after 'pen'");
            return null;
        }
        String state = currentToken().literal();
        advanceToNextStatement();
        return new PenStatement(state);
    }

    private SetColorStatement parseSetColorStatement() {
        nextToken();
        Expression colorName = parseExpression(Precedence.LOWEST);
        advanceToNextStatement();
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
        advanceToNextStatement();
        return new FunctionCallStatement(function, arguments);
    }

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
        advanceToNextStatement();
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
        advanceToNextStatement();
        return new RepeatStatement(times, body);
    }

    private IfStatement parseIfStatement() {
        nextToken();
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
            case IDENTIFIER:
                if (peekToken().type() == TokenType.LPAREN) {
                    left = parseFunctionCallExpression();
                } else {
                    left = new Identifier(currentToken().literal());
                }
                break;
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
                case LPAREN:
                    // Chained function calls like `get_list()()` are not supported.
                    nextToken(); // Consume the '('.
                    errors.add("Error line " + currentToken().lineNumber() + ": Chained function calls are not supported.");
                    return null;
                default:
                    return left;
            }
        }
        return left;
    }
    
    private Expression parseFunctionCallExpression() {
        Identifier function = new Identifier(currentToken().literal());
        nextToken(); // Consume function name, current token is now '('
        List<Expression> arguments = parseExpressionList(TokenType.RPAREN);
        if (arguments == null) {
            return null; // error already recorded by parseExpressionList
        }
        return new FunctionCallExpression(function, arguments);
    }

    private List<Expression> parseExpressionList(TokenType endToken) {
        List<Expression> list = new ArrayList<>();
        if (peekToken().type() == endToken) {
            nextToken(); // Consume end token
            return list;
        }
        nextToken(); // Move past '(' or '['
        list.add(parseExpression(Precedence.LOWEST));
        while (peekToken().type() == TokenType.COMMA) {
            nextToken();
            nextToken();
            list.add(parseExpression(Precedence.LOWEST));
        }
        if (peekToken().type() != endToken) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected '" + endToken + "' but got '" + peekToken().literal() + "'");
            return null;
        }
        nextToken(); // Consume end token
        return list;
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
        List<Expression> elements = parseExpressionList(TokenType.RBRACKET);
        if (elements == null) {
            return null; // error already recorded
        }
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