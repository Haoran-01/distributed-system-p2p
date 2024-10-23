import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

// client send the message for server
class Message implements Serializable {
	public volatile String senderID;
	public volatile String msgText;
	public volatile int port;
	public volatile InetAddress address;
	public volatile Vector<ChatUserInfo> userInfos;
	public volatile boolean isAdministrator;
}

// client send the sendMessage for other client
class SendMessage implements Serializable {
	public volatile String senderID;
	public volatile String msgText;
	public volatile String receiverID;
	public volatile int port;
	public volatile int chatPort;
	public volatile InetAddress address;
}


class client extends JFrame implements ActionListener,Runnable {
	Socket socket = null;
	Socket chatSocket = null;
	ServerSocket serverSocket;
	JLabel userNameLabel, messageLabel, statusLabel, receivedMessageLabel, serverIPLabel, serverPortLabel, jtfStatus, userPortLabel, availableChatNameLabel, chatNameLabel;
	JTextField jtfUserName, jtfMessage, jtfServerIP, jtfServerPort, jtfUserPort, jtfChatName;
	JRadioButton jrbAdministrator;
	TextArea taReceived, taAvailableChatName;
	Vector<ChatUserInfo> chatUserInfos;
	Message msg = new Message();
	SendMessage sendMessage = new SendMessage();
	InetAddress host;
	Thread thread = null;
	JButton jbSendMessage, jbConnect, jbDisconnect, jbBroadcast, jbKickID, jbStatsID;
	boolean isClick = false;

	// GUI
	client(String s) {
		super(s);
		
		myAdapter a = new myAdapter(this);
		addWindowListener(a);
		
		serverIPLabel = new JLabel("Server IP : ");
		add(serverIPLabel);
		jtfServerIP = new JTextField(15);
		add(jtfServerIP);
		jtfServerIP.setText("127.0.0.1");
		
		serverPortLabel = new JLabel("Server Port : ");
		add(serverPortLabel);
		jtfServerPort = new JTextField(15);
		add(jtfServerPort);
		jtfServerPort.setText("5000");
		
		userNameLabel = new JLabel("User Name :  ");
		add(userNameLabel);
		jtfUserName = new JTextField(15);
		add(jtfUserName);
		jtfUserName.setText("dddd");

		userPortLabel = new JLabel("User Port :  ");
		add(userPortLabel);
		jtfUserPort = new JTextField(15);
		add(jtfUserPort);
		jtfUserPort.setText("4000");
		
		jbConnect = new JButton("Connect");
		add(jbConnect);
		jbConnect.addActionListener(this);
		
		jbDisconnect = new JButton("Disconnect");
		add(jbDisconnect);
		jbDisconnect.addActionListener(this);
		jbDisconnect.setEnabled(false);

		jrbAdministrator = new JRadioButton("Administrator");
		add(jrbAdministrator);
		jrbAdministrator.addActionListener(this);


		availableChatNameLabel = new JLabel("Available Chat Name : ");
		add(availableChatNameLabel);
		taAvailableChatName = new TextArea("",5,30);
		add(taAvailableChatName);
		taAvailableChatName.setFont(Font.getFont("verdana"));
		taAvailableChatName.setBackground(Color.ORANGE);
		taAvailableChatName.setEditable(false);

		chatNameLabel = new JLabel("Chat Name");
		add(chatNameLabel);
		jtfChatName = new JTextField(34);
		add(jtfChatName);
		jtfChatName.setEditable(false);

		messageLabel = new JLabel("Message : ");
		add(messageLabel);
		jtfMessage = new JTextField(34);
		add(jtfMessage);
		jtfMessage.setEditable(false);
					
		jbSendMessage = new JButton("Send Message");
		add(jbSendMessage);
		jbSendMessage.addActionListener(this);
		jbSendMessage.setEnabled(false);

		jbBroadcast = new JButton("Broadcast");
		add(jbBroadcast);
		jbBroadcast.addActionListener(this);
		jbBroadcast.setEnabled(false);

		jbKickID = new JButton("Kick this ID");
		add(jbKickID);
		jbKickID.addActionListener(this);
		jbKickID.setEnabled(false);

		jbStatsID = new JButton("ID Stats");
		add(jbStatsID);
		jbStatsID.addActionListener(this);
		jbStatsID.setEnabled(false);
		
		statusLabel = new JLabel("Status : ");
		add(statusLabel);
		jtfStatus = new JLabel("Not connected to the server...");
		add(jtfStatus);
				
		receivedMessageLabel = new JLabel("Recieved Messages : ");
		add(receivedMessageLabel);
		taReceived = new TextArea("",15,80);
		add(taReceived);
		taReceived.setFont(Font.getFont("verdana"));
		taReceived.setBackground(Color.ORANGE);
		taReceived.setEditable(false);
		
		jtfStatus.setText("Not connected to Server, click connect");


		// layout
		serverIPLabel.setBounds(50,20,80,20);
		jtfServerIP.setBounds(120, 20, 100, 20);
		userNameLabel.setBounds(50, 60, 80, 20);
		jtfUserName.setBounds(120, 60, 100, 20);
		serverPortLabel.setBounds(310, 20, 80, 20);
		jtfServerPort.setBounds(390,20,100,20);
		userPortLabel.setBounds(310,60,80,20);
		jtfUserPort.setBounds(390,60,100,20);
		jbConnect.setBounds(270, 100, 100, 30);
		jbDisconnect.setBounds(390, 100, 100, 30);
		availableChatNameLabel.setBounds(50, 150, 150, 20);
		taAvailableChatName.setBounds(50, 180, 200, 150);
		chatNameLabel.setBounds(310, 200, 80, 20);
		jtfChatName.setBounds(390, 200, 100, 20);
		jbKickID.setBounds(310, 240, 180, 30);
		jbStatsID.setBounds(310, 290, 180, 30);
		messageLabel.setBounds(50, 350, 80, 30);
		jtfMessage.setBounds(120, 350, 370,30);
		jbBroadcast.setBounds(230, 390, 120,30);
		jbSendMessage.setBounds(370,390,120,30);
		statusLabel.setBounds(50, 430,80,20);
		jtfStatus.setBounds(120,430, 370,20);
		receivedMessageLabel.setBounds(50,460,150,20);
		taReceived.setBounds(50,490,430,200);
		jrbAdministrator.setBounds(50, 105, 120, 20);
	}
	
