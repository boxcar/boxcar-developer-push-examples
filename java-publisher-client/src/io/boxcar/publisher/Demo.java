package io.boxcar.publisher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.boxcar.publisher.client.PublisherClient;
import io.boxcar.publisher.client.builder.PublishStrategy;
import io.boxcar.publisher.model.Alert;

public class Demo {

	static final String PUBLISH_KEY =    "yourPublishKey";
	static final String PUBLISH_SECRET = "yourPublishSecret";
	static final String URL = "https://boxcar-api.io/api/push/";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PublisherClient publisherClient = null;
		
		try {
			URI uri = new URI(URL);
			publisherClient = new PublisherClient(uri, PUBLISH_KEY, PUBLISH_SECRET,
					PublishStrategy.URL_SIGNATURE);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			Alert alert = new Alert("Hello World!");
			publisherClient.publish(alert);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
