package app.pages;

import app.user.User;

public class NextPage implements Command {

    private User user;

    public NextPage(User user) {
        this.user = user;
    }

    public void execute() {
        user.nextPage();
    }
}
