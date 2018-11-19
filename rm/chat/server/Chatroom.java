package rm.chat.server;

import java.util.ArrayList;
import java.util.List;

import rm.chat.server.RemoteClient;

/**
 * A room where users can see and chat with each other. Keeps a record of all the
 * users in the room
 */
public class Chatroom {

    private static int ID;

    private int id;
    private String name;
    private List<RemoteClient> users;

    /**
     * Create a new empty chatroom.
     */
    public Chatroom() {
        this.id = getNewId();
        this.name = "Room " + this.id;
        this.users = new ArrayList<RemoteClient>();
    }

    /**
     * Create a new empty chatroom with the given name.
     * 
     * @param name to give the room
     */
    public Chatroom(String name) {
        this.id = getNewId();
        this.name = name;
        this.users = new ArrayList<RemoteClient>();
    }

    /**
     * Increment and return global ID.
     * 
     * @return ID given to the next user
     */
    private int getNewId() {
        return ++ID;
    }

    /**
     * Add a user to this room.
     * 
     * @param user
     */
    public void addUser(RemoteClient user) {
    	users.add(user);
    }

    /**
     * Remove a user to this room.
     * 
     * @param user
     */
    public void removeUser(RemoteClient user) {
    	users.remove(user);
    }

}