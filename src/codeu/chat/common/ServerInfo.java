package codeu.chat.common;

// why is this class in the util package? Can we combine the two ServerInfo classes in the
// codeu.chat.common package?

import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

import java.io.IOException;

public final class ServerInfo {

    public Time startTime;
    private final static String SERVER_VERSION = "1.0.0";
    public Uuid version;

    public ServerInfo() throws IOException {
        //Set the startTime equal to the Time value that the server was started
        this.startTime = Time.now();
        this.version = Uuid.parse(SERVER_VERSION);
    }

    public ServerInfo(Uuid version) throws IOException {
        this.version = version;
    }

    public ServerInfo(Time time) throws IOException {
        this.startTime = time;
    }

    public ServerInfo(Time startTime, Uuid version) throws IOException {
        this.startTime = startTime;
        this.version = version;
    }
}
