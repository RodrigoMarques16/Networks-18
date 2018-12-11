package rm.chat.server;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

import rm.chat.server.*;
import rm.chat.server.RemoteClient.State;
import rm.chat.shared.*;
import rm.chat.shared.Message.MessageType;

public class ChatServer {
	
	// A pre-allocated buffer for the received data
	final private static ByteBuffer buffer = ByteBuffer.allocate(16384);

	// Decoder for incoming text -- assume UTF-8
	final private static Charset charset = Charset.forName("UTF8");
	final private static CharsetDecoder decoder = charset.newDecoder();

	private static Selector selector;
	private static ServerSocketChannel ssc;

	private static ClientManager clients = new ClientManager();
	private static ChatManager rooms = new ChatManager();
	
	// private static ServerSocket ss;

	/**
	 * Wrapper for ClientManager's getClient
	 * 
	 * @param sc - SocketChannel
	 * @return the client
	 */
	private static RemoteClient getClient(SocketChannel sc) {
		int id = (Integer) sc.keyFor(selector).attachment();
		return clients.getClientById(id);
	}

	/**
	 * Process a message sent by a client
	 * 
	 * @param sc
	 * @return
	 * @throws IOException
	 */
	private static boolean processInput(SocketChannel sc) throws IOException {
		// Read the message to the buffer
		buffer.clear();
		sc.read(buffer);
		buffer.flip();

		// If no data, close the connection
		if (buffer.limit() == 0) {
			return false;
		}
		
		String input = decoder.decode(buffer).toString().trim();
		System.out.println("Received " + input);
		String[] messages = input.split("\n");
		
		RemoteClient client = getClient(sc);
		String name = client.getNick();
		
		for(String str : messages) {
			log(client, str);

			if (str.length() == 0)
				continue;

			Message message = new Message(name, str);
			
			if (message.isCommand()) {
				boolean result = processCommand(client, message);
				client.reportResult(result);
			}
			else if (client.canChat()) {
				message.clean();
				broadcast(message);
			}
		}
		return true;
	}

	/**

	* Process a user command
	 * 
	 * @param sc
	 * @param message
	 * @throws IOException
	 */
	private static boolean processCommand(RemoteClient client, Message message) throws IOException {
		String args[] = message.getArgs();

		log(client, "COMMAND: " + args[0]);
		
		if (args[0].compareTo("/nick") == 0) {
			return setNickname(client, args[1]);
		} else if (args[0].compareTo("/join") == 0) {
			return joinRoom(client, args[1]);
		} else if (args[0].compareTo("/leave") == 0) {
			return leaveRoom(client);
		} else if (args[0].compareTo("/bye") == 0) {
			return disconnect(client);
		} else {
			return false;
		}
	}
	
	/**
	 * Change a user's nickname.
	 * 
	 * @param sc   user's socket
	 * @param name to give the user
	 */
	private static boolean setNickname(RemoteClient client, String name) {
		String oldNick = client.getNick();
		boolean result = clients.setUsername(client, name);

		if (result){
			log(client, "Name set to '" + client.getNick() + "'");

			try { 
				String roomName = client.getRoom();
				if (roomName != null) {
					Chatroom room = rooms.getRoom(client.getRoom());
					room.broadcast(new Message(
						MessageType.NEWNICK,
						String.format("%s %s", oldNick, client.getNick())
					));
				}
			} catch(IOException e) {
				System.out.println(e.toString());
			}
		}
		return result;
	}
		
