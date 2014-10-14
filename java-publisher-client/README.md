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
