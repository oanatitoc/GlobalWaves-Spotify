package app.user;

import app.Admin;
import app.audio.Collections.*;
import app.audio.Files.AudioFile;
import app.audio.Files.Song;
import app.audio.LibraryEntry;
import app.pages.*;
import app.pages.CommandNextPrev.Page;
import app.user.Entities.AdBreak;
import app.user.Statistics.*;
import app.pages.FactoryPages.ArtistPageFactory;
import app.pages.FactoryPages.HostPageFactory;
import app.pages.FactoryPages.PageFactory;
import app.player.Player;
import app.player.PlayerStats;
import app.searchBar.Filters;
import app.searchBar.SearchBar;
import app.user.Entities.Merchandise;
import app.user.Entities.Notifications.NotificationListManager;
import app.user.Statistics.Infos.*;
import app.utils.Enums;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type User.
 */
public final class User extends UserAbstract {
    @Getter
    @Setter
    private ArrayList<Playlist> playlists;
    @Getter
    @Setter
    private ArrayList<Song> likedSongs;
    @Getter
    @Setter
    private ArrayList<Playlist> followedPlaylists;
    @Getter
    private final Player player;
    @Getter
    @Setter
    private boolean status;
    @Getter
    @Setter
    private final SearchBar searchBar;
    @Getter
    @Setter
    private boolean lastSearched;
    @Getter
    @Setter
    private Page currentPage;
    @Getter
    @Setter
    private HomePage homePage;
    @Getter
    @Setter
    private LikedContentPage likedContentPage;
    @Getter
    @Setter
    private Subscribe subscribes;
    @Getter
    @Setter
    private int lastNotifiedTime;
    @Getter
    @Setter
    private List<Merchandise> merches;
    @Getter
    @Setter
    private ArrayList<Song> songsRecommendations;
    @Getter
    @Setter
    private ArrayList<Playlist> playlistsRecommendations;
    @Getter
    @Setter
    private List<Page> pages; // the list of pages to navigate through
    @Getter
    @Setter
    private int currentIndex; // the current index of page from the list of pages
    @Getter
    @Setter
    private LibraryEntry lastRecommendation;
    @Getter
    @Setter
    private String lastRecommendationType;
    @Getter
    @Setter
    private LibraryEntry lastSource;
    @Getter
    @Setter
    private WrappedUser statistics;
    @Getter
    @Setter
    private List<Infos> artistsInfos;
    @Getter
    @Setter
    private List<Infos> genresInfos;
    @Getter
    @Setter
    private List<Infos> songsInfos;
    @Getter
    @Setter
    private List<Infos> songsInfosPremium;
    @Getter
    @Setter
    private List<Infos> songsInfosFree;
    @Getter
    @Setter
    private List<Infos> albumsInfos;
    @Getter
    @Setter
    private List<Infos> episodesInfos;

    @Setter
    @Getter
    private Player copyPlayer; // a copy of the player used to update the general statistics
    @Getter
    @Setter
    private boolean accessedData; // checks if user has accessed any source
    @Getter
    @Setter
    private boolean premium; // tells if the user is a premium user or not
    @Getter
    @Setter
    private PageFactory currentPageFactory;
    @Getter
    private final NotificationListManager listManager = new NotificationListManager();
    @Getter
    @Setter
    private List<Artist> artistsListened = new ArrayList<>();
    @Getter
    @Setter
    private AdBreak adBreak;
    private final int five = 5;

    /**
     * Instantiates a new User.
     *
     * @param username the username
     * @param age      the age
     * @param city     the city
     */
    public User(final String username, final int age, final String city) {
        super(username, age, city);
        playlists = new ArrayList<>();
        likedSongs = new ArrayList<>();
        followedPlaylists = new ArrayList<>();
        songsRecommendations = new ArrayList<>();
        playlistsRecommendations = new ArrayList<>();
        player = new Player();
        searchBar = new SearchBar(username);
        lastSearched = false;
        status = true;
        homePage = new HomePage(this);
        currentPage = homePage;
        likedContentPage = new LikedContentPage(this);
        subscribes = new Subscribe();
        merches = new ArrayList<>();
        currentIndex = 0;
        pages = new ArrayList<>();
        pages.add(this.currentPage);
        artistsInfos = new ArrayList<>();
        genresInfos = new ArrayList<>();
        songsInfos = new ArrayList<>();
        songsInfosPremium = new ArrayList<>();
        songsInfosFree = new ArrayList<>();
        albumsInfos = new ArrayList<>();
        episodesInfos = new ArrayList<>();
        copyPlayer = new Player();
        accessedData = false;
        premium = false;
        adBreak = new AdBreak(0);
        adBreak.AdBreakInQueue = false;

    }