	public void actionPerformed(ActionEvent ae) {
		try{
			String str = ae.getActionCommand();

			if (str.equals("Administrator")){
				isClick = !isClick;
				msg.isAdministrator = isClick;
			}

			// Click on the Connect button
			if(str.equals("Disconnect")) {
				try {
					jrbAdministrator.setEnabled(true);
					jbKickID.setEnabled(false);
					jbBroadcast.setEnabled(false);
					jbSendMessage.setEnabled(false);
					jbStatsID.setEnabled(false);
					jtfMessage.setEditable(false);
					jbConnect.setEnabled(true);
					jbDisconnect.setEnabled(false);
					jtfServerIP.setEditable(true);
					jtfServerPort.setEditable(true);
					jtfUserName.setEditable(true);
					jtfChatName.setEditable(false);
					jtfUserPort.setEditable(true);
					taAvailableChatName.setText("");
					taReceived.append("User: " + jtfUserName.getText() + " >> " + "is offline" + "\n");
					System.out.println("User: " + jtfUserName.getText() + " >> " + "is offline");
					serverSocket.close();
					socket.close();
					socket = null;					
				}
				catch(Exception ignored) {

				}
				serverSocket = null;
				thread = null;
			}

			// click the kick this id button
			if (str.equals("Kick this ID")){
				// Here senderID is the ID you want to delete
				msg.senderID = jtfChatName.getText();
				msg.msgText = "kick";

				if (!msg.senderID.equals("")){
					kickID();
				} else jtfStatus.setText("Please enter the kick name");
			}

			if (str.equals("ID Stats")){
				if (!jtfChatName.getText().equals("")){
					statsID();
				} else {
					jtfStatus.setText("Please enter the chat name");
				}
			}

			// click the broadcast button, Messages are broadcast to every client connected to the server
			if (str.equals("Broadcast")) {
				msg.senderID = jtfUserName.getText();
				msg.msgText = jtfMessage.getText();

				jtfMessage.setText("");
				broadcastData();
			}

			// click the send message button, The message is sent directly to the specified client
			if(str.equals("Send Message")) {
				sendMessage.receiverID = jtfChatName.getText();
				sendMessage.senderID = jtfUserName.getText();
				sendMessage.msgText = jtfMessage.getText();

				sendData();
				}

			// Connecting to the server
			if(str.equals("Connect")) {
				try {
					if (!jtfUserPort.getText().equals("") && !jtfUserName.getText().equals("") && !jtfServerIP.getText().equals("") && !jtfServerPort.getText().equals("")){
						try{
							serverSocket = new ServerSocket(Integer.parseInt(jtfUserPort.getText()));
							host = InetAddress.getByName(jtfServerIP.getText()); // ip
							String p = jtfServerPort.getText(); // port

							try{
								if(socket!=null)
								{
									socket.close();
									socket = null;
								}
							} catch(Exception ignored) {}

							// username
							if(!jtfUserName.getText().equals("") && !jtfUserPort.getText().equals("")) {
								socket = new Socket(host,Integer.parseInt(p));

								ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());

								// put the data into message
								msg.senderID = jtfUserName.getText();
								msg.msgText = " is now online at " + new Date();
								msg.address = InetAddress.getLocalHost();
								msg.port = Integer.parseInt(jtfUserPort.getText());
								obj.writeObject(msg);

								// Reset interaction page
								jtfMessage.setEditable(true);
								jbSendMessage.setEnabled(true);
								jbBroadcast.setEnabled(true);
								jbConnect.setEnabled(false);
								jbDisconnect.setEnabled(true);
								jtfServerIP.setEditable(false);
								jtfServerPort.setEditable(false);
								jtfUserPort.setEditable(false);
								jtfUserName.setEditable(false);
								jtfChatName.setEditable(true);
								jbStatsID.setEnabled(true);
								jrbAdministrator.setEnabled(false);
								jbKickID.setEnabled(isClick);

								jtfStatus.setText("Connection established with Server, start chatting");

								thread = new Thread(this,"Reading");
								thread.start();

								// Listening for console input
								ConsoleListener cs = new ConsoleListener(new Scanner(System.in), message -> System.out.println("Please enter the correct instructions"));

								// create the consoleListener action
								cs.addAction("BROADCAST", message -> {
									msg.senderID = jtfUserName.getText();
									msg.msgText = getContent(message);
									broadcastData();
								});

								cs.addAction("MESSAGE", message -> {
									sendMessage.senderID = jtfUserName.getText();
									sendMessage.receiverID = getID(message);
									sendMessage.msgText = getContent(message);
									sendData();
								});

								cs.addAction("STOP", msg -> jbDisconnect.doClick());

								cs.addAction("LIST", msg -> {
									for (ChatUserInfo chatUserInfo : chatUserInfos){
										System.out.println("User ID: " + chatUserInfo.getUserID());
									}
								});

								cs.addAction("KICK", message -> {
									msg.senderID = getID(message);
									msg.msgText = "kick";
									if (!msg.senderID.equals("") && msg.isAdministrator){
										kickID();
									} else {
										System.out.println("Please enter the correct instructions");
									}
								});

								cs.addAction("STATS", message -> {
									jtfChatName.setText(getID(message));
									statsID();
								});

								cs.consoleListenerThread();
								new ClientListener(sendMessage, this, serverSocket);
							}
						} catch(Exception e) {
							jtfStatus.setText("Could not connect to Server, connect again");
						}
					} else if (jtfServerIP.getText().equals("")) {
						jtfStatus.setText("Please type in the server ip");
					} else if (jtfServerPort.getText().equals("")) {
						jtfStatus.setText("Please type in the server port");
					} else if (jtfUserName.getText().equals("")) {
						jtfStatus.setText("Please type in the server port");
					} else if (jtfUserPort.getText().equals("")) {
						jtfStatus.setText("Please type in the server port");
					}
				} catch (Exception ignored){
				}
			}
			} catch(Exception e) {
				jtfStatus.setText("Action Error");
			}
	}

	// get the content from the {}
	public String getContent(String message){
		return message.substring(message.indexOf("{") + 1, message.indexOf("}"));
	}

	// get the user ID from the input command
	public String getID(String message){
		String[] strings = message.split("_");
		return strings[1];
	}

	//  gets a list of all commands used by the client identified by the ID
	public void statsID() {
		boolean isInside = false;
		ChatUserInfo temp = null;
		for (ChatUserInfo chatUserInfo: chatUserInfos){
			if (Objects.equals(chatUserInfo.getUsername(), jtfChatName.getText())){
				isInside = true;
				temp = chatUserInfo;
			}
		}

		if (isInside){
			if (temp.isAdministrator()){
				taReceived.append("All command that " + jtfChatName.getText() + " can used:" + "\n" + "1. BROADCAST_{content}" + "\n" +"2. MESSAGE_ID_{content}" + "\n" + "3. STOP"
						+ "\n" + "4. LIST" + "\n" + "5. KICK_ID" + "\n" + "6. STATS_ID" + "\n");
				System.out.println("All command that " + jtfChatName.getText() + " can used:" + "\n" + "1. BROADCAST_{content}" + "\n" +"2. MESSAGE_ID_{content}" + "\n" + "3. STOP"
						+ "\n" + "4. LIST" + "\n" + "5. KICK_ID" + "\n" + "6. STATS_ID" + "\n");
			} else {
				taReceived.append("All command that " + jtfChatName.getText() + " can used:" + "\n" + "1. BROADCAST_{content}" + "\n" +"2. MESSAGE_ID_{content}" + "\n" + "3. STOP"
						+ "\n" + "4. LIST" + "\n" + "5. STATS_ID" +"\n");
				System.out.println("All command that " + jtfChatName.getText() + " can used:" + "\n" + "1. BROADCAST_{content}" + "\n" +"2. MESSAGE_ID_{content}" + "\n" + "3. STOP"
						+ "\n" + "4. LIST" + "\n" + "5. STATS_ID" +"\n");
			}
		} else {
			System.out.println("Please choose an available chat id");
			jtfStatus.setText("Please choose an available chat id" + "\n");
		}
	}

	// broadcast the data
	public void broadcastData() {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			if(!msg.senderID.equals("") && !msg.msgText.equals("")) {
				objectOutputStream.writeObject(msg);
				jtfStatus.setText("Message was sent successfully");
				System.out.println("Message was sent successfully");
			} else {
				jtfStatus.setText("Message was not sent, type a message");
				System.out.println("Message was not sent, type a message");
			}
			msg.senderID = "";
			msg.msgText = "";
		}
		catch(Exception e) {
			jtfStatus.setText("Error occurred while broadcast message");
		}
	}

	// send the data to other client
	public void sendData() {
		try {
			// get the id port
			Boolean isInside = false;
			for (ChatUserInfo chatUserInfo: chatUserInfos){
				if (Objects.equals(chatUserInfo.getUsername(), sendMessage.receiverID)){
					sendMessage.chatPort = chatUserInfo.getPort();
					isInside = true;
				}
			}

			if (isInside){
				chatSocket = new Socket(InetAddress.getLocalHost(), sendMessage.chatPort);
				ObjectOutputStream obj = new ObjectOutputStream(chatSocket.getOutputStream());

				if (sendMessage.receiverID.equals(jtfUserName.getText())){
					jtfStatus.setText("You can't send a message to yourself");
					System.out.println("You can't send a message to yourself");
				} else if(!sendMessage.receiverID.equals("") && !sendMessage.msgText.equals("")) {
					obj.writeObject(sendMessage);
					taReceived.append("User: " + sendMessage.senderID + " >> " + sendMessage.receiverID + " " + sendMessage.msgText + "\n");
					System.out.println("User: " + sendMessage.senderID + " >> " + sendMessage.receiverID + " " + sendMessage.msgText);
					sendMessage.receiverID = "";
					sendMessage.msgText = "";
					jtfMessage.setText("");
					jtfStatus.setText("Message was sent successfully");
					System.out.println("Message was sent successfully");
				} else {
					jtfStatus.setText("Message was not sent, type a message");
					System.out.println("Message was not sent, type a message");
				}
				chatSocket.close();
			} else {
				jtfStatus.setText("Please choose one available chat name");
				System.out.println("Please choose one available chat name");
			}

		}
		catch(Exception e) {
			jtfStatus.setText("Error occurred while sending message");
		}
	}

	// kick id function
	public void kickID() {
		try {
			boolean inSide = false;
			for (ChatUserInfo chatUserInfo : chatUserInfos){
				if (chatUserInfo.getUsername().equals(msg.senderID)){
					inSide = true;
				}
			}
			if (inSide) {
				// 建立与服务器通信
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				objectOutputStream.writeObject(msg);
				jtfStatus.setText("Kick successful");
				System.out.println("Kick successful");
			} else {
				jtfStatus.setText("Please choose an available chat name");
				System.out.println("Please choose an available chat name");
			}

			msg.senderID = "";
			msg.msgText = "";

		} catch (Exception ex){
			jtfStatus.setText("Kick this ID error");
		}
	}

	public void run() {
			try {
				while(true){
					ObjectInputStream obj = new ObjectInputStream(socket.getInputStream());
					Message msg = new Message();
					msg = (Message) obj.readObject();
					if(msg.senderID!=null && msg.msgText!=null){
						taReceived.append("User: " + msg.senderID+" >> "+msg.msgText+"\n");
						System.out.println("User: " + msg.senderID+" >> "+msg.msgText+"\n");
					}


					// show all online clients
					if (msg.userInfos!=null && msg.userInfos.size()!=0){
						taAvailableChatName.setText("");
						chatUserInfos = msg.userInfos;
						for (ChatUserInfo chatUserInfo : msg.userInfos){
							taAvailableChatName.append("user name: " + chatUserInfo.getUsername() + "     port: "
									+ chatUserInfo.getPort() + "\n");
						}
					}
				}
			} catch(Exception e) {
				try {
					serverSocket.close();
					taAvailableChatName.setText("");
				} catch (Exception ex){

				}
				jtfMessage.setEditable(false);
				jbConnect.setEnabled(true);
				jbSendMessage.setEnabled(false);
				jbDisconnect.setEnabled(false);
				jbBroadcast.setEnabled(false);
				jbKickID.setEnabled(false);
				jtfChatName.setEditable(false);
				jtfUserPort.setEditable(true);
				jtfServerIP.setEditable(true);
				jtfServerPort.setEditable(true);
				jtfUserName.setEditable(true);
				jtfStatus.setText("Connection Lost");
			}
		}

	}

