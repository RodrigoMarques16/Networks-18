package rm.chat.server;

import rm.chat.shared.Message;
import rm.chat.shared.Message.MessageType;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.io.IOException;
import java.nio.ByteBuffer;

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
    private String room;
    private String nick;
    private int id;
    private SelectionKey key;

    /**
     * Create an instance of a RemoteClient and give it a name Stat initialized to
     * INIT.
     * 
     * @see State#INIT
     */
    public RemoteClient(SelectionKey key) {
        this.id     = getNewId();
        this.state  = State.INIT;
        this.nick   = "";
        this.room   = null;
        this.key    = key;
    }

    public State getState() {
        return this.state;
    }

    void setState(State state) {
        this.state = state;
    }

    public String getRoom() {
        return this.room;
    }

    void setRoom(String room) {
        this.room = room;
    }

    public String getNick() {
        return this.nick;
    }

    void setNick(String nick) {
        this.nick = nick;
        if (this.state == State.INIT)
            this.state = State.OUTSIDE;
    }

    public int getId() {
        return this.id;
    }

    public SelectionKey getKey() {
        return this.key;
    }

    /**
     * Increment the global ID and return it
     * 
     * @return: Next ID
     */
    private int getNewId() {
        return ++ID;
    }
    
    /**
     * Test if the user can chat, which is when
     * the user is inside a room
     * @see @State#INIT
     * @return True if the client can chat
     */
    public boolean canChat() {
    	return this.getState() == State.INSIDE;
    }

    /**
     * Report the result of a user's command.
     * 
     * If the result is TRUE then send the client an
     * OK message.
     * 
     * If the result is FALSE send the client an
     * ERROR message.
     * 
     * @param result - True or False
     * @throws IOException
     */
    public void reportResult(boolean result) throws IOException{
        if (result == true)
            this.sendOK();
        else 
            this.sendERROR();
    }

    /**
     * Send an OK Message to the client
     * @throws IOException
     */
    public boolean sendOK() throws IOException {
        return this.sendMessage(new Message(MessageType.OK));
    }

    /**
     * Send an ERROR message to the client
     * @throws IOException
     */
    public boolean sendERROR() throws IOException {
        return this.sendMessage(new Message(MessageType.ERROR));
    }

    /**
     * Send an ERROR message to the client
     * @throws IOException
     */
    public boolean sendBYE() throws IOException {
        return this.sendMessage(new Message(MessageType.BYE));
    }

    /**
     * Send a message to the client
     * 
     * @param message - to send
     * @return True if the message was sent
     * @throws IOException
     */
    public boolean sendMessage(Message message) throws IOException {
        if (key.isValid() && key.channel() instanceof SocketChannel) {
            SocketChannel sc = (SocketChannel) key.channel();
            
            String msg = "";
            switch(message.getType()) {
                case MESSAGE:
                case PRIV:
                    msg = String.format("MESSAGE %s %s", message.getUser(), message.getMessage());
                    break;
                case BYE:
                    msg = "BYE";
                    break;
                case ERROR:
                    msg = "ERROR";
                    break;
                case OK:
                	msg = "OK";
                	break;
                case JOINED:
                    msg = String.format("JOINED %s", message.getMessage());
                    break;
                case LEFT:
                    msg = String.format("LEFT %s", message.getMessage());
                    break;
                case NEWNICK: 
                    msg = String.format("NEWNICK %s %s", message.getMessage(), message.getUser());
                    break;
                default:
                    System.out.println("UNHANDLED MESSAGE TYPE");
                    return false;
            }

            if (msg != "") {
                ByteBuffer msgBuffer = ByteBuffer.wrap(msg.getBytes());
                sc.write(msgBuffer);
                while(msgBuffer.hasRemaining()) {
                    sc.write(msgBuffer);
                }
            }
        }
        return true;
    }

    public void joinRoom(String room) {
        this.room = room;
        this.state = State.INSIDE;
    }

    public void leaveRoom() {
        this.room = null;
        this.state = State.OUTSIDE;
    }

}