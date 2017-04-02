package seguridad;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V3CertificateGenerator;

import protocolo.Protocol;

public class Seguridad {	
	
	public X509Certificate generarCertificado(KeyPair keyPair)throws Exception{
		Date startDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date expiryDate = calendar.getTime();             
		BigInteger serialNumber = new BigInteger(""+Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong()));  
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal dnName = new X500Principal("CN=Test CA Certificate");
		certGen.setSerialNumber(serialNumber);
		certGen.setIssuerDN(dnName);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expiryDate);
		certGen.setSubjectDN(dnName);                       
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WITHRSA");

		return certGen.generate(keyPair.getPrivate());
		
		/*Date startDate = new Date();                // time from which certificate is valid
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date expiryDate = calendar.getTime();             // time after which certificate is not valid
		BigInteger serialNumber = new BigInteger(""+Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong()));     // serial number for certificate 
		PrivateKey caKey = keyPair.getPrivate();              // private key of the certifying authority (ca) certificate
		//X509Certificate caCert =     // public key certificate of the certifying authority              // public/private key pair that we are creating certificate for
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal  subjectName = new X500Principal("CN=Test V3 Certificate");

		certGen.setSerialNumber(serialNumber);
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expiryDate);
		certGen.setSubjectDN(subjectName);
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WITHRSA");


		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,new SubjectKeyIdentifierStructure(keyPair.getPublic()));

		X509Certificate cert = certGen.generate(caKey, "BC"); */

		//Aproximación de http://programtalk.com/java-api-usage-examples/org.bouncycastle.cert.X509v3CertificateBuilder/
		
		
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
	
		Cipher cipher = Cipher.getInstance(Protocol.ALG_ASIMETRICOS[0]);


		byte[] clearText = obj.getBytes();
		String s1 = new String (clearText);
		System.out.println("clave original: " + s1);
		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] cipheredText = cipher.doFinal(clearText);

		System.out.println("clave cifrada: " + cipheredText);

		return	cipheredText;
	}

	public String decifrarAsimetrica(byte[] obj, PrivateKey key) throws Exception{
		
		Cipher cipher = Cipher.getInstance(Protocol.ALG_ASIMETRICOS[0]);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] clearText = cipher.doFinal(obj);
		String s3 = new String(clearText);
		return s3;
		
	}
	

	public byte[] getKeyedDigest(String buffer, String algorithm) {
		try
		{
			byte[] text = buffer.getBytes();
			MessageDigest hasher = MessageDigest.getInstance(algorithm);
			hasher.update(text);
			return
					hasher.digest();
		} 
		catch(Exception e) {
			return null;
					
		}
	}
}
