package codeu.chat.common;

import codeu.chat.util.Uuid;

import java.io.IOException;

/**
 * Created by john on 6/5/2017.
 */
public class VersionServerInfo {

    private final static String SERVER_VERSION = "1.0.0";
    public final Uuid version;

    public VersionServerInfo() throws IOException{
        this.version = Uuid.parse(SERVER_VERSION);
    }
    public VersionServerInfo(Uuid version) throws IOException{
        this.version = version;
    }

}
