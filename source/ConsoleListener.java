import java.util.HashMap;
import java.util.Scanner;

public class ConsoleListener {
    HashMap<String, Action> answer = new HashMap<String, ConsoleListener.Action>();
    Scanner scanner;
    Action defaultAction;

    public ConsoleListener(Scanner scanner, Action defaultAction){
        this.scanner = scanner;
        this.defaultAction = defaultAction;

        if (scanner == null || defaultAction == null) {
            throw new NullPointerException("null params for ConsoleListener");
        }
    }

    // add Action
    public void addAction(String message, Action action){
        answer.put(message.toLowerCase(), action);
    }

    // remove Action
    public void removeAction(String message, Action action){
        answer.remove(message, action);
    }

    // replace Action
    public Action replaceAction(String message, Action action){
        return answer.replace(message, action);
    }

    // Listener function
    public void listen(){
        while (true){
            String lien = scanner.nextLine();
            String msg = lien.replaceAll("[\\s]+", "");
            msg = msg.trim().toLowerCase();
            msg = msg.split("_")[0];

            Action action = answer.get(msg);

            if (action == null){
                action = defaultAction;
            }
            action.act(lien);
        }
    }

    // create a new listener thread
    public void consoleListenerThread(){
        Thread thread = new Thread() {
            public void run(){
                listen();
            }
        };
        thread.start();
    }

    public static interface Action {
        public void act(String msg);
    }
}
