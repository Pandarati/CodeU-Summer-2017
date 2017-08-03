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

package codeu.chat.common;

import codeu.chat.util.Uuid;

// BASIC CONTROLLER
//
//   The controller component in the Model-View-Controller pattern. This
//   component is used to write information to the model where the model
//   is the current state of the server. Data returned from the controller
//   should be treated as read only data as manipulating any data returned
//   from the controller may have no effect on the server's state.
public interface BasicController {

  // NEW MESSAGE
  //
  //   Create a new message on the server. All parameters must be provided
  //   or else the server won't apply the change. If the operation is
  //   successful, a Message object will be returned representing the full
  //   state of the message on the server.
  Message newMessage(Uuid author, Uuid conversation, String body);

  // NEW USER
  //
  //   Create a new user on the server. All parameters must be provided
  //   or else the server won't apply the change. If the operation is
  //   successful, a User object will be returned representing the full
  //   state of the user on the server. Whether user names can be shared
  //   is undefined.
  User newUser(String name);

  // NEW CONVERSATION
  //
  //  Create a new conversation on the server. All parameters must be
  //  provided or else the server won't apply the change. If the
  //  operation is successful, a Conversation object will be returned
  //  representing the full state of the conversation on the server.
  //  Whether conversations can have the same title is undefined.
  ConversationHeader newConversation(String title, Uuid owner);

  // NEW INTEREST (in a User)
  //
  //  Create a new interest in a user on the server. All parameters must
  //  be provided or else the server won't apply the change. If the
  //  operation is successful, a Interest object will be returned
  //  representing the full state of the interest on the server.
  UserInterest newUserInterest(Uuid owner, Uuid userId);

  // NEW INTEREST (in a Conversation)
  //
  //  Create a new interest in a conversation on the server. All parameters
  //  must be provided or else the server won't apply the change. If the
  //  operation is successful, a Interest object will be returned
  //  representing the full state of the interest on the server.
  ConversationInterest newConversationInterest(Uuid owner, Uuid conversation);

  // REMOVE INTEREST (in a User)
  //
  //  Remove an existing interest in a User on the server. The parameter must
  //  correspond to an existing user or the value of false will be returned.
  //  If the operation is successful then the interest will be removed from
  //  the User's list of interests, and true is returned.
  boolean removeUserInterest(Uuid owner, Uuid interest);

  // REMOVE INTEREST (in a Conversation)
  //
  //  Remove an existing interest in a Conversation on the server. The
  //  parameter must correspond to an existing conversation or the value of false
  //  will be returned. If the operation is successful then the interest will be
  //  removed from the User's list of interests, and true is returned.
  boolean removeConversationInterest(Uuid owner, Uuid conversation);

  // STATUS UPDATE
  //
  // A status update must reset all interest activity, specifically the
  // the set of conversations for the user interest, and the missed messages
  // for the conversation interest.
  String statusUpdate(Uuid user);

  // ADD MEMBER
  //
  // Assigns a MEMBER role and corresponding permissions to the specified member.
  // The user must have the appropriate role/permission to add a member; if
  // they do the value of true is returned and the member role assigned.
  // Else the value of false is returned.
  boolean addMember(Uuid user, Uuid conversation, Uuid member);

  // ADD OWNER
  //
  // Assigns a OWNER role and corresponding permissions to the specified owner.
  // The user must have the appropriate role/permission to add a owner; if
  // they do the value of true is returned and the owner role assigned.
  // Else the value of false is returned.
  boolean addOwner(Uuid user, Uuid conversation, Uuid owner);

}
