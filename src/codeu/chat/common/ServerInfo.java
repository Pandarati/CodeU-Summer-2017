

package codeu.chat.common;
import codeu.chat.util.Uuid; 

public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";

  public final Uuid version;
  public Uuid temp = null;
  //public final Uuid temp = Uuid.parse(SERVER_VERSION);
  public ServerInfo() {
  	try {
  		temp = Uuid.parse(SERVER_VERSION);
    	//this.version = Uuid.parse(SERVER_VERSION);
    }
    catch (Exception ex) {
    	System.out.println("ERROR");
    }
    this.version = temp;
  }
  public ServerInfo(Uuid version) {
    this.version = version;
  }
}
