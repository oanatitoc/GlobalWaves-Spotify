package app.pages.CommandNextPrev;

import app.user.User;

public final class NextPage implements Command {

    private User user;

    public NextPage(final User user) {
        this.user = user;
    }
    /**
     * Executes the operation to navigate to the next page for the user.
     */
    public void execute() {
        user.nextPage();
    }
}
