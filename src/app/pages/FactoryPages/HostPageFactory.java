package app.pages.FactoryPages;

import app.pages.HostPage;
import app.pages.Page;
import app.user.Host;

public class HostPageFactory implements PageFactory {
    private final Host host;

    public HostPageFactory(Host host) {
        this.host = host;
    }

    @Override
    public Page createPage() {
        return new HostPage(host);
    }
}
