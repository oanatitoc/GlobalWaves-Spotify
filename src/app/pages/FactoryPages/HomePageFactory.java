package app.pages.FactoryPages;

import app.pages.HomePage;
import app.pages.CommandNextPrev.Page;
import app.user.User;

public final class HomePageFactory implements PageFactory {
    private final User user;

    public HomePageFactory(final User user) {
        this.user = user;
    }

    @Override
    public Page createPage() {
        return new HomePage(user);
    }
}
