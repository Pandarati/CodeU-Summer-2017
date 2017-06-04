package codeu.chat.util;

import java.util.UUID;

public final class ServerInfo {

    public final Time startTime;
    public UUID version;


    public ServerInfo() {

        //Set the startTime equal to the Time value that the server was started
        this.startTime = Time.now();

        //I was getting an error for Version not being initialized?
        //I believe this is implemented in Version Check
        //For now I just went ahead and made a version variable
        //this.version = new UUID("25");
    }

    public ServerInfo(Time startTime){
        this.startTime = startTime;

    }




}