    @Override
    public String userType() {
        return "user";
    }

    /**
     * Search array list.
     *
     * @param filters the filters
     * @param type    the type
     * @return the array list
     */
    public ArrayList<String> search(final Filters filters, final String type,
                                    final CommandInput commandInput) {
        searchBar.clearSelection();

        // we update the statistics before searching another source
        Admin.getInstance().updateStatistics(commandInput.getTimestamp(),
                Admin.getInstance().getUser(commandInput.getUsername()));

        player.stop();

        lastSearched = true;
        ArrayList<String> results = new ArrayList<>();

        if (type.equals("artist") || type.equals("host")) {
            List<ContentCreator> contentCreatorsEntries =
                    searchBar.searchContentCreator(filters, type);

            for (ContentCreator contentCreator : contentCreatorsEntries) {
                results.add(contentCreator.getUsername());
            }
        } else {
            List<LibraryEntry> libraryEntries = searchBar.search(filters, type);

            for (LibraryEntry libraryEntry : libraryEntries) {
                results.add(libraryEntry.getName());
            }
        }
        return results;
    }

    /**
     * Select string.
     *
     * @param itemNumber the item number
     * @return the string
     */
    public String select(final int itemNumber) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (!lastSearched) {
            return "Please conduct a search before making a selection.";
        }

        lastSearched = false;

