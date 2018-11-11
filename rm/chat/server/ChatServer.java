package rm.chat.server;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

import rm.chat.server.*;
import rm.chat.shared.*;

public class ChatServer {
    // A pre-allocated buffer for the received data
    final private static ByteBuffer buffer = ByteBuffer.allocate(16384);

    // Decoder for incoming text -- assume UTF-8
    final private static Charset charset = Charset.forName("UTF8");
    final private static CharsetDecoder decoder = charset.newDecoder();

    private static Selector selector;
    private static ServerSocketChannel ssc;
    //private static ServerSocket ss;

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

        // Decode message
        String message = decoder.decode(buffer).toString().trim();

        // If the message is a command process it.
        if (message.charAt(0) == '/' && message.charAt(1) != '/')
            return processCommand(sc, message);
        
        if(true) { // TODO: users outside a room can't send messages

            // Remove escaped '/'
            if(message.charAt(0) == '/' && message.charAt(1) == '/')
                message = message.substring(1);

            // TODO: broadcast message only to client's room
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
    private static boolean processCommand(SocketChannel sc, String message) throws IOException {
        /* 
        SelectionKey key = sc.keyFor(selector);
        if (key.attachment() == null) {
            key.attach(message);
        } else {
            broadcast(message);
        }
        */
        String args[] = message.split("");

        if (args[0].compareTo("nick") == 0) {
            setNickname(sc, args[1]);
        } else if (args[0].compareTo("join") == 0) {
            joinRoom(sc, args[1]);
        } else if (args[0].compareTo("leave") == 0) {
            leaveRoom(sc);
        } else if (args[0].compareTo("bye") == 0) {
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

        // Set it to non-blocking
        ssc.configureBlocking(false);

        // Get the Socket connected to this channel, and bind it to the
        // listening port   
        //ss = ssc.socket();
        InetSocketAddress isa = new InetSocketAddress(port);
        //ss.bind(isa);
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
        //Socket s = ss.accept();
        //System.out.println("Got connection from " + s);
        
        // Make sure to make it non-blocking, so we can use a selector
        // on it.
        //SocketChannel sc = s.getChannel();
        SocketChanel sc = ssc.accept();
        sc.configureBlocking(false);
        System.out.println("Got connection from " + sc);

        // TODO: CREATE NEW REMOTECLIENT

        // Register it with the selector, for reading,
        sc.register(selector, SelectionKey.OP_READ);
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
            System.out.println("Closing connection to " + sc);
        } catch (IOException ie) {
            System.err.println("Error closing " + sc + ": " + ie);
        }
    }

    public static void main(String args[]) throws Exception {
        //int port = Integer.parseInt(args[0]);
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
                for (SelectionKey key : selector.selectedKeys()) {
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