class startClient {
	public static void main(String a[]) {
		client f = new client("p2p ChatSystem");
		f.setLayout(null);
		f.setSize(560,800);
		f.setResizable(false);
		f.setVisible(true);
	}
}

// Listening for information about other threads
class ClientListener implements Runnable {
	Thread thread;
	SendMessage sendMessage;
	client client;
	ServerSocket serverSocket;

	// Listening to messages sent by other clients
	ClientListener(SendMessage sendMessage,client client, ServerSocket serverSocket){
		thread = new Thread(this, "Running");
		this.serverSocket = serverSocket;
		this.sendMessage = sendMessage;
		this.client = client;
		thread.start();
	}

	// start the thread
	@Override
	public void run() {
		while (true) {
			if (serverSocket.isClosed()){
				return;
			}

			try {
				// get connect client
				Socket client2 = serverSocket.accept();

				// get the data send from connect client
				ObjectInputStream objectInputStream = new ObjectInputStream(client2.getInputStream());

				sendMessage = (SendMessage) objectInputStream.readObject();

				client.taReceived.append("User: " + sendMessage.senderID + " >> yourself" + " message: " + sendMessage.msgText + "\n");
				System.out.println("User: " + sendMessage.senderID + " >> yourself" + " message: " + sendMessage.msgText + "\n");

				client2.close();
			} catch (Exception ignored){

			}
		}
	}
}

class myAdapter extends WindowAdapter {
	client f;
	public myAdapter(client j)
	{
		f = j;
	}
	public void windowClosing(WindowEvent we) {
		f.setVisible(false);
		try{
			f.socket.close();
			f.dispose();
		} catch(Exception e) {}
		System.exit(0);
	}
}