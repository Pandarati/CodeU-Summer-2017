package codeu.chat.common;

import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import java.io.IOException;

public final class ServerInfo {

  private final static String SERVER_VERSION = "1.0.0";
  private Time startTime;
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

  //Performs calculations to get Up Time
  public String calcUpTime(){

    String upTime = "";

    // Example value of Time.now():
    // Start Time: 06-Jul-2017 13:30:17.426
    //Should have Array of Size Two:06-Jul-2017 13:30:17.426

    //Chops out the date from the Time objects.
    String startTimeStr = this.startTime.toString().trim();
    String currTimeStr = Time.now().toString().trim();

    //Chops out hte date so we only have a time:13:30:17.426
    startTimeStr = startTimeStr.substring(startTimeStr.indexOf(":")-2);
    currTimeStr = currTimeStr.substring(currTimeStr.indexOf(":")-2);

    //Get hours
    String startTimeHrs = startTimeStr.substring(0, startTimeStr.indexOf(":"));
    String currTimeHrs = currTimeStr.substring(0, currTimeStr.indexOf(":"));

    //Chops off the hours portion and leaves:30:17.426
    startTimeStr = startTimeStr.substring(startTimeStr.indexOf(":") + 1);
    currTimeStr = currTimeStr.substring(currTimeStr.indexOf(":") + 1);

    //Get mins
    String startTimeMins = startTimeStr.substring(0, startTimeStr.indexOf(":"));
    String currTimeMins = currTimeStr.substring(0, currTimeStr.indexOf(":"));

//    //Chops off the mins portion and leaves: 17.426
//    startTimeStr = startTimeStr.substring(startTimeStr.indexOf(":") + 1);
//    currTimeStr = currTimeStr.substring(startTimeHrs.indexOf(":") + 1);
//
//    //Get seconds
//    String startTimeSecs = startTimeStr.substring(0, startTimeStr.indexOf("."));
//    String currTimeSecs = startTimeStr.substring(0, currTimeStr.indexOf("."));

    //Convert Everything to an INT for calculation
    int startHrs = Integer.parseInt(startTimeHrs);
    int currHrs = Integer.parseInt(currTimeHrs);

    int startMins = Integer.parseInt(startTimeMins);
    int currMins = Integer.parseInt(currTimeMins);

//    int startSecs = Integer.parseInt(startTimeSecs);
//    int currSecs = Integer.parseInt(currTimeSecs);

    int upTimeHrs = (currHrs - startHrs);
    int upTimeMins = (currMins - startMins);
//    int upTimeSecs = (startSecs - currSecs);

    //Catch over flow in mins and secs
    //Ex: Curr Mins = 30 Start Time Mins = 40
    if(upTimeMins < 0 ){
      int overFlowMins = upTimeMins * -1;
      upTimeMins =  currMins + overFlowMins;
      upTimeHrs++;
    }
//    if(upTimeSecs < 0){
//      int overFlowSecs = upTimeSecs * -1;
//      upTimeSecs = currSecs + overFlowSecs;
//      upTimeHrs++;
//    }

    /*
    CurrHrs: works
    CurrMins:
     */

    upTime = upTimeHrs + ":" + upTimeMins;

    return upTime;
  }
}
