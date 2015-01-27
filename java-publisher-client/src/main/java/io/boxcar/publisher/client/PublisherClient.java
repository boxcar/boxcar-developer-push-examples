package io.boxcar.publisher.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import io.boxcar.publisher.client.builder.PublishStrategy;
import io.boxcar.publisher.client.builder.impl.BasicAuthPublishStrategy;
import io.boxcar.publisher.client.builder.impl.UrlSignaturePublishStrategy;
import io.boxcar.publisher.model.Alert;
import io.boxcar.publisher.model.Result;
import io.boxcar.publisher.model.Tag;

/**
 * This is the HTTP client responsible for publishing contents
 * to the Boxcar Push Platform
 * @author jpcarlino
 *
 */
public class PublisherClient {
	
	static Logger logger;
	static {
		logger = Logger.getLogger(PublisherClient.class);
	}

    static final String PROP_PUSH_URL = "io.boxcar.publisher.push.url";
    static final String PROP_TAG_MANAGER_URL = "io.boxcar.publisher.tagmanager.url";

	String publishKey;
	String publishSecret;
	PublishStrategy publishStrategy;
    Properties urls;

	public PublisherClient(Properties urls, String publishKey, String publishSecret) {
		this.urls = urls;
		this.publishKey = publishKey;
		this.publishSecret = publishSecret;
		this.publishStrategy = new BasicAuthPublishStrategy();
	}

	public PublisherClient(Properties urls, String publishKey, String publishSecret, int requestType) {
		this.urls = urls;
		this.publishKey = publishKey;
		this.publishSecret = publishSecret;
		
		if (requestType == PublishStrategy.URL_SIGNATURE) {
			this.publishStrategy = new UrlSignaturePublishStrategy();
		} else {
			this.publishStrategy = new BasicAuthPublishStrategy();
		}
	}

    /**
     * Sends a push
     * @param alert
     * @return the id of the newly created push
     * @throws IOException
     */
	public int publish(Alert alert) throws IOException {
        return create(alert, PROP_PUSH_URL);
	}

    /**
     * Creates a tag
     * @param tag
     * @return the id of the newly created tag
     * @throws IOException
     */
    public int createTag(Tag tag) throws IOException {
        return create(tag, PROP_TAG_MANAGER_URL);
    }

    private <T> int create(T model, String url) throws IOException {
        Result result = post(model, getURL(url), Result.class);
        return Integer.valueOf(result.getOk());
    }

    private <T,R extends Result> R post(T model, URI url, Class<R> resultClass) throws IOException {

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(model);

        CloseableHttpResponse response = publishStrategy.post(
                jsonPayload, url, publishKey, publishSecret);

        try {
            StatusLine statusLine = response.getStatusLine();
            logger.debug(statusLine);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            logger.debug(entityStr);
            int status = statusLine.getStatusCode();
            if (status == 201) {
                R result = gson.fromJson(entityStr, resultClass);
                return result;
            } else {
                String reasonPhrase = statusLine.getReasonPhrase();
                try {
                    // lets try to parse the error as json {"error":"cause"}
                    // if it fails, use the HTTP reason phrase
                    R error = gson.fromJson(entityStr, resultClass);
                    reasonPhrase = error.getError();
                } catch (Exception e) {}
                logger.debug("Throwing error with status: " + status + " - reason: " + reasonPhrase);
                throw new HttpResponseException(status, reasonPhrase);
            }
        } finally {
            try {
                response.close();
            } catch (IOException e) {}
            publishStrategy.closeClient();
        }
    }

    private URI getURL(String property) {
        try {
            return new URI(urls.getProperty(property));
        } catch (URISyntaxException e) {
            logger.error("Error loading property: " + property);
            return null;
        }
    }

}
