

package codeu.chat.common;
import codeu.chat.util.Uuid; 

public final class VersionInfo {
  private final static String SERVER_VERSION = "1.0.0";

  public final Uuid version;


  // Writes object with server information to server
  public VersionInfo() {
    Uuid temp = null;
  	try {
  		temp = Uuid.parse(SERVER_VERSION);
    }
    catch (Exception ex) {
    	System.out.println("ERROR");
    }
    this.version = temp;
  }

  // Client side to read the server information
  public VersionInfo(Uuid version) {
    this.version = version;
  }
}
