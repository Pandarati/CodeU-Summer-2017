// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.server;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import codeu.chat.client.commandline.Chat;
import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.ConversationInterest;
import codeu.chat.common.Interest;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.common.UserInterest;
import codeu.chat.util.*;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final Uuid.Generator uuidGenerator;
  public boolean finishedLoadingLog = false;

  //File Info for writing to Log
  private static String serverLogLocation = "C:\\git\\CodeU-Summer-2017\\serverdata\\serverLog.txt";
  public PrintWriter outputStream;

  LogReader logReader;
  ArrayList<String> fileLines;



  ArrayList<String> storedLogCommands = new ArrayList<String>();

  public Controller(Uuid serverId, Model model) throws IOException{
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());

    //Server Variables
    try {
      outputStream = new PrintWriter(new FileWriter(serverLogLocation, true));
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }

    //Appends to Log instead of overwriting it
    outputStream.append("");
    outputStream.flush();


      //We finished Loading the Log(This is so we don't rewrite to the log stuff that's already in it)
      this.finishedLoadingLog = this.loadLog();

    //Start timer
    start();
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name) {
    return newUser(createId(), name, Time.now());
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public UserInterest newUserInterest(Uuid owner, Uuid userId) {
    return newUserInterest(createId(), owner, userId, Time.now());
  }

  @Override
  public ConversationInterest newConversationInterest(Uuid owner, Uuid conversation) {
      return newConversationInterest(createId(), owner, conversation, Time.now());
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final ConversationPayload foundConversation = model.conversationPayloadById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);
      model.add(message);
      LOG.info("Message added: %s", message.id);


      updateConversationInterests(conversation);
      updateUserInterests(author, conversation);

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuid.equals(foundConversation.firstMessage, Uuid.NULL) ?
          message.id :
          foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.
      foundConversation.lastMessage = message.id;
    }

    //After recreating the state of the server, start storing the newMessage() command calls.
    if(finishedLoadingLog) {
      storedLogCommands.add("ADD-MESSAGE " + conversation + " " + id + " " + creationTime.inMs() + " " + author + " \"" + body + "\"");
    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime);
      model.add(user);

      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);

    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    //After recreating the state of the server, start storing the newUser() command calls.
    if(finishedLoadingLog) {
      storedLogCommands.add("ADD-USER " + user.id + " \"" + name + "\" " + creationTime.inMs());
    }

    return user;
  }


  @Override
  public void addMember(Uuid user, Uuid conversation) {
    ifIdFree(user) {
      throw new IOException("Must add an existing user");
    }
    final User userToAdd = model.userById().first(user);
    final ConversationPayload _conversation = model.conversationPayloadById().first(conversation);

    // confused on what I should set this to
    UserControl level();
  }

  @Override
  public void addOwner(Uuid user, Uuid conversation) {
    ifIdFree(user) {
      throw new IOException("Must add an existing user");
    }
    final User userToAdd = model.userById().first(user);
    final ConversationPayload _conversation = model.conversationPayloadById().first(conversation);

    // confused on what I should set this to
    UserControl level();
    
  }

  @Override
  public ConversationHeader newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    ConversationHeader conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new ConversationHeader(id, owner, creationTime, title);
      model.add(conversation);
      LOG.info("Conversation added: " + id);

      updateUserInterests(owner, conversation.id);
    }

    //After recreating the state of the server, start storing the newConversation() command calls.
    if(finishedLoadingLog) {
      storedLogCommands.add("ADD-CONVERSATION " + id + " \"" + title + "\" " + owner + " " + creationTime.inMs());
    }

    return conversation;
  }

  @Override
    public UserInterest newUserInterest(Uuid id, Uuid owner, Uuid userId, Time creationTime){

        final User foundUser = model.userById().first(owner);
        final User interestUser = model.userById().first(userId);

        UserInterest interest = null;

        if(foundUser != null && interestUser != null && isIdFree(id)) {
            interest = new UserInterest(id, owner, userId, creationTime);
            model.add(interest);
            LOG.info("User interest added: " + id);
        }

        return interest;
    }

    @Override
    public ConversationInterest newConversationInterest(Uuid id, Uuid owner, Uuid conversation, Time creationTime){

        final User foundUser = model.userById().first(owner);
        final ConversationHeader foundConversation = model.conversationById().first(conversation);

        ConversationInterest interest = null;

        if(foundUser != null && foundConversation != null && isIdFree(id)) {
            interest = new ConversationInterest(id, owner, conversation, creationTime);
            model.add(interest);
            LOG.info("Conversation interest added: " + id);
        }

        return interest;
    }

    private void updateUserInterests(Uuid author, Uuid conversation){
        final ConversationHeader foundConversation = model.conversationById().first(conversation);
        LOG.info("Current Conversation: " + foundConversation.title);

        // find all UserInterests with the current user (author) as an interest and
        // add the conversation to its list
        for (final UserInterest value : model.userInterestByUserId().all()) {
            if(Uuid.equals(author, value.interest)) {
                value.conversations.add(foundConversation);
                LOG.info("User Interest updated: " + value.conversations.toString());
            }
        }
    }

    private void updateConversationInterests(Uuid conversation){
       // final ConversationHeader foundConversation = model.conversationById().first(conversation);

        // find all ConversationInterests with the current conversation as an interest
        // and add to its message count
        for (final ConversationInterest value : model.conversationInterestByConversationId().all()) {
            if (Uuid.equals(conversation, value.interest)) {
                value.updateCount();
                LOG.info("Conversation Interest updated: " + conversation + ", " + value.messageCount);
            }
        }
    }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

     // Assuming that "randomUuid" is actually well implemented, this
     // loop should never be needed, but just incase make sure that the
     // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
           model.conversationById().first(id) != null ||
           model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }

  /** Discerns the logic for code values that load in the Log
   *
   * @return boolean
   * @throws IOException
   */
  private boolean loadLog() throws IOException{

    //Reads in the Log and stores the lines in an ArrayList
    logReader = new LogReader();
    fileLines = logReader.readFile();

    //Loads in the fileLines from the Log
    for(int i = 0; i < fileLines.size(); i++){
      LogLoader logLoader = new LogLoader(fileLines.get(i));

      //Load in User
      if(logLoader.findCommmand().equals("U")){
        String[] userInfo = logLoader.loadUser();
        this.newUser(Uuid.parse(userInfo[0]), userInfo[1], Time.now());
      }
      //Load in Conversation
      else if(logLoader.findCommmand().equals("C")){
        String[] userInfo = logLoader.loadConversation();
        this.newConversation(Uuid.parse(userInfo[0]), userInfo[1], Uuid.parse(userInfo[2]), Time.fromMs(Long.parseLong(userInfo[3])));
      }
      //Load in Message
      else if(logLoader.findCommmand().equals("M")){
        String[] userInfo = logLoader.loadMessage();
        this.newMessage(Uuid.parse(userInfo[1]), Uuid.parse(userInfo[3]), Uuid.parse(userInfo[0]), userInfo[4], Time.fromMs(Long.parseLong(userInfo[2])));
      }

    }

    return true;
  }


  Timer myTimer = new Timer();


  /** Flushes stored log line info to the user
   *
   *
   *
   */
  TimerTask task = new TimerTask(){
    public void run(){
      //Flushes data to document
      for(String line : storedLogCommands){
        outputStream.println(line);
      }

      //Pushes log commands lines to the LOG.
      outputStream.flush();

      //Clear the stored Log commands since we just flushed them to the log
      storedLogCommands.clear();
    }
  };

  /**Timer for Refresh Rate of loading files to LOG
   *
   * Flushes commands to log every 1 minute with 1 second delay.
   *
   * (Can change time for better optimization)
   *
   */
  public void start(){
    //Refreshes 10 seconds with a 1 second delay
    myTimer.scheduleAtFixedRate(task, 1000, 10000);

  }




}
