package io.boxcar.publisher.client.builder.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import io.boxcar.publisher.client.builder.RequestStrategy;

public class BasicAuthPublishStrategy implements RequestStrategy {

	static Logger logger;
	static {
		logger = Logger.getLogger(BasicAuthPublishStrategy.class);
	}
	
	CloseableHttpClient httpclient;
	
	public BasicAuthPublishStrategy() {
		httpclient = null;
	}
	
	public CloseableHttpResponse post(String body, URI url, String publishKey,
			String publishSecret) throws IOException {
		StringEntity content;
		content = new StringEntity(body);
		
		logger.debug("Request body: " + body);
		
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(content);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
        		new AuthScope(getHost(url), getPort(url)),
                new UsernamePasswordCredentials(publishKey, publishSecret));
        
        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Header> headers = new ArrayList<Header>();
        headers.add(header);
        
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(URIUtils.extractHost(url), basicAuth);
        
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultHeaders(headers)
                .build();

		CloseableHttpResponse response = httpclient.execute(httpPost, context);
		
		return response;
	}

    @Override
    public CloseableHttpResponse get(Map<String, String> content, URI baseUrl,
                                     String publishKey, String publishSecret)
            throws IOException {

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

        logger.debug("GET request URL: " + url);

        HttpGet httpGet = new HttpGet(url);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(getHost(url), getPort(url)),
                new UsernamePasswordCredentials(publishKey, publishSecret));

        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Header> headers = new ArrayList<Header>();
        headers.add(header);

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(URIUtils.extractHost(url), basicAuth);

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultHeaders(headers)
                .build();

        CloseableHttpResponse response = httpclient.execute(httpGet, context);

        return response;
    }

    public void closeClient() throws IOException {
		if (httpclient != null) {
			httpclient.close();
		}
	}
	
	private String getHost(URI url) {
		HttpHost httpHost = URIUtils.extractHost(url);
		return httpHost.getHostName();
	}
	
	private int getPort(URI url) {
		HttpHost httpHost = URIUtils.extractHost(url);
		return httpHost.getPort();
	}
	
}
