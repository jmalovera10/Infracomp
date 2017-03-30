package seguridad;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.crypto.tls.CertChainType;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class Seguridad {
	
	public X509Certificate generarCertificado(KeyPair keypair){
		PrivateKey priv = keypair.getPrivate();
		X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
		X509Certificate a=null;
		try {
			a = cert.generate(priv);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return a;
		
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
