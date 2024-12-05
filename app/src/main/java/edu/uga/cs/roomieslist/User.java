package edu.uga.cs.roomieslist;

/**
 * POJO class
 */
public class User {
    public String name;
    public String email;
    public String groupId;

    public User() {}

    public User(String name, String email, String groupId) {
        this.name = name;
        this.email = email;
        this.groupId = groupId;
    }

}