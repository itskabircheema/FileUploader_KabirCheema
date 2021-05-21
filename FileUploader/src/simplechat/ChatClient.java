
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class overrides some of the methods defined in the abstract superclass
 * in order to give more functionality to the client.
 *
 *
 */
public class ChatClient extends AbstractClient {
    //Instance variables **********************************************

    /**
     * The interface type variable. It allows the implementation of the display
     * method in the client.
     */
    ChatIF clientUI;

    //Constructors ****************************************************
    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @param clientUI The interface type variable.
     */
    public ChatClient(String host, int port, ChatIF clientUI)
            throws IOException {
        super(host, port); //Call the superclass constructor
        this.clientUI = clientUI;
//        openConnection();
    }

    //Instance methods ************************************************
    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg) {
        clientUI.display(msg.toString());
        if (msg instanceof Envelope) {
            System.out.println("Envelope Found");
            Envelope e = (Envelope) msg;
            try {
                System.out.println("handling envelope");
                saveFile(e);
            } catch (IOException ex) {
                Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(Object m) throws IOException {

        String message = m.toString();
        String command = null;

        if (message.indexOf(" ") > 0) {
            command = message.substring(0, message.indexOf(" "));
        } else {
            command = message;
        }
        command=command.trim();
        
        if (command.startsWith("#")) {
            switch (command) {
                case "#quit":
                    quit();
                    break;
                case "#logoff":
                    closeConnection();
                    break;
                case "#sethost":
                    if (!isConnected()) {
                        command = message.substring(9);
                        System.out.println(command);
                        setHost(command);
                    }
                    break;
                case "#setport":
                    if (!isConnected()) {
                        command = message.substring(9);
                        System.out.println(command);
                        setPort(Integer.valueOf(command));
                        break;
                    }
                case "#login":
                    if (!isConnected()) {
                        openConnection();
                        sendToServer(message);
                    }
                    break;
                case "#getport":
                    clientUI.display(String.valueOf(getPort()));
//                    System.out.println(getPort());
                    break;
                case "#gethost":
                    clientUI.display(String.valueOf(getHost()));
//                    System.out.println(getHost());
                    break;
                case "#pm":
                    sendToServer(message);
                    break;
                case "#join":
                    sendToServer(message);
                    break;
                case "#yell":
                    sendToServer(message);
                    break;
                case "#intercom":
                    sendToServer(message);
                    break;
                case "#ison":
                    sendToServer(message);
                    break;
                case "#get": //Handles command to download files
                    sendToServer(message);
                    break;
            }
        } else {
            try {
                sendToServer(m);
            } catch (IOException e) {
                clientUI.display("Could not send message to server.  Terminating client.");
//                quit();
            }
        }
    }
    
    public void saveFile(Envelope e) throws IOException {
        
        System.out.println("Writing File to Client");
        FileOutputStream fileOutputStream = null;
        byte[] bFile = e.getFileData();
        String fileDest = e.getFileName();
        System.out.println("writing file");
        try {
            fileOutputStream = new FileOutputStream(fileDest);
            fileOutputStream.write(bFile);
            fileOutputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void connectionException(Exception exception) {
        System.out.println("The connection to the server has been lost.");
    }

    /**
     * This method terminates the client.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
        }
        System.exit(0);
    }
}
//End of ChatClient class
