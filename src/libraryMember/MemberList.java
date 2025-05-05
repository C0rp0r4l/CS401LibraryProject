package scmot;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class MemberList {
    private ArrayList<Member> members;
    private String sourceName = "memberList.txt";
    private boolean modified = false;

    public MemberList() {
        members = new ArrayList<>();
    }

    public void setFilename(String filename) {
        sourceName = filename;
    }

    public String toString() {
        StringBuilder list = new StringBuilder();
        for (Member m : members) {
            list.append(m.getName()).append(",").append(m.getUserID())
                .append(",").append(m.getStrikes()).append(",")
                .append(m.getAccountHold()).append("\n");
        }
        return list.toString();
    }

    public Object attemptLogin(String u) {
        System.out.println(u);
        Member m = searchMember(u);
        return m;
    }

    public Member getIndex(int i) {
        return members.get(i);
    }

    public Member searchMember(String memberId) {
        for (Member m : members) {
            if (m.getUserID().equals(memberId)) {
                return m;
            }
        }
        return null;
    }

    public void addMember(Object obj) {
        Member newMember = null;

        if (obj instanceof String) {
            newMember = new Member((String) obj);
        } else if (obj instanceof Member) {
            newMember = (Member) obj;
        } else {
            System.out.println("Invalid type passed to addMember.");
            return;
        }

        members.add(newMember);
        members.sort(Comparator.comparing(Member::getUserID));

        setModified(true);
        saveList();
    }

    public void setModified(boolean m) {
        modified = m;
    }

    public Integer getNumMembers() {
        return members.size();
    }

    public String getStrikes(String id) {
        for (Member m : members) {
            if (m.getUserID().equals(id)) {
                return m.getStrikes();
            }
        }
        return "User not found";
    }

    public Boolean editMember(Member m) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getUserID().equals(m.getUserID())) {
                members.set(i, m);
                saveList();
                return true;
            }
        }
        saveList();
        return false;
    }

    public Boolean getAccountHold(String id) {
        for (Member m : members) {
            if (m.getUserID().equals(id)) {
                return m.getAccountHold();
            }
        }
        return null;
    }

    public void removeMember(String id) {
        members.removeIf(m -> m.getUserID().equals(id));
        setModified(true);
        saveList();
    }

    public void addMember(String name) {
        Member temp = new Member(name);
        members.add(temp);
        members.sort(Comparator.comparing(Member::getUserID));
        setModified(true);
        saveList();
    }

    public void saveList() {
        try (FileWriter myWriter = new FileWriter(sourceName)) {
            for (Member m : members) {
                myWriter.write(m.toString() + "\n");
            }
            modified = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadList() {
        File file = new File(sourceName);
        try {
            if (file.createNewFile()) {
                return;
            }
        } catch (Exception e) {
            System.out.println("Error opening file");
        }

        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter(",|\n");

            while (scanner.hasNext()) {
                String name = scanner.next();
                String userID = scanner.next();
                String strikes = scanner.next();
                String accountHold = scanner.next();
                Member temp = new Member(name, userID, strikes, accountHold);
                members.add(temp);
            }
            modified = false;
        } catch (Exception e) {
            System.out.println("Error reading file");
        }
    }
}
