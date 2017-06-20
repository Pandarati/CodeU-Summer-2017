package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public class ConversationInterest {

    public static final Serializer<ConversationInterest> SERIALIZER = new Serializer<ConversationInterest>() {

        @Override
        public void write(OutputStream out, ConversationInterest value) throws IOException {

            Uuid.SERIALIZER.write(out, value.id);
            Uuid.SERIALIZER.write(out, value.owner);
            Uuid.SERIALIZER.write(out, value.conversation);
            Time.SERIALIZER.write(out, value.creation);

        }

        @Override
        public ConversationInterest read(InputStream in) throws IOException {

            return new ConversationInterest(
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Time.SERIALIZER.read(in)
            );

        }
    };

    public final Uuid id;
    public final Uuid owner;
    public final Uuid conversation;
    public final Time creation;

    // constructor for a User interest
    public ConversationInterest (Uuid id, Uuid owner, Uuid conversation, Time creation){
        this.id = id;
        this.owner = owner;
        this.conversation = conversation;
        this.creation = creation;
    }
}