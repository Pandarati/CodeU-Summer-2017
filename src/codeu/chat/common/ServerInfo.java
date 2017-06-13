
package codeu.chat.common;

import codeu.chat.util.Time;

public final class ServerInfo {

    public final Time startTime;

    // This is the default constructor used to create a ServerInfo with current time
    public ServerInfo() {
        this.startTime = Time.now();
    }

    // This creates a ServerInfo object with a specific time
    public ServerInfo(Time startTime) {
        this.startTime = startTime;
    }
}
