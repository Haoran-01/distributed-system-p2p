import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

class ser extends JFrame implements ActionListener,Runnable {
	Thread thread;
	JButton jbStart, jbStopServer;
	JTextField jtfEnterPort;
	JLabel statusLabel, userListLabel;
	TextArea taServerMessage, taUserList;

	UserList userList;
	Vector<ChatUserInfo> userInfos;
	ServerSocket server;
	Message msg = new Message();
	Vector<Socket> sockets;

	// UI interface buttons and input boxes
	ser(String s) {
		super(s);

		JLabel enterPortLabel = new JLabel("Enter Port No. : ");
		add(enterPortLabel);

		// 端口名
		jtfEnterPort = new JTextField(7);
		jtfEnterPort.setText("5000");
		add(jtfEnterPort);

		JLabel startServerLabel = new JLabel("Start the Server");
		add(startServerLabel);

		// 启动按钮
		jbStart = new JButton("Start");
		jbStart.addActionListener(this);
		add(jbStart);

		JLabel stopServerLabel = new JLabel("Stop the Server");
		add(stopServerLabel);

		// 结束按钮
		jbStopServer = new JButton("Stop");
		jbStopServer.addActionListener(this);
		add(jbStopServer);
		jbStopServer.setEnabled(false);

		userListLabel = new JLabel("Current connected users");
		add(userListLabel);

		taUserList = new TextArea("", 5, 70);
		taUserList.setEditable(false);
		taUserList.setBackground(Color.WHITE);
		taUserList.setFont(Font.getFont("verdana"));
		add(taUserList);

		JLabel statusLabel = new JLabel("Status : ");
		add(statusLabel);

		add(new JLabel("    "));


		// Show current server status
		this.statusLabel = new JLabel("Server is not running...");
		add(this.statusLabel);

		taServerMessage = new TextArea("", 15, 70);
		taServerMessage.setEditable(false);
		taServerMessage.setBackground(Color.WHITE);
		taServerMessage.setFont(Font.getFont("verdana"));
		add(taServerMessage);


		myWindowAdapter a = new myWindowAdapter(this);
		addWindowListener(a);
		userList = UserList.getInstance();	}


	public void actionPerformed(ActionEvent ae) {
		try {
			String str = ae.getActionCommand();

			// Server receives start command
			if (str.equals("Start")) {
				String str2 = jtfEnterPort.getText();
				if (!str2.equals("")) {
					try {
						server = new ServerSocket(Integer.parseInt(str2));
						statusLabel.setText("Server is running....");
						jtfEnterPort.setEnabled(false);

						jbStart.setEnabled(false);
						jbStopServer.setEnabled(true);

						// Store all connected sockets
						sockets = new Vector<>();

						thread = new Thread(this, "Running");
						thread.start();

					} catch (Exception e) {
						statusLabel.setText("Either the port no. is invalid or is in use");
					}
				} else
					statusLabel.setText("Enter port no.");
			}
			// Server receives termination command
			if (str.equals("Stop")) {
				try {
					server.close();

				} catch (Exception ee) {
					statusLabel.setText("Error closing server");
				}
				statusLabel.setText("Server is closed");
				jtfEnterPort.setEnabled(true);
				jbStart.setEnabled(true);
				jbStopServer.setEnabled(false);

				server = null;
				thread = null;

				// Close all clients
				for (Socket socket : sockets){
					try {
						socket.close();
					} catch (Exception e){

					}
				}
			}
		} catch (Exception ex) {
		}
	}

