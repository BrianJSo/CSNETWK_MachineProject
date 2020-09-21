import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

public class  ChatClient extends JFrame implements ActionListener {
    static JFrame serverConnectFrame;
    static JTextField tfAddress, tfPort;
    String uname;
    PrintWriter pw;
    BufferedReader br;
    JTextArea  taMessages;
    JTextField tfInput;
    JButton btnSend,btnFile,btnExit;
    Socket client;
    JFileChooser fc;
    
    public ChatClient(String uname,String servername) throws Exception {
        super(uname);  // set title for frame
        this.uname = uname;
        client  = new Socket(servername,9999);
        br = new BufferedReader( new InputStreamReader( client.getInputStream()) ) ;
        pw = new PrintWriter(client.getOutputStream(),true);
        pw.println(uname);  // send name to server
        buildInterface();
        new MessagesThread().start();  // create thread for listening for messages
    }
    
    public void buildInterface() {
        btnSend = new JButton("Send");
        btnFile = new JButton("File");
        btnExit = new JButton("Exit");
        taMessages = new JTextArea();
        taMessages.setRows(10);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        tfInput  = new JTextField(50);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp,"Center");
        JPanel bp = new JPanel( new FlowLayout());
        bp.add(tfInput);
        bp.add(btnSend);
        bp.add(btnFile);
        bp.add(btnExit);
        add(bp,"South");
        btnSend.addActionListener(this);
        btnFile.addActionListener(this);
        btnExit.addActionListener(this);
        setSize(500,300);
        setVisible(true);
        pack();
    }
    
    public void actionPerformed(ActionEvent evt) {
        if ( evt.getSource() == btnExit ) {
            pw.println("end");  // send end to server so that server know about the termination
            System.exit(0);
        } else if ( evt.getSource() == btnFile ){
            System.out.println("here");
            fc = new JFileChooser();
            fc.setCurrentDirectory(new java.io.File("c:\\"));
            fc.setDialogTitle("Choose file to send");
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if(fc.showOpenDialog(btnFile)==JFileChooser.APPROVE_OPTION){
                
            }
            System.out.println(fc.getSelectedFile().getAbsolutePath());
            taMessages.append("You: " + "sent a file" + "\n");
        }  else {
            // send message to server
            pw.println(tfInput.getText());
            taMessages.append("You: " + tfInput.getText() + "\n");
            tfInput.setText("");
        }
    }
    
    public static void main(String ... args) {

        serverConnectFrame = new JFrame("Connect to server");
        serverConnectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverConnectFrame.setBounds(200,100,400,200);

        JPanel addressPane = new JPanel();
        JLabel lblAddress = new JLabel("IP Address:");
        tfAddress = new JTextField(20);
        addressPane.add(lblAddress);
        addressPane.add(tfAddress);

        JPanel portPane = new JPanel();
        JLabel lblPort = new JLabel("Port no. :   ");
        tfPort = new JTextField(20);
        portPane.add(lblPort);
        portPane.add(tfPort);

        JButton btnConnect = new JButton("Connect to server");
        btnConnect.addActionListener(new connectToServer());

        serverConnectFrame.setLayout(new FlowLayout());
        serverConnectFrame.add(addressPane);
        serverConnectFrame.add(portPane);
        serverConnectFrame.add(btnConnect);
        serverConnectFrame.setVisible(true);

        // String name = JOptionPane.showInputDialog(null,"Server IP address:", "Input IP Address",
        //      JOptionPane.PLAIN_MESSAGE);
        // String s = JOptionPane.showInputDialog(null,"Server port number:", "Input port number",
        //      JOptionPane.PLAIN_MESSAGE);
    } // end of main

    static class connectToServer implements ActionListener {
        public void actionPerformed(ActionEvent e){
            String servername = "localhost";
            String name = "Brian";
            try {
                new ChatClient( name ,servername);
                serverConnectFrame.setVisible(false);
            } catch(Exception ex) {
                out.println( "Error --> " + ex.getMessage());
            }
        }
    }
    
    // inner class for Messages Thread
    class  MessagesThread extends Thread {
        public void run() {
            String line;
            try {
                while(true) {
                    line = br.readLine();
                    taMessages.append(line + "\n");
                } // end of while
            } catch(Exception ex) {}
        }
    }
} //  end of client


/*
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
*/