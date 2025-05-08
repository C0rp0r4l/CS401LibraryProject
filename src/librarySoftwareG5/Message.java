package librarySoftwareG5;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ActionType {
        LOGIN, LOGOUT,
        //Member actions
        ADD_MEMBER, REMOVE_MEMBER, GET_MEMBER, SEARCH_MEMBERS, GET_ALL_MEMBERS,
        MARK_MEMBER_PAST_DUE, MEMBER_ITEM_RETURNED,  
        //Staff actions
        ADD_STAFF, REMOVE_STAFF, GET_STAFF, GET_ALL_STAFF,
        //Item actions
        ADD_ITEM, REMOVE_ITEM, GET_ITEM, SEARCH_ITEMS, GET_ALL_ITEMS,
        CHECKOUT, RETURN, RESERVE, CANCEL_RESERVATION,
        //Location actions
        ADD_LOCATION, REMOVE_LOCATION, GET_LOCATIONS,
        //Data requests/errors
        ACK, ERR
    }

    private ActionType action;
    private Object data;
    private String errorMessage;

    public Message(ActionType action, Object data) {
        this.action = action;
        this.data = data;
    }

    //Getters and Setters
    public ActionType getAction() { return action; }
    public Object getData() { return data; }
    public String getErrorMessage() { return errorMessage; }

    public void setErrorMessage(String error) { this.errorMessage = error; }

    public boolean isSuccess() { return errorMessage == null; }
}