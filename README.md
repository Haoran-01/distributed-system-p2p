# Design Document

## 1. design





<img src=".\photos\流程图.drawio.svg" alt="流程图.drawio" style="zoom: 50%;" />

My p2p chat application is divided into two main parts, one for the server and the other for the client. Unlike p2s, the server in p2p is only responsible for maintaining the connection to the client and broadcasting messages. My application uses `ServerScoket` and `socket` to establish a TCP connection between the client and the server. The server holds information about all currently connected clients, and when a new client connects or a client disconnects, the server updates the list of online users and broadcasts it to all online users. If a client wants to send a message to another client, it needs to get the latest list of online users broadcast by the server, find the corresponding client port number in the list and establish a connection and send the message.

## 2 Explain the design ideas

### 2.1 GUI

I have used JAVA Swing for the UI design.

**Client Interface**

<img src=".\photos\image-20221114162337430.png" alt="image-20221114162337430" style="zoom: 33%;" />

The user can select the IP address of the server and the server port to connect to the different servers. When the user is connected to the server, the Kick this ID function is only available when the Administrator radio button is pressed, currently non-administrator users are not allowed to use the KICK_ID function. The ID Stats button is used to check the permissions of the selected ID (i.e. the control commands available) and all IDs available for that ID are displayed in the Received Messages section below.

The Broadcast button is for broadcasting a message to all clients connected to the server, while the Send Message button asks for the object you want to send the message to, which must be in the Available Chat Name. The message will be sent to the specified user. The Status button below shows the current status, showing the current client status and the type of error, the Received Message shows all broadcast messages and messages sent by other clients.

**Server-side interface**

<img src=".\photos\image-20221114135109365.png" alt="image-20221114135109365" style="zoom: 33%;" />

The server side can select the port of the server. After starting the server, Current connected users will be updated in real time according to the connection and port of the user side, showing the information of the clients currently connected to the server. The lower area is used to display the messages received by the server.

### 2.2 some data class

- `Message` class for client to server message exchange
- `SendMessage` class for client-to-client message exchange
- `ChatUserInfo` class for storing client specific information
- `UserList` class for storing `ChatUserInfo` objects 

### 2.3 Server class

<img src=".\photos\流程图.drawio-1668444962275-1.svg" alt="流程图.drawio" style="zoom:50%;" />

The server side is divided into a main thread and several sub-threads. The main thread of the server is always waiting and is responsible for accepting new connections from clients. When a new client connects to the server via the server interface, the server creates a new `ChatUserInfo` object to store the client's details, including the user's name, IP address, port number, whether they are an administrator and so on. This object is then stored in a `UserList` object in the main thread. The server then opens a sub-thread to send and listen to this client's information, and stores the port number assigned to the user in a `ChatUserInfo` object to facilitate subsequent control of the different threads. The main thread has a `Vecotr<Scoket> sockets` to store all the sockets of the server and to facilitate the management of the sub-threads. The main thread is responsible for managing the individual sub-threads, making broadcasts, and when a new client connects to the server, the server broadcasts to all connected clients and sends a list of current clients. This facilitates the sending of client-to-client information from client to client.

The server sub-thread is responsible for receiving information from the client and sending messages to the client, continuously listening to the corresponding client when a connection exists, processing the information sent by the client, parsing it and carrying out the corresponding operations. When a message with a kick request is received from the client, it searches for the user id sent and carries out the kick operation, disconnecting the socket of the thread corresponding to the id. When a client disconnects from the server, it also broadcasts to all connected clients to inform them of the disconnection, and updates the client list and broadcasts the updated client list to all online users.

### 2.4 Client Class

<img src=".\photos\P2P.drawio.svg" alt="P2P.drawio" style="zoom:50%;" />

The client is also divided into a main thread and several sub-threads. The main thread is always waiting and is used to receive messages from the server, such as getting a list of all users currently online and being notified of other users coming online and broadcasting messages. When the main thread is started, it also starts another listening thread which listens for input from the console and executes the appropriate commands based on that input.

The client sub-thread is used to listen to messages sent by other clients. In order to implement p2p, the client can communicate directly with other clients, and when the client connects to the server, a `ServerScoket` is created to listen to messages sent by other clients. At this point the client can be seen as a server side. The sub-thread will enter a waiting state, and when other clients send messages, it will disconnect from the other clients after receiving them. When a client wants to send a message to another client, it will first send a request to the server to get a list of the current online users, search for the username to get the port number of the client it wants to connect to, and send the message to that port.

### 2.5 ConsoleListener Class

Use the `ConsoleListener` class to listen for console actions. The `ConsoleListener` class has a `HashMap<String,Action> answer` to store a specific command and the action that this command initiates, with the key being the command entered on the console and the value being the specific `Action`. The `ConsoleListener` class creates a new thread to continuously monitor the console input, uses `Scanner` to read the console input and searches within `answer` to see if the command list contains the console input, if the input is not contained in `answer` the default `Action` is called which is the command that notifies the error format, if the input is contained in `answer`, the `Action` action corresponding to the command will be called. All `Action` actions are automatically created when the client's main thread is created and stored in the `HashMap<String,Action>` of the `ConsoleListener` using the `addAction` function. When the specified command is entered in the console, the `act(line)` function of the corresponding `Action` is called to perform the corresponding action. These `Action.act` actions process the console input, extracting the `{content}` and `ID` from it to perform a specific action. When the client is closed, this client's console listener thread is closed.

All commands are
- `BROADCAST_{content}` Broadcast `content` to all clients
  - This is achieved by calling the `broadcastData()` function in the client class, which extracts `content` as the content of the message sent to the server, and broadcasts it to all online clients via the server 
- `MESSAGE_ID_{content}` sends `content` to the client whose `ID` corresponds to it
  - This is achieved by calling `sendData()` in the client class, extracting the `ID` and `content`, searching for the corresponding port number according to the `ID` and sending the message to it.
- `STOP` breaks the link between the current client and the server
  - Disconnects the client from the server by calling the click function of the GUI interface disconnected.
- ` LIST` Display the user IDs of all clients currently connected to the server
  - Show the list of current users from the last server broadcast message
- `KICK_ID ` disconnects the client corresponding to `ID` from the server
  - This command, available only if the current client is an administrator, sends a message to the server with a kick identifier and the ID of the user you want to kick out. The server reads the ID and disconnects the socket on the port corresponding to that ID. The server will read the ID and disconnect the user from the server.
- `STATS_ID` shows all available commands for the client to which the `ID` corresponds
  - After extracting the ID and searching the list of users sent by the server, find the corresponding `ChatUserInfo` object and display the different available commands by determining whether the user is an administrator or not.

## 3. pros and cons

Advantages.

- High fault tolerance, no single client crash will affect the whole system
- Decentralised, in a P2P network there is no strict distinction between client and server, while each node acts as both client and server. The nodes are equal and any node can notify every node in the network of a message as long as it is connected to the network.

Disadvantages.

- The message object sent from the client to the server is too complex and contains too much content
- Currently this system can only be used within the local LAN