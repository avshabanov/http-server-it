package com.alexshabanov.it.httpserver;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
public interface ItResponseProducer {
    HttpResponse create(HttpRequest request) throws IOException;
}
