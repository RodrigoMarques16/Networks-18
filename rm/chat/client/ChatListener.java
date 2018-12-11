
package rm.chat.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import rm.chat.shared.*;

/**
 * Runnable class to listen for messages from the server
 */
public class ChatListener implements Runnable {
	
	private SocketChannel channel;
	private ChatClient client;
	
	final private static Charset charset = Charset.forName("UTF8");
	final private static CharsetDecoder decoder = charset.newDecoder();
	
	public ChatListener(SocketChannel channel, ChatClient client) {
		this.channel = channel;
		this.client = client;
	}

	@Override
	public void run() {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(16384);
			while(true) {
				buffer.clear();
				if (channel.read(buffer) > 0) {
					buffer.flip();
					String message = decoder.decode(buffer).toString();
					System.out.println("Received " + message);
					String parsed = Message.parse(message);
					if (parsed != null)
						client.printMessage(Message.parse(message));
				}
			}
		} catch(IOException e) {
			System.out.println(e);
			return;
		}
	}

}
