package rm.chat.server;

/**
 * A remote instance of a client. An instance of this class records a player's
 * ID, state and which room the client is in, if any.
 */
public class RemoteClient {

    /**
     * States a Client can be in.
     */
    public static enum State {
        /**
         * User is registered but not inside a room.
         */
        INIT,

        /**
         * User is registered but not inside a room.
         */
        OUTSIDE,

        /**
         * User is currently inside a room.
         */
        INSIDE
    }

    private static int ID;

    private State state;
    private int room;
    private String nick;
    private int id;

    /**
     * Create an instance of a RemoteClient and give it a name Stat initialized to
     * INIT.
     * 
     * @see State#INIT
     */
    public RemoteClient() {
        this.id = getNewId();
        this.state = State.INIT;
        this.nick = "";
        this.room = -1;
    }

    /**
     * Create an instance of a RemoteClient and give it a name State is initialized
     * to OUTSIDE.
     * 
     * @see State#OUTSIDE
     * @param nick Client's username
     */
    public RemoteClient(String nick) {
        this.id = ++ID;
        this.nick = nick;
        this.state = State.OUTSIDE;
    }

    public State getState() {
        return this.state;
    }

    void setState(State state) {
        this.state = state;
    }

    public int getRoom() {
        return this.room;
    }

    void setRoom(int room) {
        this.room = room;
    }

    public String getNick() {
        return this.nick;
    }

    void setNick(String nick) {
        this.nick = nick;
    }

    public int getId() {
        return this.id;
    }

    private int getNewId() {
        return ++ID;
    }
    
    public boolean canChat() {
    	return this.getState() == State.INIT;
    }

}