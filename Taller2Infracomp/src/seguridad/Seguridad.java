package seguridad;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.crypto.tls.CertChainType;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class Seguridad {
	private
	final
	static String ALGORITMO="RSA";	
	
	public X509Certificate generarCertificado(KeyPair keyPair)throws Exception{
		Date startDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date expiryDate = calendar.getTime();             
		BigInteger serialNumber = new BigInteger(""+Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong()));     // serial number for certificate 
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal dnName = new X500Principal("CN=Test CA Certificate");
		certGen.setSerialNumber(serialNumber);
		certGen.setIssuerDN(dnName);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expiryDate);
		certGen.setSubjectDN(dnName);                       // note: same as issuer
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WITHRSA");

		return certGen.generate(keyPair.getPrivate());	
	}
	
	public byte[] cifrarSimetrica(String obj, Key key, String algo ) throws Exception{
		String PADDING=algo+"/ECB/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(PADDING);
		byte[] text= obj.getBytes();
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipheredText = cipher.doFinal(text);
		String fin= new String(cipheredText);
		System.out.println("El mensaje cifrado: "+fin);
		return cipheredText;
		
	}
	
	public String decifrarSimetrica(byte[] obj, Key key, String algo) throws Exception{
		String PADDING=algo+"/ECB/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(PADDING);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipheredText = cipher.doFinal(obj);
		String fin = new String(cipheredText);
		return fin;
		
	}
	
	public byte[] cifrarAsimetrica(String obj, PublicKey key ) throws Exception{
	
		Cipher cipher = Cipher.getInstance(ALGORITMO);


		byte[] clearText = obj.getBytes();
		String s1 = new String (clearText);
		System.out.println("clave original: " + s1);
		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] cipheredText = cipher.doFinal(clearText);

		System.out.println("clave cifrada: " + cipheredText);

		return	cipheredText;
	}

	public String decifrarAsimetrica(byte[] obj, PrivateKey key) throws Exception{
		
		Cipher cipher = Cipher.getInstance(ALGORITMO);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] clearText = cipher.doFinal(obj);
		String s3 = new String(clearText);
		return s3;
		
	}
	

	public byte[] getKeyedDigest(String buffer, String algorithm) {
		try
		{
			byte[] text = buffer.getBytes();
			MessageDigest hasher = MessageDigest.getInstance("algorithm");
			hasher.update(text);
			return
					hasher.digest();
		} 
		catch(Exception e) {
			return null;
					
		}
	}

}
