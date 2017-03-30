package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.Scanner;

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
import seguridad.Seguridad;

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
	 * Relación con la clase se encarga de procesar la seguridad del sistema
	 */
	private Seguridad seguridad;

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
			seguridad = new Seguridad();

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
			for (int i = 0; i < Protocol.ALG_HMAC.length; i++) {
				System.out.println("["+(i+1)+"] "+Protocol.ALG_HMAC[i]);
			}

			System.out.print(">");
			String[] indexes = inCliente.next().split(",");
			String[] algs = new String[3];

			algs[0] = Protocol.ALG_SIMETRICOS[Integer.parseInt(indexes[0])-1];
			algs[1] = Protocol.ALG_ASIMETRICOS[Integer.parseInt(indexes[1])-1];
			algs[2] = Protocol.ALG_HMAC[Integer.parseInt(indexes[2])-1];

			socket = new Socket("localhost", puerto);
			socket.setKeepAlive(true);
			printer = new PrintWriter(socket.getOutputStream(),true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			empezarProtocolo(algs);

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try {
				reader.close();
				socket.close();
				printer.close();
				inCliente.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void empezarProtocolo(String[] algs)throws Exception {

		boolean termina = false;
		boolean waiting = true;

		int state=0;
		String command="";
		String resp = "";
		Random rand = new Random();
		long reto = 0;
		boolean response = false;
		String ls = "";
		printer.println(Protocol.HOLA);

		while(!termina){	
			if(reader.ready()){
				waiting = true;
				command = reader.readLine();
				if(command.toLowerCase().contains(Protocol.ERROR.toLowerCase())) throw new Exception(command);
				if(!(command == null && command.equals("")))System.out.println("\nEl servidor dice: "+command);
				switch(state){

				//Etapa1: Inicio de sesión
				case 0: 
					if(command.equals(Protocol.OK)){
						System.out.println("Iniciando sesión...\n");
						resp = Protocol.ALGORITMOS;
						for (String alg : algs) {
							if(!alg.equals(""))resp+=":"+alg;
						}
						printer.println(resp);
						state = 1;
						response = false;
					}
					break;

					//Etapa2: Intercambio de CD
				case 1:
					if (response){
						System.out.println("El certificado digital del servidor es: "+command);
						System.out.println("Autenticando...");
						reto = Math.abs(rand.nextLong());
						printer.println(reto);
						response = false;
					}
					else{
						if(command.equals(Protocol.OK)){
							System.out.println("Intercambiando CD...");
							KeyPairGenerator keyGen = KeyPairGenerator.getInstance(Protocol.ALG_ASIMETRICOS[0]);
							SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
							keyGen.initialize(1024, random);
							pair = keyGen.generateKeyPair();

							X509Certificate cert = seguridad.generarCertificado(pair);

							printer.println(cert);
							state=2;
							response = true;
						}
					}
					break;

					//Etapa3: Autenticación
				case 2:
					if(!response){
						if(reto == Long.parseLong(command))printer.println(Protocol.OK);
						else throw new Exception("El reto recibido no coincide con el enviado.");
						state = 3;
					}
					break;
				case 3:
					if(response){
						ls = command;
						state = 4;
						System.out.println("Consultando...");
						printer.println("1111:1111");
						response = false;
					}else{
						printer.println(command);
						response = true;
					}
					break;

					//Etapa4: Consulta
				case 4:
					if(!response){
						System.out.println("La respuesta a la consulta fue: "+command);
						printer.println(Protocol.OK);
						termina = true;
					}
					break;
				default: 
					state = 0;
					break;
				}
			}else{
				if(waiting){
					System.out.println("waiting...");
					waiting = false;
				}
			}
		}
	}

	public static void main(String[] args) {
		Cliente c = new Cliente();
	}
}
