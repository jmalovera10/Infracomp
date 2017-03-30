package cliente;

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
			
			System.out.println("\nAlgoritmos Simétricos");
			for (int i = 0; i < Protocol.ALG_SIMETRICOS.length; i++) {
				System.out.println("["+(i+1)+"] "+Protocol.ALG_SIMETRICOS[i]);
			}
			
			System.out.println("\nAlgoritmos Asimétricos");
			for (int i = 0; i < Protocol.ALG_ASIMETRICOS.length; i++) {
				System.out.println("["+(i+1)+"] "+Protocol.ALG_ASIMETRICOS[i]);
			}
			
			System.out.println("\nAlgoritmos Hash");
			for (int i = 0; i < Protocol.ALG_HASH.length; i++) {
				System.out.println("["+(i+1)+"] "+Protocol.ALG_HASH[i]);
			}
			
			System.out.print(">");
			String[] indexes = inCliente.next().split(",");
			String[] algs = new String[3];
			
			algs[0] = Protocol.ALG_SIMETRICOS[Integer.parseInt(indexes[0])];
			algs[1] = Protocol.ALG_ASIMETRICOS[Integer.parseInt(indexes[1])];
			algs[2] = Protocol.ALG_HASH[Integer.parseInt(indexes[2])];

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
		boolean response = false;
		printer.println(Protocol.HOLA);

		while(socket.isConnected()){	
			System.out.println("waiting...");
			if(reader.ready())command = reader.readLine();
			if(!(command == null && command.equals("")))System.out.println("\nEl servidor dice: "+command);
			switch(state){
		
			case 0: 
				if(command.equals(Protocol.OK)){
					if(response){
						response = false;
						state = 1;
					}
					else{
						resp = Protocol.ALGORITMOS;
						for (String alg : algs) {
							if(!alg.equals(""))resp+=":"+alg;
						}
						response = true;
					}
				}
				else if(command.contains(Protocol.ERROR)) throw new Exception(command);
				break;
			case 1:
				if(response){
					state = 2;
					response = false;
				}else{
					
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
