package app.pages;

import app.user.User;

public class PreviousPage implements Command {

    private User user;

    public PreviousPage(User user) {
        this.user = user;
    }

    public void execute() {
        user.previousPage();
    }
}
