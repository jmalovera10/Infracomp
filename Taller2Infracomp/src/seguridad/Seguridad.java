package seguridad;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V3CertificateGenerator;

public class Seguridad {	

	/**
	 * Algoritmo sim�trico
	 */
	private String algSym;

	/**
	 * Algoritmo asim�trico
	 */
	private String algAsym;

	/**
	 * Algoritmo HMAC
	 */
	private String algHMAC;

	/**
	 * Key pair for RSA
	 */
	private KeyPair pair;

	/**
	 * Llave sim�trica 
	 */
	private SecretKey key;

	/**
	 * Certificado digital del servidor
	 */
	private X509Certificate certServ;

	/**
	 * M�todo que inicializa los algoritmos que se van a utilizar para encripci�n
	 * @param algs Algoritmos de encripci�n
	 */
	public void inicializarAlgorimos(String[] algs){
		algSym = algs[0];
		algAsym = algs[1];
		algHMAC = algs[2];
	}

	/**
	 * M�todo que retorna los algoritmos utilizados para el proceso de encripci�n
	 * @return Nombres de los algoritmos definidos
	 */
	public String getAlgoritmos() {
		String resp = ":"+algSym+":"+algAsym+":"+algHMAC;
		algHMAC = algHMAC.replace("HMAC", "");
		if(algHMAC.contains("SHA")) algHMAC = algHMAC.replace("SHA", "SHA-");
		
		return resp;
	}

	/**
	 * M�todo que inicializa la llave asim�trica
	 * @throws Exception Si ocurre un error en la inicializaci�n
	 */
	public void inicializarLlaveAsimetrica()throws Exception{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algAsym);
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		keyGen.initialize(1024, random);
		pair = keyGen.generateKeyPair();
	}

	/**
	 * M�todo que inicializa la llave sim�trica
	 * @throws Exception Si ocurre un error en la inicializaci�n
	 */
	public void inicializarLlaveSimetrica(byte[] establecida)throws Exception{
		key = new SecretKeySpec(establecida, algSym);
	}

	/**
	 * M�todo que genera el certificado digital bajo el est�ndar X509
	 * @return El certificado digital del cliente
	 * @throws Exception Si hay un fallo con alguno de los par�metros del certificado
	 */
	public X509Certificate generarCertificado()throws Exception{
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
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WITHRSA");

		return certGen.generate(pair.getPrivate());

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

		//Aproximaci�n de http://programtalk.com/java-api-usage-examples/org.bouncycastle.cert.X509v3CertificateBuilder/


	}

	public byte[] cifrarSimetrica(String obj) throws Exception{
		String PADDING=algSym+"/ECB/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(PADDING);
		byte[] text= obj.getBytes();
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipheredText = cipher.doFinal(text);
		String fin= new String(cipheredText);
		return cipheredText;

	}

	public String descifrarSimetrica(byte[] obj) throws Exception{
		String PADDING=algSym+"/ECB/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(PADDING);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipheredText = cipher.doFinal(obj);
		String fin = new String(cipheredText);
		return fin;

	}

	/**
	 * M�todo que cifra el mensaje con la llave p�blica del servidor, con el algoritmo
	 * especificado por el usuario
	 * @param obj Mensaje para ser cifrado
	 * @return Un arreglo de bytes con el mensaje cifrado
	 * @throws Exception Si ocurre alg�n error con el cifrado
	 */
	public byte[] cifrarAsimetrica(String obj) throws Exception{

		Cipher cipher = Cipher.getInstance(algAsym);

		byte[] clearText = obj.getBytes();
		String s1 = new String (clearText);
		System.out.println("clave original: " + s1);
		cipher.init(Cipher.ENCRYPT_MODE, certServ.getPublicKey());

		byte[] cipheredText = cipher.doFinal(clearText);

		System.out.println("clave cifrada: " + cipheredText);

		return	cipheredText;
	}

	/**
	 * M�todo que descifra el mensaje con la llave privada del cliente, con el algoritmo
	 * especificado por el usuario
	 * @param obj Mensaje para ser descifrado
	 * @return El mensaje descifrado
	 * @throws Exception Si ocurre alg�n error con el descifrado
	 */
	public String descifrarAsimetrica(byte[] obj) throws Exception{

		Cipher cipher = Cipher.getInstance(algAsym);
		cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
		byte[] clearText = cipher.doFinal(obj);
		String s3 = new String(clearText);
		return s3;

	}


	public byte[] getKeyedDigest(String buffer) throws Exception{
		byte[] text = buffer.getBytes();
		MessageDigest hasher = MessageDigest.getInstance(algHMAC);
		hasher.update(text);
		return hasher.digest();
	}

	/**
	 * M�todo que actualiza el certificado digital del servidor
	 * @param certServ Certificado digital del servidor
	 */
	public void setCertificadoServidor(X509Certificate certServ) {
		this.certServ = certServ;
	}

}