        if (searchBar.getLastSearchType().equals("artist")
                || searchBar.getLastSearchType().equals("host")) {
            ContentCreator selected = searchBar.selectContentCreator(itemNumber);

            if (selected == null) {
                return "The selected ID is too high.";
            }

            currentPage = selected.getPage();
            return "Successfully selected %s's page.".formatted(selected.getUsername());
        } else {
            LibraryEntry selected = searchBar.select(itemNumber);

            if (selected == null) {
                return "The selected ID is too high.";
            }

            return "Successfully selected %s.".formatted(selected.getName());
        }
    }

    /**
     * Load string.
     *
     * @return the string
     */
    public String load(final CommandInput commandInput) {
        if (!status) {
            copyPlayer = null;
            return "%s is offline.".formatted(getUsername());
        }

        if (searchBar.getLastSelected() == null) {
            copyPlayer = null;
            return "Please select a source before attempting to load.";
        }

        if (!searchBar.getLastSearchType().equals("song")
                && ((AudioCollection) searchBar.getLastSelected()).getNumberOfTracks() == 0) {
            copyPlayer = null;
            return "You can't load an empty audio collection!";
        }

        player.setSource(searchBar.getLastSelected(), searchBar.getLastSearchType());
        searchBar.clearSelection();

        player.pause();

        // update the number of Plays of an artist
        if (player.getType().equals("song")) {
            String songName = player.getCurrentAudioFile().getName();
            Song song = Admin.getInstance().getSongs()
                    .stream()
                    .filter(sg -> sg.getName()
                            .equals(songName))
                    .findFirst()
                    .orElse(null);
            Artist artist = Admin.getInstance().getArtist(song.getArtist());
            artist.setNoPlays(artist.getNoPlays() + 1);
        }

        // update the copyPlayer
        copyPlayer = new Player(player.getSource(), player.getType());
        // set the time when the source was load in the copyPlayer
        copyPlayer.setLoadTimestamp(commandInput.getTimestamp());
        // set the time when the copyPlayer was last updated
        copyPlayer.setUpdatedTimestamp(commandInput.getTimestamp());

        return "Playback loaded successfully.";
    }

    /**
     * Play pause string.
     *
     * @return the string
     */
    public String playPause() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before attempting to pause or resume playback.";
        }

        player.pause();

        if (player.getPaused()) {
            return "Playback paused successfully.";
        } else {
            return "Playback resumed successfully.";
        }
    }

    /**
     * Repeat string.
     *
     * @return the string
     */
    public String repeat() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before setting the repeat status.";
        }

        Enums.RepeatMode repeatMode = player.repeat();
        String repeatStatus = "";

        switch (repeatMode) {
            case NO_REPEAT -> {
                repeatStatus = "no repeat";
            }
            case REPEAT_ONCE -> {
                repeatStatus = "repeat once";
            }
            case REPEAT_ALL -> {
                repeatStatus = "repeat all";
            }
            case REPEAT_INFINITE -> {
                repeatStatus = "repeat infinite";
            }
            case REPEAT_CURRENT_SONG -> {
                repeatStatus = "repeat current song";
            }
            default -> {
                repeatStatus = "";
            }
        }

        return "Repeat mode changed to %s.".formatted(repeatStatus);
    }

    /**
     * Shuffle string.
     *
     * @param seed the seed
     * @return the string
     */
    public String shuffle(final Integer seed) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before using the shuffle function.";
        }

        if (!player.getType().equals("playlist")
                && !player.getType().equals("album")) {
            return "The loaded source is not a playlist or an album.";
        }

        player.shuffle(seed);

        if (player.getShuffle()) {
            return "Shuffle function activated successfully.";
        }
        return "Shuffle function deactivated successfully.";
    }

    /**
     * Forward string.
     *
     * @return the string
     */
    public String forward() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before attempting to forward.";
        }

        if (!player.getType().equals("podcast")) {
            return "The loaded source is not a podcast.";
        }

        player.skipNext();

        return "Skipped forward successfully.";
    }

    /**
     * Backward string.
     *
     * @return the string
     */
    public String backward() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please select a source before rewinding.";
        }

        if (!player.getType().equals("podcast")) {
            return "The loaded source is not a podcast.";
        }

        player.skipPrev();

        return "Rewound successfully.";
    }

    /**
     * Like string.
     *
     * @return the string
     */
    public String like() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before liking or unliking.";
        }

        if (!player.getType().equals("song") && !player.getType().equals("playlist")
                && !player.getType().equals("album")) {
            return "Loaded source is not a song.";
        }

        Song song = (Song) player.getCurrentAudioFile();

        if (likedSongs.contains(song)) {
            likedSongs.remove(song);
            song.dislike();

            return "Unlike registered successfully.";
        }

        likedSongs.add(song);
        song.like();
        return "Like registered successfully.";
    }

    /**
     * Next string.
     *
     * @return the string
     */
    public String next() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before skipping to the next track.";
        }

        player.next();

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before skipping to the next track.";
        }

        return "Skipped to next track successfully. The current track is %s."
                .formatted(player.getCurrentAudioFile().getName());
    }

    /**
     * Prev string.
     *
     * @return the string
     */
    public String prev() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before returning to the previous track.";
        }

        player.prev();

        return "Returned to previous track successfully. The current track is %s."
                .formatted(player.getCurrentAudioFile().getName());
    }

    /**
     * Create playlist string.
     *
     * @param name      the name
     * @param timestamp the timestamp
     * @return the string
     */
    public String createPlaylist(final String name, final int timestamp) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (playlists.stream().anyMatch(playlist -> playlist.getName().equals(name))) {
            return "A playlist with the same name already exists.";
        }

        playlists.add(new Playlist(name, getUsername(), timestamp));

        return "Playlist created successfully.";
    }

    /**
     * Add remove in playlist string.
     *
     * @param id the id
     * @return the string
     */
    public String addRemoveInPlaylist(final int id) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before adding to or removing from the playlist.";
        }

        if (player.getType().equals("podcast")) {
            return "The loaded source is not a song.";
        }

        if (id > playlists.size()) {
            return "The specified playlist does not exist.";
        }

        Playlist playlist = playlists.get(id - 1);

        if (playlist.containsSong((Song) player.getCurrentAudioFile())) {
            playlist.removeSong((Song) player.getCurrentAudioFile());
            return "Successfully removed from playlist.";
        }

        playlist.addSong((Song) player.getCurrentAudioFile());
        return "Successfully added to playlist.";
    }

    /**
     * Switch playlist visibility string.
     *
     * @param playlistId the playlist id
     * @return the string
     */
    public String switchPlaylistVisibility(final Integer playlistId) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (playlistId > playlists.size()) {
            return "The specified playlist ID is too high.";
        }

        Playlist playlist = playlists.get(playlistId - 1);
        playlist.switchVisibility();

        if (playlist.getVisibility() == Enums.Visibility.PUBLIC) {
            return "Visibility status updated successfully to public.";
        }

        return "Visibility status updated successfully to private.";
    }

    /**
     * Show playlists array list.
     *
     * @return the array list
     */
    public ArrayList<PlaylistOutput> showPlaylists() {
        ArrayList<PlaylistOutput> playlistOutputs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            playlistOutputs.add(new PlaylistOutput(playlist));
        }

        return playlistOutputs;
    }

    /**
     * Follow string.
     *
     * @return the string
     */
    public String follow() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        LibraryEntry selection = searchBar.getLastSelected();
        String type = searchBar.getLastSearchType();

        if (selection == null) {
            return "Please select a source before following or unfollowing.";
        }

        if (!type.equals("playlist")) {
            return "The selected source is not a playlist.";
        }

        Playlist playlist = (Playlist) selection;

        if (playlist.getOwner().equals(getUsername())) {
            return "You cannot follow or unfollow your own playlist.";
        }

        if (followedPlaylists.contains(playlist)) {
            followedPlaylists.remove(playlist);
            playlist.decreaseFollowers();

            return "Playlist unfollowed successfully.";
        }

        followedPlaylists.add(playlist);
        playlist.increaseFollowers();


        return "Playlist followed successfully.";
    }

    /**
     * Gets player stats.
     *
     * @return the player stats
     */
    public PlayerStats getPlayerStats() {
        return player.getStats();
    }

    /**
     * Show preferred songs array list.
     *
     * @return the array list
     */
    public ArrayList<String> showPreferredSongs() {
        ArrayList<String> results = new ArrayList<>();
        for (AudioFile audioFile : likedSongs) {
            results.add(audioFile.getName());
        }

        return results;
    }

    /**
     * Gets preferred genre.
     *
     * @return the preferred genre
     */
    public String getPreferredGenre() {
        String[] genres = {"pop", "rock", "rap"};
        int[] counts = new int[genres.length];
        int mostLikedIndex = -1;
        int mostLikedCount = 0;

        for (Song song : likedSongs) {
            for (int i = 0; i < genres.length; i++) {
                if (song.getGenre().equals(genres[i])) {
                    counts[i]++;
                    if (counts[i] > mostLikedCount) {
                        mostLikedCount = counts[i];
                        mostLikedIndex = i;
                    }
                    break;
                }
            }
        }

        String preferredGenre = mostLikedIndex != -1 ? genres[mostLikedIndex] : "unknown";
        return "This user's preferred genre is %s.".formatted(preferredGenre);
    }

    /**
     * Switch status.
     */
    public void switchStatus() {
        status = !status;
    }

    /**
     * Simulate time.
     *
     * @param time the time
     */
    public void simulateTime(final int time) {
        if (!status) {
            return;
        }

        player.simulatePlayer(time);
    }

    /**
     * Find out if a user has accessed a host or an artist page
     *
     * @return the host's or artist's page if they were accessed, or else, the user's current page
     */
    public PageFactory findArtistOrHostPage() {
        if (player.getCurrentAudioFile() != null || player.getCurrentAudioCollection() != null) {
            String currentAudioFileName = player.getCurrentAudioFile().getName();
            // check if current AudioFile is a song
            Song song = Admin.getInstance().getSongs()
                            .stream()
                            .filter(sg -> sg.getName().equals(currentAudioFileName))
                            .findFirst()
                            .orElse(null);
            if (song != null) {
                Artist artist = Admin.getInstance().getArtist(song.getArtist());
                return new ArtistPageFactory(artist);
            }

            String currentAudioCollectionName = player.getCurrentAudioCollection().getName();
            // check if current AudioFile is a podcast
            Podcast podcast = Admin.getInstance().getPodcasts()
                    .stream()
                    .filter(podcast1 -> podcast1.getName().equals(currentAudioCollectionName))
                    .findFirst()
                    .orElse(null);
            if (podcast != null) {
                Host host = Admin.getInstance().getHost(podcast.getOwner());
                return new HostPageFactory(host);
            }
        }
        // ELSE, return the current Page of the user
        return (PageFactory) currentPage;
    }
    /**
     * Navigates to the next page in the collection of pages if the index of the current page
     * allows it.
     */
    public void nextPage() {
        if (currentIndex < pages.size() - 1) {
            currentIndex++;
            currentPage = pages.get(currentIndex);
        }
    }

    /**
     * Navigates to the previous page in the collection of pages if the index of the current page
     * allows it.
     */
    public void previousPage() {
        if (currentIndex > 0) {
            currentIndex--;
            currentPage = pages.get(currentIndex);
        }
    }

    /**
     * This method loads the user's recommendation
     *
     * @return A message indicating whether the recommendation load was successful or if there
     * were any issues.
     */
    public String loadRecommendations() {

        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (getLastRecommendation() == null) {
            return "No recommendations available.";
        }

        player.setSource(getLastRecommendation(), getLastRecommendationType());
        searchBar.clearSelection();

        player.pause();

        // update the number of Plays of an artist
        if (player.getType().equals("song")) {
            String songName = player.getCurrentAudioFile().getName();
            Song song = Admin.getInstance().getSongs()
                                            .stream()
                                            .filter(sg -> sg.getName()
                                            .equals(songName))
                                            .findFirst()
                                            .orElse(null);
            Artist artist = Admin.getInstance().getArtist(song.getArtist());
            artist.setNoPlays(artist.getNoPlays() + 1);
        }

        return "Playback loaded successfully.";
    }

    /**
     * Arranges the top items in a list based on the number of listens and names.
     * The list will be sorted in descending order by the number of listens,
     * and for items with the same number of listens, they will be sorted in ascending
     * order by name.
     * Only the top 5 items will be retained in the list.
     *
     * @param items The list of items to be arranged.
     * @param <T>   The type of items in the list, must extend the Infos interface.
     */
    public <T extends Infos> void arrangeTopItems(final List<T> items) {
        List<T> top5Items = items.stream()
                .sorted(Comparator
                        .comparingInt(Infos::getNoListen)
                        .reversed()
                        .thenComparing(Infos::getName))
                .limit(five)
                .toList();

        items.clear();
        items.addAll(top5Items);
    }

    /**
     * This method arranges all user's items calling the previous method.
     * The items in each list implement the Infos interface.
     */
    public void arrangeStatistics() {
        arrangeTopItems(statistics.getTopAlbums());
        arrangeTopItems(statistics.getTopGenres());
        arrangeTopItems(statistics.getTopSongs());

        List<Infos> artists = statistics.getTopArtists();
        List<Infos> top5Artists = artists.stream()
                .sorted(Comparator
                        .comparingInt(Infos::getNoListen)
                        .reversed()
                        .thenComparing(Infos::getName))
                .limit(five)
                .collect(Collectors.toList());
        statistics.setTopArtists(top5Artists);

    }

    /**
     * The method creates the object node with user's statistics based on the
     * information accumulated up to the moment of the "wrapped" command
     *
     * @return the object node containing the statistics
     */
    public ObjectNode formattedStatisticsUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();

        // Retrieving user statistics
        WrappedUser wrappedUser = getStatistics();

        // Verifying if the user has accessed any source
        accessedData = Stream.of(
                        wrappedUser.getTopArtists(),
                        wrappedUser.getTopGenres(),
                        wrappedUser.getTopSongs(),
                        wrappedUser.getTopAlbums(),
                        wrappedUser.getTopEpisodes()
                )
                .anyMatch(thisList -> thisList != null && !thisList.isEmpty());

        // Creating a JSON object for top artists and adding information
        ObjectNode topArtistsNode = resultNode.putObject("topArtists");
        wrappedUser.getTopArtists().forEach(artist -> topArtistsNode.put(artist.getName(),
                                                                         artist.getNoListen()));

        // Creating a JSON object for top genres and adding information
        ObjectNode topGenresNode = resultNode.putObject("topGenres");
        wrappedUser.getTopGenres().forEach(genre -> topGenresNode.put(genre.getName(),
                                                                      genre.getNoListen()));

        // Creating a JSON object for top songs and adding information
        ObjectNode topSongsNode = resultNode.putObject("topSongs");
        wrappedUser.getTopSongs().forEach(song -> topSongsNode.put(song.getName(),
                                                                   song.getNoListen()));

        // Creating a JSON object for top albums and adding information
        ObjectNode topAlbumsNode = resultNode.putObject("topAlbums");
        wrappedUser.getTopAlbums().forEach(album -> topAlbumsNode.put(album.getName(),
                                                                      album.getNoListen()));

        // Creating a JSON object for top episodes and adding information
        ObjectNode topEpisodesNode = resultNode.putObject("topEpisodes");
        wrappedUser.getTopEpisodes().forEach(episode -> topEpisodesNode.put(episode.getName(),
                                                                        episode.getNoListen()));

        return resultNode;
    }
}
