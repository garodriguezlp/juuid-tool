
# UUID Generator Tool - Specification

## Overview
Create a JBang-based Java 17+ command-line tool for generating UUIDs.

## Base Implementation
Use [JUUIDTool.java](./JUUIDTool.java) as the starting point.

## Functionality
When executed, the tool shall:
1. Generate a random UUID
2. Print the UUID to the console
3. Copy the UUID to the system clipboard
4. Display a confirmation message indicating the UUID is now in the clipboard

## Technical Requirements
- JBang compatible
- Java 17 or higher
- Use Picocli for command-line interface
- Single-file implementation

## Code Quality Standards
- Single level of abstraction per method
- Use nested static classes for maintainability and clean code organization
- Follow SOLID principles