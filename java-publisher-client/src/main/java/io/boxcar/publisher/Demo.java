package io.boxcar.publisher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import io.boxcar.publisher.client.PublisherClient;
import io.boxcar.publisher.client.builder.PublishStrategy;
import io.boxcar.publisher.model.Alert;

/**
 * This is the main entry point to execut the demo application.
 * 
 * This demo just connects with the Boxcar Universal Push Notification
 * Platform and publishes a "Hello World" content to all Android devices
 * using the URL signature authorization method. 
 * 
 * @author jpcarlino
 *
 */
public class Demo {

	static final String PROP_PUBLISH_KEY = "io.boxcar.publisher.key";
	static final String PROP_PUBLISH_SECRET = "io.boxcar.publisher.secret";
	static final String PROP_URL = "io.boxcar.publisher.url";

	static String PUBLISH_KEY;
	static String PUBLISH_SECRET;
	static String PUBLISH_URL;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PublisherClient publisherClient = null;
		
		// 1. Build the publisher client
		
		try {
			loadProperties();
			URI uri = new URI(PUBLISH_URL);
			publisherClient = new PublisherClient(uri, PUBLISH_KEY, PUBLISH_SECRET,
					PublishStrategy.URL_SIGNATURE);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// 2. Publish
		
		try {
			StringBuffer text = getText(args);
			Alert alert = new Alert(text.toString());
			// remove this line if you just want to send it to all registered
			// devices
			alert.setTargetOS("android");
		
			publisherClient.publish(alert);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadProperties() throws IOException {
		InputStream fileUrl = Demo.class.getResourceAsStream("/publisher.properties");
		Properties properties = new Properties();
		properties.load(fileUrl);
		PUBLISH_KEY = properties.getProperty(PROP_PUBLISH_KEY);
		PUBLISH_SECRET = properties.getProperty(PROP_PUBLISH_SECRET);
		PUBLISH_URL = properties.getProperty(PROP_URL);
	}
	
	private static StringBuffer getText(String[] args) throws IOException {
		StringBuffer text = new StringBuffer();
		if (args.length > 0) {
			if (args[0].toLowerCase().trim().equals("--file")) {
				String filename = args[1].trim();
				FileReader fileReader = new FileReader(filename);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					text.append(line);
					text.append("\n");
				}
				fileReader.close();
			} else if (args[0].toLowerCase().trim().equals("--text")) {
				text.append(args[1].trim());
			} else {
				text.append("Hello World!");
			}
		} else {
			text.append("Hello World!");
		}
		return text;
	}
}
