package app.pages.FactoryPages;

import app.pages.HostPage;
import app.pages.CommandNextPrev.Page;
import app.user.Host;

public final class HostPageFactory implements PageFactory {
    private final Host host;

    public HostPageFactory(final Host host) {
        this.host = host;
    }

    @Override
    public Page createPage() {
        return new HostPage(host);
    }
}
