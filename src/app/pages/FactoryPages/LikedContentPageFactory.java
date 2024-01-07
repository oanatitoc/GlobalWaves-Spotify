package app.pages.FactoryPages;

import app.pages.LikedContentPage;
import app.pages.Page;
import app.user.User;

public class LikedContentPageFactory implements PageFactory {
    private final User user;

    public LikedContentPageFactory(User user) {
        this.user = user;
    }

    @Override
    public Page createPage() {
        return new LikedContentPage(user);
    }
}
