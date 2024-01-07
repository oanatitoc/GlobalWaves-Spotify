package app.pages.FactoryPages;

import app.pages.HomePage;
import app.pages.Page;
import app.user.User;

public class HomePageFactory implements PageFactory {
    private final User user;

    public HomePageFactory(User user) {
        this.user = user;
    }

    @Override
    public Page createPage() {
        return new HomePage(user);
    }
}
