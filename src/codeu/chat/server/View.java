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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import java.io.IOException;

import codeu.chat.common.BasicView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.ConversationInterest;
import codeu.chat.common.Message;
import codeu.chat.common.SinglesView;
import codeu.chat.common.User;
import codeu.chat.common.UserInterest;
import codeu.chat.common.ServerInfo;
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.StoreAccessor;

public final class View implements BasicView, SinglesView {

  private final static Logger.Log LOG = Logger.newLog(View.class);

  private final Model model;
  private static ServerInfo info;

  public View(Model model) {
    this.model = model;
    try
    {
      info = new ServerInfo();
    }
    catch (IOException ex)
    {
      System.out.println("ERROR: Failed to create ServerInfo, " + ex.getMessage());
    }
  }


  @Override
  public Collection<User> getUsers() {
    return all(model.userById());
  }

  @Override
  public Collection<ConversationHeader> getConversations() {
    return all(model.conversationById());
  }

  @Override
  public Collection<ConversationPayload> getConversationPayloads(Collection<Uuid> ids) {
    return intersect(model.conversationPayloadById(), ids);
  }

  @Override
  public Collection<Message> getMessages(Collection<Uuid> ids) {
    return intersect(model.messageById(), ids);
  }

  @Override
  public ServerInfo getInfo(){
    return info;
  }

  @Override
  public Collection<UserInterest> getUserInterests() { return  all(model.userInterestById()); }

  @Override
  public Collection<ConversationInterest> getConversationInterests() {
    return all(model.conversationInterestById());
  }

  @Override
  public User findUser(Uuid id) { return model.userById().first(id); }

  @Override
  public ConversationHeader findConversation(Uuid id) { return model.conversationById().first(id); }

  @Override
  public Message findMessage(Uuid id) { return model.messageById().first(id); }

  @Override
  public UserInterest findUserInterest(Uuid id) { return model.userInterestById().first(id); }

  @Override
  public ConversationInterest findConversationInterest(Uuid id) { return model.conversationInterestById().first(id); }

  private static <S,T> Collection<T> all(StoreAccessor<S,T> store) {

    final Collection<T> all = new ArrayList<>();

    for (final T value : store.all()) {
        all.add(value);
    }

    return all;
  }

  private static <T> Collection<T> intersect(StoreAccessor<Uuid, T> store, Collection<Uuid> ids) {

    // Use a set to hold the found users as this will prevent duplicate ids from
    // yielding duplicates in the result.

    final Collection<T> found = new HashSet<>();

    for (final Uuid id : ids) {

      final T t = store.first(id);

      if (t == null) {
        LOG.warning("Unmapped id %s", id);
      } else if (found.add(t)) {
        // do nothing
      } else {
        LOG.warning("Duplicate id %s", id);
      }
    }

    return found;
  }
}
