package codeu.chat.util;

import java.io.IOException;

/**This class interprets the logs information
 *
 * It breaks it down into useful information.
 *
 * Created by jam on 6/18/2017.
 */
public class LogLoader {

    private Tokenizer tokenizer;

    public LogLoader(String logLine){
        this.tokenizer = new Tokenizer(logLine);

    }

    /** Finds the command that was entered in, as is.
     *
     * @return String
     */
    public String readCommand() throws IOException {
        return tokenizer.next();
    }

    /** Gets info for loading a new user
     *
     *  @return userInfo
     */
    public String[] readParameters(int count) throws IOException {

        String[] params = new String[count];
        for (int i = 0; i < count; i++) {
            params[i] = tokenizer.next();
        }

        return params;
    }
}
