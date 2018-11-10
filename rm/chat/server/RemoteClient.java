package rm.chat.server;

public class RemoteClient {
    private State state;
    private int room;
    private String nick;
    private int id;

    public int getState() {
        return this.state;
    }

    setState(State state) {
        this.state = state;
    }

    public int getRoom() {
        return this.room;
    }

    setRoom(int room) {
        this.room = room;
    }

    public String getNick() {
        return this.nick;
    }

    setNick(String nick) {
        this.nick = nick;
        // broadcast
    }

    public int getId() {
        return this.id;
    }

    setId(int id) {
        this.id = id;
    }
}