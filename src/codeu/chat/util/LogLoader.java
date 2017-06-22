package codeu.chat.util;

/**This class interprets the logs information
 *
 * It breaks it down into useful information.
 *
 * Created by jam on 6/18/2017.
 */
public class LogLoader {

    private String logLine;

    public LogLoader(String logLine){
        this.logLine = logLine;

    }

    /** Finds the command that was entered in
     *
     *  C = Conversation
     *  U = User
     *  M = Message
     *
     * @return String
     */
    public String findCommmand(){

        //It cuts out the "ADD-" part and gets the command based off of the next letter
        String command = this.logLine.substring(4, 5);

        switch (command){
            case "U": return "U";
            case "C": return "C";
            case "M": return "M";
        }

        return "X";
    }

    /** Gets info for loading a new user
     *
     *  @return userInfo
     */
    public String[] loadUser(){

        String[] userInfo = new String[3];
        String[] splitLogLine = this.logLine.split(" ");

        //Find the ID
        int endOfID = this.logLine.indexOf("\"") - 1;
        String userID = this.logLine.substring(9, endOfID);
        userInfo[0] = userID;

        //Finds the NAME
        int startOfName = this.logLine.indexOf("\"") + 1;
        int endOfName = this.logLine.indexOf("\"", startOfName +1);
        String userName = this.logLine.substring(startOfName, endOfName);
        userInfo[1] = userName;

        //Finds the TIME
        String userTime = splitLogLine[3];
        userInfo[2] = userTime;


        return userInfo;
    }


    /** Gets info for loading a new conversation
     *
     * @return userInfo
     */
    public String[] loadConversation(){

        String[] userInfo = new String[4];
        String[] splitLogLine = this.logLine.split(" ");

        //Get Convo ID
        userInfo[0] =  splitLogLine[1];

        //Get Convo Title
        userInfo[1] = splitLogLine[2].substring(1, splitLogLine[2].length()-1);

        //Get User ID
        userInfo[2] = splitLogLine[3];

        //Gets the Creation Time
        userInfo[3] = splitLogLine[4];

        return userInfo;
    }

    /** Gets info for loading a new message
     *
     * @return userInfo
     */
    public String[] loadMessage(){

        String[] userInfo = new String[5];
        String[] splitLogLine = this.logLine.split(" ");

        //Get Convo ID
        userInfo[0] =  splitLogLine[1];

        //Get Message ID
        userInfo[1] = splitLogLine[2];

        //Get the TIME
        userInfo[2] = splitLogLine[3];

        //Get the userID
        userInfo[3] = splitLogLine[4];

        //Get the Message Content
        int startIndex = this.logLine.indexOf("\"");
        int endIndex = this.logLine.indexOf("\"", startIndex+1);
        userInfo[4] = this.logLine.substring(startIndex+1, endIndex);


        return userInfo;
    }


}
