# **KidCode Visual Interpreter Documentation**

---

## **Introduction**

Welcome to **KidCode Visual Interpreter**, a simple, kid-friendly visual coding environment that lets users control a character named "Cody" using text-based commands. Cody can perform various actions, such as moving, turning, and interacting through text, and users can now perform arithmetic operations, set variables, and create simple control structures.

This interpreter is designed to teach basic programming concepts such as loops, conditions, variable assignments, and arithmetic operations in a fun and visual way.

---

## **Features**

### 1. **Movement Commands**
- **Move Forward**: Move Cody forward by a specified number of steps.
    - Syntax: `move forward [steps]`
    - Example: `move forward 50`

- **Turn Right**: Turn Cody to the right by a specified number of degrees.
    - Syntax: `turn right [degrees]`
    - Example: `turn right 90`

- **Turn Left**: Turn Cody to the left by a specified number of degrees.
    - Syntax: `turn left [degrees]`
    - Example: `turn left 90`

### 2. **Text Commands**
- **Say**: Make Cody display a message.
    - Syntax: `say [message]`
    - Example: `say "Hello, World!"`

### 3. **Control Structures**
- **Repeat**: Repeat a set of commands a certain number of times.
    - Syntax:
      ```
      repeat [times]
          // commands
      end repeat
      ```
    - Example:
      ```
      repeat 3
          move forward 50
          turn right 90
      end repeat
      ```

- **If**: Execute commands if a certain condition is true.
    - Syntax:
      ```
      if [condition]
          // commands
      end if
      ```
    - Example:
      ```
      if true
          say "Condition is true!"
      end if
      ```

### 4. **Variable Management**
- **Set**: Assign a value to a variable.
    - Syntax: `set [variable] [value]`
    - Example: `set score 100`

### 5. **Function Definition**
- **Define**: Define a custom function that can be called later.
    - Syntax:
      ```
      define [function_name]
          // commands
      end define
      ```
    - Example:
      ```
      define greet
          say "Hello!"
      end define
      ```

- **Calling Functions**: Once defined, a function can be called by simply writing its name.
    - Example:
      ```
      greet
      ```

### 6. **Arithmetic Operations**

KidCode Visual Interpreter now supports basic arithmetic operations. These commands allow you to perform calculations using variables.

- **Add**: Adds two variables and stores the result in the first variable.
    - Syntax: `add [var1] [var2]`
    - Example:
      ```
      set score 10
      set bonus 5
      add score bonus
      ```

- **Subtract**: Subtracts the second variable from the first and stores the result in the first variable.
    - Syntax: `subtract [var1] [var2]`
    - Example:
      ```
      set score 15
      set penalty 3
      subtract score penalty
      ```

- **Multiply**: Multiplies two variables and stores the result in the first variable.
    - Syntax: `multiply [var1] [var2]`
    - Example:
      ```
      set total 2
      set factor 5
      multiply total factor
      ```

- **Divide**: Divides the first variable by the second and stores the result in the first variable.
    - Syntax: `divide [var1] [var2]`
    - Example:
      ```
      set total 10
      set divisor 2
      divide total divisor
      ```

---

## **Command Reference**

### **Command List**

| Command         | Description                                              | Example                        |
|-----------------|----------------------------------------------------------|--------------------------------|
| `move forward`  | Move Cody forward by a specified number of steps.         | `move forward 50`              |
| `turn right`    | Turn Cody to the right by a specified number of degrees.  | `turn right 90`                |
| `turn left`     | Turn Cody to the left by a specified number of degrees.   | `turn left 90`                 |
| `say`           | Make Cody display a message.                             | `say "Hello, World!"`          |
| `repeat`        | Repeat a block of code a number of times.                | See **Repeat Example**         |
| `if`            | Execute a block of code if a condition is true.          | See **If Example**             |
| `set`           | Set the value of a variable.                             | `set score 100`                |
| `add`           | Add the values of two variables and store the result.     | `add score bonus`              |
| `subtract`      | Subtract the second variable from the first.             | `subtract score penalty`       |
| `multiply`      | Multiply the values of two variables and store the result.| `multiply total factor`        |
| `divide`        | Divide the first variable by the second.                 | `divide total divisor`         |
| `define`        | Define a custom function.                                | See **Define Example**         |

---

## **Usage Examples**

### **Arithmetic Example**

This example demonstrates how to perform arithmetic operations on variables.

```
set score 50
set bonus 10
add score bonus
say "Score after bonus: " + score

set penalty 5
subtract score penalty
say "Score after penalty: " + score

set multiplier 2
multiply score multiplier
say "Final score: " + score
```

### **Function Example**

This example defines a function and calls it multiple times.

```
define greet
    say "Hello!"
end define

greet
greet
```

### **Repeat Example**

This example moves Cody in a square pattern by repeating a block of code.

```
repeat 4
    move forward 100
    turn right 90
end repeat
```

### **If Example**

This example demonstrates how to use an `if` statement to check a condition.

```
set score 100
if score > 50
    say "You passed!"
end if
```

---

## **Help and Tips**

- **Variable Scope**: Variables are global and accessible anywhere within the code.
- **Error Handling**: The interpreter will handle basic errors, such as division by zero, by displaying error messages.
- **Functionality**: You can combine commands, variables, and control structures to create more complex programs.
