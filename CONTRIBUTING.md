# 🧩 Contributing to KidCode

## 🎨 KidCode Visual Interpreter

**KidCode** is an educational programming language and visual interpreter designed to teach programming concepts through instant visual feedback.  
Control a turtle named **“Cody”** to draw, create patterns, and learn coding fundamentals interactively!

---

## 💬 Welcome!

Thanks for your interest in contributing to **KidCode**! 🌟  
Whether you’re fixing a bug, improving documentation, or adding new features, your help makes this project better for everyone.  

This guide will help you get started and understand how to contribute effectively.

---

## 🚀 Getting Started

### 🧰 Prerequisites

Before contributing, make sure you have the following installed:

- **Java 17+**
- **Maven 3.8+**
- A code editor (e.g., IntelliJ IDEA, VS Code)

### 🏗️ Build the Project

Clone the repository and build all modules:

```bash
git clone https://github.com/<your-username>/KidCode.git
cd KidCode
mvn clean package
```

---

## 📁 Project Structure (Maven Multi-Module)

| Module | Description |
|--------|--------------|
| **kidcode-core** | Headless, event-driven core logic (lexer, parser, evaluator, event API) |
| **kidcode-desktop** | Swing GUI and CLI visual interpreter |
| **kidcode-web** | Spring Boot backend & modern web frontend (Monaco editor, REST API) |

---

## ▶️ Running the Applications

### 🖥️ Desktop App
```bash
cd kidcode-desktop
mvn exec:java -Dexec.mainClass="com.kidcode.gui.KidCodeVisualInterpreter"
```

### 🧮 Command Line Interface
```bash
cd kidcode-desktop
mvn exec:java -Dexec.mainClass="com.kidcode.cli.CommandLineRunner" -Dexec.args="../test_scripts/<script.kc>"
```

### 🌐 Web App
```bash
cd kidcode-web
mvn spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080) in your browser.

---

## 💡 Ways to Contribute

You can contribute in many ways:

- 🐞 **Report Bugs:** Found something broken? Open an issue with clear reproduction steps.
- 🧠 **Suggest Features:** Have an idea? Propose it in the Issues tab.
- 🧰 **Improve Documentation:** Fix typos, clarify instructions, or add new guides.
- 🧪 **Write Tests:** Strengthen reliability by writing or improving unit tests.
- 💅 **Enhance UI/UX:** Suggest or implement frontend or visual improvements.

---

## 🛠️ Development Workflow

1. **Create a new branch**
   ```bash
   git checkout -b feature/awesome-improvement
   ```

2. **Make your changes**

3. **Run tests and build**
   ```bash
   mvn clean package
   ```

4. **Commit and push**
   ```bash
   git add .
   git commit -m "Add awesome improvement"
   git push origin feature/awesome-improvement
   ```

5. **Open a Pull Request (PR)**
   Go to your fork on GitHub and open a PR into the `main` branch of the original repository.

---

## 🧭 Code Style & Guidelines

- Follow standard **Java naming conventions**
- Keep methods small and readable
- Write comments for complex logic
- Test your code before committing
- Keep pull requests focused and descriptive

---

## 🏷️ Hacktoberfest Participation

This project proudly welcomes **Hacktoberfest** contributions! 🎃💻

To participate:
1. Sign up at [hacktoberfest.com](https://hacktoberfest.com)
2. Submit **4 quality pull requests** during October
3. Earn a digital badge or limited-edition swag!


---

## 🤝 Code of Conduct

Please be kind, respectful, and collaborative.  
KidCode is a welcoming space for learners and contributors of all backgrounds.  
Harassment or disrespectful behavior will not be tolerated.

---

## 💬 Need Help?

If you’re stuck or unsure about something:
- Open a **GitHub issue**
- Comment on related discussions
- Tag maintainers or contributors for guidance

---

## 💖 Thank You

Your contributions make **KidCode** a more powerful and educational tool for everyone.  
Let’s inspire creativity and learning — one line of code at a time! ✨🐢
