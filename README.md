http-server-it
==============

A very simple HTTP server designed solely for integration testing.

## Install
mvn clean install

## Usage

Sample usage in tests:

```java
    ItHttpServer server = new DefaultItHttpServer(port);
    // TODO: continue
```

## Notes
You should note, that:
+ You should make non-concurrent requests to the server
+ You should properly set the expectations prior to server requests



