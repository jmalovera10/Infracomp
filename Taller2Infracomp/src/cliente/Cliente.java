package cliente;

import seguridad.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.x509.*;

/*https://docs.oracle.com/javase/tutorial/security/apisign/step2.html


/*
public java.security.cert.X509Certificate generateX509Certificate(java.security.PrivateKey key,
        java.lang.String provider)

/*http://www.bouncycastle.org/wiki/display/JA1/X.509+Public+Key+Certificate+and+Certification+Request+Generation
 * static {
  Security.addProvider(new BouncyCastleProvider());
}

public void someMethod() {
  KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
  Key key = fact.generatePublic(PUB_KEY_SPEC);
  // do stuff
}
 */

import protocolo.Protocol;

public class Cliente {

	/**
	 * Socket del cliente
	 */
	private Socket socket;

	/**
	 * Scanner que lee la entrada desde el cliente
	 */
	private Scanner inCliente;

	/**
	 * Printer que escribe hacia el servidor
	 */
	private PrintWriter printer;

	/**
	 * Lector de los datos enviados desde el servidor
	 */
	private BufferedReader reader;

	/*
	 * Key pair for RSA
	 * 
	 */
	private KeyPair pair;
	
	
	/**
	 * Constructor por defecto
	 */
	public Cliente() {

		try{
			inCliente = new Scanner(System.in);

			System.out.println("----------------------------------\nTaller Canales Seguros\n----------------------------------\n");
			System.out.print("Ingrese el puerto al cual se quiere conectar\n>");
			int puerto = inCliente.nextInt();

			System.out.println("\nSeleccione, separando con comas, los números de los algorimtos de cifrado que desea utilizar\n");
			for (int i = 0; i < Protocol.ALGORITMOS_ARR.length; i++) {
				System.out.println("["+(i+1)+"] "+Protocol.ALGORITMOS_ARR[i]);
			}
			
			System.out.print(">");
			String[] algs = inCliente.next().split(",");
			
			for (String string : algs) {
				System.out.println(">"+string);
			}

			for (int i = 0; i < algs.length; i++) {
				if(!(algs[i].equals("")||algs[i]==null))algs[i] = Protocol.ALGORITMOS_ARR[Integer.parseInt(algs[i])];
			}

			socket = new Socket("localhost", puerto);
			socket.setKeepAlive(true);
			printer = new PrintWriter(socket.getOutputStream(),true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			
			empezarProtocolo(algs);
			//printer.println(Protocol.HOLA);
			//System.out.println(reader.readLine().equals(Protocol.OK)?"\n----------------------------\n"
			//+ "Conectado con el servidor\n----------------------------\n":"Ha ocurrio un error en la comunicación con el servidor");

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void empezarProtocolo(String[] algs)throws Exception {

		int state=0;
		String command="";
		String resp = "";
		printer.println(Protocol.HOLA);

		while(socket.isConnected()){
			
			System.out.println("waiting");
			if(reader.ready()) command = reader.readLine();
			System.out.println(command);
			
			switch(state){
		
			case 0: 
				if(command.equals(Protocol.OK)){
					if(algs==null) state = 1;
					else{
						resp = Protocol.ALGORITMOS;
						for (String alg : algs) {
							if(!alg.equals(""))resp+=":"+alg;
						}
						System.out.println(resp);
						state=1;
					}
				}
				else if(command.contains(Protocol.ERROR)) throw new Exception(command);
				break;
			case 1:
				if (command.equals(Protocol.OK)){
					KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
					SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
					keyGen.initialize(1024, random);
					pair = keyGen.generateKeyPair();
					
					X509Certificate cert = seguridad.Seguridad.generarCertificado(pair);
					
					printer.println(cert);
					state=2;
				}else{
					System.out.println("Error");
					state=0;
				}
				break;
			default: 
				state = 0;
				break;
			}
		}
	}

	public static void main(String[] args) {
		Cliente c = new Cliente();
	}
}
