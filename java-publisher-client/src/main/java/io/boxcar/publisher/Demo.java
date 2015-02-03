package io.boxcar.publisher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import io.boxcar.publisher.client.APIClient;
import io.boxcar.publisher.client.builder.RequestStrategy;
import io.boxcar.publisher.model.Alert;
import io.boxcar.publisher.model.Tag;

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

    static org.apache.log4j.Logger logger;
    static {
        logger = org.apache.log4j.Logger.getLogger(Demo.class);
    }

	static final String PROP_PUBLISH_KEY = "io.boxcar.publisher.key";
	static final String PROP_PUBLISH_SECRET = "io.boxcar.publisher.secret";

	static String PUBLISH_KEY;
	static String PUBLISH_SECRET;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		APIClient apiClient = null;
		
		// 1. Build the api client
		
		try {

			Properties properties = loadProperties();

            PUBLISH_KEY = properties.getProperty(PROP_PUBLISH_KEY);
            PUBLISH_SECRET = properties.getProperty(PROP_PUBLISH_SECRET);

			apiClient = new APIClient(properties, PUBLISH_KEY, PUBLISH_SECRET,
					RequestStrategy.URL_SIGNATURE);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

        // 2. Publish

        try {
            StringBuffer text = getText(args);
            sendPush(text.toString(), "android", 120, apiClient);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 3. Get tags
        try {
            List<Tag> tags = getTags(apiClient);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

	}

    private static void sendPush(String text, String targetOS, int ttl, APIClient apiClient) {
        try {
            Alert alert = new Alert(text);
            // remove this line if you just want to send it to all registered
            // devices
            alert.setTargetOS(targetOS);
            // do not keep the push more than 2 minutes if device is not
            // available
            alert.setTTL(ttl);

            int id = apiClient.publish(alert);

            logger.debug("Push sent with id " + id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createTag(String tagName, APIClient apiClient) {
        try {

            Tag tag = new Tag(tagName);

            int id = apiClient.createTag(tag);

            logger.debug("Tag " + tag.getName() + " created with id " + id);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<Tag> getTags(APIClient apiClient) {
        List<Tag> tags = null;

        try {
            // FIXME: creation date is not being parsed
            tags = apiClient.getTags();

            logger.debug("Available tags on SaaS: ");
            for (Tag tag : tags) {
                logger.debug("Tag " + tag.getName() + " - id " + tag.getId()
                                    + " - created at : " + tag.getCreationDate()
                                    + " - devices: " + tag.getDevices()
                                    + " - deprecated: " + tag.isDeprecated());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return tags;
    }

	private static Properties loadProperties() throws IOException {
		InputStream fileUrl = Demo.class.getResourceAsStream("/publisher.properties");
		Properties properties = new Properties();
		properties.load(fileUrl);
        return properties;
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
