/*--------------------------------------------------------

 1. Afshin Jamali / Date: 1/23/14

 2. Java version used, if not the official version for the class: 

 Version "1.7.0_51", build 1.7.0_51-b13

 3. Precise command-line compilation examples / instructions:

 > javac JokeClient.java

 4. Precise examples / instructions to run this program:

 In separate shell windows:

 > java JokeServer
 > java JokeClient
 > java JokeClientAdmin

 All acceptable commands are displayed on the various consoles.

 This runs across machines, in which case you have to pass the IP address of
 the server to the clients. For exmaple, if the server is running at
 140.192.1.22 then you would type:

 > java JokeServer 140.192.1.22
 > java JokeClient 140.192.1.22
 > java JokeClient 140.192.1.22

 5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

 5. Notes: 

 ----------------------------------------------------------*/
import java.io.*; // For input and output
import java.net.*; // For networking 

/**
 * This class contains the main method. It sends a request to server
 * to receive a message. The user inputs username and client establishes
 * a connection with the server with the specified username and IP address.
 * 
 * @author Afshin Jamali
 */
public class JokeClient {

    /**
     * main method. The first input is the username. Afterwards request is
     * sent to server every time user presses Enter.
     * 
     * @param args IP address of server
     */
    public static void main(String args[]) {
        // Declare variables
        int port = 5035;
        String serverName;

        // Server name by default is localhost unless specified otherwise
        if (args.length < 1) {
            serverName = "localhost";
        } else {
            serverName = args[0];
        }

        System.out.println("Afshin Jamali's Joke Client, 1.7.\n");
        System.out.println("Using server: " + serverName + ", Port: " + port);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String name;
            String inpt;
            System.out.print("Enter your username, (quit) to end: ");            
            name = in.readLine();
            if (name.equalsIgnoreCase("quit") == true){ // Cancel program if input is "quit"
                System.out.println("Cancelled by user request.");
                System.exit(0);
            }
            do {
                System.out.print("Press Enter to receive output from server, (quit) to end: ");
                System.out.flush();
                inpt = in.readLine();
                if (inpt.equalsIgnoreCase("quit") == false) { // Process client request or exit if input equals "quit"
                    getRemoteMessage(name, serverName, port);
                }
            } while (inpt.equalsIgnoreCase("quit") == false); // Check for user input while input is not "quit"
            System.out.println("Cancelled by user request.");
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    /**
     * 
     * @param name name of user
     * @param serverName IP address of server
     * @param port  Port to which connects to JokeServer listener
     */
    static void getRemoteMessage(String name, String serverName, int port) {
        // Declare variables
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try {
            // Open a new connection to server
            sock = new Socket(serverName, port);

            // Create input output streams to and from server 
            fromServer
                    = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream());
            // Send host name to server
            toServer.println(name);
            toServer.flush(); // Empty buffer datat between client and server

            // Ouput results from server as long as it is not null: 
            for (int i = 1; i <= 3; i++) {
                textFromServer = fromServer.readLine();
                if (textFromServer != null) {
                    System.out.println(textFromServer);
                }
            }
            sock.close();
        } catch (IOException x) {
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }
}
