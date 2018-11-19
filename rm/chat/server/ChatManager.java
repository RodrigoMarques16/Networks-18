package rm.chat.server;

import java.util.Hashtable;
import java.util.Map;

public class ChatManager {
	private static Map<Integer, Chatroom> rooms = new Hashtable<>();
	
	public ChatManager() {
		
	}
	
	public void joinRoom(RemoteClient user, int roomId) {
		Chatroom room = rooms.get(roomId);
		if (rooms == null) {
			room = new Chatroom();
			rooms.put(roomId, room);
		}
		room.addUser(user);
	}
	
	
}
