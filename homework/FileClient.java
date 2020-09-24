/*
Cai, Mark Jayson X.
So, Brian Jezreel A.
*/

import java.net.*;
import java.io.*;

public class FileClient
{
	public static void main(String[] args)
	{
		String sServerAddress = args[0];
		int nPort = Integer.parseInt(args[1]);
		File file = new File("Received.txt");
		
		try
		{
			Socket clientEndpoint = new Socket(sServerAddress, nPort);
			
			System.out.println("Client: Connected to server at " + clientEndpoint.getRemoteSocketAddress());
			
			file.createNewFile();
			DataOutputStream dosWriter = new DataOutputStream(new FileOutputStream(file));
			
			DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());

			int count;
			byte[] buffer = new byte[8192];
			while ((count = disReader.read(buffer)) > 0)
			{
				dosWriter.write(buffer, 0, count);
			}

			System.out.println("Client: Downloaded file \"Received.txt\"");
			dosWriter.close();
			clientEndpoint.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Client: Connection is terminated.");
		}
	}
}