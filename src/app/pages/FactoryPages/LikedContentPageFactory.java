package app.pages.FactoryPages;

import app.pages.LikedContentPage;
import app.pages.CommandNextPrev.Page;
import app.user.User;

public final class LikedContentPageFactory implements PageFactory {
    private final User user;

    public LikedContentPageFactory(final User user) {
        this.user = user;
    }

    @Override
    public Page createPage() {
        return new LikedContentPage(user);
    }
}
