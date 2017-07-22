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
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  // Map to store all the Interests in the system, for every User there is a set
  // of Interests
  private HashMap<Uuid, HashSet<Interest>> interestMap = new HashMap<Uuid, HashSet<Interest>>();

  private final Model model;
  private final Uuid.Generator uuidGenerator;

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
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

    return user;
  }

  @Override
  public ConversationHeader newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    ConversationHeader conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new ConversationHeader(id, owner, creationTime, title);
      model.add(conversation);
      LOG.info("Conversation added: " + id);

      updateUserInterests(owner, conversation);
    }

    return conversation;
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
    public ConversationInterest newConversationInterest(Uuid id, Uuid owner, Uuid conversation, Time creationTime){

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

   // @Override
    public boolean removeUserInterest (Uuid owner, Uuid interest){

        final User foundUser = model.userById().first(owner);
        final User interestUser = model.userById().first(interest);

        boolean removed = false;

        if(foundUser != null && interestUser != null) {
            removed = interestMap.get(owner).remove(interest);
            if(removed)
                LOG.info("User Interest removed");
        } else
            LOG.info("User Interest failed to be removed");

        return removed;
    }

    //@Override
    public boolean removeConversationInterest(Uuid owner, Uuid conversation){

        final User foundUser = model.userById().first(owner);
        final ConversationHeader foundConversation = model.conversationById().first(conversation);

        boolean removed = false;

        if(foundUser != null && foundConversation != null) {
            removed = interestMap.get(owner).remove(conversation);
            if(removed)
                LOG.info("Conversation Interest removed");
        } else
            LOG.info("Conversation Interest failed to be removed");

        return removed;
    }

    private void updateUserInterests(Uuid author, ConversationHeader conversation){

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

    private void updateConversationInterests(Uuid conversation){

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
                + "\n\n--- update: conversation interests ---\n" + conversationInterests ;
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

}
