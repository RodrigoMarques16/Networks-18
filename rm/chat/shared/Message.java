package rm.chat.shared;


/**
 * Represents a message sent from the server to the client.
 */
public class Message {
	
	/**
	 * The different types of message
	 */
	public enum MessageType {
		/**
		 * Last command sent by the client was successful.
		 */
		OK,
		/**
		 * Last command send by the client was unsuccessful.
		 */
		ERROR,
		/**
		 * Message from another client.
		 */
		MESSAGE,
		/**
		 * A user in the same room changed their name.
		 */
		NEWNICK,
		/**
		 * A user joined the room.
		 */
		JOINED,
		/**
		 * A user left the room.
		 */
		LEFT,
		/**
		 * Confirm a user's exit command.
		 */
		BYE
	}
	
	private MessageType type;
	private String message;
	
	public Message(MessageType type) {
		this.type = type;
		this.message = "";
	}
	
	public Message(MessageType type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public MessageType getType() {
		return type;
	}

	void setType(MessageType type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	void setMessage(String message) {
		this.message = message;
	}
	
	public static Message errorMessage() {
		return new Message(MessageType.ERROR);
	}
	public boolean isError() {
		return this.type == MessageType.ERROR;
	}
	
}
