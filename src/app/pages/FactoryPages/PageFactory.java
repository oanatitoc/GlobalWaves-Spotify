package app.pages.FactoryPages;
import app.pages.CommandNextPrev.Page;

/**
 * Interface for a factory that creates instances of Page.
 */
public interface PageFactory {
    /**
     * Creates a new instance of Page.
     *
     * @return the instance of Page.
     */
    Page createPage();
}
