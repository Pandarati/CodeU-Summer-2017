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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import codeu.chat.common.BasicController;
import codeu.chat.common.BasicView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class ConversationContext {

  public final User user;
  public final ConversationHeader conversation;

  private final BasicView view;
  private final BasicController controller;

  public ConversationContext(User user,
                             ConversationHeader conversation,
                             BasicView view,
                             BasicController controller) {

    this.user = user;
    this.conversation = conversation;
    this.view = view;
    this.controller = controller;
  }

  public MessageContext add(String messageBody) {

    final Message message = controller.newMessage(user.id,
                                                  conversation.id,
                                                  messageBody);

    return message == null ?
        null :
        new MessageContext(message, view);
  }

  public MessageContext firstMessage() {

    // As it is possible for the conversation to have been updated, so fetch
    // a new copy.
    final ConversationPayload updated = getUpdated();

    return updated == null ?
        null :
        getMessage(updated.firstMessage);
  }

  public MessageContext lastMessage() {

    // As it is possible for the conversation to have been updated, so fetch
    // a new copy.
    final ConversationPayload updated = getUpdated();

    return updated == null ?
        null :
        getMessage(updated.lastMessage);
  }

  public boolean addMember(String name) {
    User member = findUser(name);
    if (member == null)
      return false;
    else {
      return controller.addMember(user.id, conversation.id, member.id);
    }
  }

  public boolean addOwner(String name) {
    User owner = findUser(name);
    if (owner == null)
      return false;
    else {
      return controller.addOwner(user.id, conversation.id, owner.id);
    }
  }

  public boolean removeMember(String name) {
    User member = findUser(name);
    if (member == null)
      return false;
    else {
      return controller.removeMember(user.id, conversation.id, member.id);
    }
  }

  public boolean removeOwner(String name) {
    User owner = findUser(name);
    if (owner == null)
      return false;
    else {
      return controller.removeOwner(user.id, conversation.id, owner.id);
    }
  }

  // Find the first user by entered name
  private User findUser(String name) {
    for (final User other : view.getUsers()) {
      String otherName = other.name;
      if (name.equals(otherName)) {
        return other;
      }
    }
    return null;
  }

  private ConversationPayload getUpdated() {
    final Collection<Uuid> ids = Arrays.asList(conversation.id);
    final Iterator<ConversationPayload> payloads = view.getConversationPayloads(ids).iterator();
    return payloads.hasNext() ? payloads.next() : null;
  }

  private MessageContext getMessage(Uuid id) {
    final Iterator<Message> messages = view.getMessages(Arrays.asList(id)).iterator();
    return messages.hasNext() ? new MessageContext(messages.next(), view) : null;
  }
}
