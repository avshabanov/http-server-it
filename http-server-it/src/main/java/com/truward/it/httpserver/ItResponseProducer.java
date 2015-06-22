package com.truward.it.httpserver;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * Abstracts an entity that parses requests and produces an http response to be sent back to the client.
 *
 * @author Alexander Shabanov
 */
public interface ItResponseProducer {
  HttpResponse create(HttpRequest request) throws IOException;
}
