package rm.chat.server;

import rm.chat.server.RemoteClient;

/**
 * A room where users can see and chat with eachother. Keeps a record of all the
 * users in the room
 */
public class Chatroom {

    private static int ID;

    private int id;
    private String name;
    private List<Client> users;

    /**
     * Create a new empty chatroom.
     */
    public Chatroom() {
        this.id = getNewId();
        this.name = "Room " + this.id;
        this.users = new ArrayList<Client>();
    }

    /**
     * Create a new empty chatroom with the given name.
     * 
     * @param name to give the room
     */
    public Chatroom(String name) {
        this.id = getNewId();
        this.name = name;
        this.users = new ArrayList<Client>();
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
     * @param id of the user
     */
    public void addUser(int id) {

    }

    /**
     * Remove a user to this room.
     * 
     * @param id of the user
     */
    public void removeUser(int id) {

    }

}