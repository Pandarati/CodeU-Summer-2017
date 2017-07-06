package codeu.chat.common;

import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import java.io.IOException;

public final class ServerInfo {

    private final static String SERVER_VERSION = "1.0.0";
    private final Time startTime;
    private final Uuid version;

  // This is the default constructor used to create a ServerInfo with current time
  public ServerInfo() throws IOException{
    // Set the startTime equal to the Time value that the server was started
    this(Time.now(), Uuid.parse(SERVER_VERSION));
  }

  // This creates a ServerInfo object with a specified time and version
  public ServerInfo(Time startTime, Uuid version) throws IOException {
    this.startTime = startTime;
    this.version = version;
  }

  public Time getStartTime(){
    return this.startTime;
  }

  public Uuid getVersion() {
        return this.version;
    }
}
