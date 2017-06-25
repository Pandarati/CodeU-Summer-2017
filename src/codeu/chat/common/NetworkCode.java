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

public final class NetworkCode {

  public static final int
      NO_MESSAGE = 0,
      GET_USERS_REQUEST = 1,
      GET_USERS_RESPONSE = 2,
      GET_ALL_CONVERSATIONS_REQUEST = 3,
      GET_ALL_CONVERSATIONS_RESPONSE = 4,
      GET_CONVERSATIONS_BY_ID_RESPONSE = 5,
      GET_CONVERSATIONS_BY_ID_REQUEST = 6,
      GET_MESSAGES_BY_ID_REQUEST = 7,
      GET_MESSAGES_BY_ID_RESPONSE = 8,
      NEW_MESSAGE_REQUEST = 9,
      NEW_MESSAGE_RESPONSE = 10,
      NEW_USER_REQUEST = 11,
      NEW_USER_RESPONSE = 12,
      NEW_CONVERSATION_REQUEST = 13,
      NEW_CONVERSATION_RESPONSE = 14,
      NEW_USER_INTEREST_REQUEST = 15,
      NEW_USER_INTEREST_RESPONSE = 16,
      NEW_CONVERSATION_INTEREST_REQUEST = 17,
      NEW_CONVERSATION_INTEREST_RESPONSE = 18,
      GET_ALL_USER_INTERESTS_REQUEST = 19,
      GET_ALL_USER_INTERESTS_RESPONSE = 20,
      GET_ALL_CONVERSATION_INTERESTS_REQUEST = 21,
      GET_ALL_CONVERSATION_INTERESTS_RESPONSE = 22,
      GET_USER_UPDATE_REQUEST = 23,
      GET_USER_UPDATE_RESPONSE = 24,
      GET_CONVERSATION_UPDATE_REQUEST = 25,
      GET_CONVERSATION_UPDATE_RESPONSE = 26,
      RELAY_READ_REQUEST = 27,
      RELAY_READ_RESPONSE = 28,
      RELAY_WRITE_REQUEST = 29,
      RELAY_WRITE_RESPONSE = 30,
      SERVER_INFO_REQUEST = 31,
      SERVER_INFO_RESPONSE = 32;
}
