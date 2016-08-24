package io.boxcar.publisher.client.builder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;

public interface RequestStrategy {
	
	public static final int BASIC_AUTH = 0;
	public static final int URL_SIGNATURE = 1;
	
	CloseableHttpResponse post(String content, URI baseUrl, String publishKey,
			String publishSecret) throws IOException;
    CloseableHttpResponse get(Map<String, String> params, URI baseUrl, String publishKey,
                               String publishSecret) throws IOException;
    CloseableHttpResponse delete(Map<String, String> content, URI baseUrl, String publishKey,
                              String publishSecret) throws IOException;
    CloseableHttpResponse put(String content, URI baseUrl, String publishKey,
    		String publishSecret) throws IOException;
	void closeClient() throws IOException;
}
