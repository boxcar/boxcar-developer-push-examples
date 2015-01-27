package io.boxcar.publisher.client.builder.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import io.boxcar.publisher.client.builder.PublishStrategy;
import io.boxcar.publisher.client.util.Signature;

public class UrlSignaturePublishStrategy implements PublishStrategy {

	static Logger logger;
	static {
		logger = Logger.getLogger(UrlSignaturePublishStrategy.class);
	}

	CloseableHttpClient httpclient;
	
	public UrlSignaturePublishStrategy() {
		httpclient = null;
	}
	
	public CloseableHttpResponse post(String body, URI url,
			String publishKey, String publishSecret) throws IOException {
		String signature = Signature.sign("POST", url.getHost(),
				url.getPath(), body, publishSecret);
		
		StringEntity content;
		content = new StringEntity(body);

        logger.debug("Request body: " + body);
		
		URI uriWithURLParams;
		try {
			uriWithURLParams = new URI(url.getScheme(),
					url.getAuthority(), url.getPath(),
					String.format("publishkey=%s&signature=%s", publishKey, signature),
					url.getFragment());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	
        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Header> headers = new ArrayList<Header>();
        headers.add(header);
		
		HttpPost httpPost = new HttpPost(uriWithURLParams);
		httpPost.setEntity(content);

        httpclient = HttpClients.custom().setDefaultHeaders(headers)
        		.build();

		CloseableHttpResponse response = httpclient.execute(httpPost);
		
		return response;
	}

	public void closeClient() throws IOException {
		if (httpclient != null) {
			httpclient.close();
		}
	}

}
