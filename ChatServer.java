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
	
	public void broadcastFile(String user, File file)  {
		// send file to all connected users
		String destination = "";
		for ( HandleClient c : clients )
			if ( ! c.getUserName().equals(user) ){
				c.sendFile(user, file);
				destination += ","+c.getUserName();
			}
		addLog(user, destination.substring(1), "FileTransfer");
	}

	class  HandleClient extends Thread {
		String name = "";
		BufferedReader input;
		PrintWriter output;
		Socket client;
		File fileToSend;

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

		public void sendMessage(String uname, String  msg)  {
			output.println( uname + ": " + msg);
		}

		public void sendFile(String uname, File  file)  {
			sendMessage(uname,"sent "+file.getName());
			output.println("File");
			output.println(file.getName());
			fileToSend = file;
		}

		public void writeFileToOutput(){
			try{
				DataInputStream disReader = new DataInputStream(new FileInputStream(fileToSend));
				DataOutputStream dosWriter = new DataOutputStream(client.getOutputStream());
				long fileSize = fileToSend.length();
				dosWriter.writeLong(fileSize);
				int count;
				byte[] buffer = new byte[8192];
				while ((count = disReader.read(buffer)) > 0)
				{
					dosWriter.write(buffer, 0, count);
				}
				dosWriter.flush();
				disReader.close();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
        public String getUserName() {  
            return name;
        }

        public void run()  {
			String line;
			try {
                while(true) {
					line = input.readLine();
					if ( line.equals("serverCommandEnd") ) { // client exit
						broadcast("Server", name+" disconnected.");
						clients.remove(this);
						users.remove(name);
						addLog(name, "Server", "Logout");

						if(users.size() == 0){
							out.println("No users connected");
							out.println("Terminating connection");

							File dir = new File("serverTemp");
							if( dir.exists() ){
								for(File file: dir.listFiles()) 
									if (!file.isDirectory()) 
										file.delete();
							}
							out.println("Server shutting down");
							System.exit(0);
						}
						break;
					} else if ( line.equals("serverCommandGetLogs") ){ // logs request
						String strLogs = "";
						for(String log: logs){
							strLogs += log+"\n";
						}
						strLogs += "endOfLogs";
						sendMessage("Logs", strLogs);
					} else if ( line.equals("serverCommandFile") ){ // file receive from client to server

						String filename = input.readLine();
						File dir = new File("serverTemp");
						if( ! dir.exists()){
							dir.mkdirs();
						}

						File file = new File("serverTemp/"+filename);
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
						dosWriter.flush();
						dosWriter.close();

						if(users.size() < 2 ){
							sendMessage("Server", "file not sent. No users to send to");
						} else {
							broadcastFile(name, file);
						}

					} else if ( line.equals("serverCommandStartFileSend") ){ // start sending file
						writeFileToOutput();
					} else if ( users.size() < 2 ){ // prevent actions if no other connected users
						sendMessage("Server", "message not sent. No users to send to");
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