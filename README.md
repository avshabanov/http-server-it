http-server-it
==============

A very simple HTTP server for integration testing.

## Install

mvn clean install

## Usage

Sample usage in tests:

```java
    ItHttpServer server = new DefaultItHttpServer(executorService);
    // Set response handler that returns 204 NO CONTENT for all the requests
    server.setResponseProducer(EmptyItResponseProducer.NO_CONTENT_INSTANCE);

    // request for something
    HttpDelete httpDelete = new HttpDelete("http://127.0.0.1:" + server.getPort() + "/delete/something";
    HttpResponse response = httpClient.execute(httpDelete);
    // response.getStatusLine().getStatusCode() will return 204
```

## Notes

You should note, that:
+ You should make non-concurrent requests to the server
+ You should properly set the expectations prior to server requests

## Maven

```xml
    <dependency>
        <groupId>com.truward.it.httpserver</groupId>
        <artifactId>http-server-it</artifactId>
        <version>1.0.1</version>
    </dependency>
```
