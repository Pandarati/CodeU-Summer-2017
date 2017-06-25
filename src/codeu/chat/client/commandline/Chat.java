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

package codeu.chat.client.commandline;

import java.util.Scanner;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.io.IOException;

import codeu.chat.client.core.Context;
import codeu.chat.client.core.ConversationContext;
import codeu.chat.client.core.ConversationInterestContext;
import codeu.chat.client.core.MessageContext;
import codeu.chat.client.core.UserContext;
import codeu.chat.client.core.UserInterestContext;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationInterest;
import codeu.chat.common.Interest;
import codeu.chat.common.ServerInfo;
import codeu.chat.common.User;
import codeu.chat.common.UserInterest;
import codeu.chat.util.Time;
import codeu.chat.util.Tokenizer;
import codeu.chat.util.Uuid;

public final class Chat {

  // PANELS
  //
  // We are going to use a stack of panels to track where in the application
  // we are. The command will always be routed to the panel at the top of the
  // stack. When a command wants to go to another panel, it will add a new
  // panel to the top of the stack. When a command wants to go to the previous
  // panel all it needs to do is pop the top panel.
  private final Stack<Panel> panels = new Stack<>();
  private HashMap<Uuid, HashSet<Interest>> interestMap = new HashMap<Uuid, HashSet<Interest>>();

  public Chat(Context context) {
    this.panels.push(createRootPanel(context));
  }

  // HANDLE COMMAND
  //
  // Take a single line of input and parse a command from it. If the system
  // is willing to take another command, the function will return true. If
  // the system wants to exit, the function will return false.
  //
  public boolean handleCommand(String line) {

    final List<String> args = new ArrayList<String>();
    final Tokenizer tokenizer = new Tokenizer(line);

    try {
      for (String token = tokenizer.next(); token != null; token = tokenizer.next()) {
        args.add(token);
      }
    }
    catch(IOException ex) {
      System.out.println("ERROR: " + ex.getMessage());
    }

    final String command = args.get(0);

    args.remove(0);


    // Because "exit" and "back" are applicable to every panel, handle
    // those commands here to avoid having to implement them for each
    // panel.

    if ("exit".equals(command)) {
      // The user does not want to process any more commands
      return false;
    }

    // Do not allow the root panel to be removed.
    if ("back".equals(command) && panels.size() > 1) {
      panels.pop();
      return true;
    }

    if (panels.peek().handleCommand(command, args)) {
      // the command was handled
      return true;
    }

    // If we get to here it means that the command was not correctly handled
    // so we should let the user know. Still return true as we want to continue
    // processing future commands.
    System.out.println("ERROR: Unsupported command");
    return true;
  }

