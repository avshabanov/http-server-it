package com.truward.it.httpserver;

import com.truward.it.httpserver.producer.EmptyItResponseProducer;
import com.truward.it.httpserver.producer.StaticJsonResponseProducer;
import com.truward.it.httpserver.support.DefaultItHttpServer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Sample spring-driven test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ItHttpServerTest.Config.class)
public class ItHttpServerTest {

  private final String host = "http://127.0.0.1";

  @Autowired
  private ItHttpServer httpServer;

  @Autowired
  private HttpClient httpClient;

  @Before
  public void clearRequests() {
    httpServer.clearReceivedRequests();
  }

  @Test
  public void shouldReceiveNoContent() throws Exception {
    httpServer.setResponseProducer(EmptyItResponseProducer.NO_CONTENT_INSTANCE);
    final HttpDelete httpDelete = new HttpDelete(host + ":" + httpServer.getPort() + "/delete/something");

    executeAndCompare(httpDelete, HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void shouldReceiveError() throws Exception {
    final int statusCode = HttpStatus.SC_NOT_FOUND;

    httpServer.setResponseProducer(new EmptyItResponseProducer(statusCode));
    final HttpGet httpGet = new HttpGet(host + ":" + httpServer.getPort() + "/get/something");
    executeAndCompare(httpGet, HttpStatus.SC_NOT_FOUND);
  }

  @Test
  public void shouldReceiveJsonContent() throws Exception {
    final String sentContent = "{ \"a\": 4, \"b\": 10 }";

    httpServer.setResponseProducer(new StaticJsonResponseProducer(sentContent));
    final HttpPost httpPost = new HttpPost(host + ":" + httpServer.getPort() + "/post/something");
    httpPost.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

    final StringEntity entity = new StringEntity("{ \"value\": 5 }", ContentType.APPLICATION_JSON);
    httpPost.setEntity(entity);

    try {
      executeAndCompare(httpPost, HttpStatus.SC_OK,
          new ImmutablePair<String, ContentType>(sentContent, ContentType.APPLICATION_JSON));
    } finally {
      EntityUtils.consume(entity);
    }
  }

  private void executeAndCompare(HttpUriRequest request,
                                 int statusCode) throws Exception {
    executeAndCompare(request, statusCode, null);
  }

  private void executeAndCompare(HttpUriRequest request,
                                 int statusCode,
                                 Pair<String, ContentType> expectedResponseContent) throws Exception {
    final HttpResponse response = httpClient.execute(request);
    final HttpEntity responseEntity = response.getEntity();

    try {
      // This is to make sure the request has been submitted to the server's fetched requests queue
      Thread.sleep(100L);

      final List<HttpRequest> receivedRequests = httpServer.getReceivedRequests();
      assertFalse("Received requests can not be empty", receivedRequests.isEmpty());

      final HttpRequest receivedRequest = receivedRequests.get(0);
      compareRequests(request, receivedRequest);

      assertEquals(statusCode, response.getStatusLine().getStatusCode());

      if (expectedResponseContent != null) {
        final String content = readContent(responseEntity);
        final ContentType contentType = ContentType.get(responseEntity);
        assertNotNull(contentType);

        assertEquals(content, expectedResponseContent.getLeft());
        assertEquals(contentType.getMimeType(), expectedResponseContent.getRight().getMimeType());
      }
    } finally {
      EntityUtils.consume(responseEntity);
    }
  }

  private void compareRequests(HttpUriRequest sentRequest, HttpRequest obtainedRequest) throws IOException {
    assertEquals(sentRequest.getMethod(), obtainedRequest.getRequestLine().getMethod());
    assertEquals(sentRequest.getURI().getPath(), obtainedRequest.getRequestLine().getUri());

    // compare contents
    final Pair<String, ContentType> sentContent = fetchUtf8Content(sentRequest);
    final Pair<String, ContentType> receivedContent = fetchUtf8Content(obtainedRequest);

    if (sentContent != null) {
      assertNotNull(receivedContent);

      assertEquals(sentContent.getLeft(), receivedContent.getLeft());
      assertEquals(sentContent.getRight().getMimeType(), receivedContent.getRight().getMimeType());
    } else {
      assertNull(receivedContent);
    }
  }

  private Pair<String, ContentType> fetchUtf8Content(HttpRequest request) throws IOException {
    if (!(request instanceof HttpEntityEnclosingRequest)) {
      return null;
    }

    final HttpEntityEnclosingRequest enclosingSentRequest = (HttpEntityEnclosingRequest) request;
    final HttpEntity entity = enclosingSentRequest.getEntity();

    return new ImmutablePair<String, ContentType>(readContent(entity), ContentType.get(entity));
  }

  private String readContent(HttpEntity entity) throws IOException {
    final InputStream stream = entity.getContent();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Consts.UTF_8));
    return reader.readLine();
  }

  @Configuration
  public static class Config {

    @Bean
    public AsyncTaskExecutor taskExecutor() {
      return new ThreadPoolTaskExecutor() {
        {
          setCorePoolSize(5);
          setKeepAliveSeconds(10);
        }
      };
    }

    @Bean
    public ExecutorService executorService() {
      return new ExecutorServiceAdapter(taskExecutor());
    }

    @Bean
    public ItHttpServer itHttpServer() {
      return new DefaultItHttpServer(executorService());
    }

    @Bean
    public HttpClient httpClient() {
      final HttpParams params = new BasicHttpParams();
      HttpConnectionParams.setSoTimeout(params, 1000);
      HttpConnectionParams.setConnectionTimeout(params, 1000);

      return new DefaultHttpClient(params);
    }
  }
}
