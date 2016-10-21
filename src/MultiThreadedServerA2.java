/*
 Multithreaded version of Area of Circle Client/Server programme
*/
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import com.mysql.jdbc.Connection;

import java.util.Date;

public class MultiThreadedServerA2 extends JFrame {
	Connection con;
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
			con = (Connection) DriverManager.getConnection(url, user, pass);	// Instantiating the connection object		
		}
		catch(Exception e){
			print("Error: " + e);
		}
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
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
	} // End Server Construct

	private class myClient extends Thread {
		// The socket the client is connected through
		private Socket socket;
		// The ip address of the client
		private InetAddress address;// = socket.getInetAddress();
		// The input and output streams to the client
		private DataInputStream inputFromClient;
		private DataOutputStream outputToClient;

		// The Constructor for the client
		public myClient(Socket socket) throws IOException {
			// Declare & Initialise input/output streams
			address = socket.getInetAddress();
		      // Create data input and output streams
		      inputFromClient = new DataInputStream(
		        socket.getInputStream());
		     outputToClient = new DataOutputStream(
		        socket.getOutputStream());
		}

		/*
		 * The method that runs when the thread starts
		 */
		public void run() {
			boolean authenticated = false;
			String user = "";
			try {
				 while (true) {
					 if(authenticated){
						 double radius = inputFromClient.readDouble();
						 double area = radius * radius * Math.PI;
						 String strArea = String.valueOf(area);
						 // Send area back to the client
						 outputToClient.writeUTF(strArea);
						 
						 jta.append("Client/" + address.getHostName() + "/" + address.getHostAddress() + ": " + radius + '\n');
						 jta.append("Area found: " + area + '\n');				        						 
					 }
					 else{
						 user = inputFromClient.readUTF();
						 ResultSet rs = null;
						 Statement authenticate = con.createStatement();
						 rs = authenticate.executeQuery("SELECT * FROM registeredapplicants WHERE accountnum=" + user);
						 if(rs.next()){
							 authenticated = true;
							 String fname = rs.getString("FirstName"), lname = rs.getString("LastName");				        	
							 print("NAME: " + fname + " " + lname);
							 jta.append("User authenticated: " + fname + " " + lname + '\n');
							 outputToClient.writeUTF("Welcome " + fname + " " + lname);
						 }
						 else{
							 print("User doesn't exist HERE");
							 outputToClient.writeUTF("User doesn't exist");
							 jta.append("User doesn't exist: " + user + '\n');
						 }
						 
					 }
				        // Receive radius from the client
				 }
		    
			} catch (Exception e) {
				System.err.println(e + " on " + socket);
			}
		}
	}
	
	public void print(String print){
		System.out.println(print);
	}
}