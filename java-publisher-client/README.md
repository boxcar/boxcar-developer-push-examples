Publisher Demo for Java
=======================

Publisher demo for Boxcar Universal Push Notification Platform written in Java

## How to use

This project is managed by [Gradle](http://www.gradle.org/). If you don't have it in your environment, you should install it following the instructions in the official Gradle site. This project was verified with version 1.11.

### Configure the project for Eclipse IDE (optional)

Execute the following statement in your command shell:

    gradle eclipse

Then open Eclipse and import the project as an existing project into workspace.

### Set your keys

Set your publish and secret keys as defined in your Boxcar Push Notification project. Just edit the file *src/main/resources/publisher.properties* and set these accordingly.

### Edit push arguments (optional)

Edit your push parameters on *src/main/java/io/boxcar/publisher/Demo.java*

By default it uses URL signature to authorize your publish request. You can change it to Basic Auth by modifying the following line:

			publisherClient = new PublisherClient(uri, PUBLISH_KEY, PUBLISH_SECRET,
					PublishStrategy.URL_SIGNATURE);

by this:

			publisherClient = new PublisherClient(uri, PUBLISH_KEY, PUBLISH_SECRET,
					PublishStrategy.BASIC_AUTH);

### Run

Type and execute the following statement in your command shell:

    gradle run

If you want to pass your own text from command line:

    gradle run -PappArgs="['--text', 'This is a test push']"

or

    gradle run -PappArgs="['--file', '/home/user/a_text_file.txt']"

## Troubleshooting

### StartSSL is not recognized as a certification authority when connecting to https://boxcar-api.io
It is possible that you find an exception similar to this when running the API client:

    javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

This is by the fact that StartSSL is not trusted by default as a certification authority by the Oracle JDK. In that case you will need to add the root certificate as follows:

1. Download the root certificate from StartSSL (ca.crt)
        $ wget https://www.startssl.com/certs/ca.crt
2. Find what is the keystore file used by your Java JVM. Usually it can be found on $JAVA_HOME/jre/lib/security/cacerts
3. Add the contents of ca.crt to the Java keystore:
        $ keytool -import -trustcacerts -alias startsslca -file ca.crt -keystore $JAVA_HOME/jre/lib/security/cacerts
