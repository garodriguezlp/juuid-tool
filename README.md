# JUUIDTool

A cross-platform command-line UUID generator built with JBang and Java 17+.

## Features

- Generates UUIDs in multiple versions (v1, v3, v4, v5) per RFC 4122
- Version 4 (random) is the default and most commonly used
- Automatically copies to system clipboard with fallback strategies
- Works on Windows, macOS, and Linux
- Supports both desktop and headless environments
- Single-file, zero-configuration implementation

## Requirements

- [JBang](https://www.jbang.dev/) 0.56.0 or higher (or use the wrapper script)
- Java 17 or higher

## Installation

Install with a convenient lowercase name:

```bash
jbang app install --name juuid JUUIDTool.java
```

Or use the JBang wrapper if you don't have JBang installed locally - it will download and set up JBang automatically:

**Windows:**

```bash
# The shebang in the file allows direct execution
jbang JUUIDTool.java
```

**Unix/Linux/macOS:**

```bash
chmod +x JUUIDTool.java
./JUUIDTool.java
```

## Usage

### Basic Usage (Type 4 - Random)

```bash
# Using installed app
juuid

# Or run directly
jbang JUUIDTool.java
```

**Output:**

```
a1b2c3d4-e5f6-7890-abcd-ef1234567890
UUID copied to clipboard!
```

### UUID Types

#### Type 1 (Time-based)

```bash
juuid -t 1
# Generates time-based UUID with timestamp and node identifier
```

#### Type 3 (MD5 Name-based)

```bash
juuid -t 3 -n "example.com"
# Generates UUID from name using MD5 hash
```

#### Type 4 (Random - Default)

```bash
juuid
# or explicitly:
juuid -t 4
```

#### Type 5 (SHA-1 Name-based)

```bash
juuid -t 5 -n "example.com"
# Generates UUID from name using SHA-1 hash
```

### Advanced Options

Specify a custom namespace for type 3/5 (default is DNS namespace):

```bash
juuid -t 5 -n "myapp" -ns "6ba7b811-9dad-11d1-80b4-00c04fd430c8"
```

### Help

View all available options:

```bash
juuid --help
```

## Cross-Platform Clipboard Support

The tool uses a robust multi-strategy approach for clipboard operations:

**Primary Strategy:**

- Java AWT clipboard API (works in desktop environments)

**Fallback Strategies:**

- **Windows**: `clip.exe`
- **macOS**: `pbcopy`
- **Linux**: `xclip` or `xsel`

This ensures clipboard functionality works in headless environments, SSH sessions, and Docker containers.

### Linux Setup

For Linux headless environments, install clipboard utilities:

```bash
# Debian/Ubuntu
sudo apt-get install xclip

# or
sudo apt-get install xsel
```

## Command-Line Options

| Option        | Short | Description                                             |
|---------------|-------|---------------------------------------------------------|
| `--type`      | `-t`  | UUID type (1, 3, 4, or 5) - default: 4                  |
| `--name`      | `-n`  | Name for type 3/5 generation (required for those types) |
| `--namespace` | `-ns` | Namespace UUID for type 3/5 (default: DNS namespace)    |
| `--help`      | `-h`  | Show help message                                       |
| `--version`   | `-V`  | Show version information                                |

## License

MIT
