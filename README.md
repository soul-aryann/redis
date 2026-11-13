# Redis in Java

[![CodeCrafters progress](https://backend.codecrafters.io/progress/redis/a3100fd5-ae8d-4654-bc04-84e31d1f8c5e)](https://app.codecrafters.io/users/soul-aryann?r=2qF)

A lightweight Redis-inspired server built in Java as part of the [CodeCrafters Build Your Own Redis challenge](https://codecrafters.io/challenges/redis). It accepts RESP commands over TCP and supports `PING`, `ECHO`, `SET`, and `GET`, including millisecond key expiry with `PX`.

## Features

- RESP array and bulk-string request parsing
- RESP simple-string, bulk-string, integer, null, and error responses
- `PING`, `ECHO`, `SET`, and `GET` commands
- `SET key value PX milliseconds` expiration
- Concurrent client connections and thread-safe key storage

## Requirements

- Java 25 with preview features enabled
- Maven

## Run the server

Start the server on port `6379`:

```sh
./your_program.sh
```

The script builds the project and launches the server. Connect with `redis-cli` or another RESP-compatible client:

```sh
redis-cli -p 6379 PING
redis-cli -p 6379 SET greeting "Hello, Redis!"
redis-cli -p 6379 GET greeting
redis-cli -p 6379 SET temporary value PX 5000
```

## Build

To package the application with Maven:

```sh
mvn clean package
```

## Supported commands

| Command | Example | Result |
| --- | --- | --- |
| `PING` | `PING` | `PONG` |
| `ECHO` | `ECHO hello` | Returns `hello` |
| `SET` | `SET name redis` | Stores a string and returns `OK` |
| `SET` with expiry | `SET name redis PX 1000` | Stores a string for 1,000 milliseconds |
| `GET` | `GET name` | Returns the value, or a null bulk string if missing or expired |

## Project layout

- `Main.java` — opens the TCP server and accepts clients
- `ClientHandler.java` — reads commands and writes responses for each connection
- `RespParser.java` — parses RESP requests
- `CommandHandler.java` — validates and dispatches commands
- `KeyValueStore.java` — stores values and handles expiration
- `ResponseHandler.java` — encodes RESP responses

This project is an educational Redis implementation and supports only the commands listed above. For CodeCrafters challenge instructions and submission, see [Build Your Own Redis](https://codecrafters.io/challenges/redis).
