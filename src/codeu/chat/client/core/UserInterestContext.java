package codeu.chat.client.core;

import java.util.Arrays;
import java.util.Iterator;

import codeu.chat.common.BasicView;
import codeu.chat.common.User;
import codeu.chat.common.UserInterest;
import codeu.chat.util.Uuid;

public final class UserInterestContext {

    public final User user;
    public final UserInterest interest;
    public final User other;

    private BasicView view;

    public UserInterestContext(User user, UserInterest interest, User other, BasicView view) {
        this.user = user;
        this.interest = interest;
        this.other = other;
        this.view = view;
    }

    //public UserInterestContext next() { return interest.next == null ? null : getInterest(interest.next) }

   // public InterestContext previous() {
     //   return interest.previous == null ? null : getInterest(interest.previous);
   // }
/*
    private UserInterestContext getInterest(Uuid id) {
        final Iterator<UserInterest> interests = view.getUserInterests(Arrays.asList(id)).iterator();
        return interests.hasNext() ? new UserInterestContext(interests.next(), view) : null;
    }*/

}
