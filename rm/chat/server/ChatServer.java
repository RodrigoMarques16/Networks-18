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

		String message = decoder.decode(buffer).toString().trim();

		if (isCommand(message))
			return processCommand(sc, message);
		
		RemoteClient client = getClient(sc);
		if (client.canChat()) {
			cleanMessage(message);
			broadcast(message);
		}
		
		return true;
	}
	
	private static RemoteClient getClient(SocketChannel sc) {
		int id = (Integer) sc.keyFor(selector).attachment();
		return clients.getClientById(id);
	}
	
	private static boolean isCommand(String message) {
		return (message.charAt(0) == '/' && message.length() > 1 && message.charAt(1) != '/');
	}
	
	private static String cleanMessage(String message) {
		if (message.charAt(0) == '/' && message.length() > 1 && message.charAt(1) == '/')
			message = message.substring(1);
		return message;
	}

	/**
	 * Process a user command
	 * 
	 * @param sc
	 * @param message
	 * @throws IOException
	 */
	private static boolean processCommand(SocketChannel sc, String message) throws IOException {
		String args[] = message.split(" ");

		log(sc, "COMMAND: " + args[0]);
		
		if (args[0].compareTo("/nick") == 0) {
			setNickname(sc, args[1]);
		} else if (args[0].compareTo("/join") == 0) {
			joinRoom(sc, args[1]);
		} else if (args[0].compareTo("/leave") == 0) {
			leaveRoom(sc);
		} else if (args[0].compareTo("/bye") == 0) {
			disconnect(sc);
		} else {
			sendMessage(sc, Message.errorMessage());
		}
		
		return true;
	}

	private static void log(SocketChannel sc, String message) {
		int id = (int) sc.keyFor(selector).attachment();
		System.out.println("Client " + id + ": " + message);
	}
	
	/**
	 * Change a user's nickname.
	 * 
	 * @param sc   user's socket
	 * @param name to give the user
	 */
	private static boolean setNickname(SocketChannel sc, String name) {
		SelectionKey key = sc.keyFor(selector);
		int id = (int) key.attachment();
		boolean result = clients.setUsername(id, name);
		log(sc, "Name set to '" + clients.getUsername(id) + "'");
		return result;
	}

	private static boolean sendMessage(SocketChannel sc, Message message) {
		// TODO:
		return true;
	}
	
	/**
	 * Change the room a user is in.
	 * 
	 * @param sc   user's socket
	 * @param room to put the user in
	 */
	private static boolean joinRoom(SocketChannel sc, String room) {
		int roomId = Integer.parseInt(room);
		
		return true;
	}

	/**
	 * Remove a user from the room they are currently in.
	 * 
	 * @param sc user's socket
	 */
	private static boolean leaveRoom(SocketChannel sc) {

		return true;
	}

	/**
	 * Remove a user from the server.
	 * 
	 * @param sc user's socket
	 */
	private static boolean disconnect(SocketChannel sc) {

		return true;
	}

	/**
	 * Broadcast a message to all connected clients
	 * 
	 * @param message to broadcast
	 * @throws IOException
	 */
	private static void broadcast(String message) throws IOException {
		for (SelectionKey key : selector.keys()) {
			if (key.isValid() && key.channel() instanceof SocketChannel) {
				Integer username = (Integer) key.attachment();
				message = String.format("%s: %s\n", username, message);
				ByteBuffer msgBuffer = ByteBuffer.wrap(message.getBytes());
				SocketChannel sc = (SocketChannel) key.channel();
				// sc.write(msgBuffer);
				// while(msgBuffer.hasRemaining()) {
				sc.write(msgBuffer);
				// }
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
		
		// Create a new RemoteClient
		RemoteClient client = new RemoteClient();
		clients.add(client);
		
		// Register it with the selector, for reading,
		SelectionKey readKey = sc.register(selector, SelectionKey.OP_READ);
		
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
		// int port = Integer.parseInt(args[0]);
		int port = 6699;
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

}