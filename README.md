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

## 🎓 Quickstart Tutorial

### Your First Program: Drawing a Square

Let's start with a simple program that draws a square:

```kidcode
# Draw a square
repeat 4
    move forward 100
    turn right 90
end
```

**What's happening?**
- `repeat 4` loops the commands 4 times
- `move forward 100` moves Cody 100 pixels forward
- `turn right 90` turns Cody 90 degrees to the right
- `end` marks the end of the repeat block

### Adding Colors

Make your square more colorful:

```kidcode
# Colorful square
color "red"
repeat 4
    move forward 100
    turn right 90
end
```

### Using Variables

Create a variable to control the size:

```kidcode
# Variable-controlled square
set size = 150

repeat 4
    move forward size
    turn right 90
end
```

### Drawing a Star

Let's create something more complex:

```kidcode
# Five-pointed star
color "gold"
repeat 5
    move forward 200
    turn right 144
end
```

### Conditional Drawing

Use if statements to create conditional patterns:

```kidcode
# Draw based on condition
set size = 100

if size > 50
    color "blue"
    repeat 4
        move forward size
        turn right 90
    end
else
    color "red"
    move forward size
end
```

### Creating Reusable Functions

Define custom functions for complex patterns:

```kidcode
# Define a function to draw a square
define draw_square(side_length)
    repeat 4
        move forward side_length
        turn right 90
    end
end

# Use the function multiple times
color "purple"
draw_square(100)

pen up
move forward 150
pen down

color "orange"
draw_square(80)
```

### Working with Lists

Create and use lists to draw multiple shapes:

```kidcode
# List of sizes
set sizes = [50, 100, 150]

repeat 3
    set i = 0
    repeat 3
        set size = sizes[i]
        move forward size
        turn right 120
        set i = i + 1
    end
    turn right 120
end
```

### Complete Example: Spiral Pattern

Here's a complete example combining multiple concepts:

```kidcode
# Beautiful spiral
set angle = 90
set distance = 10
set colors = ["red", "orange", "yellow", "green", "blue", "purple"]

repeat 36
    set color_index = 0
    color colors[color_index]
    move forward distance
    turn right angle
    set distance = distance + 5
    set color_index = (color_index + 1)
end
```

---

## 📚 Language Reference

### Basic Commands
| Command | Syntax | Description | Example |
|---------|--------|-------------|---------|
| move forward | `move forward <value>` | Moves Cody forward by pixels | `move forward 100` |
| turn | `turn <left/right> <degrees>` | Turns Cody | `turn left 90` |
| say | `say "message"` | Displays a message | `say "Hello!"` |
| pen | `pen <up/down>` | Lifts/lowers pen | `pen up` |
| color | `color "color_name"` | Changes pen color | `color "red"` |
| home | `home` | Moves Cody to center (250, 250) | `home` |

### Variables and Arithmetic
```kidcode
# Create variables
set x = 10
set y = 20

# Arithmetic operations
set sum = x + y          # Addition: 30
set difference = y - x   # Subtraction: 10
set product = x * y      # Multiplication: 200
set quotient = y / x     # Division: 2

# Use in movement
move forward sum
```

### Lists and Indexing
```kidcode
# Create a list
set numbers = [10, 20, 30, 40, 50]

# Access elements (0-indexed)
set first = numbers[0]    # Gets 10
set third = numbers[2]    # Gets 30

# Mix different types
set mixed = [1, "hello", 42]
```

### Control Flow - Repeat Loops
```kidcode
# Basic repeat
repeat 5
    move forward 50
    turn right 72
end

# Nested loops
repeat 4
    repeat 3
        move forward 30
        turn right 120
    end
    turn right 90
end
```

### Control Flow - If/Else Statements
```kidcode
set x = 10

if x > 5
    color "green"
    say "x is greater than 5"
else
    color "red"
    say "x is 5 or less"
end

# Comparison operators: ==, !=, <, >, <=, >=
if x == 10
    say "x equals 10"
end
```

### Functions
```kidcode
# Define a function
define triangle(size)
    repeat 3
        move forward size
        turn right 120
    end
end

# Call the function
triangle(100)
triangle(50)

# Functions with multiple parameters
define rectangle(width, height)
    repeat 2
        move forward width
        turn right 90
        move forward height
        turn right 90
    end
end

rectangle(100, 50)
```

### String Operations
```kidcode
# String concatenation
set first_name = "Kid"
set last_name = "Code"
set full_name = first_name + " " + last_name

say full_name  # Displays: "Kid Code"

# Combining strings and numbers
set age = 10
say "I am " + age + " years old"
```

### Supported Colors
KidCode supports the following color names:
- `"red"`, `"green"`, `"blue"`
- `"yellow"`, `"orange"`, `"purple"`
- `"pink"`, `"cyan"`, `"magenta"`
- `"black"`, `"white"`, `"gray"`
- `"brown"`, `"lime"`, `"navy"`
- `"teal"`, `"maroon"`, `"olive"`
- `"silver"`, `"gold"`

### Comments
```kidcode
# This is a single-line comment
# Comments are ignored by the interpreter

move forward 100  # You can also add comments after code
```

### Complete Examples

#### Example 1: Hexagon
```kidcode
# Draw a colorful hexagon
set colors = ["red", "orange", "yellow", "green", "blue", "purple"]
set i = 0

repeat 6
    color colors[i]
    move forward 100
    turn right 60
    set i = i + 1
end
```

#### Example 2: Flower Pattern
```kidcode
# Draw a flower with 8 petals
define petal()
    repeat 2
        move forward 100
        turn right 60
        move forward 100
        turn right 120
    end
end

color "pink"
repeat 8
    petal()
    turn right 45
end
```

#### Example 3: Checkerboard Pattern
```kidcode
# Simple checkerboard
define square(size)
    repeat 4
        move forward size
        turn right 90
    end
end

set row = 0
repeat 4
    set col = 0
    repeat 4
        if (row + col) == (row + col) / 2 * 2
            color "black"
        else
            color "red"
        end
        square(50)
        pen up
        move forward 50
        pen down
        set col = col + 1
    end
    pen up
    turn right 180
    move forward 200
    turn left 90
    move forward 50
    turn right 90
    pen down
    set row = row + 1
end
```

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
