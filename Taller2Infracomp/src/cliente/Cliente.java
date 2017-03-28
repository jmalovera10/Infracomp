package cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

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
			if(reader.ready())command = reader.readLine();
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
						algs = null;
					}
				}
				else if(command.contains(Protocol.ERROR)) throw new Exception(command);
				break;
			case 1:
				if(inCliente.hasNext())resp = inCliente.nextLine();
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
