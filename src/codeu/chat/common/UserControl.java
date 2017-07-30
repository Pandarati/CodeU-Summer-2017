import codeu.chat.util.Uuid;

public class UserControl {

	public enum UserType {
		CREATOR
		OWNER
		MEMBER
	}

	private final Uuid user;
	private final UserType type;

	public UserControl(Uuid user) {
		this.user = user;
		this.type = CREATOR;
	}


	public UserControl(Uuid user, UserType type) {
		this.user = user;
		this.type = type;
	}

	public UserType getType() {
		return type;
	}

	public Uuid getUser() {
		return user;
	}

	public void setType(UserType type) {
		this.type = type;
	}

	public boolean isCreator() {
		return (this.type == CREATOR);
	}

	public boolean isOwner() {
		return (this.type == OWNER);
	}

	public boolean isMember() {
		return (this.type == MEMBER);
	}
		
}
