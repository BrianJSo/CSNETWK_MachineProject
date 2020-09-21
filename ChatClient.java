import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

public class  ChatClient extends JFrame implements ActionListener {
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
    
        // take username from user
        String name = JOptionPane.showInputDialog(null,"Enter your name :", "Username",
             JOptionPane.PLAIN_MESSAGE);
        String servername = "localhost";  
        try {
            new ChatClient( name ,servername);
        } catch(Exception ex) {
            out.println( "Error --> " + ex.getMessage());
        }
        
    } // end of main
    
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