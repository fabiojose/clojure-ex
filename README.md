[![Build Status](https://travis-ci.org/fabiojose/clojure-ex.svg?branch=master)](https://travis-ci.org/fabiojose/clojure-ex)
[![codecov](https://codecov.io/gh/fabiojose/clojure-ex/branch/master/graph/badge.svg)](https://codecov.io/gh/fabiojose/clojure-ex)

# userin

A Clojure APP to read json entries from stdin until receive an empty line.

Entries like that:

```json
{ "account": { "activeCard": true, "availableLimit": 100 } }
{ "transaction": { "merchant": "Burger King", "amount": 20, "time": "2019-02-13T10:00:00.000Z" } }
{ "transaction": { "merchant": "Habbib's", "amount": 80, "time": "2019-02-13T11:00:00.000Z" } }
```

## Building & Run

### Using Docker

- Install Docker 18+

**Building a uberjar**

```bash
./docker-build.sh
```

**Building a native executable**

```bash
./docker-build-native.sh
```

**To run**

```bash
./docker-run.sh
```

### Using Leiningen

- Install Java 1.8+
- Install Leiningen 2.9+

**Builing a uberjar**

```bash
lein do clean, uberjar
```

**To run**

```bash
java -jar target/app.jar < input.json
```

Or, run using Leiningen

```bash
lein run < input.json
```

## License

Apache License Version 2.0
