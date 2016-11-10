/**
 * @author Shane Lacey 20013687
 * @version 1.0.0
 * @date 10/11/16
 */
/*
 Multithreaded version of Area of Circle Client/Server programme
 */
import java.io.*;
import java.net.*;
import java.sql.*;
import java.awt.*;
import javax.swing.*;
import java.util.Date;
import com.mysql.jdbc.Connection;

public class MultiThreadedServerA2 extends JFrame {
	Connection con; // The connection object for the database
	String url = "jdbc:mysql://localhost:3306/areadatabase"; //Change this to switch database
	private String user = "root"; // The username for the DB authentication
	private String pass = ""; // The password for the DB authentication

	// Text area for displaying contents
	private JTextArea jta = new JTextArea();

	public static void main(String[] args) {
		new MultiThreadedServerA2();
	}

	public MultiThreadedServerA2() {
		try{
			con = (Connection) DriverManager.getConnection(url, user, pass); // Instantiating the connection object		
		}
		catch(Exception e){
			print("Error: " + e);
		}
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
		jta.setEditable(false); // Disable this so the output cannot be edited
		setTitle("Server");
		setSize(500, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true); // It is necessary to show the frame here!
		try {
			// Create a server socket
			ServerSocket serverSocket = new ServerSocket(8000);
			jta.append("Server started at " + new Date() + '\n');

			while (true) {
				Socket s1 = serverSocket.accept();
				myClient c = new myClient(s1);
				c.start();
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private class myClient extends Thread {
		// The socket the client is connected through
		private Socket socket;
		// The ip address of the client
		private InetAddress address;
		// The input and output streams to the client
		private DataInputStream inputFromClient;
		private DataOutputStream outputToClient;

		// The Constructor for the client
		public myClient(Socket socket) throws IOException {
			address = socket.getInetAddress(); // The Inet address
			// Declare & Initialise input/output streams
			inputFromClient = new DataInputStream(
					socket.getInputStream());
			outputToClient = new DataOutputStream(
					socket.getOutputStream());
		}

		/*
		 * The method that runs when the thread starts
		 */
		public void run() {
			boolean authenticated = false; // Will be false if the user hasn't successfully authenticated 
			String user = "", fname = "", lname = ""; // Details of the user using this thread
			try {
				while (true) {
					if(authenticated){ // If user has authenticated then calculate the area and send it back to them.
						double radius = inputFromClient.readDouble();
						double area = radius * radius * Math.PI;
						String strArea = String.valueOf(area); // Change it to a string
						outputToClient.writeUTF(strArea); // Send area back to the client
						jta.append("Client/" + fname + lname + "/" + address.getHostName() + "/" + address.getHostAddress() + ": " + radius + '\n');
						jta.append("Area found: " + area + '\n');				        						 
					}
					else{ // If the user has not yet authenticated then get the username they sent and try to authenticate them
						user = inputFromClient.readUTF();
						ResultSet rs = null;
						Statement authenticate = con.createStatement();
						rs = authenticate.executeQuery("SELECT * FROM registeredapplicants WHERE accountnum=" + user); // Retrieve the user with this a/c number if it exists
						if(rs.next()){
							authenticated = true;
							fname = rs.getString("FirstName");
							lname = rs.getString("LastName");				        	
							jta.append("User authenticated: " + fname + " " + lname + '\n');
							outputToClient.writeUTF("Welcome " + fname + " " + lname);
						}
						else{ // If the a/c number entered doesn't exist then throw an error and let them try again.
							print("User doesn't exist");
							outputToClient.writeUTF("User doesn't exist");
							jta.append("User doesn't exist: " + user + '\n');
						}

					}
				}

			} catch (Exception e) {
				System.err.println(e + " on " + socket);
			}
		}
	}

	public void print(String print){
		System.out.println("SERVER: " + print);
	}
}