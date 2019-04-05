import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static void main(String[] args) {

	}

	public static byte[] getChunkContent(byte[] message, int length){

		byte[] chunkContent = new byte[64*1000];

		for (int i = 0; i < message.length; i++) {
			if ((int) message[i] == 13 && (int)message[i + 1] == 10 && (int)message[i + 2] == 13
					&& (int)message[i + 3] == 10) {
						chunkContent = new byte[length - i - 4];
				System.arraycopy(message, i + 4, chunkContent, 0, length - i - 4);
				break;
			}
		}

		return chunkContent;
	}

	public static String[] getHeader(byte[] message){

		byte[] header = new byte[1000];

		for (int i = 0; i < message.length; i++) {
			if ((int) message[i] == 13 && (int)message[i + 1] == 10 && (int)message[i + 2] == 13
					&& (int)message[i + 3] == 10) {
						header = new byte[i];
				System.arraycopy(message, 0, header, 0, i);
				break;
			}
		}

		return new String(header).split(" ");
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

	public static byte[] concatenateArrays(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);

		return c;
	}

	public static byte[] getSHA(String input) {
		try {

			// Static getInstance method is called with hashing SHA
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			// digest() method called
			// to calculate message digest of an input
			// and return array of byte
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			return messageDigest;
		}
		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			System.out.println("Exception thrown" + " for incorrect algorithm: " + e);

			return null;
		}
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static int getRandomNumber(int ceil) {
		Random random = new Random();
		return random.nextInt(ceil);
	}

}
