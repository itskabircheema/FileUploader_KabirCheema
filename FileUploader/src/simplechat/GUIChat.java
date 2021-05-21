/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author masse8465
 */
public class GUIChat extends JFrame implements ChatIF {

    final public static int DEFAULT_PORT = 1234;

    ChatClient client;

    private JButton uploadB = new JButton("Upload");
 //   private JButton downloadB = new JButton("Download");
    private JButton sendB = new JButton("Send");
    private JButton quitB = new JButton("Quit");

    private JTextField portTxF = new JTextField("1234");
    private JTextField hostTxF = new JTextField("127.0.0"
            + ".1");
    private JTextField messageTxF = new JTextField("");
  //  private JTextField fileTxF = new JTextField("");

    private JLabel portLB = new JLabel("Port: ", JLabel.RIGHT);
    private JLabel hostLB = new JLabel("Host: ", JLabel.RIGHT);
    private JLabel messageLB = new JLabel("Message: ", JLabel.RIGHT);

    private JTextArea messageList = new JTextArea();

    public GUIChat(String host, int port) {
        super("Simple Chat GUI");
        setSize(300, 400);

        setLayout(new BorderLayout(5, 5));
        JPanel bottom = new JPanel();
        add("Center", messageList);
        add("South", bottom);

        bottom.setLayout(new GridLayout(6, 2, 5, 5));
        bottom.add(hostLB);
        bottom.add(hostTxF);
        bottom.add(portLB);
        bottom.add(portTxF);
        bottom.add(messageLB);
        bottom.add(messageTxF);
    //    bottom.add(downloadB);
        bottom.add(sendB);
        bottom.add(uploadB);
        bottom.add(quitB);
     //   bottom.add(fileTxF);

        sendB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
                //display(messageTxF.getText() + "\n");
            }
        });

        quitB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quit();
            }
        });

        uploadB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                upload();
            }
        });

//        downloadB.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                download();
//            }
//        });

        setVisible(true);

        try {
            client = new ChatClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection!"
                    + " Terminating client.");
            System.exit(1);
        }
    }

    public void display(String message) {
        messageList.insert(message, 0);
    }

    public void send() {
        try {
            String message = messageTxF.getText() + "\n";
            client.handleMessageFromClientUI(message);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void quit() {
        System.exit(0);
    }

    public void upload() {
        fileChooser();
    }

//    public void download() {
//        try {
//            //Converts textbox text into string
//            String filename = fileTxF.getText();
//            //Sends to be handled by client
//            client.handleMessageFromClientUI("#get " + filename);
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//    }

    //Allows for a file to be chosen to upload
    private void fileChooser() {
        //Makes a new file chooser instance
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jfc.showOpenDialog(null);
        //int returnValue = jfc.showSaveDialog(null);
        try {
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String selectedFile = jfc.getSelectedFile().getName();
                //Creates an instance of Envelope
                Envelope e = new Envelope();
                e.setFileName(selectedFile.toString());
                File file = new File(jfc.getSelectedFile().getAbsolutePath());
                
                System.out.println("File "+file.getAbsolutePath());
                
                //Create a new byte array for files
                byte[] bytesArray = new byte[(int) file.length()];
                
                FileInputStream fis = new FileInputStream(file);
                fis.read(bytesArray); //read file into bytes[]
                fis.close();
                
                e.setFileData(bytesArray);
                
                System.out.println(">>"+bytesArray.length);
                System.out.println(selectedFile);
                //Sends object e to be handled by client
                client.handleMessageFromClientUI(e);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String host = "";
        int port = 0;  //The port number

        try {
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            port = DEFAULT_PORT;
        }

        System.out.println("PORT: " + port);

        try {
            host = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            host = "localhost";
        }

        GUIChat clientConsole = new GUIChat(host, port);
    }
}
