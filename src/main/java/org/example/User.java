package org.example;

import java.util.Objects;

public class User {
    private String name;
    private String lastName;
    private String chatId;

    public User(String name,String lastName,String chatId) {
        this.name = name;
        this.lastName = lastName;
        this.chatId=chatId;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getChatId() {
        return chatId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return this.name.equals(user.name) && lastName.equals(user.lastName);
    }

    // מתודת hashCode
    @Override
    public int hashCode() {
        return Objects.hash(name, lastName);
    }

    @Override
    public String toString() {
        return this.name+ " " + this.lastName +" ChatId: " + this.chatId;
    }
}
