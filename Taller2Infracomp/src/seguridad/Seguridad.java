package seguridad;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Seguridad {
	public enum algoritmos{
		
	}
	
	public void generarCertificado(KeyPair keypair){}
	
	public void cifrarSimetrica(String obj, Key key, String algo ){}
	
	public void decifrarSimetrica(String obj, Key key, String algo){}
	
	public void cifrarAsimetrica(String obj, PublicKey key ){}
	
	public void decifrarAsimetrica(String obj, PrivateKey key){}
		

}
