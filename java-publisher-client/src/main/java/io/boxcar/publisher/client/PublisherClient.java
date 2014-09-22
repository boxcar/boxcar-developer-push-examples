package io.boxcar.publisher.client;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import io.boxcar.publisher.client.builder.PublishStrategy;
import io.boxcar.publisher.client.builder.impl.BasicAuthPublishStrategy;
import io.boxcar.publisher.client.builder.impl.UrlSignaturePublishStrategy;
import io.boxcar.publisher.model.Alert;

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
	
	String publishKey;
	String publishSecret;
	URI url;
	PublishStrategy publishStrategy;
	
	public PublisherClient(URI url, String publishKey, String publishSecret) {
		this.url = url;
		this.publishKey = publishKey;
		this.publishSecret = publishSecret;
		this.publishStrategy = new BasicAuthPublishStrategy();
	}

	public PublisherClient(URI url, String publishKey, String publishSecret, int requestType) {
		this.url = url;
		this.publishKey = publishKey;
		this.publishSecret = publishSecret;
		
		if (requestType == PublishStrategy.URL_SIGNATURE) {
			this.publishStrategy = new UrlSignaturePublishStrategy();
		} else {
			this.publishStrategy = new BasicAuthPublishStrategy();
		}
	}
	
	public void publish(Alert alert) throws IOException {

		CloseableHttpResponse response = publishStrategy.post(
				alertToJson(alert),	url, publishKey, publishSecret);

		try {
		    logger.info(response.getStatusLine());
		    HttpEntity entity = response.getEntity();
		    logger.info(EntityUtils.toString(entity));
		} finally {
		    try {
				response.close();
			} catch (IOException e) {}
	    	publishStrategy.closeClient();
		}		
	}
	
	private String alertToJson(Alert alert) {
		Gson gson = new Gson();
		return gson.toJson(alert);  
	}
	
}
