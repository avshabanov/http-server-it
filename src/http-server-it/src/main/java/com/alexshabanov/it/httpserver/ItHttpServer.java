package com.alexshabanov.it.httpserver;

import org.apache.http.HttpRequest;

import java.util.List;

/**
 * Represents HTTP server that might be used for test purposes.
 *
 * @author Alexander Shabanov
 */
public interface ItHttpServer {
    /**
     * Establishes currently used response producer.
     *
     * @param producer Response producer that should be used from here on.
     */
    void setResponseProducer(ItResponseProducer producer);

    /**
     * @return Port number, that is used to open server connection
     */
    int getPort();

    /**
     * @return Lately received http requests.
     */
    List<HttpRequest> getReceivedRequests();

    /**
     * Empties the received requests queue
     */
    void clearReceivedRequests();
}
