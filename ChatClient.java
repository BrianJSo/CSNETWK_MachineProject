import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import static java.lang.System.out;

public class  ChatClient extends JFrame implements ActionListener {
    static JFrame serverConnectFrame;
    static JTextField tfAddress, tfPort, tfName;
    static JLabel lblError;
    String uname;
    PrintWriter pw;
    BufferedReader br;
    JTextArea  taMessages;
    JTextField tfInput;
    JButton btnSend,btnFile,btnExit,btnLogs;
    Socket client;
    JFileChooser fc;
    
    public ChatClient(String uname,String serverAddress,int serverPort) throws Exception {
        super("DLSUsap "+uname);  // set title for frame
        this.uname = uname;
        client = new Socket(serverAddress,serverPort);
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
        btnLogs = new JButton("Logs");
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
        bp.add(btnLogs);
        add(bp,"South");
        btnSend.addActionListener(this);
        btnFile.addActionListener(this);
        btnExit.addActionListener(this);
        btnLogs.addActionListener(this);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                pw.println("serverCommandEnd");  // send end to server so that server know about the termination
                System.exit(0);
            }
        });
        setSize(500,300);
        setVisible(true);
        pack();
    }
    
    public void actionPerformed(ActionEvent evt) {
        if ( evt.getSource() == btnExit ) {
            pw.println("serverCommandEnd");  // send end to server so that server know about the termination
            System.exit(0);
        } else if ( evt.getSource() == btnFile ){
            fc = new JFileChooser();
            fc.setCurrentDirectory(new java.io.File("c:\\"));
            fc.setDialogTitle("Choose file to send");
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if(fc.showOpenDialog(btnFile)==JFileChooser.APPROVE_OPTION){
                File curFile = fc.getSelectedFile();
                System.out.println(curFile.getAbsolutePath());
                taMessages.append("You: sent "+curFile.getName()+".\n");

                pw.println("serverCommandFile");

                try{
                    DataInputStream disReader = new DataInputStream(new FileInputStream(curFile));
                    DataOutputStream dosWriter = new DataOutputStream(client.getOutputStream());			
                    
                    int count;
                    byte[] buffer = new byte[8192];
                    while ((count = disReader.read(buffer)) > 0)
                    {
                        dosWriter.write(buffer, 0, count);
                    }
                    dosWriter.flush();
                    out.println("done");
                    disReader.close();
                    out.println("closed file reader");
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if ( evt.getSource() == btnLogs ){
            pw.println("serverCommandGetLogs");
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
        tfPort.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                String value = tfPort.getText();
                int l = value.length();
                if ((ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9')||(ke.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
                    tfPort.setEditable(true);
                } else {
                    tfPort.setEditable(false);
                }
            }
        });

        JPanel namePane = new JPanel();
        JLabel lblName = new JLabel("Name:         ");
        tfName = new JTextField(20);
        namePane.add(lblName);
        namePane.add(tfName);


        JPanel connectPane = new JPanel();
        JButton btnConnect = new JButton("Connect to server");
        btnConnect.addActionListener(new connectToServer());
        connectPane.add(btnConnect);

        JPanel errorPane = new JPanel();
        lblError = new JLabel("");
        errorPane.add(lblError);

        serverConnectFrame.setLayout(new FlowLayout());
        serverConnectFrame.add(addressPane);
        serverConnectFrame.add(portPane);
        serverConnectFrame.add(namePane);
        serverConnectFrame.add(connectPane);
        serverConnectFrame.add(errorPane);
        serverConnectFrame.setVisible(true);

    } // end of main

    static class connectToServer implements ActionListener {
        public void actionPerformed(ActionEvent e){
            String serverAddress = tfAddress.getText();
            String serverPort = tfPort.getText();
            String name = tfName.getText();
            try {
                if(!name.equals("")){
                    new ChatClient( name ,serverAddress, Integer.parseInt(serverPort));
                    serverConnectFrame.setVisible(false);
                } else {
                    lblError.setText("Error: Name field is required");
                    out.println( "Error --> Name field is required");
                }
            } catch(Exception ex) {
                lblError.setText("Error: "+ ex.getMessage());
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
                    if(line.substring(0, 4).equals("Logs")){
                        out.println("logging");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        try {
                            File logFile = new File("log"+timestamp.getTime()+".txt");
                            PrintWriter logWriter = new PrintWriter(logFile);

                            String curLine = line.substring(6);
                            while(!curLine.equals("endOfLogs")){
                                out.println(curLine);
                                logWriter.println(curLine);
                                curLine = br.readLine();
                            }
                            out.println("end of logging");
                            logWriter.flush();
                            logWriter.close();
                            taMessages.append("Server: Logs written to "+logFile.getName());
                            System.out.println("Successfully wrote to "+logFile.getName());
                        } catch (IOException e) {
                            System.out.println("Cannot create text file");
                            e.printStackTrace();
                        }
                    } else {
                        taMessages.append(line + "\n");
                    }
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