	public void run() {
		while (true) {
			// Stop the while loop when the server is shut down
			if (server.isClosed()) {
				// Clear the registration list
				userList.clearUserList();
				return;
			}
			try {
				//  Get client
				Socket client = server.accept();

				// get client input data, including user id and user message
				ObjectInputStream obj = new ObjectInputStream(client.getInputStream());

				// accept the request
				msg = (Message) obj.readObject();
				userList.addInstance(msg.senderID, msg.address, msg.port, client.getPort(), msg.isAdministrator);

				// Exporting a list of users
				userInfos = userList.getUserList();

				// show the message
				taServerMessage.append(msg.senderID + " >> " + msg.msgText + "\n");
				msg.userInfos = userInfos;

				sockets.add(client);

				// Display of messages sent by users within the server, broadcast
				for (Socket socket : sockets){
					try {
						ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
						objectOutputStream.writeObject(msg);
					} catch (Exception ex){

					}
				}

				taUserList.setText("");
				for (ChatUserInfo chatUserInfo: userInfos){
					taUserList.append("user name: " + chatUserInfo.getUsername() + "     port: "
							+ chatUserInfo.getPort() + "\n");
				}

				// Create a new thread
				new newThread(client, msg, sockets, this, server, userList);

			} catch (Exception e) {
				//  Restore the displayed UI
				statusLabel.setText("Server is stopped");
				jtfEnterPort.setEnabled(true);
				try {
					server.close();
				} catch (Exception ey) {
					statusLabel.setText("Error closing server");
				}
			}
		}
	}
}

class startServer{
	public static void main(String a[]) throws IOException {
		// 展示页面, s: 页面标题
		ser f = new ser("p2p ChatSystem Server");
		f.setLayout(new FlowLayout());
		f.setSize(550, 470);
		f.setResizable(false);
		f.setVisible(true);
	}
}

class newThread implements Runnable {
	Thread t;
	Socket client;
	Message msg;
	Vector<Socket> sockets;
	ser f;
	ServerSocket server;
	Vector<ChatUserInfo> userInfos;
	UserList userList;

	newThread(Socket client, Message msg, Vector<Socket> sockets, ser f, ServerSocket server, UserList userList) {
		t = new Thread(this, "Client");
		this.server = server;
		this.client = client;
		this.msg = msg;
		this.f = f;
		this.sockets = sockets;
		this.userList = userList;
		t.start();
	}

	public void run() {
		String name = msg.senderID;
		try {
			while (!server.isClosed()) {

				// Initialisation
				ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());

				// Receive client messages, Message
				msg = (Message) objectInputStream.readObject();

				// Execute the kick out user command
				if (msg.senderID != null && msg.msgText != null && msg.msgText.equals("kick")) {
					if (userList.isUserInside(msg.senderID)){
						int kickPort = userList.getUserInfo(msg.senderID).getServerEndPort();
						f.taServerMessage.append(msg.senderID + " has been kicked" + "\n");
						for (Socket socket : sockets){
							if (socket.getPort() == kickPort){
								socket.close();
							}
						}
					}
				} else if (msg.senderID != null && msg.msgText != null){
					f.taServerMessage.append(msg.senderID + " >> " + msg.msgText + "\n");
					name = msg.senderID;
					// Broadcast off-line notice
					for (Socket socket : sockets){
						try {
							ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
							objectOutputStream.writeObject(msg);
						} catch (Exception ex){

						}
					}
				}
			}
			// Close all sockets
			if (server.isClosed()) {
				for (Socket socket : sockets){
					try {
						socket.close();
					}catch (Exception ex){

					}
				}
			}
		} catch (Exception e) {
			f.taServerMessage.append("User: " + name + " is offline\n");
			try {
				msg.msgText = " is offline";
				// Synchronisation when users log out
				UserList.getInstance().removeInstance(msg.senderID);
				userInfos = UserList.getInstance().getUserList();
				msg.userInfos = userInfos;

				// Update server-side online customer display
				f.taUserList.setText("");
				for (ChatUserInfo chatUserInfo : userInfos){
					f.taUserList.append("user name: " + chatUserInfo.getUsername() + "     port: "
							+ chatUserInfo.getPort() + "\n");
				}

				for (Socket socket : sockets){
					try {
						ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
						objectOutputStream.writeObject(msg);
					} catch (Exception ex){

					}
				}
				client.close();
			} catch (Exception ex) {

			}
		}
	}
}


class myWindowAdapter extends WindowAdapter {
	ser f;

	public myWindowAdapter(ser j) {
		f = j;
	}

	public void windowClosing(WindowEvent we) {
		f.setVisible(false);
		try {
			f.server.close();
		} catch (Exception e) {
		}
		f.dispose();
		System.exit(0);
	}
}