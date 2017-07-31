package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public final class ConversationInterest extends Interest {

    public static final Serializer<ConversationInterest> SERIALIZER = new Serializer<ConversationInterest>() {

        @Override
        public void write(OutputStream out, ConversationInterest value) throws IOException {

            Uuid.SERIALIZER.write(out, value.id);
            Uuid.SERIALIZER.write(out, value.owner);
            Uuid.SERIALIZER.write(out, value.interest);
            Serializers.INTEGER.write(out, value.messageCount);
            Time.SERIALIZER.write(out, value.creation);

        }

        @Override
        public ConversationInterest read(InputStream in) throws IOException {

            return new ConversationInterest(
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Serializers.INTEGER.read(in),
                    Time.SERIALIZER.read(in)
            );

        }
    };

    public int messageCount;

    public ConversationInterest (Uuid id, Uuid owner, Uuid conversation, Time creation){
        super(id, owner, conversation, creation);
        messageCount = 0;
    }

    public ConversationInterest (Uuid id, Uuid owner, Uuid conversation, int messageCounter, Time creation){
        super(id, owner, conversation, creation);
        messageCount = messageCounter;
    }
/*
    public ConversationInterest (Uuid owner, Uuid conversation, Time creation){
        super(owner, conversation, creation);
        messageCount = 0;
    }
    */

    @Override
    public void updateCount (){
        messageCount = messageCount + 1;
    }

    @Override
    public void addConversation(ConversationHeader conversation) { }

    @Override
    public void reset (){
        messageCount = 0;
    }

    public String toString() {
        return "Conversation Interest: " + this.interest + ", Number of missed messages: " + messageCount;
    }
}