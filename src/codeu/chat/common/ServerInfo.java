package codeu.chat.common;

public final class ServerInfo {

    public final Time startTime;

    public ServerInfo() {
        this.startTime = Time.now();
    }

    public ServerInfo(Time startTime){
        this.startTime = startTime;
    }




}