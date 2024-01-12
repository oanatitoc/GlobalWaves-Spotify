package app.pages.FactoryPages;

import app.pages.ArtistPage;
import app.pages.CommandNextPrev.Page;
import app.user.Artist;

public final class ArtistPageFactory implements PageFactory {
    private final Artist artist;

    public ArtistPageFactory(final Artist artist) {
        this.artist = artist;
    }

    @Override
    public Page createPage() {
        return new ArtistPage(artist);
    }
}
