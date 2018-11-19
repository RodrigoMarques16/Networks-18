package rm.chat.server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import rm.chat.server.RemoteClient;

public class ClientManager {
	private Map<Integer, RemoteClient> clients = new Hashtable<>();
	private List<String> namesInUse = new ArrayList<>();
	
	public ClientManager() {
		
	}
	
	public RemoteClient getClientById(Integer id) {
		return clients.get(id);
	}
	
	public void add(RemoteClient client) {
		clients.put(client.getId(), client);
		namesInUse.add(client.getNick());
	}
	
	public String getUsername(Integer id) {
		return clients.get(id).getNick();
	}
	
	public boolean setUsername(Integer id, String name) {
		if (namesInUse.contains(name)) 
			return false;
		RemoteClient client = clients.get(id);
		namesInUse.remove(client.getNick());
		client.setNick(name);
		namesInUse.add(name);
		return true;
	}
	
}
