package rm.chat.server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rm.chat.server.RemoteClient;

/**
 * Keep track of existing clients.
 * Does username checks
 */
public class ClientManager {
	
	// should have mapped clients to name instead of id
	private Map<Integer, RemoteClient> clients;
	private List<String> namesInUse;
	
	public ClientManager() {
		this.clients = new Hashtable<>();
		this.namesInUse = new ArrayList<>();
	}
	
	/**
	 * Retrieve a RemoteClient by its Id
	 * @param id
	 * @return - the client
	 */
	public RemoteClient getClientById(Integer id) {
		return clients.get(id);
	}
	
	/**
	 * Retrieve a RemoteClient by its Id
	 * @param id
	 * @return - the client
	 */
	public RemoteClient getClientByName(String name) {
		Set<Integer> keys = clients.keySet();
		for(Integer id : keys) {
			if (clients.get(id).getNick().equals(name))
				return clients.get(id);
		}
		return null;
	}
	
	/**
	 * Add a new client
	 * @param client
	 */
	public void add(RemoteClient client) {
		clients.put(client.getId(), client);
		namesInUse.add(client.getNick());
	}
	
	/**
	 * Get the username associated with a given Id;
	 * @param id
	 * @return - the username
	 */
	public String getUsername(Integer id) {
		return clients.get(id).getNick();
	}
	
	/**
	 * Set the username associated with a given id
	 * @param id
	 * @param name
	 * @return - True if the username was available
	 */
	public boolean setUsername(Integer id, String name) {
		if (namesInUse.contains(name)) 
			return false;
		RemoteClient client = clients.get(id);
		namesInUse.remove(client.getNick());
		client.setNick(name);
		namesInUse.add(name);
		return true;
	}

	/**
	 * Set the username associated with a given id
	 * @param id
	 * @param name
	 * @return - True if the username was available
	 */
	public boolean setUsername(RemoteClient client, String name) {
		if (namesInUse.contains(name)) 
			return false;
		namesInUse.remove(client.getNick());
		client.setNick(name);
		namesInUse.add(name);
		return true;
	}

	
}
