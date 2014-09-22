package io.boxcar.publisher.client.builder;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.CloseableHttpResponse;

public interface PublishStrategy {
	
	public static final int BASIC_AUTH = 0;
	public static final int URL_SIGNATURE = 1;
	
	CloseableHttpResponse post(String content, URI url, String publishKey,
			String publishSecret) throws IOException;
	void closeClient() throws IOException;
}
