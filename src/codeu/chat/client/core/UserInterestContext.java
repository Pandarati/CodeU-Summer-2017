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

}
