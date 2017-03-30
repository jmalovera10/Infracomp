package seguridad;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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
		/*Date startDate = new Date();
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
		certGen.setSignatureAlgorithm("SHA1WITHRSA");*/

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
		X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
		nameBuilder.addRDN(BCStyle.CN, "localhost");
		Date notBefore = new Date();
		Date notAfter = new Date(System.currentTimeMillis() + 24 * 3 * 60 * 60 * 1000);
		BigInteger serialNumber = new BigInteger(""+Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong()));
		X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(nameBuilder.build(), serialNumber, notBefore, notAfter, nameBuilder.build(), keyPair.getPublic());
		ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());
		X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certificateBuilder.build(contentSigner));
		return certificate;
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
