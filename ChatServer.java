// Chat Server runs at port no. 9999
import java.io.*;
import java.util.*;
import java.net.*;
import static java.lang.System.out;

public class  ChatServer {
	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();

	public void process() throws Exception  {
		ServerSocket server = new ServerSocket(9999,10,InetAddress.getLocalHost());
		out.println("Server Started...");
		out.println("IP Address: "+InetAddress.getLocalHost());
		out.println("Port: "+9999);
		while( true) {
			Socket client = server.accept();
			HandleClient c = new HandleClient(client);
			clients.add(c);
		}  // end of while
	}

	public static void main(String ... args) throws Exception {
		new ChatServer().process();
	} // end of main

	public void boradcast(String user, String message)  {
			// send message to all connected users
			for ( HandleClient c : clients )
				if ( ! c.getUserName().equals(user) )
					c.sendMessage(user,message);
	}

	class  HandleClient extends Thread {
		String name = "";
		BufferedReader input;
		PrintWriter output;

		public HandleClient(Socket  client) throws Exception {
			// get input and output streams
			input = new BufferedReader( new InputStreamReader( client.getInputStream())) ;
			output = new PrintWriter ( client.getOutputStream(),true);
			// read name
			name  = input.readLine();
			users.add(name); // add to vector
			start();
		}

		public void sendMessage(String uname,String  msg)  {
			output.println( uname + ": " + msg);
		}
		
        public String getUserName() {  
            return name; 
        }

        public void run()  {
			String line;
			try {
                while(true) {
					line = input.readLine();
					if ( line.equals("end") ) {
						clients.remove(this);
						users.remove(name);
						break;
					}
					boradcast(name,line); // method  of outer class - send messages to all
				} // end of while
			} catch(Exception ex) {
				System.out.println(ex.getMessage());
			}
		} // end of run()
   } // end of inner class

} // end of Server

/*
import java.net.*;
import java.io.*;

public class FileServer
{
	public static void main(String[] args)
	{
		int nPort = Integer.parseInt(args[0]);
		System.out.println("Server: Listening on port " + args[0] + "...");
		ServerSocket serverSocket;
		Socket serverEndpoint;
		File file = new File("./Download.txt");

		try 
		{
			serverSocket = new ServerSocket(nPort);
			serverEndpoint = serverSocket.accept();
			
			System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());
			
			DataInputStream disReader = new DataInputStream(new FileInputStream(file));
			DataOutputStream dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());			
			
			System.out.println("Server: Sending file \"Download.txt\" (" + file.length() + " bytes)" );

			int count;
			byte[] buffer = new byte[8192];
			while ((count = disReader.read(buffer)) > 0)
			{
				dosWriter.write(buffer, 0, count);
			}

			disReader.close();
			serverEndpoint.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Server: Connection is terminated.");
		}
	}
}

*/