package fr.univlorraine.mondossierweb.utils;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;


@Component(value="cypherUtils")
public class CypherUtils {
	
	private Logger LOG = LoggerFactory.getLogger(CypherUtils.class);

	public static String ALGO = "AES/ECB/PKCS5Padding";
	public static String ALGO_KEY = "AES";

	private Key key;

	public String decrypt(String chaine) {

		try {
			Cipher decryptCipher = Cipher.getInstance(ALGO);
			decryptCipher.init(Cipher.DECRYPT_MODE, getKey());
			return new String(decryptCipher.doFinal(Hex.decodeHex(chaine.toCharArray())));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String encrypt(String chaine) {
		try {
			Cipher cryptCipher = Cipher.getInstance(ALGO);
			cryptCipher.init(Cipher.ENCRYPT_MODE, getKey());
			String param =  Hex.encodeHexString((cryptCipher.doFinal(chaine.getBytes("UTF-8"))));
			LOG.debug("encrypt param "+param);
			return param;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Key getKey() {
		if(key == null) {
			//String cle = System.getProperty("context.param.photo.url.key");
			Random ranGen = new SecureRandom();
			byte[] aesKey = new byte[16]; // 16 bytes = 128 bits
			ranGen.nextBytes(aesKey);
			LOG.debug("16 bytes key : " + aesKey);
			key = new SecretKeySpec(aesKey, ALGO_KEY);
		}
		return key;
	}


}
