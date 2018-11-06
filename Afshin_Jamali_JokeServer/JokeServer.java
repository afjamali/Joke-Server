/*--------------------------------------------------------

 1. Afshin Jamali / Date: 1/23/14

 2. Java version used, if not the official version for the class: 

 Version "1.7.0_51", build 1.7.0_51-b13

 3. Precise command-line compilation examples / instructions:

 > javac JokeServer.java

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

 5. Notes: J, P, M are abbreviations for modes: Joke, Proverb, and Maintanence.
           the JokeServer maintains the whole state of client.

 ----------------------------------------------------------*/

import java.io.*; // For input and output 
import java.net.*; // For networking 
import java.util.*; // For using HashMap

/**
 * JokeServer class with main method. This class is a server for clients to
 * connect and get their jokes and proverbs in distinct random order. The
 * server's responsibility is to listen for multiple clients to connect and then
 * create worker threads for outputting random jokes/provers. If ClientAdmin
 * connects, the admin has the ability to change modes (Joke, Prover,
 * Maintenence) The port number is 5035 for JokeServer, and 5036 for ClientAdmin
 *
 * @author Afshin Jamali
 */
public class JokeServer {

    // Declare static variables
    static Map<String, HashMap<String, HashMap<Character, Integer>>> cookie; // This is the information that is stored for each client.
    static Map<String, HashMap<Character, String>> jokesProverbs; // Contains 5 proverbs and 5 jokes
    static String mode = "J"; // Default mode is Joke

    /**
     * main method. The parameter a is not used for anything.
     *
     * @param a
     * @throws IOException
     */
    public static void main(String a[]) throws IOException {

        // Initialize fields and assign
        cookie = new HashMap<>();
        jokesProverbs = new HashMap<>();

        jokesProverbs.put("J", new HashMap<Character, String>());
        jokesProverbs.put("P", new HashMap<Character, String>());
        jokesProverbs.get("J").put('A', "Xname totally understand how batteries feel because Xname is rarely ever included in things either.");
        jokesProverbs.get("J").put('B', "Xname kills vegetarian vampires with a steak to the heart.");
        jokesProverbs.get("J").put('C', "What seven letters did Xname say when he opened\n"
                + "the cookie jar?\n"
                + "O I C U R M T");
        jokesProverbs.get("J").put('D', "How does Xname get a tissue to dance?\n"
                + "Put a little boogey in it!");
        jokesProverbs.get("J").put('E', "Xname walks into a bar with jumper cables around the neck. The bartender\n"
                + "says, \"You can stay but don't try to start anything.\"");
        jokesProverbs.get("P").put('A', "All work and no play makes Xname a dull young person.");
        jokesProverbs.get("P").put('B', "Xname can't make an omelette without breaking eggs.");
        jokesProverbs.get("P").put('C', "Xname can't please everyone.");
        jokesProverbs.get("P").put('D', "What Xname doesn't know can't hurt Xname.");
        jokesProverbs.get("P").put('E', "The grass is always greener on Xname's side of the fence.");
        int q_len = 6; // Max number of requests at the same time
        int port = 5035; // Port of which to connect by     
        Socket sock; // unconnected socket

        AdminLooper AL = new AdminLooper(); // create a seperate thread for ClientAdmin
        Thread t = new Thread(AL);
        t.start();  // Wait for input from ClientAdmin

        // Create serversock variable to accept client connection
        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out.println("Afshin Jamali's Joke server starting up, listening at port " + port + "\n");
        while (AdminLooper.adminControlSwitch) {
            sock = servsock.accept(); // wait for a client connection and accept it 
            if (AdminLooper.adminControlSwitch) {
                new Worker(sock).start(); // Instantiate Worker class to look up next joke/proverb
            } // Uncomment to see shutdown oddity: 
            // try{Thread.sleep(10000);} catch(InterruptedException ex) {} 
        }
    }
}

/**
 * This class listens for ClientAdmin to connect, accept input from admin and
 * switch modes according to input.
 *
 * @author Admin
 */
class AdminLooper implements Runnable {

    public static boolean adminControlSwitch = true; // If control switch is false, program will exit while loop and program ends

