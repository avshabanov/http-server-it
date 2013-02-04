package com.alexshabanov.it.httpserver.producer;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
public class StaticJsonResponseProducer extends AbstractResponseProducer {
    private final String content;

    public StaticJsonResponseProducer(String content) {
        this.content = content;
    }

    @Override
    public HttpResponse create(HttpRequest request) throws IOException {
        return createResponse(HttpStatus.SC_OK, ContentType.APPLICATION_JSON, content);
    }
}
