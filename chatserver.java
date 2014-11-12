import java.io.*;
import java.net.*;
import java.nio.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*  For the sake of clarification...
	clientSocket = The socket a client connects on.  This may be manifested in one of the following ways:
					clientSocket.getInputStream(), clientSocket.getInetAddress() and so on.
	clientInput =  The input from a client, ie typing which is read via InputStreamReader.
	serverOutput = The output from the server going TO the client console NOT the server console.
					The server console receives it's output via System.out.println and nothing else.
	ccc = Chat Client connection.  The connection # comes from the clientSocket + incremented variable counter i.
*/

class IncomingStuff extends Thread {
	protected Socket clientSocket;
	protected PrintWriter serverOutput;
	protected BufferedReader clientInput;
	protected int ccc;

	public IncomingStuff(Socket clientSocket, int ccc) {
		this.clientSocket = clientSocket;
		this.ccc = ccc;
	}

	//Prints output to the server console.
	public static void serverMsgBox(String msg) {
		if (msg != null) {
			System.out.println(msg);
		}
	}

	//Prints output to the client console.
	public void clientMsgBox(String msg) {
		if (msg != null) {
			serverOutput.println(msg); 
			serverOutput.flush();
		}
	}

public void run() {
		//Default settings in case chatserver.ini does not exist.
		int port = 6667;	//6665–6669 TCP = Known Internet Relay Chat(IRC) port
		String servername = "Free Chatserver";
		String version = "1.0";
		String motd = "Welcome!";

		//Read in from chatserver.ini file.	
		try	{
			File f = new File("chatserver.ini");	//test if chatserver.ini exists.
			if(!f.exists())	{
				System.out.println("\tchatserver.ini does not exist.....using default settings.");
			} else	{	//it exists so use it.
					Properties p = new Properties();
					p.load(new FileInputStream("chatserver.ini"));
					port = Integer.parseInt(p.getProperty("port"));
					servername = p.getProperty("servername");
					version = p.getProperty("version");
					motd = p.getProperty("motd");
			}
		}	catch (Exception e) {
				System.out.println(e);
		}
	
		try {
			clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
			serverOutput = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

			//The next three lines are printed to the client console.	
			clientMsgBox("-----===::|| " + servername + " " + version + " ||::===-----\n");
			clientMsgBox("          "+motd+"\n");
			clientMsgBox("              Welcome "+clientSocket.getInetAddress() + "_cc" + ccc);
			
			serverMsgBox("\tAccepting from   : "+clientSocket.getInetAddress());	//This only occurs if a connection is made.
			
			for (;;) {
				String clin = clientInput.readLine(); 
				if (clin == null) {
					break; 
				} else {
					//The next line echos what the client typed to the server console.
					serverMsgBox(clientSocket.getInetAddress() + "_cc" + ccc + ": " + clin);
					if (clin.trim().equals("exit"))	{ //if the client types exit, the client will exit.
						serverMsgBox(clientSocket.getInetAddress() + "_cc" + ccc + " left chat.");
						break;
					} else	{
						Enumeration ccTracker = chatserver.liveClients.elements();
						while(ccTracker.hasMoreElements()) {
							IncomingStuff tracked = (IncomingStuff) ccTracker.nextElement();
							if (tracked != this) {
								//This line echoes what client typed to all other clients.
								tracked.clientMsgBox("cc_" + ccc + ": " + clin);
								serverOutput.flush();
							}
						}
					}
				}
			}
			clientSocket.close(); 
		} catch (Exception e) {
			System.out.println("Oh snap! You broke me with an error: " + e); 
		}
	}
}

public class chatserver	{
	static protected Vector liveClients;
	public static void main(String[] args) {

		//Default settings in case chatserver.ini does not exist.
		int port = 6667;	//6665–6669 TCP = Known Internet Relay Chat(IRC) port
		String servername = "Wu-Tang chatserver";
		String version = "36c";

		int i = 1;
		liveClients = new Vector();

		//Read in from chatserver.ini file.	
		try	{
			File f = new File("chatserver.ini");	//test if chatserver.ini exists.
			if(!f.exists())	{
				System.out.println("\tchatserver.ini does not exist.....using default settings.");
			} else	{	//it exists so use it.
						Properties p = new Properties();
						p.load(new FileInputStream("chatserver.ini"));
						port = Integer.parseInt(p.getProperty("port"));
						servername = p.getProperty("servername");
						version = p.getProperty("version");
			}
		}	catch (Exception e) {
			System.out.println(e);
		}
		
		IncomingStuff.serverMsgBox("\n\n      -----===::|| " + servername + " " + version + " ||::===-----\n");
		IncomingStuff.serverMsgBox("\tListening on port: " + port);
		
		try {
			ServerSocket s = new ServerSocket(port);
			InetAddress serverIp = InetAddress.getLocalHost();
			IncomingStuff.serverMsgBox("\tServer ip addr   : "+serverIp.getHostAddress());
			IncomingStuff.serverMsgBox("\tServer hostname  : "+serverIp.getHostName());
			//Identify server character set.
			System.out.println("\tServer charset   : " + java.nio.charset.Charset.defaultCharset().name());		
		 
			for (;;) {
				Socket clientSocket = s.accept(); 
				IncomingStuff iThread = new IncomingStuff(clientSocket, i); 
				liveClients.addElement(iThread); 
				iThread.start(); 
				i++; 
			}
		} catch (Exception e) {
				System.out.println("Oh snap! You broke me with an error: " + e); 
		}
		IncomingStuff.serverMsgBox("Chat Server closed.");
	}
}
