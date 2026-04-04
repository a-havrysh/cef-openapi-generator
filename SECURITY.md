# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 3.1.x   | Yes       |
| < 3.1   | No        |

## Reporting a Vulnerability

If you discover a security vulnerability, please report it responsibly:

1. **Do NOT** open a public issue
2. Email: [open an advisory](https://github.com/a-havrysh/cef-openapi-generator/security/advisories/new)
3. Include: description, steps to reproduce, potential impact

We will respond within 72 hours and provide a fix timeline.

## Scope

This generator produces source code — it does not execute at runtime. Security considerations:

- Generated code uses Jackson for JSON parsing (ensure your project uses a recent Jackson version)
- Generated auth interceptors validate credentials but do not store them
- Generated CORS interceptor validates origins against a whitelist
- No network calls are made by the generator itself
