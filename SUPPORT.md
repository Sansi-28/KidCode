# Support

## Getting Help

If you need help with KidCode, here are the best ways to get support:

### 📖 Documentation
- Check the [README.md](README.md) for basic usage instructions
- Look at the example scripts in the [`test_scripts/`](test_scripts/) directory
- Review the language reference in the web interface help section

### 🐛 Bug Reports
If you've found a bug, please [open an issue](https://github.com/Sansi-28/KidCode/issues/new) and include:
- Steps to reproduce the problem
- Expected behavior vs actual behavior
- Your operating system and Java version
- Any error messages or screenshots
- The KidCode script that caused the issue (if applicable)

### 💡 Feature Requests
Have an idea for improving KidCode? [Open an issue](https://github.com/Sansi-28/KidCode/issues/new) and describe:
- What feature you'd like to see
- Why it would be useful
- How you imagine it working
- Any examples or mockups

### 🤝 Community Support
- Browse existing [GitHub Issues](https://github.com/Sansi-28/KidCode/issues) - your question might already be answered
- Join our [Discussions](https://github.com/Sansi-28/KidCode/discussions) for community help
- Check the [Contributing Guide](CONTRIBUTING.md) if you want to help improve KidCode

### 📧 Direct Contact
For private or security-related issues, you can:
- Open a private security advisory on GitHub: https://github.com/Sansi-28/KidCode/security/advisories/new
- Contact the maintainers through GitHub

## Response Times

- **Bug reports**: We aim to respond within 48-72 hours
- **Feature requests**: We review these weekly and will respond within a week
- **Security issues**: We respond within 24 hours
- **General questions**: Usually answered within 2-3 days

## Self-Service Resources

Before reaching out, you might find these resources helpful:

1. **Language Reference**: Available in the web interface help modal
2. **Example Scripts**: See the `test_scripts/` folder for working examples
3. **Architecture Guide**: Check the README.md for technical details
4. **Common Issues**: Look at closed GitHub issues for solutions

## KidCode Language Quick Reference

### Basic Commands
- `say "Hello World"` - Display text
- `move 100` - Move forward 100 pixels
- `turn 90` - Turn right 90 degrees
- `pen up` / `pen down` - Control drawing
- `color "red"` - Set drawing color

### Variables and Data
- `set x = 5` - Create a variable
- `set myList = [1, 2, 3]` - Create a list
- `set name = "Alice"` - Create a string

### Control Flow
- `if (x > 5) { ... }` - Conditional execution
- `repeat 4 { ... }` - Loop 4 times

### Functions
```kidcode
function drawSquare(size) {
    repeat 4 {
        move size
        turn 90
    }
}
```

## Contributing

If you'd like to contribute to KidCode, please see our [Contributing Guide](CONTRIBUTING.md).

Thank you for using KidCode! 🎨 Happy coding!