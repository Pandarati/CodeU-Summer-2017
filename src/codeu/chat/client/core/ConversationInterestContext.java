package codeu.chat.client.core;

import java.util.Arrays;
import java.util.Iterator;

import codeu.chat.common.BasicView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationInterest;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class ConversationInterestContext {

    public final User user;
    public final ConversationInterest interest;
    public final ConversationHeader conversation;

    private BasicView view;

    public ConversationInterestContext(User user, ConversationInterest interest,
                                       ConversationHeader conversation, BasicView view) {
        this.user = user;
        this.interest = interest;
        this.conversation = conversation;
        this.view = view;
    }

    /*
    public ConversationInterestContext next() {
        return interest.next == null ? null : getInterest(interest.next)
    }

    public ConversationInterestContext previous() {
        return interest.previous == null ? null : getInterest(interest.previous);
    }

    private ConversationInterestContext getInterest(Uuid id) {
        final Iterator<UserInterest> interests = view.getConversationInterests(Arrays.asList(id)).iterator();
        return interests.hasNext() ? new ConversationInterestContext(user, interests.next(), conversation, view) : null;
    }*/
}