package codeu.chat.util;


import java.io.PrintWriter;



/**This class loads(interprets) the log
 *
 * It breaks it down into useful information.
 *
 * Created by jam on 6/18/2017.
 */
//THIS WOULD RUN in a For-loop that passes each of the log lines so that it can be loaded into the server
public class LogLoader {

    //Takes in the ArrayList of Log Lines
//    public LogLoader(ArrayList<String> fileLines){
//
//    }


    //Log Files Info
    private static String serverLogLocation = "C:\\git\\CodeU-Summer-2017\\serverdata\\serverLog.txt";
    private PrintWriter outputStream;

    private String logLine;

    public LogLoader(String logLine){
        this.logLine = logLine;

    }

    //Finds the command that's being used and returns it
    //Ex: ADD-CONVERSATION, ADD-USER and ADD-MESSAGE
    public String findCommmand(){
        //It cuts out the "ADD-" part and gets the command based off of the next letter
        //C = Conversation
        //U = User
        //M = Message
        String command = this.logLine.substring(4, 5);

        switch (command){

            case "U": return "U";
            case "C": return "C";
            case "M": return "M";

        }

        return "X";
    }



    //Loads the USER into the chat
    //FORMAT: [ID] [NAME] [DATE] [TIME]
    //FOCUS ON LOADING USERS BACK INTO THE SYSTEM FOR NOW
    //WHAT WE NEED TO CREATE A NEW USER:
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

        //Finds the DATE
        //NOT NEEDED FOR MEDIUM PROJECT(FOR NOW AT LEAST)

        //Finds the TIME
        String userTime = this.logLine.substring(this.logLine.indexOf(splitLogLine[4]));
        userInfo[2] = userTime;


        return userInfo;
    }


    //Loads the CONVERSATION into the chat
    public String[] loadConversation(){
        //userInfo Array: [convoID] [convo title] [user ID] [time]
        String[] userInfo = new String[4];

        //FORMAT: ADD-CONVERSATION 11.1163373635 "JonJon" 11.1944713052 18-Jun-2017 22:39:21.814
        //We should have size of 6
        String[] splitLogLine = this.logLine.split(" ");


        //Get Convo ID
        userInfo[0] =  splitLogLine[1];

        //Get Convo Title
        userInfo[1] = splitLogLine[2].substring(1, splitLogLine[2].length()-1);

        //Get USER ID
        userInfo[2] = splitLogLine[3];

        userInfo[3] = splitLogLine[5];

        //Finds the DATE
        //NOT NEEDED FOR MEDIUM PROJECT(FOR NOW AT LEAST)


        return userInfo;

    }

    //Loads the MESSAGE into the chat
    public String[] loadMessage(){
        //userInfo Array: [convoID] [messageID] [TIME] [userID] [messageContent]
        //SHould be a size of: 5
        String[] userInfo = new String[5];

        //FORMAT: ADD-MESSAGE 11.1163373635 11.84060427 19-Jun-2017 12:33:59.574 11.1944713052 "Hello World, it's a great new sunshiney day!"
        //We should have size of 5
        String[] splitLogLine = this.logLine.split(" ");


        //Get Convo ID
        userInfo[0] =  splitLogLine[1];

        //Get Message ID
        userInfo[1] = splitLogLine[2];

        //Get the TIME
        userInfo[2] = splitLogLine[4];

        //Get the userID
        userInfo[3] = splitLogLine[5];

        //Message Content
        int startIndex = this.logLine.indexOf("\"");
        int endIndex = this.logLine.indexOf("\"", startIndex+1);
        userInfo[4] = this.logLine.substring(startIndex+1, endIndex);


        return userInfo;
    }


}
