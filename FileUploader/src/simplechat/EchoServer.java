import java.awt.EventQueue;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import javax.swing.JPanel;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 *
 */
public class EchoServer extends AbstractServer {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 1234;
    Envelope env;
    ChatClient chat;

    //Constructors ****************************************************
    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {
        super(port);
    }

    //Instance methods ************************************************
    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {

        String message = msg.toString();

        if (message.startsWith("#")) {
            handleCommandFromClient(message, client);
        } else {
            System.out.println("Message received: " + msg + " from " + client);
            this.sendToAllClientsInRoom(msg, client.getInfo("room").toString(), client);
        }
        if (msg instanceof Envelope) {
            Envelope e = (Envelope) msg;
            try {
                System.out.println("handling envelope");
                saveFile(e);
            } catch (IOException ex) {
                Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void handleCommandFromClient(String message, ConnectionToClient client) {

        if (message.startsWith("#login")) {
            String userId = message.substring(message.indexOf(" ") + 1, message.length());
            userId = userId.trim();
            System.out.println(">>> " + userId);
            client.setInfo("userId", userId);
            sendToAllClients(userId + " just logged in");
        } else if (message.startsWith("#ison")) {
            String userId = message.substring(message.indexOf(" ") + 1, message.length());
            userId = userId.trim();
            String senderId = client.getInfo("userId").toString();
            String room = client.getInfo("room").toString();
            boolean isFound = false;
            Thread[] clientThreadList = getClientConnections();
            for (int i = 0; i < clientThreadList.length; i++) {
                ConnectionToClient user = ((ConnectionToClient) clientThreadList[i]);
                if (user.getInfo("userId").equals(userId)) {
                    try {
                        isFound = true;
                        String isOnMessage = userId + " is on and in room: " + room;
                        sendBackToClient(isOnMessage, senderId, client);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (!isFound) {
                String isNotOnMessage = userId + " is not on";
                sendBackToClient(isNotOnMessage, senderId, client);
            }
        } else if (message.startsWith("#join")) {
            String room = message.substring(message.indexOf(" ") + 1, message.length());
            room = room.trim();
            System.out.println(">>> " + room);
            client.setInfo("room", room);
            String userId = client.getInfo("userId").toString();
            sendToAllClients(userId + " just joined room: " + room);
        } else if (message.startsWith("#yell")) {
            String yellMessage = "";

            yellMessage = message.substring(message.indexOf(" ") + 1, message.length());

            sendToAllClients(yellMessage);
        } else if (message.startsWith("#pm")) {
            String target = "";
            String pmMessage = "";

            String messageWOCommand = message.substring(message.indexOf(" ") + 1, message.length());
            target = messageWOCommand.substring(0, messageWOCommand.indexOf(" "));
            pmMessage = messageWOCommand.substring(messageWOCommand.indexOf(" "), messageWOCommand.length());

            sendToAClient(pmMessage, target, client);
        } else if (message.startsWith("#intercom")) {
            String room = "";
            String intMessage = "";

            String messageWOCommand = message.substring(message.indexOf(" ") + 1, message.length());
            room = messageWOCommand.substring(0, messageWOCommand.indexOf(" "));
            intMessage = messageWOCommand.substring(messageWOCommand.indexOf(" "), messageWOCommand.length());

            sendToAllClientsInRoom(intMessage, room, client);
        } else if (message.startsWith("#get")) {
            //Get the filename from the command
            String filename = "";
            filename = message.substring(message.indexOf(" "), message.length());
            filename = filename.trim();

            //Create an instance of Envelope
            Envelope e = new Envelope();

            File file = new File("uploads\\" + e.getFileName());

            System.out.println("File " + file.getAbsolutePath());

            byte[] bytesArray = new byte[(int) file.length()];

            try {
                FileInputStream fis = new FileInputStream(file);
                fis.read(bytesArray); //read file into bytes[]
                fis.close();
                e.setFileName(filename);
                byte[] bFile = e.getFileData();
                String fileDest = e.getFileName();
                System.out.println("writing file");
                System.out.println("File " + fileDest);

                //Sends to be handled by client
                client.sendToClient(e);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendBackToClient(Object msg, String target, ConnectionToClient client) {
        Thread[] clientThreadList = getClientConnections();

        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient user = ((ConnectionToClient) clientThreadList[i]);
            if (user.getInfo("userId").equals(target)) {
                try {
                    String message = msg.toString();
                    user.sendToClient(message);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void sendToAllClientsInRoom(Object msg, String room, ConnectionToClient client) {
        Thread[] clientThreadList = getClientConnections();

        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient user = ((ConnectionToClient) clientThreadList[i]);
            if (user.getInfo("room").equals(room)) {
                try {
                    user.sendToClient(msg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void sendToAClient(Object msg, String target, ConnectionToClient client) {
        Thread[] clientThreadList = getClientConnections();

        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient user = ((ConnectionToClient) clientThreadList[i]);
            if (user.getInfo("userId").equals(target)) {
                try {
                    String message = client.getInfo("userId") + " PM:" + msg;
                    user.sendToClient(message);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    //Saves file to directory
    public void saveFile(Envelope e) throws IOException {
        FileOutputStream fileOutputStream = null;
        //Gets file data and name
        byte[] bFile = e.getFileData();
        String fileDest = e.getFileName();
        System.out.println("writing file");
        try {
            //Uploads file to uploads folder with filename
            fileOutputStream = new FileOutputStream("uploads\\" + fileDest);
            fileOutputStream.write(bFile);
            fileOutputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        System.out.println(client + " Connected");
    }

    @Override
    synchronized protected void clientException(
            ConnectionToClient client, Throwable exception) {
        this.clientDisconnected(client);
    }

    @Override
    synchronized protected void clientDisconnected(
            ConnectionToClient client) {
        System.out.println(client + " Disconnected");
    }

    //Class methods ***************************************************
    /**
     * This method is responsible for the creation of the server instance (there
     * is no UI in this phase).
     *
     * @param args[0] The port number to listen on. Defaults to 5555 if no
     * argument is entered.
     */
    public static void main(String[] args) {

        int port = 0; //Port to listen on

        try {
            port = Integer.parseInt(args[0]); //Get port from command line
        } catch (Throwable t) {
            port = DEFAULT_PORT; //Set port to 5555
        }

        EchoServer sv = new EchoServer(port);

        try {
            sv.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }
}
//End of EchoServer class
