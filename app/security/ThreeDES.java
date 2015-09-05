package security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by edatsuv on 9/5/15.
 */
public class ThreeDES {


    private static String THREEDES_KEY="4B623645777548784E6B784C674365384B3650676A646E4E";
    public static String FORM_ID="574301A941C9A095E474EF84D558739D7629CC97FFFFF5C1";
    public static String SESSION_ID="62A6DC8C9A988EA9";


    public static String toHexStringFromByte(byte[] array) {
        return DatatypeConverter.printHexBinary(array);
    }

    public static byte[] toByteArrayFromHex(String s) {
        byte [] tmp= DatatypeConverter.parseHexBinary(s);
        return tmp;
    }

    public static byte[] toByteArrayFromString(String s) throws UnsupportedEncodingException {
        byte [] tmp=s.getBytes("US-ASCII");
        return tmp;
    }

    public static String hexToASCII(String hexValue)
    {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2)
        {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String decrypt(String dataSupply)
            throws NoSuchAlgorithmException, InvalidKeyException,BadPaddingException,IllegalBlockSizeException,
            NoSuchPaddingException,InvalidKeySpecException, UnsupportedEncodingException{
        byte[] tmp = toByteArrayFromHex(THREEDES_KEY);
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(tmp, "DESede"));
        byte[] plaintext = cipher.doFinal(toByteArrayFromHex(dataSupply));
//    	    String s = new String(plaintext, Charset.forName("ASCII"));
//    	    System.out.println(s);
        return toHexStringFromByte(plaintext);

    }

    public static String encrypt(String dataSupply)
            throws NoSuchAlgorithmException, InvalidKeyException,BadPaddingException,IllegalBlockSizeException,
            NoSuchPaddingException,InvalidKeySpecException, UnsupportedEncodingException{
        byte[] tmp = toByteArrayFromHex(THREEDES_KEY);
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(tmp, "DESede"));
        byte[] plaintext = cipher.doFinal(toByteArrayFromString(dataSupply));
        return toHexStringFromByte(plaintext);

    }




}
