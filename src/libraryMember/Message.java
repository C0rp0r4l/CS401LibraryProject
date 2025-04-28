package libraryMember;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	//header is identifying the class 'type'
	protected HeaderEnum header;
	//commands are what command the user or server needs, ex. add, remove, etc.
	protected CommandEnum commands;
	//shows connection status or illustrates intended direction of message, ex. outbound, inbound, success, failure etc.
    protected StatusEnum status;
    //actual data contained inside a text string to be used for passing user or server inputs back and forth
    protected String text;
    
    //Constructor
    public Message(HeaderEnum header, CommandEnum commands, StatusEnum status, String text) {
    	//type shouldn't be changed after creation, directly assign in constructor.
    	this.header = header; 
    	this.commands = commands;
        this.status = status;
        this.text = text;
    }
    
    //We can only set values in the constructor so we only need getters, no setters
    public HeaderEnum getHeader() {
        return header;
    }

    public CommandEnum getcommands() {
    	return commands;
    }
    
    public StatusEnum getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }
}
