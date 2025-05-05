package libraryMember;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private Header primaryHeader;
    private Header secondaryHeader;
    private Object data;    // <-- Can hold any data type
    private String actioner;
    private String receiver;
    private String to;
    private String from;

    // Constructor
    public Message(Header primaryHeader, Header secondaryHeader, Object data, 
                   String actioner, String receiver, String to, String from) {
        this.primaryHeader = primaryHeader;
        this.secondaryHeader = secondaryHeader;
        this.data = data;
        this.actioner = actioner;
        this.receiver = receiver;
        this.to = to;
        this.from = from;
    }

    // Getters and Setters
    public Header getPrimaryHeader() {
        return primaryHeader;
    }

    public void setPrimaryHeader(Header primaryHeader) {
        this.primaryHeader = primaryHeader;
    }

    public Header getSecondaryHeader() {
        return secondaryHeader;
    }

    public void setSecondaryHeader(Header secondaryHeader) {
        this.secondaryHeader = secondaryHeader;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getActioner() {
        return actioner;
    }

    public void setActioner(String actioner) {
        this.actioner = actioner;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
