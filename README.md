# KidCode Visual Interpreter 🎨

**KidCode** is an educational programming language and visual interpreter designed to teach programming concepts through instant visual feedback. Control a turtle named "Cody" to draw, create patterns, and learn coding fundamentals interactively!

---
[![An image of @sansi28's Holopin badges, which is a link to view their full Holopin profile](https://holopin.me/sansi28)](https://holopin.io/@sansi28)

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

[MIT License](LICENSE)

---
**Happy Coding with KidCode!** 🎨✨ 