  // CREATE ROOT PANEL
  //
  // Create a panel for the root of the application. Root in this context means
  // the first panel and the only panel that should always be at the bottom of
  // the panels stack.
  //
  // The root panel is for commands that require no specific contextual information.
  // This is before a user has signed in. Most commands handled by the root panel
  // will be user selection focused.
  //
  private Panel createRootPanel(final Context context) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command to print a list of all commands and their description when
    // the user for "help" while on the root panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("ROOT MODE");
        System.out.println(" ");
        System.out.println("  User Commands: ");
        System.out.println("    u-list");
        System.out.println("      List all users.");
        System.out.println("    u-add <name>");
        System.out.println("      Add a new user with the given name.");
        System.out.println("    u-sign-in <name>");
        System.out.println("      Sign in as the user with the given name.");
        System.out.println(" ");
        System.out.println("  General Commands: ");
        System.out.println("    exit");
        System.out.println("      Exit the program.");
        System.out.println(" ");
      }
    });

    // U-LIST (user list)
    //
    // Add a command to print all users registered on the server when the user
    // enters "u-list" while on the root panel.
    //
    panel.register("u-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for (final UserContext user : context.allUsers()) {
          System.out.format(
              "USER %s (UUID:%s)\n",
              user.user.name,
              user.user.id);
        }
      }
    });

    // U-ADD (add user)
    //
    // Add a command to add and sign-in as a new user when the user enters
    // "u-add" while on the root panel.
    //
    panel.register("u-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        final String name = args.size() > 0 ? args.get(0) : "";
        if (name.length() > 0) {
          UserContext user = context.create(name);
          if (user == null) {
            System.out.println("ERROR: Failed to create new user");
          }
          else
            interestMap.put(user.user.id, new HashSet<Interest>());
        } else {
          System.out.println("ERROR: Missing <username>");
        }
      }
    });

    // U-SIGN-IN (sign in user)
    //
    // Add a command to sign-in as a user when the user enters "u-sign-in"
    // while on the root panel.
    //
    panel.register("u-sign-in", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        final String name = args.size() > 0 ? args.get(0) : "";
        if (name.length() > 0) {
          final UserContext user = findUser(name);
          if (user == null) {
            System.out.format("ERROR: Failed to sign in as '%s'\n", name);
          } else {
            panels.push(createUserPanel(user));
          }
        } else {
          System.out.println("ERROR: Missing <username>");
        }
      }


      // Find the first user with the given name and return a user context
      // for that user. If no user is found, the function will return null.
      private UserContext findUser(String name) {
        for (final UserContext user : context.allUsers()) {
          if (user.user.name.equals(name)) {
            return user;
          }
        }
        return null;
      }
    });

      /**
       * Command line command that will use the context that was created
       */
    panel.register("info", new Panel.Command(){
      @Override
      public void invoke(List<String> args){
        final ServerInfo info = context.getInfo();
        if(info == null){
          // Communicate error to user - the server did not send a valid info object.
          System.out.println("The server did not send a valid info object.");
        }
        else{
          //THIS NEEDS TO BE UPDATED
          System.out.println("RUNNNING INFO");
        }
      }
    });

    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }

  private Panel createUserPanel(final UserContext user) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command that will print a list of all commands and their
    // descriptions when the user enters "help" while on the user panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("USER MODE");
        System.out.println(" ");
        System.out.println("  Conversation Commands: ");
        System.out.println("    c-list");
        System.out.println("      List all conversations that the current user can interact with.");
        System.out.println("    i-list");
        System.out.println("      List all interests that the current user can interact with.");
        System.out.println("    c-add <title>");
        System.out.println("      Add a new conversation with the given title and join it as the current user.");
        System.out.println("    c-join <title>");
        System.out.println("      Join the conversation as the current user.");
        System.out.println(" ");
        System.out.println("  Interest Commands: ");
        System.out.println("    i-user-add <name>");
        System.out.println("      Add a new interest in a given user and follow their activity.");
        System.out.println("    i-convo-add <title>");
        System.out.println("      Add a new interest in a given conversation title and follow its activity.");
        System.out.println("    i-user-update");
        System.out.println("      Get a status update on a user and their activity.");
        System.out.println("    i-convo-update");
        System.out.println("      Get a status update on a conversation and its activity.");
        System.out.println(" ");
        System.out.println("  General Commands: ");
        System.out.println("    info");
        System.out.println("      Display all info for the current user");
        System.out.println("    back");
        System.out.println("      Go back to ROOT MODE.");
        System.out.println("    exit");
        System.out.println("      Exit the program.");
        System.out.println(" ");
      }
    });

    // C-LIST (list conversations)
    //
    // Add a command that will print all conversations when the user enters
    // "c-list" while on the user panel.
    //
    panel.register("c-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for (final ConversationContext conversation : user.conversations()) {
          System.out.format(
              "CONVERSATION %s (UUID:%s)\n",
              conversation.conversation.title,
              conversation.conversation.id);
        }
      }
    });

    // C-ADD (add conversation)
    //
    // Add a command that will create and join a new conversation when the user
    // enters "c-add" while on the user panel.
    //
    panel.register("c-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        final String name = args.size() > 0 ? args.get(0) : "";
        if (name.length() > 0) {
          final ConversationContext conversation = user.start(name);
          if (conversation == null) {
            System.out.println("ERROR: Failed to create new conversation");
          } else {
            updateInterests(conversation.conversation);
            panels.push(createConversationPanel(conversation));
          }
        } else {
          System.out.println("ERROR: Missing <title>");
        }
      }

      private void updateInterests(ConversationHeader conversation){
        for (HashSet<Interest> interests : interestMap.values()) {
          Iterator<Interest> iterator = interests.iterator();
          while(iterator.hasNext()){
            Interest current = iterator.next();

            if (current.getClass() == UserInterest.class) {
              if(Uuid.equals(current.interest, user.user.id))
                System.out.println("User found");
              current.addConversation(conversation);
            }
          }
        }
      }
    });

    // C-JOIN (join conversation)
    //
    // Add a command that will joing a conversation when the user enters
    // "c-join" while on the user panel.
    //
    panel.register("c-join", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        final String name = args.size() > 0 ? args.get(0) : "";
        if (name.length() > 0) {
          final ConversationContext conversation = find(name);
          if (conversation == null) {
            System.out.format("ERROR: No conversation with name '%s'\n", name);
          } else {
            panels.push(createConversationPanel(conversation));
          }
        } else {
          System.out.println("ERROR: Missing <title>");
        }
      }

      // Find the first conversation with the given name and return its context.
      // If no conversation has the given name, this will return null.
      private ConversationContext find(String title) {
        for (final ConversationContext conversation : user.conversations()) {
          if (title.equals(conversation.conversation.title)) {
            return conversation;
          }
        }
        return null;
      }
    });

    //i-list (list interests)
    //
    // Add a command that will print all interests when the user enters
    // "i-list" while on the user panel.
    panel.register("i-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for (HashSet<Interest> interests : interestMap.values()){
          System.out.println("--- user interests ---");
          System.out.println(interests.toString());
          System.out.println("--- conversation interests ---");
          System.out.println(interests.toString());
        }
      }
    });

    // i-user-add (add user interest)
    //
    // Add a command to add a new interest when the user enters
    // userI-add while on the interest panel.
    //
    panel.register("i-user-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        // also needs to check that interest is not already added
        final String name = args.size() > 0 ? args.get(0) : "";
        if (name.length() > 0 ) {
          final UserInterestContext interestContext = user.addUserInterest(name);
          final UserContext userInterest = findUser(name);

          // Find the first user by entered name
          if (interestContext == null || userInterest == null) {
            System.out.println("ERROR: Failed to create new user interest");
          }
          else {
            UserInterest interest = new UserInterest(user.user.id, userInterest.user.id, Time.now());
            interestMap.get(user.user.id).add(interest);
          }
        } else {
          System.out.println("ERROR: Enter valid user name");
        }
      }

      private UserContext findUser(String name) {
        for (final UserContext other : user.users()) {
          User otherUser = other.user;
          if (name.equals(otherUser.name)) {
            return other;
          }
        }
        return null;
      }
    });

    // i-convo-add (add conversation interest)
    //
    // Add a command to add a new interest when the user enters
    // convI-add while on the interest panel.
    //
    panel.register("i-convo-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        // also needs to check that interest is not already added
        final String title = args.size() > 0 ? args.get(0) : "";
        if (title.length() > 0 ) {
          final ConversationInterestContext conversationContext = user.addConversationInterest(title);
          final ConversationContext conversationInterest = findConversation(title);

          if (conversationContext == null || conversationInterest == null) {
            System.out.println("ERROR: Failed to create new conversation interest");
          }
          else {
            ConversationInterest interest =
                    new ConversationInterest(user.user.id, conversationInterest.conversation.id, Time.now());
            interestMap.get(user.user.id).add(interest);
          }
        } else {
          System.out.println("ERROR: Enter valid conversation title");
        }
      }

      private ConversationContext findConversation(String title) {
        for (final ConversationContext other : user.conversations()) {
          if (title.equals(other.conversation.title)) {
            return other;
          }
        }
        return null;
      }
    });

    // i-status-update (status update for a user's interests)
    //
    // Add a command to add a new interest when the user enters
    // convI-add while on the interest panel.
    //
    panel.register("i-status-update", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {

        Iterator<Interest> iterator = interestMap.get(user.user.id).iterator();
        String userInterests = "";
        String conversationInterests = "";
        while(iterator.hasNext()){
          Interest current = iterator.next();

          if (current.getClass() == ConversationInterest.class) {
            conversationInterests = conversationInterests + current.toString() + "\n";
            current.reset();
          }

          if (current.getClass() == UserInterest.class) {
            userInterests = userInterests + current.toString() + "\n";
            current.reset();
          }
        }

        System.out.println("--- update: user interests ---");
        System.out.println(userInterests);
        System.out.println("--- update: conversation interests ---");
        System.out.println(conversationInterests);
      }

    });

    // INFO
    //
    // Add a command that will print info about the current context when the
    // user enters "info" while on the user panel.
    //
    panel.register("info", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("User Info:");
        System.out.format("  Name : %s\n", user.user.name);
        System.out.format("  Id   : UUID:%s\n", user.user.id);
      }
    });

    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }

  private Panel createConversationPanel(final ConversationContext conversation) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command that will print all the commands and their descriptions
    // when the user enters "help" while on the conversation panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("USER MODE");
        System.out.println(" ");
        System.out.println("  Message Commands: ");
        System.out.println("    m-list");
        System.out.println("      List all messages in the current conversation.");
        System.out.println("    m-add <message>");
        System.out.println("      Add a new message to the current conversation as the current user.");
        System.out.println("    info");
        System.out.println("      Display all info about the current conversation.");
        System.out.println(" ");
        System.out.println("  General Commands: ");
        System.out.println("    back");
        System.out.println("      Go back to USER MODE.");
        System.out.println("    exit");
        System.out.println("      Exit the program.");
        System.out.println(" ");
      }
    });

    // M-LIST (list messages)
    //
    // Add a command to print all messages in the current conversation when the
    // user enters "m-list" while on the conversation panel.
    //
    panel.register("m-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("--- start of conversation ---");
        for (MessageContext message = conversation.firstMessage();
                            message != null;
                            message = message.next()) {
          System.out.println();
          System.out.format("USER : %s\n", message.message.author);
          System.out.format("SENT : %s\n", message.message.creation);
          System.out.println();
          System.out.println(message.message.content);
          System.out.println();
        }
        System.out.println("---  end of conversation  ---");
      }
    });

    // M-ADD (add message)
    //
    // Add a command to add a new message to the current conversation when the
    // user enters "m-add" while on the conversation panel.
    //
    panel.register("m-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        final String message = args.size() > 0 ? args.get(0) : "";
        if (message.length() > 0) {
          conversation.add(message);
          updateInterests();
        } else {
          System.out.println("ERROR: Messages must contain text");
        }
      }

      private void updateInterests(){
        for (HashSet<Interest> interests : interestMap.values()) {
          Iterator<Interest> iterator = interests.iterator();
          while(iterator.hasNext()){
            Interest current = iterator.next();

            if (current.getClass() == ConversationInterest.class) {
              if (Uuid.equals(current.interest, conversation.conversation.id)) {
                current.updateCount();
                System.out.println("Conversation found: ");
              }
            }

            if (current.getClass() == UserInterest.class) {
              if(Uuid.equals(current.interest, conversation.user.id))
                System.out.println("User found: ");
                current.addConversation(conversation.conversation);
            }

          }
        }
      }
    });

    // INFO
    //
    // Add a command to print info about the current conversation when the user
    // enters "info" while on the conversation panel.
    //
    panel.register("info", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("Conversation Info:");
        System.out.format("  Title : %s\n", conversation.conversation.title);
        System.out.format("  Id    : UUID:%s\n", conversation.conversation.id);
        System.out.format("  Owner : %s\n", conversation.conversation.owner);
      }
    });

    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }
}
