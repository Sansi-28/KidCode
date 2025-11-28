package com.kidcode.web.controller;

import com.kidcode.core.KidCodeEngine;
import com.kidcode.core.event.ExecutionEvent;
import com.kidcode.core.lexer.Lexer;
import com.kidcode.core.parser.Parser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // All routes in this controller will start with /api
@CrossOrigin(origins = {"https://kidcode.santosh-singh.me", "http://localhost:8080"})
public class KidCodeController {

    // A private record to define the structure of our expected JSON request body.
    // Spring Boot will automatically deserialize {"code": "..."} into this object.
    private record CodeExecutionRequest(String code) {}

    // A record for the validation error response
    public record ValidationError(String message, int lineNumber) {}

    @PostMapping("/execute")
    public List<ExecutionEvent> executeCode(@RequestBody CodeExecutionRequest request) {
        // Check for null or empty code to be safe
        if (request.code() == null || request.code().trim().isEmpty()) {
            return List.of(new ExecutionEvent.ErrorEvent("Code cannot be empty."));
        }

        // Instantiate our engine from the core module
        KidCodeEngine engine = new KidCodeEngine();

        // Execute the code and return the list of events.
        // Spring Boot will handle converting this list into a JSON array.
        return engine.execute(request.code());
    }

    // --- NEW VALIDATION ENDPOINT ---
    @PostMapping("/validate")
    public List<ValidationError> validateCode(@RequestBody CodeExecutionRequest request) {
        if (request.code() == null || request.code().isBlank()) {
            return List.of(); // No errors for empty code
        }

        Lexer lexer = new Lexer(request.code());
        Parser parser = new Parser(lexer);
        parser.parseProgram(); // This populates the error list in the parser

        // Convert the parser's string errors into structured ValidationError objects
        return parser.getErrors().stream()
                .map(errorString -> {
                    // Simple parsing to extract line number. Example error: "Error line 5: ..."
                    int lineNumber = 1; // Default
                    try {
                        String[] parts = errorString.split(":");
                        String linePart = parts[0].replaceAll("\\D+", "");
                        if (!linePart.isEmpty()) {
                            lineNumber = Integer.parseInt(linePart);
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors, just use line 1
                    }
                    return new ValidationError(errorString, lineNumber);
                })
                .toList();
    }
} 