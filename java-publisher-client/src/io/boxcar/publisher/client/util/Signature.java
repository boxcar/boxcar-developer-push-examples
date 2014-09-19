package io.boxcar.publisher.client.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public class Signature {

	static Logger logger;
	static {
		logger = Logger.getLogger(Signature.class);
	}

	public static String sign(String method, String host, String path, String body,
			String secret) {
		
		// Remove trailing slash
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		StringBuffer contentToSign = new StringBuffer();
		
		contentToSign.append(method).append('\n')
                     .append(host.toLowerCase()).append('\n')
                     .append(path).append('\n')
                     .append(body);

		return hashHmac("HmacSHA1", contentToSign.toString(), secret);
	}

	private static String hashHmac(String type, String value, String key) {
		try {
			javax.crypto.Mac mac = javax.crypto.Mac.getInstance(type);
			javax.crypto.spec.SecretKeySpec secret = new javax.crypto.spec.SecretKeySpec(
					key.getBytes(), type);
			mac.init(secret);
			byte[] digest = mac.doFinal(value.getBytes());
			return convertToHex(digest);
		} catch (Exception e) {
			logger.error("Error signing content", e);
		}
		return "";
	}

	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfByte = (b >>> 4) & 0x0F;
			int twoHalfs = 0;
			do {
				buf.append((0 <= halfByte) && (halfByte <= 9) ? (char) ('0' + halfByte)
						: (char) ('a' + (halfByte - 10)));
				halfByte = b & 0x0F;
			} while (twoHalfs++ < 1);
		}
		return buf.toString();
	}
	
    public static String makeSHA1Hash(String input) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance("SHA1");
	    md.reset();
	    byte[] buffer = input.getBytes();
	    md.update(buffer);
	    byte[] digest = md.digest();
	
	    String hexStr = "";
	    for (int i = 0; i < digest.length; i++) {
	        hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
	    }
	    return hexStr;
    }
	
}
