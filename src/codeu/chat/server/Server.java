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

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.ConversationInterest;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.Relay;
import codeu.chat.common.Secret;
import codeu.chat.common.User;
import codeu.chat.common.UserInterest;
import codeu.chat.common.ServerInfo;
import codeu.chat.util.LogReader;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Timeline;
import codeu.chat.util.Uuid;
import codeu.chat.util.LogLoader;
import codeu.chat.util.connections.Connection;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Server {

  private interface Command {
    void onMessage(InputStream in, OutputStream out) throws IOException;
  }

  private static final Logger.Log LOG = Logger.newLog(Server.class);

  private static final int RELAY_REFRESH_MS = 5000;  // 5 seconds

  private final Timeline timeline = new Timeline();

  private final Map<Integer, Command> commands = new HashMap<>();

  private final Uuid id;
  private final Secret secret;

  private final Model model = new Model();
  private final View view = new View(model);
  private final Controller controller;

  private final Relay relay;
  private Uuid lastSeen = Uuid.NULL;

  //Creates an instance of ServerInfo that helps keep the Time and version of when the server started
  private static ServerInfo serverInfo;

  //Log Files Info
  private static String serverLogLocation = "C:\\git\\CodeU-Summer-2017\\serverdata\\serverLog.txt";
  public PrintWriter outputStream;

  public Server(final Uuid id, final Secret secret, final Relay relay) throws IOException{

    this.id = id;
    this.secret = secret;
    this.controller = new Controller(id, model);
    this.relay = relay;


    //Connects the Log to the Server
    try {
        outputStream = new PrintWriter(new FileWriter(serverLogLocation, true));
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }

    //Set-ups OutputStream to append to current Log Information
    outputStream.append("");
    outputStream.flush();


    // New Message - A client wants to add a new message to the back end.
    this.commands.put(NetworkCode.NEW_MESSAGE_REQUEST, new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Uuid author = Uuid.SERIALIZER.read(in);
        final Uuid conversation = Uuid.SERIALIZER.read(in);
        final String content = Serializers.STRING.read(in);

        final Message message = controller.newMessage(author, conversation, content);

        Serializers.INTEGER.write(out, NetworkCode.NEW_MESSAGE_RESPONSE);
        Serializers.nullable(Message.SERIALIZER).write(out, message);

        timeline.scheduleNow(createSendToRelayEvent(
            author,
            conversation,
            message.id));
      }
    });

    // New User - A client wants to add a new user to the back end.
    this.commands.put(NetworkCode.NEW_USER_REQUEST,  new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final String name = Serializers.STRING.read(in);
        final User user = controller.newUser(name);

        Serializers.INTEGER.write(out, NetworkCode.NEW_USER_RESPONSE);
        Serializers.nullable(User.SERIALIZER).write(out, user);


      }
    });

    // New Conversation - A client wants to add a new conversation to the back end.
    this.commands.put(NetworkCode.NEW_CONVERSATION_REQUEST,  new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final String title = Serializers.STRING.read(in);
        final Uuid owner = Uuid.SERIALIZER.read(in);
        final ConversationHeader conversation = controller.newConversation(title, owner);

        Serializers.INTEGER.write(out, NetworkCode.NEW_CONVERSATION_RESPONSE);
        Serializers.nullable(ConversationHeader.SERIALIZER).write(out, conversation);
      }
    });

    // Add Member - A creator or owner wants to add a new member to a conversation.
    this.commands.put(NetworkCode.ADD_MEMBER_REQUEST,  new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Uuid user = Uuid.SERIALIZER.read(in);
        final Uuid conversationId = Uuid.SERIALIZER.read(in);
        final Uuid member = Uuid.SERIALIZER.read(in);
        final boolean added = controller.addMember(user, conversationId, member);

        Serializers.INTEGER.write(out, NetworkCode.ADD_MEMBER_RESPONSE);
        Serializers.nullable(Serializers.BOOLEAN).write(out, added);
      }
    });

    // Add Owner - A creator wants to add a new owner to a conversation.
    this.commands.put(NetworkCode.ADD_OWNER_REQUEST,  new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Uuid user = Uuid.SERIALIZER.read(in);
        final Uuid conversationId = Uuid.SERIALIZER.read(in);
        final Uuid owner = Uuid.SERIALIZER.read(in);
        final boolean added = controller.addOwner(user, conversationId, owner);

        Serializers.INTEGER.write(out, NetworkCode.ADD_OWNER_RESPONSE);
        Serializers.nullable(Serializers.BOOLEAN).write(out, added);

      }
    });

    // New Interest - A client wants to add a new interest in a user to the back end.
    this.commands.put(NetworkCode.NEW_USER_INTEREST_REQUEST,  new Command() {
        @Override
        public void onMessage(InputStream in, OutputStream out) throws IOException {

            final Uuid owner = Uuid.SERIALIZER.read(in);
            final Uuid userId = Uuid.SERIALIZER.read(in);
            final UserInterest interest = controller.newUserInterest(owner, userId);

            Serializers.INTEGER.write(out, NetworkCode.NEW_USER_INTEREST_RESPONSE);
            Serializers.nullable(UserInterest.SERIALIZER).write(out, interest);
        }
    });

    // New Interest - A client wants to add a new interest in a conversation to the back end
    this.commands.put(NetworkCode.NEW_CONVERSATION_INTEREST_REQUEST,  new Command() {
        @Override
        public void onMessage(InputStream in, OutputStream out) throws IOException {

            final Uuid owner = Uuid.SERIALIZER.read(in);
            final Uuid conversationId = Uuid.SERIALIZER.read(in);
            final ConversationInterest interest = controller.newConversationInterest(owner, conversationId);

            Serializers.INTEGER.write(out, NetworkCode.NEW_CONVERSATION_INTEREST_RESPONSE);
            Serializers.nullable(ConversationInterest.SERIALIZER).write(out, interest);
        }
    });

    // Remove Interest - A client wants to remove an interest in a user from the back end
    this.commands.put(NetworkCode.REMOVE_USER_INTEREST_REQUEST,  new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Uuid owner = Uuid.SERIALIZER.read(in);
        final Uuid userId = Uuid.SERIALIZER.read(in);
        final boolean removed = controller.removeUserInterest(owner, userId);

        Serializers.INTEGER.write(out, NetworkCode.REMOVE_USER_INTEREST_RESPONSE);
        Serializers.nullable(Serializers.BOOLEAN).write(out, removed);
      }
    });

    // Remove Interest - A client wants to remove an interest in a conversation from the back end
    this.commands.put(NetworkCode.REMOVE_CONVERSATION_INTEREST_REQUEST,  new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Uuid owner = Uuid.SERIALIZER.read(in);
        final Uuid conversationId = Uuid.SERIALIZER.read(in);
        final boolean removed = controller.removeConversationInterest(owner, conversationId);

        Serializers.INTEGER.write(out, NetworkCode.REMOVE_CONVERSATION_INTEREST_RESPONSE);
        Serializers.nullable(Serializers.BOOLEAN).write(out, removed);
      }
    });

    // Status Update - A client wants a status update on their interests from the back end
    this.commands.put(NetworkCode.STATUS_UPDATE_REQUEST,  new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Uuid user = Uuid.SERIALIZER.read(in);
        final String update = controller.statusUpdate(user);

        Serializers.INTEGER.write(out, NetworkCode.STATUS_UPDATE_RESPONSE);
        Serializers.nullable(Serializers.STRING).write(out, update);
      }
    });

    // Get Users - A client wants to get all the users from the back end.
    this.commands.put(NetworkCode.GET_USERS_REQUEST, new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Collection<User> users = view.getUsers();

        Serializers.INTEGER.write(out, NetworkCode.GET_USERS_RESPONSE);
        Serializers.collection(User.SERIALIZER).write(out, users);
      }
    });

    // Get Conversations - A client wants to get all the conversations from the back end.
    this.commands.put(NetworkCode.GET_ALL_CONVERSATIONS_REQUEST, new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Collection<ConversationHeader> conversations = view.getConversations();

        Serializers.INTEGER.write(out, NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE);
        Serializers.collection(ConversationHeader.SERIALIZER).write(out, conversations);
      }
    });

    /*
    // Get User Interests - A client wants to get all the user interests from the back end.
    this.commands.put(NetworkCode.GET_ALL_USER_INTERESTS_REQUEST, new Command() {
        @Override
        public void onMessage(InputStream in, OutputStream out) throws IOException {

            final Collection<UserInterest> userInterests = view.getUserInterests();

            Serializers.INTEGER.write(out, NetworkCode.GET_ALL_USER_INTERESTS_RESPONSE);
            Serializers.collection(UserInterest.SERIALIZER).write(out, userInterests);
          }
      });

    // Get Conversation Interests - A client wants to get all the conversation interests from the back end.
    this.commands.put(NetworkCode.GET_ALL_CONVERSATION_INTERESTS_REQUEST, new Command() {
        @Override
        public void onMessage(InputStream in, OutputStream out) throws IOException {

            final Collection<ConversationInterest> conversationInterests = view.getConversationInterests();

            Serializers.INTEGER.write(out, NetworkCode.GET_ALL_CONVERSATION_INTERESTS_RESPONSE);
            Serializers.collection(ConversationInterest.SERIALIZER).write(out, conversationInterests);
          }
      });
      */

    // Get Server Request - A client whats to get all the request to the server from the back end.
    this.commands.put(NetworkCode.SERVER_INFO_REQUEST, new Command(){
        @Override
        public void onMessage(InputStream in, OutputStream out) throws IOException{
            Serializers.INTEGER.write(out, NetworkCode.SERVER_INFO_RESPONSE);

            try{
              serverInfo = new ServerInfo();
            }catch (IOException ex){
              LOG.error(ex, "There was a problem with parsing the ServerInfo.");
            }

            // Writes out the ServerInfo Version and StartTime to the user
            //Serializer OUT must be in the same order as Serializer IN
            Uuid.SERIALIZER.write(out, serverInfo.getVersion());
            Time.SERIALIZER.write(out, serverInfo.getStartTime());
        }
    });

    // .
    //
    //
    // .0Conversations By Id - A client wants to get a subset of the converations from
    //                           the back end. Normally this will be done after calling
    //                           Get Conversations to get all the headers and now the client
    //                           wants to get a subset of the payloads.
    this.commands.put(NetworkCode.GET_CONVERSATIONS_BY_ID_REQUEST, new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);
        final Collection<ConversationPayload> conversations = view.getConversationPayloads(ids);

        Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_ID_RESPONSE);
        Serializers.collection(ConversationPayload.SERIALIZER).write(out, conversations);
      }
    });

    // Get Messages By Id - A client wants to get a subset of the messages from the back end.
    this.commands.put(NetworkCode.GET_MESSAGES_BY_ID_REQUEST, new Command() {
      @Override
      public void onMessage(InputStream in, OutputStream out) throws IOException {

        final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);
        final Collection<Message> messages = view.getMessages(ids);

        Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_ID_RESPONSE);
        Serializers.collection(Message.SERIALIZER).write(out, messages);
      }
    });

    // add status update commands
      // view.statusUpdate

    this.timeline.scheduleNow(new Runnable() {
      @Override
      public void run() {
        try {

          LOG.info("Reading update from relay...");

          for (final Relay.Bundle bundle : relay.read(id, secret, lastSeen, 32)) {
            onBundle(bundle);
            lastSeen = bundle.id();
          }

        } catch (Exception ex) {

          LOG.error(ex, "Failed to read update from relay.");

        }

        timeline.scheduleIn(RELAY_REFRESH_MS, this);
      }
    });
  }

  public void handleConnection(final Connection connection) {
    timeline.scheduleNow(new Runnable() {
      @Override
      public void run() {
        try {

          LOG.info("Handling connection...");

          final int type = Serializers.INTEGER.read(connection.in());
          final Command command = commands.get(type);

          if (command == null) {
            // The message type cannot be handled so return a dummy message.
            Serializers.INTEGER.write(connection.out(), NetworkCode.NO_MESSAGE);
            LOG.info("Connection rejected");
          } else {
            command.onMessage(connection.in(), connection.out());
            LOG.info("Connection accepted");
          }

        } catch (Exception ex) {

          LOG.error(ex, "Exception while handling connection.");

        }

        try {
          connection.close();
        } catch (Exception ex) {
          LOG.error(ex, "Exception while closing connection.");
        }
      }
    });
  }

  private void onBundle(Relay.Bundle bundle) {

    final Relay.Bundle.Component relayUser = bundle.user();
    final Relay.Bundle.Component relayConversation = bundle.conversation();
    final Relay.Bundle.Component relayMessage = bundle.user();

    User user = model.userById().first(relayUser.id());

    if (user == null) {
      user = controller.newUser(relayUser.id(), relayUser.text(), relayUser.time());
    }

    ConversationHeader conversation = model.conversationById().first(relayConversation.id());

    if (conversation == null) {

      // As the relay does not tell us who made the conversation - the first person who
      // has a message in the conversation will get ownership over this server's copy
      // of the conversation.
      conversation = controller.newConversation(relayConversation.id(),
                                                relayConversation.text(),
                                                user.id,
                                                relayConversation.time());
    }

    Message message = model.messageById().first(relayMessage.id());

    if (message == null) {
      message = controller.newMessage(relayMessage.id(),
                                      user.id,
                                      conversation.id,
                                      relayMessage.text(),
                                      relayMessage.time());
    }
  }

  private Runnable createSendToRelayEvent(final Uuid userId,
                                          final Uuid conversationId,
                                          final Uuid messageId) {
    return new Runnable() {
      @Override
      public void run() {
        final User user = view.findUser(userId);
        final ConversationHeader conversation = view.findConversation(conversationId);
        final Message message = view.findMessage(messageId);
        relay.write(id,
                    secret,
                    relay.pack(user.id, user.name, user.creation),
                    relay.pack(conversation.id, conversation.title, conversation.creation),
                    relay.pack(message.id, message.content, message.creation));
      }
    };
  }
}
