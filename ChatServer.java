// Chat Server runs at port no. 9999
import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.Timestamp;
import static java.lang.System.out;

public class  ChatServer {
	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();
	Vector<String> logs = new Vector<String>();

	public void process() throws Exception  {
		ServerSocket server = new ServerSocket(9999,10,InetAddress.getLocalHost());
		out.println("Server Started...");
		out.println("IP Address: "+InetAddress.getLocalHost());
		out.println("Port: "+9999);
		while(true) {
			Socket client = server.accept();
			HandleClient c = new HandleClient(client);
			clients.add(c);
		}  // end of while
	}

	public static void main(String ... args) throws Exception {
		new ChatServer().process();
	} // end of main

	public void addLog(String source, String dest, String activity){
		Timestamp time = new Timestamp(System.currentTimeMillis());
		String log = time+" "+source+" to "+dest+" "+activity;
		logs.add(log);
		out.println(log);
	}

	public void broadcast(String user, String message)  {

		// send message to all connected users
		String destination = "";
		for ( HandleClient c : clients )
			if ( ! c.getUserName().equals(user) ){
				c.sendMessage(user,message);
				destination += ","+c.getUserName();
			}
		if( ! user.equals("Server")){
			addLog(user, destination.substring(1), "Message");
		}
	}

	class  HandleClient extends Thread {
		String name = "";
		BufferedReader input;
		PrintWriter output;
		Socket client;

		public HandleClient(Socket  client) throws Exception {
			// get input and output streams
			input = new BufferedReader( new InputStreamReader( client.getInputStream())) ;
			output = new PrintWriter ( client.getOutputStream(),true);
			this.client = client;
			name  = input.readLine();

			String curUsers = "";
			for(String user: users){
				curUsers += ", "+user;
			}
			users.add(name); // add to vector

			if(users.size()<2){
				sendMessage("Server","Successfully joined chat. No other participants.");
			} else {
				sendMessage("Server","Successfully joined chat. Now chatting with "+curUsers.substring(2));
			}
			addLog(name, "Server", "Login");
			broadcast("Server", name+" has joined the chat.");
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
					if ( line.equals("serverCommandEnd") ) {
						broadcast("Server", name+" disconnected.");
						clients.remove(this);
						users.remove(name);
						addLog(name, "Server", "Logout");

						if(users.size() == 0){
							out.println("No users connected");
							out.println("Terminating connection");
							out.println("Server shutting down");
							System.exit(0);
						}
						break;
					} else if ( line.equals("serverCommandGetLogs") ){
						String strLogs = "";
						for(String log: logs){
							strLogs += log+"\n";
						}
						strLogs += "endOfLogs";
						sendMessage("Logs", strLogs);
					} else if ( line.equals("serverCommandFile") ){
						File file = new File("Received.txt");
						file.createNewFile();

						DataOutputStream dosWriter = new DataOutputStream(new FileOutputStream(file));
						DataInputStream disReader = new DataInputStream(client.getInputStream());

						int count;
						byte[] buffer = new byte[8192];
						while ((count = disReader.read(buffer)) > 0)
						{
							dosWriter.write(buffer, 0, count);
							if(disReader.available() < 1){
								break;
							}
						}
                		out.println("done");
						dosWriter.close();
               			out.println("closed file writer");
					} else if (users.size() < 2){
						sendMessage("Server", "message not broadcasted. No users to send to");
					} else {
						broadcast(name,line); // method  of outer class - send messages to all
					}
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