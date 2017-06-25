package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public final class UserInterest extends Interest{

    public static final Serializer<UserInterest> SERIALIZER = new Serializer<UserInterest>() {

        @Override
        public void write(OutputStream out, UserInterest value) throws IOException {

            Uuid.SERIALIZER.write(out, value.id);
            Uuid.SERIALIZER.write(out, value.owner);
            Uuid.SERIALIZER.write(out, value.interest);
            Time.SERIALIZER.write(out, value.creation);

        }

        @Override
        public UserInterest read(InputStream in) throws IOException {

            return new UserInterest(
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Time.SERIALIZER.read(in)
            );

        }
    };

    public Set<ConversationHeader> conversations;

    // constructor for a User interest
    public UserInterest(Uuid id, Uuid owner, Uuid userId, Time creation) {
        super(id, owner, userId, creation);
        conversations = new HashSet<ConversationHeader>();
    }

    public UserInterest(Uuid owner, Uuid userId, Time creation) {
        super(owner, userId, creation);
        conversations = new HashSet<ConversationHeader>();
    }

    public String toString() {
        String convos = "";
        Iterator<ConversationHeader> iterator = conversations.iterator();
        while(iterator.hasNext()){
            convos = convos + iterator.next().title + "\n";
        }
        return "User Interest: " + owner + ", \nupdate conversations: \n" + convos;
    }

    @Override
    public void updateCount(){ }

    @Override
    public void addConversation(ConversationHeader conversation){
        conversations.add(conversation);
    }

    public void reset() {
        conversations.clear();
    }
}