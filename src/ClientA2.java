/**
 * @author Shane Lacey 20013687
 * @version 1.0.0
 * @date 10/11/16
 */

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientA2 extends JFrame implements ActionListener {
	// Text fields for receiving radius and user a/c number
	private JTextField jtfArea = new JTextField();
	private JTextField jtfUser = new JTextField();
	// Text area to display contents
	private JTextArea jta = new JTextArea();

	// IO streams
	private DataOutputStream toServer;
	private DataInputStream fromServer;

	private String address = ""; // the Inet address of the server

	public static void main(String[] args) {
		new ClientA2();
	}

	public ClientA2() {
		// Panel p to hold the labels and text fields
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2));
		p.add(new JLabel("Radius"), BorderLayout.WEST);
		p.add(jtfArea, BorderLayout.CENTER);
		p.add(new JLabel("A/C number"), BorderLayout.CENTER);
		p.add(jtfUser, BorderLayout.EAST);
		jtfArea.setHorizontalAlignment(JTextField.LEFT); // set text alignment to the left
		jtfUser.setHorizontalAlignment(JTextField.LEFT);

		add(p, BorderLayout.NORTH); // Add the panel to the JFrame
		add(new JScrollPane(jta), BorderLayout.CENTER); // Adding the text area to the JFrame

		jtfArea.addActionListener(this); // Register listener
		jtfUser.addActionListener(this); // Register listener
		jtfArea.setEnabled(false);
		jta.setEditable(false); // disable this so the output cannot be edited

		setTitle("Client");
		setSize(500, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true); // It is necessary to show the frame here!

		try {
			// Create a socket to connect to the server
			Socket socket = new Socket("localhost", 8000);
			address = socket.getInetAddress().toString();
			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException ex) {
			jta.append(ex.toString() + '\n');
		}
	}


	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jtfArea){ // If the event was triggered by jtfArea then do this
			try{
				double radius = 0; // The requested radius
				try{
					radius = Double.parseDouble(jtfArea.getText().trim());	
					jta.append("Radius is " + radius + "\n");
					toServer.writeDouble(radius); // Send the radius to the server
					toServer.flush();
					String res = fromServer.readUTF(); // Get area from the server;
					jta.append("Server/" + address + ": " + res + '\n'); // Display to the text area
					jtfArea.setText(""); // Clear the jtfArea text box
				}
				catch(NumberFormatException nfe){
					jta.append("ERROR: Please enter a number \n");
					jtfArea.setText("");
				}
			}
			catch(IOException ex){
				jta.append("ERROR: " + ex.getMessage() + "\n");
				print(ex.toString());
			}
		}
		else if(e.getSource() == jtfUser){ // If the event was triggered by jtfUser then do this
			try{
				double user = 0;
				try{
					user = Double.parseDouble(jtfUser.getText().trim());
					String strUser = String.valueOf(user); // Make it a string
					toServer.writeUTF(strUser); // Send the a/c number to the server for authentication
					toServer.flush();
					String res = fromServer.readUTF(); // Get response from the server;
					if(res.equals("User doesn't exist")){// If the user doesn't exist then throw an error and let them try again
						print(res);
						jtfUser.setText("");
						jta.append("Server/" + address + ": " + res + ", please try again" + '\n');
					}
					else{
						jta.append("Server/" + address + ": " + res + '\n'); // Display to the text area
						jtfArea.setEnabled(true); // Now allow the user to request the area of a circle
						jtfArea.setText("");
						jtfUser.setEnabled(false); // Stop the user entering another a/c number
					}
				}
				catch(NumberFormatException nfe){
					jta.append("ERROR: Please enter a number \n"); // If it reaches here then the a/c number entered wasn't a double
					jtfUser.setText("");
				}
			}
			catch(IOException ex){
				jta.append("ERROR: " + ex.getMessage() + "\n");
				print(ex.toString());
			}
		}
	}

	public void print(String print){
		System.out.println("CLIENT: " + print);
	}
}