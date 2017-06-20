package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public class UserInterest {

    public static final Serializer<UserInterest> SERIALIZER = new Serializer<UserInterest>() {

        @Override
        public void write(OutputStream out, UserInterest value) throws IOException {

            Uuid.SERIALIZER.write(out, value.id);
            Uuid.SERIALIZER.write(out, value.owner);
            Uuid.SERIALIZER.write(out, value.userId);
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

    public final Uuid id;
    public final Uuid owner;
    public final Uuid userId;
    public final Time creation;

    // constructor for a User interest
    public UserInterest (Uuid id, Uuid owner, Uuid userId, Time creation){
        this.id = id;
        this.owner = owner;
        this.userId = userId;
        this.creation = creation;
    }
}