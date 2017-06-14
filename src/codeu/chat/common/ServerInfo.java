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

    // This is the default constructor used to create a ServerInfo with current time
    public ServerInfo() throws IOException {
        // Set the startTime equal to the Time value that the server was started
        this.startTime = Time.now();
        this.version = Uuid.parse(SERVER_VERSION);
    }

    // This creates a ServerInfo object with a specifed version
    public ServerInfo(Uuid version) throws IOException {
        this.startTime = Time.now();
        this.version = version;
    }

    // This creates a ServerInfo object with a specified time
    public ServerInfo(Time time) throws IOException {
        this.startTime = time;
        this.version = Uuid.parse(SERVER_VERSION);
    }

    // This creates a ServerInfo object with a specified time and version
    public ServerInfo(Time startTime, Uuid version) throws IOException {
        this.startTime = startTime;
        this.version = version;
    }
}
