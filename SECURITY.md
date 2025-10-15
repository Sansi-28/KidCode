# Security Policy

## Supported Versions

We currently support the following versions of KidCode with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

We take the security of KidCode seriously. If you believe you have found a security vulnerability, please report it to us as described below.

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report them by opening a private security advisory on GitHub or by contacting the project maintainers directly.

Please include the requested information listed below (as much as you can provide) to help us better understand the nature and scope of the possible issue:

* Type of issue (e.g. code injection, unauthorized access, etc.)
* Full paths of source file(s) related to the manifestation of the issue
* The location of the affected source code (tag/branch/commit or direct URL)
* Any special configuration required to reproduce the issue
* Step-by-step instructions to reproduce the issue
* Proof-of-concept or exploit code (if possible)
* Impact of the issue, including how an attacker might exploit the issue

This information will help us triage your report more quickly.

## Preferred Languages

We prefer all communications to be in English.

## Response Timeline

We aim to respond to security vulnerability reports within 48 hours and will keep you informed of our progress throughout the resolution process.

## Security Considerations for KidCode

KidCode is designed as an educational programming language for children. We take the following security measures:

- **Execution Timeout**: Code execution is limited to prevent infinite loops
- **Sandboxed Environment**: The interpreter runs in a controlled environment
- **Input Validation**: All user input is validated before processing
- **Limited System Access**: The language intentionally has limited system interaction capabilities