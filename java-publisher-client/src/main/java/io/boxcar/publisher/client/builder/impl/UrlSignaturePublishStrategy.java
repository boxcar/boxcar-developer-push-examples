package io.boxcar.publisher.client.builder.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import io.boxcar.publisher.client.builder.RequestStrategy;
import io.boxcar.publisher.client.util.Signature;

public class UrlSignaturePublishStrategy implements RequestStrategy {

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

        logger.trace("Request body: " + body);
		
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

    @Override
    public CloseableHttpResponse get(Map<String, String> params, URI baseUrl, String publishKey, String publishSecret) throws IOException {

        URIBuilder uriBuilder = new URIBuilder(baseUrl);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        URI url = null;

        try {
            url = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        String payload = "";
        String signature = Signature.sign("GET", url.getHost(), url.getPath(),
                                          payload, publishSecret);

        logger.trace("GET request URL: " + url);

        uriBuilder.addParameter("publishkey", publishKey);
        uriBuilder.addParameter("signature", signature);
 
        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Header> headers = new ArrayList<Header>();
        headers.add(header);

        try {
	        HttpGet httpGet = new HttpGet(uriBuilder.build());
	
	        httpclient = HttpClients.custom().setDefaultHeaders(headers)
	                .build();
	
	        CloseableHttpResponse response = httpclient.execute(httpGet);
	
	        return response;        
        } catch (URISyntaxException e) {
        	throw new IOException(e);
        }
    }

    @Override
    public CloseableHttpResponse delete(Map<String, String> content, URI baseUrl, String publishKey, String publishSecret) throws IOException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl);

        for (Map.Entry<String, String> entry : content.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        URI url = null;

        try {
            url = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        String payload = "";
        String signature = Signature.sign("DELETE", url.getHost(), url.getPath(),
                payload, publishSecret);

        logger.trace("DELETE request URL: " + url);

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

        HttpDelete httpDelete = new HttpDelete(uriWithURLParams);

        httpclient = HttpClients.custom().setDefaultHeaders(headers)
                .build();

        CloseableHttpResponse response = httpclient.execute(httpDelete);

        return response;
    }

	public CloseableHttpResponse put(String body, URI url,
			String publishKey, String publishSecret) throws IOException {
		String signature = Signature.sign("PUT", url.getHost(),
				url.getPath(), body, publishSecret);
		
		StringEntity content;
		content = new StringEntity(body);

        logger.trace("PUT request body: " + body);
		
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
		
		HttpPut httpPut = new HttpPut(uriWithURLParams);
		httpPut.setEntity(content);

        httpclient = HttpClients.custom().setDefaultHeaders(headers)
        		.build();

		CloseableHttpResponse response = httpclient.execute(httpPut);
		
		return response;
	}

    public void closeClient() throws IOException {
		if (httpclient != null) {
			httpclient.close();
		}
	}

}
