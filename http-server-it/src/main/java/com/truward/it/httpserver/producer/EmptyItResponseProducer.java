package com.truward.it.httpserver.producer;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

/**
 * Response writer that answers with 'NO CONTENT' to all the requests
 *
 * @author Alexander Shabanov
 */
public final class EmptyItResponseProducer extends AbstractResponseProducer {
    /**
     * Singleton instance, that generates response with NO CONTENT status code
     */
    public static final EmptyItResponseProducer NO_CONTENT_INSTANCE = new EmptyItResponseProducer(HttpStatus.SC_NO_CONTENT);

    private final int statusCode;

    public EmptyItResponseProducer(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public HttpResponse create(HttpRequest request) {
        return createResponse(statusCode);
    }
}
