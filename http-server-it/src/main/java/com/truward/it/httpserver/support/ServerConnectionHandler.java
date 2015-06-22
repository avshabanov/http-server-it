package com.truward.it.httpserver.support;

import com.truward.it.httpserver.ItResponseProducer;
import com.truward.it.httpserver.producer.EmptyItResponseProducer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Handles one server connection to certain client
 *
 * @author Alexander Shabanov
 */
public final class ServerConnectionHandler implements Callable<HttpRequest> {
  private final Logger log = LoggerFactory.getLogger(ServerConnectionHandler.class);

  private final ItResponseProducer responseWriter;
  private final Socket connectionSocket;

  public ServerConnectionHandler(Socket connectionSocket, ItResponseProducer responseWriter) {
    this.connectionSocket = connectionSocket;
    this.responseWriter = responseWriter;
  }

  @Override
  public HttpRequest call() throws Exception {
    final DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
    serverConnection.bind(connectionSocket, new BasicHttpParams());
    try {
      final HttpRequest request = serverConnection.receiveRequestHeader();
      if (request instanceof HttpEntityEnclosingRequest) {
        serverConnection.receiveRequestEntity((HttpEntityEnclosingRequest) request);
      }

      ItResponseProducer producer = this.responseWriter;
      if (producer == null) {
        log.warn("No response writer, 'No Content' response writer will be used");
        producer = EmptyItResponseProducer.NO_CONTENT_INSTANCE;
      }

      final HttpResponse response = producer.create(request);
      serverConnection.sendResponseHeader(response);

      final HttpEntity entity = response.getEntity();
      if (entity != null) {
        try {
          serverConnection.sendResponseEntity(response);
        } finally {
          EntityUtils.consume(entity);
        }
      }

      return request;
    } finally {
      serverConnection.close();
      assert connectionSocket.isClosed();
    }
  }
}
