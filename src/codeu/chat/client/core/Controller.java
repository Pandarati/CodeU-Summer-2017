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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;

import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationInterest;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.common.UserInterest;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

final class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public Controller(ConnectionSource source) {
    this.source = source;
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {

    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), author);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Serializers.STRING.write(connection.out(), body);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User newUser(String name) {

    User response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner)  {

    ConversationHeader response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(ConversationHeader.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean addMember(Uuid user, Uuid conversation, Uuid member) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.ADD_MEMBER_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Uuid.SERIALIZER.write(connection.out(), member);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.ADD_MEMBER_RESPONSE) {
        response = Serializers.nullable(Serializers.BOOLEAN).read(connection.in());
        LOG.info("addMember: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean addOwner(Uuid user, Uuid conversation, Uuid owner) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.ADD_OWNER_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.ADD_OWNER_RESPONSE) {
        response = Serializers.nullable(Serializers.BOOLEAN).read(connection.in());
        LOG.info("addOwner: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean removeMember(Uuid user, Uuid conversation, Uuid member) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_MEMBER_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Uuid.SERIALIZER.write(connection.out(), member);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_MEMBER_RESPONSE) {
        response = Serializers.nullable(Serializers.BOOLEAN).read(connection.in());
        LOG.info("removeMember: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean removeOwner(Uuid user, Uuid conversation, Uuid owner) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_OWNER_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_OWNER_RESPONSE) {
        response = Serializers.nullable(Serializers.BOOLEAN).read(connection.in());
        LOG.info("removeOwner: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean permissionJoinConversation(Uuid user, Uuid conversation) {
    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.PERMISSION_JOIN_CONVERSATION_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), conversation);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.PERMISSION_JOIN_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(Serializers.BOOLEAN).read(connection.in());
        LOG.info("hasPermission: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public UserInterest newUserInterest(Uuid owner, Uuid userId) {

    UserInterest response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), owner);
      Uuid.SERIALIZER.write(connection.out(), userId);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_INTEREST_RESPONSE) {
        response = Serializers.nullable(UserInterest.SERIALIZER).read(connection.in());
        LOG.info("newUserInterest: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public ConversationInterest newConversationInterest(Uuid owner, Uuid conversation) {

    ConversationInterest response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), owner);
      Uuid.SERIALIZER.write(connection.out(), conversation);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_INTEREST_RESPONSE) {
        response = Serializers.nullable(ConversationInterest.SERIALIZER).read(connection.in());
        LOG.info("newConversationInterest: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean removeUserInterest(Uuid owner, Uuid interest) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_USER_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), owner);
      Uuid.SERIALIZER.write(connection.out(), interest);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_USER_INTEREST_RESPONSE) {
        response = Serializers.nullable(Serializers.BOOLEAN).read(connection.in());
        LOG.info("removeUserInterest: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean removeConversationInterest(Uuid owner, Uuid conversation) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_CONVERSATION_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), owner);
      Uuid.SERIALIZER.write(connection.out(), conversation);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_CONVERSATION_INTEREST_RESPONSE) {
        response = Serializers.nullable(Serializers.BOOLEAN).read(connection.in());
        LOG.info("removeConversationInterest: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public String statusUpdate(Uuid user) {

    String response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.STATUS_UPDATE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.STATUS_UPDATE_RESPONSE) {
        response = Serializers.nullable(Serializers.STRING).read(connection.in());
        LOG.info("Status Update: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
    return response;
  }

}
