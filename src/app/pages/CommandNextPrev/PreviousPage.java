package app.pages.CommandNextPrev;

import app.user.User;

public final class PreviousPage implements Command {

    private User user;

    public PreviousPage(final User user) {
        this.user = user;
    }

    /**
     * Executes the operation to navigate to the previous page for the user.
     */
    public void execute() {
        user.previousPage();
    }
}
