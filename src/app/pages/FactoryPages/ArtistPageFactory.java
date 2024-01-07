package app.pages.FactoryPages;

import app.pages.ArtistPage;
import app.pages.Page;
import app.user.Artist;

public class ArtistPageFactory implements PageFactory {
    private final Artist artist;

    public ArtistPageFactory(Artist artist) {
        this.artist = artist;
    }

    @Override
    public Page createPage() {
        return new ArtistPage(artist);
    }
}
