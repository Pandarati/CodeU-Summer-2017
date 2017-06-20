
package codeu.chat.client.core;

import java.util.Arrays;
import java.util.Iterator;

import codeu.chat.common.BasicView;
import codeu.chat.common.Message;
import codeu.chat.util.Uuid;

public final class InterestContext {

  public final Interest interest;
  private final BasicView view;

  public InterestContext(Interest interest, BasicView view) {
    this.interest = interest;
    this.view = view;
  }

  public InterestContext next() {
    return interest.next == null ? null : getInterest(interest.next);
  }

  public InterestContext previous() {
    return interest.previous == null ? null : getInterest(interest.previous);
  }

  private InterestContext getInterest(Uuid id) {
    final Iterator<Interest> interests = view.getInterests(Arrays.asList(id)).iterator();
    return interests.hasNext() ? new InterestContext(interests.next(), view) : null;
  }
}
