package seguridad;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.crypto.tls.CertChainType;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class Seguridad {
	
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
	
	public void cifrarSimetrica(String obj, Key key, String algo ){
		
	}
	
	public void decifrarSimetrica(String obj, Key key, String algo){
		
	}
	
	public void cifrarAsimetrica(String obj, PublicKey key ){
		
	}
	
	public void decifrarAsimetrica(String obj, PrivateKey key){
		
	}
		

}
