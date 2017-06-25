package codeu.chat.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jam on 6/18/2017.
 */
public class LogReader {

    //Log Files Info
    private static String serverLogLocation = "C:\\git\\CodeU-Summer-2017\\serverdata\\serverLog.txt";
    private static Scanner inputStream;

    ArrayList<String> fileLines;


    public LogReader() throws IOException{

        try {
            inputStream = new Scanner(new File(serverLogLocation));
        }catch (FileNotFoundException ex){
            ex.printStackTrace();
        }

        fileLines = new ArrayList<String>();

    }


    //Reads in the file and stores in ArrayList
    public ArrayList<String> readFile(){

        ArrayList<String> fileLines = new ArrayList<String>();

        while(inputStream.hasNext()){
            String line = inputStream.nextLine();
            fileLines.add(line);
        }
        return fileLines;
    }






}
