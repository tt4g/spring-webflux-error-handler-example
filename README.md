# Overview

Spring WebFlux error handling example.

Official Reference: https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/#boot-features-webflux-error-handling

## Run

```bash
$ ./gradlew bootRun

# Not Found.
$ curl -D - http://127.0.0.1:8080/
HTTP/1.1 404 Not Found
content-length: 0

# throew new RuntimeExeption()
$ curl -D - http://127.0.0.1:8080/throwError
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 131

{"timestamp":1600682055230,"path":"/throwError","status":500,"error":"Internal Server Error","message":"","requestId":"166f1f9d-3"}

# Mono.error(new RuntimeException())
$ curl -D - http://127.0.0.1:8080/returnError
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 132

{"timestamp":1600682017622,"path":"/returnError","status":500,"error":"Internal Server Error","message":"","requestId":"ff7a87a8-2"}

# Invalid path variable.
$ curl -D - http://127.0.0.1:8080/pathVariable/invalid
HTTP/1.1 400 Bad Request
content-length: 0

# Validation error (@Validated)
$ curl -D - -X POST -H "Content-Type: application/json" http://127.0.0.1:8080/validationError
HTTP/1.1 400 Bad Request
content-length: 0

# Validation error and manually error handling.
$ curl -D - -X POST -H "Content-Type: application/json" -d "{}" http://127.0.0.1:8080/validationErrorWithErrors
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 59

{"errors":{"name":["must not be null"]}}

# Success.
$ curl -D - -X POST -H "Content-Type: application/json" -d "{\"name\": \"foo\"}" http://127.0.0.1:8080/validationErrorWithErrors
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 14
  
{"name":"foo"}
```
