import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class Utils {
	
	public static void main(String[] args) {
		String ascii = fileIdToAscii("2");
		System.out.println(ascii);
		System.out.println(asciiToFileId(ascii));
		
		System.out.println(numberToAscii(0));
	}
	
	public static String fileIdToAscii(String fileId){

		String hex = "";
		for(int i = 0; i < fileId.length(); i++){
			hex = hex.concat(String.format("%02x", (int) fileId.charAt(i)));
		}
		
		return hex;
	}
	
	public static String asciiToFileId(String ascii) {
		
		String res = "";
		try {
			res = new String(DatatypeConverter.parseHexBinary(ascii), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}

	public static String numberToAscii(int number){

		int digit = 0;
		String ascii = "";
		
		if(number == 0) {
			return "48";
		}

		while(number > 0) {
			digit = number % 10;
			digit += 48;
			ascii = ascii.concat(Integer.toString(digit));
			number /= 10;
		}

		return ascii;
	}
	
	public static int asciiToNumber(String ascii) {
		
		int num = 0;
		for(int i = 0; i < ascii.length(); i++) {
			num += Character.getNumericValue(ascii.charAt(i))*(int)Math.pow(10, ascii.length() - i - 1);
		}
		
		return num;
	}
	
    public static String getSHA(String input) {
        try {

            // Static getInstance method is called with hashing SHA
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // digest() method called
            // to calculate message digest of an input
            // and return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown" + " for incorrect algorithm: " + e);

            return null;
        }
    }

}