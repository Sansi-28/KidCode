package com.kidcode.web.controller;

import com.kidcode.core.evaluator.ExecutionContext;  // <-- core ExecutionContext
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/debugger")
public class DebuggerController {

    private final ExecutionContext context;

    public DebuggerController(ExecutionContext context) {
        this.context = context;
    }
    @PostMapping("/pause")
    public String pause() {
        context.pause();
        return "Paused";
    }

    @PostMapping("/resume")
    public String resume() {
        context.resume();
        return "Resumed";
    }

    @PostMapping("/step")
    public String step() {
        context.step();
        return "Stepped one statement";
    }

    @PostMapping("/terminate")
    public String terminate() {
        context.terminate();
        return "Terminated";
    }
}