    public void run() { // Run the admin listener loop
        System.out.println("In the admin looper thread");

        int q_len = 6; /* Number of requests for OpSys to queue */

        int port = 5036;  // We are listening at a different port for Admin clients
        Socket sock;

        try {
            ServerSocket servsock = new ServerSocket(port, q_len);
            while (adminControlSwitch) {
                // wait for the next ADMIN client connection:
                sock = servsock.accept(); // wait for admin connection and accept it 
                new ModeWorker(sock).start(); // Instantiate ModeWorker class to change mode and notify admin
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

/**
 * This class performs tasks for displaying random jokes/proverbs to clients, or
 * notifying clients if server is under maintenence.
 *
 * @author Admin
 */
class Worker extends Thread { // Class definition 

    // Define local variables 
    Socket sock;
    String abc = "ABCDE"; // Labels for jokes/proverbs
    Random rn = new Random(); // Initialize random number generator

    Worker(Socket s) {
        // Initialize sock in constructor
        sock = s;
    }

    // Read in Socket and ouput data 
    public void run() {
        PrintStream out = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());
            // Note that this branch might not execute when expected:
            if (AdminLooper.adminControlSwitch != true) {
                System.out.println("Listener is now shutting down as per Administrator's request.");
                out.println("Server is now shutting down. Goodbye!");
            } else {
                try {
                    String name;
                    name = in.readLine(); // Outputs the message as soon as it receives input from client
                    System.out.println("Generating " + JokeServer.mode + " message for user "+ name);
                    printRemoteMessage(name, out);

                } catch (IOException x) {
                    System.out.println("Server read error");
                    x.printStackTrace();
                }
            }
            sock.close(); // closes the current connection 
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    /**
     * @param name the username
     * @param out  output stream for output to user
     */
    private void printRemoteMessage(String name, PrintStream out) {
        try {

            if (JokeServer.mode.equals("M")) {
                out.println("The server is temporarily unavailable -- check-back shortly.");
                return;
            }

            // If username does not exists, create a new username
            if (!JokeServer.cookie.containsKey(name)) {
                JokeServer.cookie.put(name, new HashMap<String, HashMap<Character, Integer>>());
                JokeServer.cookie.get(name).put("J", new HashMap<Character, Integer>());
                JokeServer.cookie.get(name).put("P", new HashMap<Character, Integer>());

                // Add jokes/proverbs to user's profile
                for (char item : JokeServer.jokesProverbs.get("J").keySet()) {
                    JokeServer.cookie.get(name).get("J").put(item, 0);
                }
                for (char item : JokeServer.jokesProverbs.get("P").keySet()) {
                    JokeServer.cookie.get(name).get("P").put(item, 0);
                }
            }

            // Check if all jokes or proverbs have been read to user. If so, reset to 0 (not read)
            int n = 0;
            for (int item : JokeServer.cookie.get(name).get(JokeServer.mode).values()) {
                n += item;
            }
            if (n == 5) {
                rn = new Random();
                for (char item : JokeServer.jokesProverbs.get(JokeServer.mode).keySet()) {
                    JokeServer.cookie.get(name).get(JokeServer.mode).put(item, 0);
                }
            }

            // Generate random number, and map number to a character in the label string for jokes/proverbs
            while (true) {
                int randomNum = rn.nextInt(abc.length()); // Generate random number

                // If message not previously read, output to user
                if (JokeServer.cookie.get(name).get(JokeServer.mode).get(abc.charAt(randomNum)) != 1) {
                    out.println(abc.charAt(randomNum) + ". " + JokeServer.jokesProverbs.get(JokeServer.mode).get(abc.charAt(randomNum)).replace("Xname", name));

                    JokeServer.cookie.get(name).get(JokeServer.mode).put(abc.charAt(randomNum), 1);
                    break;
                }
            }

        } catch (Exception ex) {
            out.println("Failed in atempt to process client request " + ex);
        }
    }
}

/**
 * This class performs tasks for changing server modes upon request of admin.
 * The admin also has the authority to shutdown the server.
 * 
 * @author Admin
 */
class ModeWorker extends Thread { // Class definition 

    // Define local variables 
    Socket sock;
    Random rn = new Random();

    ModeWorker(Socket s) {
        // Initialize sock in constructor
        sock = s;
    }

    // Read in Socket and ouput data 
    public void run() {
        PrintStream out = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());
            // Note that this branch might not execute when expected:
            if (AdminLooper.adminControlSwitch != true) {
                System.out.println("Listener is now shutting down as per Administrator's request.");
                out.println("Server is now shutting down. Goodbye!");
            } else {
                try {
                    String mode;
                    mode = in.readLine(); // Outputs the current server mode as soon as it receives input from clientAdmin 
                    if (mode.equalsIgnoreCase("shutdown")) { //Exit server program if admin requests to shutdown server
                        AdminLooper.adminControlSwitch = false;
                        System.out.println("ModeWorker has captured a shutdown request.");
                        out.println("Shutdown request has been noted by Modeworker.");
                        out.println("Please send final shutdown request to listener.");
                    } else {
                        System.out.println("Performing task " + mode + " for ClientAdmin...");
                        printRemoteAdminMessage(mode, out);
                    }
                } catch (IOException x) {
                    System.out.println("Server read error");
                    x.printStackTrace();
                }
            }
            sock.close(); // closes the current connection 
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    /**
     * Change mode and notify ClientAdmin
     * 
     * @param m mode to change
     * @param out  notify ClientAdmin
     */
    private void printRemoteAdminMessage(String m, PrintStream out) {
        try {
            JokeServer.mode = m;
            out.println("Server now in " + JokeServer.mode + " mode");

        } catch (Exception ex) {
            out.println("Failed in atempt to process task " + m);
        }
    }
}
