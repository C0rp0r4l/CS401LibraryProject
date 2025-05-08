package scmot;

import java.io.Serializable;

public class StaffMember extends Member implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userPassword = "";
    private String location = null;
    
    public StaffMember(String name) {
        super(name);
        userPassword = genPassword();
    }
    
    
  //staffmembers.txt
  ///staffMemberID, Name,strikes,hold,Banned,location,password
    public String toString() {
        return super.getUserID() + "," + super.getName() + "," + super.getStrikes() + "," + super.getAccountHold().toString() + "," + super.getAccountBan() + "," + super.getAccountLocation() +userPassword;
    }
    
    private String genPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String result = "";
    	for (int i = 0; i < 5; i++) {
            int index = (int) (Math.random() * chars.length());
            result += chars.charAt(index);
        }
    	return result;
    }

    // Constructor only for saving and loading
    public StaffMember(String n, String uID, String pw, String s, String a) {
        super(n, uID, s, a);
        userPassword = (pw == null ? genPassword() : pw);
    }
    
    public String getPassword() {
    	return userPassword;
    }
    
    public String getLocation() {
    	return location;
    }

    public boolean loginAttempt(String id, String pw) {
        if(userPassword.compareTo(pw) == 0 && id.compareTo(super.getUserID()) == 0) {
            return true;
        }
        else{
            return false;
        }
    }
}