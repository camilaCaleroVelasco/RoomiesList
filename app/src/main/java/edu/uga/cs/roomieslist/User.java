package edu.uga.cs.roomieslist;

public class User {
    public String name;
    public String email;
    public String groupId;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(edu.uga.cs.roomieslist.User.class)
    }

    public User(String name, String email, String groupId) {
        this.name = name;
        this.email = email;
        this.groupId = groupId;
    }
}