package rm.chat.server;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;

import rm.chat.shared.Message;

public class ChatManager {
	private Map<String, Chatroom> rooms;
	
	public ChatManager() {
		this.rooms = new Hashtable<>();
	}

	public Chatroom getRoom(String name) {
		return rooms.get(name);
	}

	private Chatroom createRoom(String name) {
		Chatroom room = new Chatroom(name);
		this.rooms.put(name, room);
		return room;
	}
	
	public void joinRoom(RemoteClient user, String newRoom) throws IOException {
		if (user.getRoom() != null) {
			Chatroom oldRoom = rooms.get(user.getRoom());
			if (oldRoom != null) {
				oldRoom.removeUser(user);
			}
		}
		
		Chatroom room = this.rooms.get(newRoom);
		if (room == null) {
			room = createRoom(newRoom);
		}
		room.addUser(user);
	}
	
	public void leaveRoom(RemoteClient user) throws IOException{
		Chatroom room = rooms.get(user.getRoom());
		if (room != null) {
			room.removeUser(user);
		}
	}

	
}
