package rm.chat.server;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

import rm.chat.server.*;

public class ChatServer {
    // A pre-allocated buffer for the received data
    final private static ByteBuffer buffer = ByteBuffer.allocate(16384);

    // Decoder for incoming text -- assume UTF-8
    final private static Charset charset = Charset.forName("UTF8");
    final private static CharsetDecoder decoder = charset.newDecoder();

    // A selector for each room
    private static Vector<Selector> rooms = new Vector<Selector>();
    private static Selector selector;
    private static ServerSocketChannel ssc;
    private static ServerSocket ss;

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

        // Decode and print the message to stdout
        String message = decoder.decode(buffer).toString().trim();

        if (message.charAt(0) == '/')
            processCommand(sc, message);

        // Check if the user
        SelectionKey key = sc.keyFor(selector);
        if (key.attachment() == null) {
            key.attach(message);
        } else {
            broadcast(message);
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
    private static void processCommand(SocketChannel sc, String message) throws IOException {
        String args[] = message.split("");
        if (args[0] == "nick") {
            setNickname(sc, args[1]);
        } else if (args[0] == "join") {
            addUser(sc, args[1]);
        } else if (args[0] == "leave") {
            removeUser(sc);
        } else if (args[0] == "bye") {
            disconnect(sc);
        }
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
                String username = (String) key.attachment();
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

        // Set it to non-blocking, so we can use select
        ssc.configureBlocking(false);

        // Get the Socket connected to this channel, and bind it to the
        // listening port
        ss = ssc.socket();
        InetSocketAddress isa = new InetSocketAddress(port);
        ss.bind(isa);

        // Create a new Selector for selecting
        selector = Selector.open();

        // Register the ServerSocketChannel, so we can listen for incoming
        // connections
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Listening on port " + port);
    }

    /**
     * Accept an incoming connection from a client.
     * 
     * @throws IOException
     * @throws ClosedChannelException
     */
    private static void acceptConnection() throws IOException, ClosedChannelException {
        // It's an incoming connection. Register this socket with
        // the Selector so we can listen for input on it
        Socket s = ss.accept();
        System.out.println("Got connection from " + s);

        // Make sure to make it non-blocking, so we can use a selector
        // on it.
        SocketChannel sc = s.getChannel();
        sc.configureBlocking(false);

        // Register it with the selector, for reading
        sc.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Read incoming data from a given SocketChannel.
     * 
     * @param key of the SocketChannel
     */
    private static void readSocket(SelectionKey key) {
        SocketChannel sc = null;

        try {

            // It's incoming data on a connection -- process it
            sc = (SocketChannel) key.channel();
            boolean ok = processInput(sc);

            // If the connection is dead, remove it from the selector
            // and close it
            if (!ok) {
                key.cancel();
                closeSocket(sc.socket());
            }

        } catch (IOException ie) {
            // On exception, remove this channel from the selector
            key.cancel();
            closeChannel(sc);
        }
    }

    /**
     * Closes given socket.
     * 
     * @param s socket to close
     */
    private static void closeSocket(Socket s) {
        try {
            s = sc.socket();
            System.out.println("Closing connection to " + s);
            s.close();
        } catch (IOException ie) {
            System.err.println("Error closing socket " + s + ": " + ie);
        }
    }

    /** 
     * Closes given channel
     * 
     * @param sc channel to close
     */
    private static void closeChannel(SocketChannel sc) {
        try {
            sc.close();
            System.out.println("Closed " + sc);
        } catch (IOException ie2) {
            System.out.println(ie2);
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
                        acceptConnection();
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