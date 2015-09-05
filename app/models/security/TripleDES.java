package models.security;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.DESedeKeySpec;

/**
 * Created by edatsuv on 9/3/15.
 */
public class TripleDES {

    private static SecretKey keyProvider() throws InvalidKeyException,NoSuchAlgorithmException,InvalidKeySpecException
    {
        SecretKeyFactory keyFactory= SecretKeyFactory.getInstance("DESede");
        DESedeKeySpec dks = new DESedeKeySpec("4B623645777548784E6B784C674365384B3650676A646E4E".getBytes());
        SecretKey desKey=keyFactory.generateSecret(dks);
        return desKey;
    }


    public static void encrypt(String dataSupply)
            throws NoSuchAlgorithmException, InvalidKeyException,BadPaddingException,IllegalBlockSizeException,
            NoSuchPaddingException,InvalidKeySpecException{
        // Create and initialize the encryption engine
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider());

        // buffer return
        byte[] buffer = new byte[2048];
        buffer=cipher.doFinal(dataSupply.getBytes());

    }

}
