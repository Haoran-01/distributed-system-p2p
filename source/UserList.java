import java.net.InetAddress;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

// a class in order to store all ChatUserInfo object
public class UserList {

    private static UserList instance;

    private HashMap<String, ChatUserInfo> userList;

    private UserList(){
        userList = new HashMap<>();
    }

    // get UserList instance
    public static UserList getInstance(){
        if (instance == null) {
            instance = new UserList();
        }
        return instance;
    }

    // clear all the userList
    public void clearUserList(){
        userList.clear();
    }

    // Get the user information of the corresponding user
    public ChatUserInfo getUserInfo(String username){
        return userList.get(username);
    }

    // Query if a user exists in the user list
    public boolean isUserInside(String username){
        return userList.containsKey(username);
    }

    // Adding a user instance
    public synchronized void addInstance(String username, InetAddress address, int port, int serverEndPort, boolean isAdministrator){
        if (!userList.containsKey(username)){
            userList.put(username, new ChatUserInfo(address, username, port, serverEndPort, isAdministrator));
        }
    }

    // Delete user instance
    public void removeInstance(String username){
        userList.remove(username);
    }

    // Get user ip address
    public InetAddress getUserAddress(String username){
        if (userList.containsKey(username)){
            return userList.get(username).getAddress();
        } else {
            return null;
        }
    }

    // get user id
    public String getUserID(String username){
        if (userList.containsKey(username)){
            return userList.get(username).getUserID();
        } else {
            return null;
        }
    }

    // get user list
    public Vector<ChatUserInfo> getUserList(){
        Vector<ChatUserInfo> temp = new Vector<ChatUserInfo>();
        for (ChatUserInfo userInfo : userList.values()){
            temp.addElement(userInfo);
        }
        return temp;
    }

    // search for the user
    public Vector<ChatUserInfo> searchUserList(String userInput){
        if (Objects.equals(userInput, "")){
            return getUserList();
        } else {
            Vector<ChatUserInfo> temp = new Vector<ChatUserInfo>();
            for (ChatUserInfo userInfo : userList.values()){
                if (userInfo.getUsername().contains(userInput)){
                    temp.addElement(userInfo);
                }
            }
            return temp;
        }
    }

}
