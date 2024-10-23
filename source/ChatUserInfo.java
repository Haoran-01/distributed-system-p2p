import java.io.Serializable;
import java.net.InetAddress;

// class to store all information about user
public class ChatUserInfo implements Serializable {
    private volatile InetAddress address;
    private volatile String username;
    private volatile String userID;
    private volatile int port;
    private volatile int serverEndPort;
    private volatile boolean isAdministrator;

    public ChatUserInfo(InetAddress ip, String username, int port, int serverEndPort, boolean isAdministrator){
        this.address = ip;
        this.username = username;
        this.port = port;
        this.serverEndPort = serverEndPort;
        userID = username + ":" + port;
        this.isAdministrator = isAdministrator;
    }

    public boolean isAdministrator() {
        return isAdministrator;
    }

    public void setAdministrator(boolean administrator) {
        isAdministrator = administrator;
    }

    public int getServerEndPort() {
        return serverEndPort;
    }

    public void setServerEndPort(int serverEndPort) {
        this.serverEndPort = serverEndPort;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "ChatUserInfo{" +
                "address=" + address +
                ", username='" + username + '\'' +
                ", userID='" + userID + '\'' +
                ", port=" + port +
                '}';
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
