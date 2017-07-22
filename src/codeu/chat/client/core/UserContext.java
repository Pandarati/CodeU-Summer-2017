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

package codeu.chat.client.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import codeu.chat.common.BasicController;
import codeu.chat.common.BasicView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationInterest;
import codeu.chat.common.Interest;
import codeu.chat.common.User;
import codeu.chat.common.UserInterest;
import codeu.chat.util.Uuid;

public final class UserContext {

  public final User user;
  private final BasicView view;
  private final BasicController controller;

  public UserContext(User user, BasicView view, BasicController controller) {
    this.user = user;
    this.view = view;
    this.controller = controller;
  }

  public Iterable<UserContext> users() {

    // Use all the ids to get all users and convert them to User Contexts.
    final Collection<UserContext> all = new ArrayList<>();
    for (final User other : view.getUsers()) {
      all.add(new UserContext(other, view, controller));
    }

    return all;
  }

  public UserInterest addUserInterest(String name) {
    User other = findUser(name);
    if (other == null)
      return null;
    else {
      final UserInterest interest = controller.newUserInterest(user.id, other.id);
      return interest;
    }
  }

  public ConversationInterest addConversationInterest(String title) {
    ConversationHeader conversation = findConversation(title);
    if (conversation == null)
      return null;
    else {
      final ConversationInterest interest = controller.newConversationInterest(user.id, conversation.id);
      return interest;
    }
  }

  // Find the first user by entered name
  public User findUser(String name) {
    for (final User other : view.getUsers()) {
      String otherName = other.name;
      if (name.equals(otherName)) {
        return other;
      }
    }
    return null;
  }

  // Find the first conversation entered by title
  public ConversationHeader findConversation(String title) {
    for (final ConversationHeader conversation : view.getConversations()) {
      if (title.equals(conversation.title)) {
        return conversation;
      }
    }
    return null;
  }

  public String statusUpdate() {
    return controller.statusUpdate(user.id);
  }

  public ConversationContext start(String name) {
    final ConversationHeader conversation = controller.newConversation(name, user.id);
    return conversation == null ?
          null :
          new ConversationContext(user, conversation, view, controller);
  }

  public Iterable<ConversationContext> conversations() {

    // Use all the ids to get all the conversations and convert them to
    // Conversation Contexts.
    final Collection<ConversationContext> all = new ArrayList<>();
    for (final ConversationHeader conversation : view.getConversations()) {
      all.add(new ConversationContext(user, conversation, view, controller));
    }

    return all;
  }
}
