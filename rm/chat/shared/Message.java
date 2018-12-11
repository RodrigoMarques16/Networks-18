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
	private String username;

	public Message(MessageType type) {
		this.type = type;
		this.username = "";
		this.message = "";
	}
	
	public Message(MessageType type, String message) {
		this.type = type;
		this.username = "";
		this.message = message;
	}
	
	public Message(String username, String message) {
		this.type = MessageType.MESSAGE;
		this.username = "";
		this.message = message;
	}

	public MessageType getType() {
		return type;
	}

	void setType(MessageType type) {
		this.type = type;
	}

	public String getMessage() {
		return this.message;
	}

	public String getUser() {
		return this.username;
	}

	void setMessage(String message) {
		this.message = message;
	}
	
	public static Message errorMessage() {
		return new Message(MessageType.ERROR);
	}

	public static Message okMessage() {
		return new Message(MessageType.OK);
	}

	public static Message joinMessage(String name) {
		return new Message(MessageType.JOINED, name);
	}

	public static Message leaveMessage(String name) {
		return new Message(MessageType.LEFT, name);
	}

	public boolean isError() {
		return this.type == MessageType.ERROR;
	}

	/**isCommand
	 * Test if a message is a command
	 * @param message
	 * @return
	 */
	public boolean isCommand() {
		return (this.message.charAt(0) == '/' 
			&& this.message.length() > 1 
			&& this.message.charAt(1) != '/');
	}

	/**
	 * Clean up a message by removing escape characters
	 * @param message
	 * @return
	 */
	public String clean() {
		if (this.message.charAt(0) == '/' && this.message.length() > 1 && this.message.charAt(1) == '/') {
			message = message.substring(1);
		}
		message.trim();
		return message;
	}

	public void log() {
		System.out.println(this.username + ": " + this.message);
	}

	public String[] getArgs() {
		return this.message.split(" ");
	}

}
