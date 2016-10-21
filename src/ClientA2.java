/**
 * @author Shane Lacey 20013687
 * @version 1.0.0
 * @date 21/10/16
 */

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientA2 extends JFrame {
	// Text field for receiving radius
	private JTextField jtfArea = new JTextField();
	private JTextField jtfUser = new JTextField();

	// Text area to display contents
	private JTextArea jta = new JTextArea();

	// IO streams
	private DataOutputStream toServer;
	private DataInputStream fromServer;

	private boolean authenticated = false;
	private double radius = 0;
	private String address = "";

	public static void main(String[] args) {
		new ClientA2();
	}

	public ClientA2() {
		// Panel p to hold the label and text field
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2));
		p.add(new JLabel("Radius"), BorderLayout.WEST);
		p.add(jtfArea, BorderLayout.CENTER);
		p.add(new JLabel("A/C number"), BorderLayout.CENTER);
		p.add(jtfUser, BorderLayout.EAST);
		jtfArea.setHorizontalAlignment(JTextField.LEFT);
		jtfUser.setHorizontalAlignment(JTextField.LEFT);

		setLayout(new BorderLayout());
		add(p, BorderLayout.NORTH);
		add(new JScrollPane(jta), BorderLayout.CENTER);

		jtfArea.addActionListener(new Listener()); // Register listener
		jtfUser.addActionListener(new Listener()); // Register listener
		jtfArea.setEnabled(false);

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

	private class Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				boolean isANumber = false;
				if(authenticated){
					double radius = 0;
					try{
						radius = Double.parseDouble(jtfArea.getText().trim());	
						isANumber = true;
						jta.append("Radius is " + radius + "\n");
						// Send the radius to the server
						toServer.writeDouble(radius);
						toServer.flush();
					}
					catch(NumberFormatException nfe){
						isANumber = false;
						jta.append("ERROR: Please enter a number \n");
					}
				}
				else{
					double user = 0;
					try{
						user = Double.parseDouble(jtfUser.getText().trim());
						isANumber = true;
						String strUser = String.valueOf(user);
						toServer.writeUTF(strUser);
						toServer.flush();    		  
					}
					catch(NumberFormatException nfe){
						jta.append("ERROR: Please enter a number \n");
					}
				}

				if(isANumber){
					// Get area from the server;
					String res = fromServer.readUTF();
					if(res.contains("Welcome")){
						authenticated = true;
						jtfArea.setEnabled(true);
					}
					
					if(res.equals("User doesn't exist")){
						print(res);
						jta.append("Server/" + address + ": " + res + ", please try again" + '\n');
					}
					else{
						// Display to the text area
						jta.append("Server/" + address + ": " + res + '\n');
						jtfArea.setText("");
						jtfUser.setEnabled(false);        	
					}
				}
			}
			catch (IOException ex) {
				System.err.println(ex);
			}
		}
	}

	public void print(String print){
		System.out.println(print);
	}
}