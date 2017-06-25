package codeu.chat.common;

import java.util.HashSet;
import java.util.Set;

import codeu.chat.util.Uuid;
import codeu.chat.util.Time;

public abstract class Interest{

    public final Uuid id;
    public final Uuid owner;
    public final Uuid interest;
    public final Time creation;

    public Interest(Uuid id, Uuid owner, Uuid interest, Time creation) {
        this.id = id;
        this.owner = owner;
        this.interest = interest;
        this.creation = creation;
    }

    public Interest(Uuid owner, Uuid interest, Time creation) {
        id = Uuid.NULL;
        this.owner = owner;
        this.interest = interest;
        this.creation = creation;
    }

    public abstract void updateCount();

    public abstract void addConversation(ConversationHeader conversation);

    public abstract void reset();
}