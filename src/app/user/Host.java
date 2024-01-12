package app.user;

import app.audio.Collections.Podcast;
import app.pages.HostPage;

import app.user.Entities.Announcement;
import app.user.Statistics.Infos;
import app.user.Statistics.WrappedHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Host.
 */
public final class Host extends ContentCreator {
    private ArrayList<Podcast> podcasts;
    private ArrayList<Announcement> announcements;
    @Getter
    @Setter
    private WrappedHost statistics;
    @Getter
    @Setter
    private List<User> listeners;
    @Getter
    @Setter
    private int noListeners = 0;
    @Getter
    @Setter
    private List<Infos> episodesInfos;

    /**
     * Instantiates a new Host.
     *
     * @param username the username
     * @param age      the age
     * @param city     the city
     */
    public Host(final String username, final int age, final String city) {
        super(username, age, city);
        podcasts = new ArrayList<>();
        announcements = new ArrayList<>();
        listeners = new ArrayList<>();
        episodesInfos = new ArrayList<>();

        super.setPage(new HostPage(this));
    }

    /**
     * Gets podcasts.
     *
     * @return the podcasts
     */
    public ArrayList<Podcast> getPodcasts() {
        return podcasts;
    }

    /**
     * Sets podcasts.
     *
     * @param podcasts the podcasts
     */
    public void setPodcasts(final ArrayList<Podcast> podcasts) {
        this.podcasts = podcasts;
    }

    /**
     * Gets announcements.
     *
     * @return the announcements
     */
    public ArrayList<Announcement> getAnnouncements() {
        return announcements;
    }

    /**
     * Sets announcements.
     *
     * @param announcements the announcements
     */
    public void setAnnouncements(final ArrayList<Announcement> announcements) {
        this.announcements = announcements;
    }

    /**
     * Gets podcast.
     *
     * @param podcastName the podcast name
     * @return the podcast
     */
    public Podcast getPodcast(final String podcastName) {
        for (Podcast podcast: podcasts) {
            if (podcast.getName().equals(podcastName)) {
                return podcast;
            }
        }

        return null;
    }

    /**
     * Gets announcement.
     *
     * @param announcementName the announcement name
     * @return the announcement
     */
    public Announcement getAnnouncement(final String announcementName) {
        for (Announcement announcement: announcements) {
            if (announcement.getName().equals(announcementName)) {
                return announcement;
            }
        }

        return null;
    }

    @Override
    public String userType() {
        return "host";
    }

    /**
     * The method creates the object node with host's statistics based on the
     * information accumulated up to the moment of the "wrapped" command
     *
     * @return the object node containing the statistics
     */
    public ObjectNode formattedStatisticsHost() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();

        WrappedHost wrappedHost = getStatistics();

        // Creating a JSON object for top episodes and adding information
        ObjectNode topEpisodesNode = resultNode.putObject("topEpisodes");
        wrappedHost.getTopEpisodes().forEach(episode -> topEpisodesNode.put(episode.getName(),
                episode.getNoListen()));

        // Adding the number of listeners
        resultNode.put("listeners", wrappedHost.getListeners());
        return resultNode;
    }
}
