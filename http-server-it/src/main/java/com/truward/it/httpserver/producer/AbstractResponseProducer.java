package com.truward.it.httpserver.producer;

import com.truward.it.httpserver.ItResponseProducer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

import java.util.Locale;

/**
 * Abstract implementation of the response producer.
 *
 * @author Alexander Shabanov
 */
public abstract class AbstractResponseProducer implements ItResponseProducer {
    protected final ReasonPhraseCatalog reasonPhraseCatalog = EnglishReasonPhraseCatalog.INSTANCE;

    /**
     * Creates the response with no content attached
     *
     * @param statusCode Status code
     * @return An entity-less instance of {@link HttpResponse}
     */
    protected HttpResponse createResponse(int statusCode) {
        final String reason = reasonPhraseCatalog.getReason(statusCode, Locale.ENGLISH);
        return new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, reason);
    }

    protected HttpResponse createResponse(int statusCode, ContentType contentType, String content) {
        final String reason = reasonPhraseCatalog.getReason(statusCode, Locale.ENGLISH);
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, reason);
        response.setHeader(HTTP.CONTENT_TYPE, contentType.toString());

        final StringEntity entity = new StringEntity(content, contentType);
        response.setEntity(entity);

        return response;
    }
}
