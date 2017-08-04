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


import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import codeu.chat.common.UserControl;
import codeu.chat.common.UserInterest;
import codeu.chat.util.*;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  // Map to store all the Interests in the system, for every User there is a set
  // of Interests
  private HashMap<Uuid, HashSet<Interest>> interestMap = new HashMap<Uuid, HashSet<Interest>>();
  private HashMap<Uuid, HashSet<UserControl>> permissionMap = new HashMap<Uuid, HashSet<UserControl>>();

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
    final ConversationHeader conversationHeader = model.conversationById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);
      model.add(message);
      LOG.info("Message added: %s", message.id);


      updateConversationInterests(conversation);
      updateUserInterests(author, conversationHeader);

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
      interestMap.put(user.id, new HashSet<Interest>());

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
  public ConversationHeader newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    ConversationHeader conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new ConversationHeader(id, owner, creationTime, title);
      model.add(conversation);
      permissionMap.put(conversation.id, new HashSet<UserControl>());
      UserControl creator = new UserControl(owner);
      permissionMap.get(conversation.id).add(creator);
      LOG.info("Conversation added: " + id);

      updateUserInterests(owner, conversation);
    }

    //After recreating the state of the server, start storing the newConversation() command calls.
    if(finishedLoadingLog) {
      storedLogCommands.add("ADD-CONVERSATION " + id + " \"" + title + "\" " + owner + " " + creationTime.inMs());
    }

    return conversation;
  }

  // Serena's code
  @Override
  public boolean addMember(Uuid user, Uuid conversation, Uuid member) {

      final User foundUser = model.userById().first(user);
      final ConversationHeader foundConversation = model.conversationById().first(conversation);
      final User foundMember = model.userById().first(member);

      if(foundUser != null && foundConversation != null && foundMember != null) {
          // check that the role of the user is either Owner or Creator
          if (isCreator(user, conversation) || isOwner(user, conversation)) {
              // if so we add the member
              UserControl newMember = new UserControl(member, true);
              permissionMap.get(conversation).add(newMember);
              LOG.info("Permission granted: Member added");
              LOG.info(newMember.toString());
              return true;
          } else
              LOG.info("Permission denied");
      }
      return false;
  }

  @Override
  public boolean addOwner(Uuid user, Uuid conversation, Uuid owner) {

      final User foundUser = model.userById().first(user);
      final ConversationHeader foundConversation = model.conversationById().first(conversation);
      final User foundOwner = model.userById().first(owner);

      if(foundUser != null && foundConversation != null && foundOwner != null) {
          // check that the role of the user is Creator
          if (isCreator(user, conversation)) {
              // if so we add the owner
              UserControl newOwner = new UserControl(owner, false);
              permissionMap.get(conversation).add(newOwner);
              LOG.info("Permission granted: Owner added");
              LOG.info(newOwner.toString());
              return true;
          } else
              LOG.info("Permission denied");
      }

      return false;
  }

  @Override
  public boolean removeMember (Uuid user, Uuid conversation, Uuid member) {

      final User foundUser = model.userById().first(user);
      final ConversationHeader foundConversation = model.conversationById().first(conversation);
      final User foundMember = model.userById().first(member);

      if(foundUser != null && foundConversation != null && foundMember != null) {
          // check if the current user is an owner or a creator
          if(isOwner(user, conversation) || isCreator(user, conversation)) {
              // fetch the conversation's user controls
              Iterator<UserControl> iterator = permissionMap.get(conversation).iterator();
              while (iterator.hasNext()) {
                  UserControl current = iterator.next();

                  // check if the current UserControl is the owner to be removed
                  if (Uuid.equals(current.getUser(), member)) {
                      // check that the role of the user is in fact Member
                      if (current.isMember()) {
                          // if so we remove that member
                          LOG.info("Permission granted: Member removed");
                          return permissionMap.get(conversation).remove(current);
                      }
                  }
              }
          }
      }

      LOG.info("User Interest failed to be removed");
      return false;
  }

  @Override
  public boolean removeOwner (Uuid user, Uuid conversation, Uuid owner) {

      final User foundUser = model.userById().first(user);
      final ConversationHeader foundConversation = model.conversationById().first(conversation);
      final User foundOwner = model.userById().first(owner);

      if(foundUser != null && foundConversation != null && foundOwner != null) {
          // check if the current user is a creator
          if(isCreator(user, conversation)) {
              // fetch the conversation's user controls
              Iterator<UserControl> iterator = permissionMap.get(conversation).iterator();
              while (iterator.hasNext()) {
                  UserControl current = iterator.next();

                  // check if the current UserControl is the owner to be removed
                  if (Uuid.equals(current.getUser(), owner)) {
                      // check that the role of the user is in fact Owner
                      if (current.isOwner()) {
                          // if so we remove that owner
                          LOG.info("Permission granted: Owner removed");
                          return permissionMap.get(conversation).remove(current);
                      }
                  }
              }
          }
      }

      LOG.info("User Interest failed to be removed");
      return false;
  }

  private boolean isCreator(Uuid user, Uuid conversation){

      Iterator<UserControl> iterator = permissionMap.get(conversation).iterator();
      while (iterator.hasNext()) {
          UserControl current = iterator.next();

          // check if the current UserControl is the current user
          if (Uuid.equals(current.getUser(), user)) {
              // check if the role of the user is Creator
              if (current.isCreator()) {
                  return true;
              }
          }
      }
      return false;
  }

  private boolean isOwner(Uuid user, Uuid conversation){

      Iterator<UserControl> iterator = permissionMap.get(conversation).iterator();
      while (iterator.hasNext()) {
          UserControl current = iterator.next();

          // check if the current UserControl is the current user
          if (Uuid.equals(current.getUser(), user)) {
              // check if the role of the user is Owner
              if (current.isOwner()) {
                  return true;
              }
          }
      }
      return false;
  }

  @Override
  public boolean permissionJoinConversation(Uuid user, Uuid conversation) {

      final User foundUser = model.userById().first(user);
      final ConversationHeader foundConversation = model.conversationById().first(conversation);

      if(foundUser != null && foundConversation != null) {

          Iterator<UserControl> iterator = permissionMap.get(conversation).iterator();
          while (iterator.hasNext()) {
              UserControl current = iterator.next();

              // check if the current UserControl is the current user
              if (Uuid.equals(current.getUser(), user)) {
                  // because the user if present in the UserControl list
                  // they are either a MEMBER, OWNER, CREATOR
                  LOG.info("Has Permission to join conversation");
                  LOG.info(current.toString());
                  return true;
              }
          }
      }

      LOG.info("Permission denied");
      return false;
  }

  @Override
    public UserInterest newUserInterest(Uuid id, Uuid owner, Uuid userId, Time creationTime){

        final User foundUser = model.userById().first(owner);
        final User interestUser = model.userById().first(userId);

        UserInterest interest = null;

        if(foundUser != null && interestUser != null && isIdFree(id)) {
            interest = new UserInterest(id, owner, userId, new HashSet<ConversationHeader> (), creationTime);
            model.add(interest);
            interestMap.get(foundUser.id).add(interest);
            LOG.info("User interest added: " + id);
        }

        return interest;
    }

    @Override
    public ConversationInterest newConversationInterest(Uuid id, Uuid owner, Uuid conversation, Time creationTime) {

        final User foundUser = model.userById().first(owner);
        final ConversationHeader foundConversation = model.conversationById().first(conversation);

        ConversationInterest interest = null;

        if(foundUser != null && foundConversation != null && isIdFree(id)) {
            interest = new ConversationInterest(id, owner, conversation, 0, creationTime);
            model.add(interest);
            interestMap.get(foundUser.id).add(interest);
            LOG.info("Conversation interest added: " + id);
        }

        return interest;
    }

    @Override
    public boolean removeUserInterest (Uuid owner, Uuid interest) {

        final User foundUser = model.userById().first(owner);
        final User interestUser = model.userById().first(interest);

        if(foundUser != null && interestUser != null) {
            // fetch the current user's interests
            Iterator<Interest> iterator = interestMap.get(owner).iterator();
            while (iterator.hasNext()) {
                Interest current = iterator.next();

                // check if the interest is a UserInterest
                if (current.getClass() == UserInterest.class) {
                    // check if the interest is in the desired user
                    if (Uuid.equals(current.interest, interest)) {
                        // if so we remove that user interest
                        LOG.info("User Interest removed");
                        return interestMap.get(owner).remove(current);
                    }
                }
            }
        }

        LOG.info("User Interest failed to be removed");
        return false;
    }

    @Override
    public boolean removeConversationInterest(Uuid owner, Uuid conversation) {

        final User foundUser = model.userById().first(owner);
        final ConversationHeader foundConversation = model.conversationById().first(conversation);

        if(foundUser != null && foundConversation != null) {
            // fetch the current user's interests
            Iterator<Interest> iterator = interestMap.get(owner).iterator();
            while (iterator.hasNext()) {
                Interest current = iterator.next();

                // check if the interest is a ConversationInterest
                if (current.getClass() == ConversationInterest.class) {
                    // check if the interest is in the desired conversation
                    if (Uuid.equals(current.interest, conversation))
                        // if so we remove that conversation interest
                        LOG.info("Conversation Interest removed");
                    return interestMap.get(owner).remove(current);
                }
            }
        }

        LOG.info("Conversation Interest failed to be removed");
        return false;
    }

    private void updateUserInterests(Uuid author, ConversationHeader conversation) {

        for (HashSet<Interest> interests : interestMap.values()) {
            Iterator<Interest> iterator = interests.iterator();
            while(iterator.hasNext()){
                Interest current = iterator.next();

                // check if the interest is a UserInterest
                if (current.getClass() == UserInterest.class) {
                    // check if someone is interest in the current user
                    if(Uuid.equals(current.interest, author)) {
                        // if so we update that user's interest
                        current.addConversation(conversation);
                        LOG.info("User Interest updated: " + current.toString());
                    }
                }
            }
        }
    }

    private void updateConversationInterests(Uuid conversation) {

        for (HashSet<Interest> interests : interestMap.values()) {
            Iterator<Interest> iterator = interests.iterator();
            while(iterator.hasNext()){
                Interest current = iterator.next();

                // check if the interest is a ConversationInterest
                if (current.getClass() == ConversationInterest.class) {
                    // check if someone is interested in this conversation
                    if (Uuid.equals(current.interest, conversation)) {
                        // if so we update that user's interest
                        current.updateCount();
                        LOG.info("Conversation Interest updated: " + conversation + ", " + current.toString());
                    }
                }
            }
        }
    }

    @Override
    public String statusUpdate(Uuid user) {
        String conversationInterests = "";
        String userInterests = "";

        Iterator<Interest> iterator = interestMap.get(user).iterator();

        while(iterator.hasNext()){
            Interest current = iterator.next();

            if (current.getClass() == ConversationInterest.class) {
                conversationInterests = conversationInterests + current.toString() + "\n";
                // reset the information
                current.reset();
            }

            if (current.getClass() == UserInterest.class) {
                userInterests = userInterests + current.toString() + "\n";
                // reset the information
                 current.reset();
            }
        }

        LOG.info("Status Update completed");

        return "Status Update: \n\n--- update: user interests ---\n" + userInterests
                + "\n--- update: conversation interests ---\n" + conversationInterests ;
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
