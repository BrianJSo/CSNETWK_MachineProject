/*
Cai, Mark Jayson X.
So, Brian Jezreel A.
*/

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