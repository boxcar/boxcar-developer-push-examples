package io.boxcar.publisher.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import io.boxcar.publisher.client.builder.RequestStrategy;
import io.boxcar.publisher.client.builder.impl.BasicAuthPublishStrategy;
import io.boxcar.publisher.client.builder.impl.UrlSignaturePublishStrategy;
import io.boxcar.publisher.client.util.DateTimeConverter;
import io.boxcar.publisher.model.Alert;
import io.boxcar.publisher.model.Result;
import io.boxcar.publisher.model.Tag;

/**
 * This is the HTTP client responsible for publishing contents
 * to the Boxcar Push Platform
 * @author jpcarlino
 *
 */
public class APIClient {
	
	static Logger logger;
	static {
		logger = Logger.getLogger(APIClient.class);
	}

    static final String PROP_PUSH_URL = "io.boxcar.publisher.push.url";
    static final String PROP_TAG_MANAGER_URL = "io.boxcar.publisher.tagmanager.url";

	String publishKey;
	String publishSecret;
	RequestStrategy requestStrategy;
    Properties urls;
    GsonBuilder gsonBuilder;

	public APIClient(Properties urls, String publishKey, String publishSecret) {
		this.urls = urls;
		this.publishKey = publishKey;
		this.publishSecret = publishSecret;
		this.requestStrategy = new BasicAuthPublishStrategy();
        initGsonBuilder();
	}

	public APIClient(Properties urls, String publishKey, String publishSecret, int requestType) {
		this.urls = urls;
		this.publishKey = publishKey;
		this.publishSecret = publishSecret;
		
		if (requestType == RequestStrategy.URL_SIGNATURE) {
			this.requestStrategy = new UrlSignaturePublishStrategy();
		} else {
			this.requestStrategy = new BasicAuthPublishStrategy();
		}
        initGsonBuilder();
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
     * Retrieves all the tags available for the given project
     * @return all existing tags
     */
    public List<Tag> getTags() throws IOException {
        URI url = getURL(PROP_TAG_MANAGER_URL);

        TypeToken<List<Tag>> tagList = new TypeToken<List<Tag>>() {};
        List<Tag> tags = get(url, new HashMap<String, String>(), tagList.getType());

        return tags;
    }

    public Tag getTag(int id) throws IOException {
        URI url = getURL(PROP_TAG_MANAGER_URL, id);

        Tag tag = get(url, new HashMap<String, String>(), Tag.class);

        return tag;
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

    public void deprecateTag(int id) throws IOException {
        URI url = getURL(PROP_TAG_MANAGER_URL, id, "deprecate");

        CloseableHttpResponse response = requestStrategy.post("", url,
                publishKey, publishSecret);

        try {
            StatusLine statusLine = response.getStatusLine();
            logger.trace(statusLine);
            int status = statusLine.getStatusCode();
            if (status == 204) {
                return;
            } else {
                String reasonPhrase = statusLine.getReasonPhrase();
                try {
                    // lets try to parse the error as json {"error":"cause"}
                    // if it fails, use the HTTP reason phrase
                    HttpEntity entity = response.getEntity();
                    String entityStr = EntityUtils.toString(entity);
                    logger.trace(entityStr);
                    Gson gson = gsonBuilder.create();
                    Result error = gson.fromJson(entityStr, Result.class);
                    reasonPhrase = error.getError();
                } catch (Exception e) {}
                throw new HttpResponseException(status, reasonPhrase);
            }
        } finally {
            try {
                response.close();
            } catch (Exception e) {}
            requestStrategy.closeClient();
        }

    }

    public void deleteTag(int id) throws IOException {
        URI url = getURL(PROP_TAG_MANAGER_URL, id);

        CloseableHttpResponse response = requestStrategy.delete(
                new HashMap<String, String>(), url, publishKey, publishSecret);

        try {
            StatusLine statusLine = response.getStatusLine();
            logger.trace(statusLine);
            int status = statusLine.getStatusCode();
            if (status == 204) {
                return;
            } else {
                String reasonPhrase = statusLine.getReasonPhrase();
                try {
                    // lets try to parse the error as json {"error":"cause"}
                    // if it fails, use the HTTP reason phrase
                    Gson gson = gsonBuilder.create();
                    HttpEntity entity = response.getEntity();
                    String entityStr = EntityUtils.toString(entity);
                    logger.trace(entityStr);
                    Result error = gson.fromJson(entityStr, Result.class);
                    reasonPhrase = error.getError();
                } catch (Exception e) {
                }
                throw new HttpResponseException(status, reasonPhrase);
            }
        } finally {
            try {
                response.close();
            } catch (Exception e) {}
            requestStrategy.closeClient();
        }

    }

    private <T> int create(T model, String url) throws IOException {
        Result result = post(model, getURL(url), Result.class);
        return Integer.valueOf(result.getOk());
    }

    public <R> R get(URI url, HashMap<String, String> params, Type resultType) throws IOException {
        Gson gson = gsonBuilder.create();

        CloseableHttpResponse response = requestStrategy.get(
                params, url, publishKey, publishSecret);

        try {
            StatusLine statusLine = response.getStatusLine();
            logger.trace(statusLine);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            logger.trace(entityStr);
            int status = statusLine.getStatusCode();
            if (status == 200) {
                R result = gson.fromJson(entityStr, resultType);
                return result;
            } else {
                String reasonPhrase = statusLine.getReasonPhrase();
                try {
                    // lets try to parse the error as json {"error":"cause"}
                    // if it fails, use the HTTP reason phrase
                    Result error = gson.fromJson(entityStr, Result.class);
                    reasonPhrase = error.getError();
                } catch (Exception e) {
                }
                throw new HttpResponseException(status, reasonPhrase);
            }
        } finally {
            try {
                response.close();
            } catch (Exception e) {}
            requestStrategy.closeClient();
        }
    }

    private <T,R extends Result> R post(T model, URI url, Class<R> resultClass) throws IOException {

        Gson gson = gsonBuilder.create();
        String jsonPayload = gson.toJson(model);

        CloseableHttpResponse response = requestStrategy.post(
                jsonPayload, url, publishKey, publishSecret);

        try {
            StatusLine statusLine = response.getStatusLine();
            logger.trace(statusLine);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            logger.trace(entityStr);
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
                throw new HttpResponseException(status, reasonPhrase);
            }
        } finally {
            try {
                response.close();
            } catch (Exception e) {}
            requestStrategy.closeClient();
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

    private URI getURL(String property, int id) {
        try {
            String url = urls.getProperty(property).trim();
            url = url.endsWith("/") ? url + id : url + '/' + id;
            return new URI(url);
        } catch (URISyntaxException e) {
            logger.error("Error loading property: " + property);
            return null;
        }
    }

    private URI getURL(String property, int id, String action) {
        try {
            String url = urls.getProperty(property).trim();
            url = url.endsWith("/") ? url + id : url + '/' + id;
            return new URI(url + '/' + action);
        } catch (URISyntaxException e) {
            logger.error("Error loading property: " + property);
            return null;
        }
    }

    private void initGsonBuilder() {
        this.gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateTimeConverter());
    }
}
