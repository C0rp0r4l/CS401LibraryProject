package libraryMember;

public class StaffMember extends Member{
    private String userPassword = "";
    
    public StaffMember(String name) {
        super(name);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String result = "";

        for (int i = 0; i < 10; i++) {
            int index = (int) (Math.random() * chars.length());
            result += chars.charAt(index);
        }
    userPassword = result;
    }

    public String toString() {
        return super.toString() + userPassword;
    }

    // Constructor only for saving and loading
    public StaffMember(String n, String uID, String s, String a, String pw) {
        super(n, uID, s, a);
        userPassword = pw;
    }
    
    public String getPassword() {
    	return userPassword;
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