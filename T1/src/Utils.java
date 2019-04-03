import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {

	public static void main(String[] args) {
		System.out.println(asciiToNumber("50"));
	}

	public static String numberToAscii(int number) {

		int digit = 0;
		String ascii = "";

		if (number == 0) {
			return "48";
		}

		while (number > 0) {
			digit = number % 10;
			digit += 48;
			ascii = ascii.concat(Integer.toString(digit));
			number /= 10;
		}

		return ascii;
	}

	public static int asciiToNumber(String ascii) {

		int num = 0;
		for (int i = 0; i < ascii.length(); i++) {
			num += Character.getNumericValue(ascii.charAt(i)) * (int) Math.pow(10, ascii.length() - i - 1);
		}

		return num - 48;
	}

	public static String bytesToHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static String getSHA(String input) {
		try {

			// Static getInstance method is called with hashing SHA
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			// digest() method called
			// to calculate message digest of an input
			// and return array of byte
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			return messageDigest.toString();
		}
		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			System.out.println("Exception thrown" + " for incorrect algorithm: " + e);

			return null;
		}
	}

	public static String getHeader(String message) {
		return message.substring(0, message.indexOf("\r\n"));
	}

	public static String getChunkContent(String message) {
		return message.substring(message.indexOf("\r\n\r\n"));
	}

	public static int getRandomNumber(int ceil) {
		Random random = new Random();
		return random.nextInt(ceil);
	}

}