	/**
	 * Add a user to a room
	 * 
	 * @param client
	 * @param room
	 */
	private static boolean joinRoom(RemoteClient client, String room) {
		log(client, "Join " + room);

		if (client.getNick().compareTo("") == 0)
			return false;
		
		String oldRoom = client.getRoom();
		if (oldRoom != null && room.compareTo(oldRoom) == 0)
			return false;

		try {
			rooms.joinRoom(client, room);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	
		return true;
	}	

	/**
	 * Remove a user from a room
	 * 
	 * @param client
	 * @param room
	 */
	private static boolean leaveRoom(RemoteClient client) {
		log(client, "Left " + client.getRoom());
		try {
			if (client.getRoom() != null){
				rooms.leaveRoom(client);
				return true;
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}

		return false;
	}

	/**
	 * Remove a user from the server.
	 * 
	 * @param sc user's socket
	 */
	private static boolean disconnect(RemoteClient client) {
		leaveRoom(client);
		SocketChannel sc = (SocketChannel) client.getKey().channel();
		close(sc);
		return true;
	}
		
	/**
	 * Broadcast a message to all connected clients
	 * 
	 * @param message to broadcast
	 * @throws IOException
	 */
	private static void broadcast(Message message) throws IOException {
		for (SelectionKey key : selector.keys()) {
			if (key.isValid() && key.channel() instanceof SocketChannel) {
				Integer username = (Integer) key.attachment();
				//String msg = message.addUsername();
				//message = String.format("%s: %s\n", username, message);
				//ByteBuffer msgBuffer = ByteBuffer.wrap(message.getBytes());
				SocketChannel sc = (SocketChannel) key.channel();
				// sc.write(msgBuffer);
				// while(msgBuffer.hasRemaining()) {
				//     sc.write(msgBuffer);
				//}
				// msgBuffer.rewind();
			}
		}
	}
		
	/**
	 * Creates a server-socket channel to listen on a given port and registers it
	 * with a selector.
	 * 
	 * @param port to listen on
	 * @throws IOException If an IO error occurs
	 */
	private static void setup(int port) throws IOException {		
		// Create a ServerSocketChannel
		ssc = ServerSocketChannel.open();
		
		// Set it to non-blocking
		ssc.configureBlocking(false);
		
		// Get the Socket connected to this channel, and bind it to the
		// listening port
		// ss = ssc.socket();
		InetSocketAddress isa = new InetSocketAddress(port);
		// ss.bind(isa);
		ssc.bind(isa);
		
		// Create a new Selector for selecting
		selector = Selector.open();
		
		// Register the ServerSocketChannel, so we can listen for incoming
		// connections
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Listening on port " + port);
	}

	/**
	 * Accept an incoming connection from a client. Configures it so it's
	 * non-blocking and registers it with the selector.
	 * 
	 * @throws IOException
	 * @throws ClosedChannelException
	 */
	private static void acceptConnection(SelectionKey key) throws IOException {
		// It's an incoming connection. Register this socket with
		// the Selector so we can listen for input on it.
		// Socket s = ss.accept();
		// System.out.println("Got connection from " + s);

		// Make sure to make it non-blocking, so we can use a selector
		// on it.
		// SocketChannel sc = s.getChannel();
		SocketChannel sc = ssc.accept();
		sc.configureBlocking(false);
		System.out.println("Got connection from " + sc);

		// Register it with the selector, for reading,
		SelectionKey readKey = sc.register(selector, SelectionKey.OP_READ);

		// Create a new RemoteClient
		RemoteClient client = new RemoteClient(readKey);
		clients.add(client);

		// Attach this clients Id to his key
		readKey.attach(client.getId());

		System.out.println("Client " + client.getId() + " has connected");
	}

	/**
	 * Read incoming data from a given SocketChannel.
	 * 
	 * @param key of the SocketChannel
	 */
	private static void readSocket(SelectionKey key) {
		SocketChannel sc = (SocketChannel) key.channel();

		try {
			// It's incoming data on a connection -- process it
			boolean ok = processInput(sc);

			// If the connection is dead, remove it from the selector
			// and close it
			if (!ok) {
				key.cancel();
				close(sc);
			}
		} catch (IOException ie) {
			// On exception, remove this channel from the selector
			key.cancel();
			close(sc);
		}
	}
	
	/**
	 * Closes a given socket.
	 * 
	 * @param s socket to close
	 */
	private static void closeSocket(Socket s) {
		try {
			System.out.println("Closing connection to " + s);
			s.close();
		} catch (IOException ie) {
			System.err.println("Error closing socket " + s + ": " + ie);
		}
	}

	/**
	 * Closes a given channel
	 * 
	 * @param sc channel to close
	 */
	private static void close(SocketChannel sc) {
		try {
			sc.close();
			sc.socket().close(); // necessary?
			System.out.println("Closing connection to client" + sc.keyFor(selector).attachment());
		} catch (IOException ie) {
			System.err.println("Error closing client" + sc.keyFor(selector).attachment() + ": " + ie);
		}
	}

	public static void main(String args[]) throws Exception {
		int port = Integer.parseInt(args[0]);

		try {
			setup(port);

			while (true) {

				// See if we've had any activity -- either an incoming connection,
				// or incoming data on an existing connection
				int num = selector.select();

				// If we don't have any activity, loop around and wait again
				if (num == 0) {
					continue;
				}

				// Get the keys corresponding to the activity that has been
				// detected, and process them one by one
				Set<SelectionKey> keys = selector.selectedKeys();
				for (SelectionKey key : keys) {
					if (key.isAcceptable()) {
						acceptConnection(key);
					} else if (key.isReadable()) {
						readSocket(key);
					}
				}

				// We remove the selected keys, because we've dealt with them.
				keys.clear();
			}
		} catch (IOException ie) {

			System.err.println(ie);
		}
	}

	private static void log(RemoteClient client, String message) {
		System.out.println("Client " + client.getId() + ": " + message);
	}

}