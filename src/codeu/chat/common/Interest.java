package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public class Interest {

    public static final Serializer<Interest> SERIALIZER = new Serializer<Interest>() {

        @Override
        public void write(OutputStream out, Interest value) throws IOException {

            Uuid.SERIALIZER.write(out, value.id);
            Uuid.SERIALIZER.write(out, value.owner);
            Uuid.SERIALIZER.write(out, value.interestId);
            Time.SERIALIZER.write(out, value.creation);

        }

        @Override
        public Interest read(InputStream in) throws IOException {

            return new Interest(
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Uuid.SERIALIZER.read(in),
                    Time.SERIALIZER.read(in)
            );

        }
    };

    public final Uuid id;
    public final Uuid owner;
    public final Uuid interestId;
    public final Time creation;

    // constructor for a User interest
    public Interest (Uuid id, Uuid owner, Uuid interestId, Time creation){
        this.id = id;
        this.owner = owner;
        this.interestId = interestId;
        this.creation = creation;
    }
}