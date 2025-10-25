# KidCode Visual Interpreter 🎨

**KidCode** is an educational programming language and visual interpreter designed to teach programming concepts through instant visual feedback. Control a turtle named "Cody" to draw, create patterns, and learn coding fundamentals interactively!

---
[![An image of @sansi28's Holopin badges, which is a link to view their full Holopin profile](https://holopin.me/sansi28)](https://holopin.io/@sansi28)

## 🐢 What is KidCode?

**KidCode** is an educational programming language inspired by **turtle graphics** – a visual way to learn programming where a "turtle" moves on the screen, drawing lines according to commands. This approach helps beginners see the results of their code step-by-step, making abstract programming concepts tangible and fun.

### Meet Cody, Your Drawing Companion
In KidCode, you control a turtle named **"Cody"** using simple, intuitive commands:
- `move forward 100` – Move Cody forward
- `turn right 90` – Rotate Cody
- `pen down` / `pen up` – Control whether Cody draws while moving
- `color "red"` – Change the drawing color

These basic building blocks allow you to create everything from simple lines to complex patterns, spirals, and geometric art!

### How the Interpreter Works

KidCode uses a **multi-stage interpreter pipeline** that transforms your code into visual output:

1. **Lexer (Tokenization)**: Reads your source code character by character and breaks it into meaningful tokens (keywords, numbers, strings, operators).

2. **Parser (Syntax Analysis)**: Takes the stream of tokens and constructs an Abstract Syntax Tree (AST) that represents the structure of your program. It checks that your code follows KidCode's grammar rules.

3. **Evaluator (Execution)**: Walks through the AST and executes your code, evaluating expressions, managing variables, and handling control flow (loops, conditionals, functions).

4. **Event Generation**: As the evaluator runs, it generates **execution events** (move, turn, say, color change, etc.) instead of directly manipulating the UI. This event-driven architecture keeps the core interpreter UI-agnostic.

5. **Visualization**: The UI layer (desktop or web) consumes these events and renders them on the canvas – currently showing the **final result** of all drawing commands.

---

## 🏗️ Project Structure (Maven Multi-Module)

This project is organized as a Maven multi-module repository:

- **kidcode-core**: Headless, event-driven core logic (lexer, parser, AST, evaluator, event API)
- **kidcode-desktop**: Desktop application (Swing GUI) and CLI runner, consuming the core event API
- **kidcode-web**: Spring Boot backend (REST API) and modern web frontend (Monaco editor, live validation, HTML5 canvas)

---

## ✨ Features

### 🎯 Language Features
- **Variables, Arithmetic, Lists**: Store/manipulate values, collections, and perform calculations
- **Loops & Conditionals**: `repeat`, `if`/`else` for control flow
- **Functions**: Define reusable commands
- **String Operations**: Concatenate and display messages

### 🎨 Visual & UI Features
- **Real-time Visualization**: Watch Cody draw on canvas (desktop & web)
- **Pen & Color Control**: `pen up/down`, `color` commands
- **Modern Web Editor**: Monaco-based code editor with syntax highlighting and live validation
- **Output Panel**: See logs, errors, and messages
- **Execution Control**: Stop/clear execution at any time

### 🌐 Web API & Frontend
- **REST API**: `/api/execute` runs code, `/api/validate` checks syntax (JSON events/errors)
- **Web UI**: Monaco editor, live error squiggles, HTML5 canvas, responsive design

### 🔒 Safety & Performance
- **Execution Timeout**: Prevents infinite loops
- **Graceful Error Handling**: Clear, line-numbered diagnostics
- **Responsive UI**: Remains interactive during execution

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven

### Build All Modules
```bash
# Clone the repository
 git clone <repository-url>
 cd KidCode

# Build all modules
 mvn clean package
```

### Run the Desktop App
```bash
cd kidcode-desktop
mvn exec:java -Dexec.mainClass="com.kidcode.gui.KidCodeVisualInterpreter"
```
Or run the CLI:
```bash
cd kidcode-desktop
mvn exec:java -Dexec.mainClass="com.kidcode.cli.CommandLineRunner" -Dexec.args="../test_scripts/<script.kc>"
```

### Run the Web App
```bash
cd kidcode-web
mvn spring-boot:run
```
Then open [http://localhost:8080](http://localhost:8080) in your browser.

---

## 📚 Language Reference

### Basic Commands
| Command | Syntax | Description |
|---------|--------|-------------|
| move forward | `move forward <value>` | Moves Cody forward by pixels |
| turn | `turn <left/right> <degrees>` | Turns Cody |
| say | `say "message"` | Displays a message |
| pen | `pen <up/down>` | Lifts/lowers pen |
| color | `color "color_name"` | Changes color |

### Variables, Lists, Functions, Control Flow
(See original README for full table and examples)

Example and test scripts can be found in the `test_scripts/` folder at the project root.

---

## 🏛️ Architecture

- **kidcode-core**: Pure Java, event-driven. Exposes a sealed `ExecutionEvent` API (Move, Say, Error, Clear, etc.) for UI-agnostic consumption.
- **kidcode-desktop**: Swing GUI and CLI, visualizes events from the core.
- **kidcode-web**: Spring Boot REST API (`/api/execute`, `/api/validate`) and static frontend (Monaco editor, canvas, output log).

### Event-Driven API
The core emits events (move, say, error, clear, etc.) as code executes. Both desktop and web UIs consume these events to update the UI or canvas.

### REST API
- `POST /api/execute` — Run KidCode, returns a list of events as JSON
- `POST /api/validate` — Validate code, returns syntax errors (for Monaco squiggles)

---

## 🖥️ Web Frontend
- **Monaco Editor**: Professional code editing, syntax highlighting, and error squiggles
- **Live Validation**: Errors shown instantly as you type
- **HTML5 Canvas**: Visualizes Cody's drawing
- **Output Log**: See messages and errors

---

## 🤝 Contributing

Contributions are welcome! Please fork, branch, and submit a pull request.

---

## 📄 License

MIT License

---
**Happy Coding with KidCode!** 🎨✨ 
