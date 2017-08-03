package codeu.chat.common;

import codeu.chat.util.Uuid;

// Serena's back-end
public final class UserControl {

    public enum UserType {
        CREATOR,
        OWNER,
        MEMBER
    }

    private final Uuid user;
    private UserType type;

    public UserControl(Uuid user) {
        this.user = user;
        this.type = UserType.CREATOR;
    }

    public UserControl(Uuid user, UserType type) {
        this.user = user;
        this.type = type;
    }

    public UserControl(Uuid user, boolean isMember) {
        this.user = user;
        if(isMember)
            this.type = UserType.MEMBER;
        else
            this.type = UserType.OWNER;
    }

    public UserType getType() {
        return this.type;
    }

    public Uuid getUser() {
        return this.user;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public boolean isCreator() {
        return (this.type == UserType.CREATOR);
    }

    public boolean isOwner() {
        return (this.type == UserType.OWNER);
    }

    public boolean isMember() {
        return (this.type == UserType.MEMBER);
    }

}