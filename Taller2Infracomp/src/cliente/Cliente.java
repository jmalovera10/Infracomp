package cliente;

import java.awt.RenderingHints.Key;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.SecretKey;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

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
			
			seguridad.inicializarAlgorimos(algs);

			socket = new Socket("localhost", puerto);
			socket.setKeepAlive(true);
			printer = new PrintWriter(socket.getOutputStream(),true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			empezarProtocolo();

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

	private void empezarProtocolo()throws Exception {

		boolean termina = false;
		boolean waiting = true;

		int state=0;
		String command="";
		String resp = "";
		Random rand = new Random();
		String certificate = "";
		long reto = 0;
		boolean response = false;
		byte[] cifrado;
		printer.println(Protocol.HOLA);

		while(!termina){	
			if(reader.ready()){
				waiting = true;
				command = reader.readLine();
				if(command == null || command.equals("")) continue;
				else if(command.toLowerCase().contains(Protocol.ERROR.toLowerCase())) throw new Exception(command);
				else if(state == 1 && response) certificate+=command+"\n";
				else System.out.println("\nEl servidor dice: "+command);
				switch(state){

				//Etapa1: Inicio de sesión
				case 0: 
					if(command.equals(Protocol.OK)){
						System.out.println("Iniciando sesión...\n");
						
						resp = Protocol.ALGORITMOS;
						resp += seguridad.getAlgoritmos();
						printer.println(resp);
						
						state = 1;
						response = false;
					}
					break;

				//Etapa2: Intercambio de CD
				case 1:
					if (response && command.contains("END CERTIFICATE")){
						System.out.println("\nSe ha recibido el certificado digital del servidor:\n"+certificate);
						PrintWriter pw = new PrintWriter(new FileOutputStream("data/cert.txt"));
						pw.println(certificate);
						pw.close();
						
						System.out.println("Autenticando...");
						
						reto = Math.abs(rand.nextLong());
						CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
						X509Certificate certServ = (X509Certificate)certFactory.generateCertificate(new FileInputStream("data/cert.txt"));
						seguridad.setCertificadoServidor(certServ);
						cifrado = seguridad.cifrarAsimetrica(""+reto);
					    cifrado = Hex.encode(cifrado);
					    
						printer.println(Protocol.OK);
						Thread.sleep(500);
						printer.println(new String(cifrado));
						
						state=2;
						response = false;
					}
					else{
						if(command.equals(Protocol.OK)){
							System.out.println("Intercambiando CD...");
							seguridad.inicializarLlaveAsimetrica();

							X509Certificate cert = seguridad.generarCertificado();
							String cert_begin = "-----BEGIN CERTIFICATE-----\n";
							String end_cert = "\n-----END CERTIFICATE-----";

							byte[] derCert = cert.getEncoded();
							String pemCertPre = new String(Base64.encode(derCert));
							String pemCert = cert_begin + pemCertPre + end_cert;
							printer.println(pemCert);
							
							response = true;
						}
					}
					break;

				//Etapa3: Autenticación
				case 2:
					if(!response){
						
						cifrado = Hex.decode(command);
						long val = Long.parseLong(seguridad.descifrarAsimetrica(cifrado));
						if(val==reto)printer.println(Protocol.OK);
						else throw new Exception("El reto recibido no coincide con el enviado.");
						
						state = 3;
					}
					break;
				
				//Etapa4: Consulta
				case 3:
					if(response){
						cifrado = Hex.decode(command);
						resp = seguridad.descifrarAsimetrica(cifrado);
						seguridad.inicializarLlaveSimetrica(resp.getBytes());
			
						System.out.println("Consultando...");
						System.out.print(">Ingrese su número de cédula\n>");
						String cedula = inCliente.next().trim();
						cifrado = seguridad.cifrarSimetrica(cedula);
						resp = new String(cifrado);
						resp += ":"+seguridad.cifrarSimetrica(new String(seguridad.getKeyedDigest(resp)));
						cifrado = Hex.encode(resp.getBytes());
						printer.println(new String(cifrado));
						
						state = 4;
						response = false;
					}else{
						cifrado = Hex.decode(command);
						resp = seguridad.descifrarAsimetrica(cifrado);
						System.out.println("El número aleatorio es : "+resp);
						cifrado = seguridad.cifrarAsimetrica( resp );
					    cifrado = Hex.encode(cifrado);
						printer.println(new String(cifrado));
						response = true;
					}
					break;
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
