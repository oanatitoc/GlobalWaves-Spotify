package app.user;

import app.Admin;
import app.audio.Collections.*;
import app.audio.Files.AudioFile;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.audio.LibraryEntry;
import app.pages.*;
import app.player.Player;
import app.player.PlayerStats;
import app.searchBar.Filters;
import app.searchBar.SearchBar;
import app.user.Statistics.*;
import app.utils.Enums;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import fileio.input.EpisodeInput;
import lombok.Getter;
import lombok.Setter;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    @Setter
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
    private List<Page> pages;
    @Getter
    @Setter
    private int currentIndex;
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
    private List<ArtistInfo> artistsInfos;
    @Getter
    @Setter
    private List<GenreInfo> genresInfos;
    @Getter
    @Setter
    private List<SongInfo> songsInfos;
    @Getter
    @Setter
    private List<AlbumInfo> albumsInfos;
    @Getter
    @Setter
    private List<EpisodeInfo> episodesInfos;

    @Getter
    @Setter
    private int lastTimestamp;
    @Setter
    private Player copyPlayer;


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
        albumsInfos = new ArrayList<>();
        episodesInfos = new ArrayList<>();
        copyPlayer = new Player();

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
    public ArrayList<String> search(final Filters filters, final String type, CommandInput commandInput) {
        searchBar.clearSelection();
        updateStatistics(lastTimestamp, commandInput.getTimestamp(), commandInput.getUsername());
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
    public String load(CommandInput commandInput) {
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
        setLastTimestamp(commandInput.getTimestamp());

        player.pause();

        // update the number of Plays of an artist
        if (player.getType().equals("song")) {
            for (Song song : Admin.getInstance().getSongs()) {
                if (song.getName().equals(player.getCurrentAudioFile().getName())) {
                    String artistName = song.getArtist();
                    for (Artist artist : Admin.getInstance().getArtists()) {
                        if (artist.getUsername().equals(artistName)) {
                            artist.setNoPlays(artist.getNoPlays() + 1);
                        }
                    }
                }
            }
        }
        copyPlayer = new Player(player.getRepeatMode(), player.getShuffle(), player.getPaused(),
                player.getSource(), player.getType(), player.getBookmarks());
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

    public Page findArtistHostPage() {
        if (player.getCurrentAudioFile() != null || player.getCurrentAudioCollection() != null) {
            String currentAudioFileName = player.getCurrentAudioFile().getName();
            // check if current AudioFile is a song
            for (Song song : Admin.getInstance().getSongs()) {
                if (song.getName().equals(currentAudioFileName)) {
                    Artist artist = Admin.getInstance().getArtist(song.getArtist());
                    return artist.getPage();
                }
            }
            String currentAudioCollectionName = player.getCurrentAudioCollection().getName();
            // check if current AudioFile is a podcast
            for (Podcast podcast : Admin.getInstance().getPodcasts()) {
                if (podcast.getName().equals(currentAudioCollectionName)) {
                    Host host = Admin.getInstance().getHost(podcast.getOwner());
                    return host.getPage();
                }
            }
        }
        return currentPage;
    }

    public void nextPage() {
        if (currentIndex < pages.size() - 1) {
            currentIndex++;
            currentPage = pages.get(currentIndex);
        }
    }

    public void previousPage() {
        if (currentIndex > 0) {
            currentIndex--;
            currentPage = pages.get(currentIndex);
        }
    }

    public String loadRecommendations() {

        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (getLastRecommendation() == null) {
            return "No recommendations available.";
        }

//        if (!getLastRecommendationType().equals("song")
//                && ((AudioCollection) getLastRecommendation().getNumberOfTracks() == 0) {
//            return "You can't load an empty audio collection!";
//        }

        player.setSource(getLastRecommendation(), getLastRecommendationType());
        searchBar.clearSelection();

        player.pause();

        // update the number of Plays of an artist
        if (player.getType().equals("song")) {
            for (Song song : Admin.getInstance().getSongs()) {
                if (song.getName().equals(player.getCurrentAudioFile().getName())) {
                    String artistName = song.getArtist();
                    for (Artist artist : Admin.getInstance().getArtists()) {
                        if (artist.getUsername().equals(artistName)) {
                            artist.setNoPlays(artist.getNoPlays() + 1);
                        }
                    }
                }
            }
        }

        return "Playback loaded successfully.";
    }

    public void updateStatisticsForEpisodes(Podcast podcast, Episode episode, User user) {
        Host host = Admin.getInstance().getHost(podcast.getOwner());
        if (host != null) {
            // update the statistics for the host
            List<User> hostListeners = host.getListeners();
            if (!hostListeners.contains(user)) {
                hostListeners.add(user);
                host.setListeners(hostListeners);
                host.setNoListeners(host.getNoListeners() + 1);
            }
            List<EpisodeInfo> episodeInfos = host.getEpisodesInfos();
            if (!episodeInfos.stream().anyMatch(ep -> ep.getName().equals(episode.getName()))) {
                episodeInfos.add(new EpisodeInfo(episode.getName()));
            } else {
                System.out.println("bau ");
                episodeInfos.stream()
                        .filter(ep -> ep.getName().equals(episode.getName()))
                        .forEach(ep -> ep.setNoListen(ep.getNoListen() + 1));

            }
            host.setEpisodesInfos(episodeInfos);

        }
        // update the statistics for the user
        List<EpisodeInfo> episodeInfos = user.getEpisodesInfos();
        if (!episodeInfos.stream().anyMatch(ep -> ep.getName().equals(episode.getName()))) {
            episodeInfos.add(new EpisodeInfo(episode.getName()));
        } else {
            episodeInfos.stream()
                    .filter(ep -> ep.getName().equals(episode.getName()))
                    .forEach(ep -> ep.setNoListen(ep.getNoListen() + 1));

        }
        user.setEpisodesInfos(episodeInfos);
    }

    public void updateStatisticsForAlbums(Album album, User user, int passedTime) {
        Artist artist = Admin.getInstance().getArtist(album.getOwner());
        if (artist != null) {
            //update number of listeners for artist
            List<User> artistListeners = artist.getListeners();
            if (!artistListeners.contains(user)) {
                artistListeners.add(user);
                artist.setListeners(artistListeners);
                artist.setNoListeners(artist.getNoListeners() + 1);
            }

            //update fansInfos for artist
            List<String> fansInfos = artist.getFansNames();
            if(!fansInfos.contains(user.getUsername())) {
                fansInfos.add(user.getUsername());
                artist.setFansNames(fansInfos);
            }

            //update songInfos for artist and for user
            List<SongInfo> songsInfos = artist.getSongsInfos();
            List<SongInfo> songsInfosUser = user.getSongsInfos();
            int countTime = 0;
            int noListensForAlbum = 0;
            for (Song song : album.getSongs()) {
                if (countTime <= passedTime) {
                    countTime += song.getDuration();
                    noListensForAlbum++;
                    // load this song in the statistics
                    if (!songsInfos.stream().anyMatch(sg -> sg.getName().equals(song.getName()))) {
                        songsInfos.add(new SongInfo(song.getName(), 1));
                    } else {
                        songsInfos.stream()
                                .filter(sg -> sg.getName().equals(song.getName()))
                                .forEach(sg -> sg.setNoListen(sg.getNoListen() + 1));

                    }
                    if (!songsInfosUser.stream().anyMatch(sg -> sg.getName().equals(song.getName()))) {
                        songsInfosUser.add(new SongInfo(song.getName(), 1));
                    } else {
                        songsInfosUser.stream()
                                .filter(sg -> sg.getName().equals(song.getName()))
                                .forEach(sg -> sg.setNoListen(sg.getNoListen() + 1));
                    }
                    artist.setSongsInfos(songsInfos);
                    user.setSongsInfos(songsInfosUser);
                }
            }
            // updates album info for artist and user
            List<AlbumInfo> albumsInfos = artist.getAlbumsInfos();
            List<AlbumInfo> albumsInfosUser = user.getAlbumsInfos();
            if (!albumsInfos.stream().anyMatch(albm -> albm.getName().equals(album.getName()))) {
                albumsInfos.add(new AlbumInfo(album.getName(), noListensForAlbum));
            } else {
                int finalNoListensForAlbum = noListensForAlbum;
                albumsInfos.stream().filter(alb -> alb.getName().equals(album.getName())).
                        forEach(alb ->alb.setNoListen(alb.getNoListen() + finalNoListensForAlbum));
            }
            artist.setAlbumsInfos(albumsInfos);

            if (!albumsInfosUser.stream().anyMatch(albm -> albm.getName().equals(album.getName()))) {
                albumsInfosUser.add(new AlbumInfo(album.getName(), noListensForAlbum));
            } else {
                int finalNoListensForAlbum = noListensForAlbum;
                albumsInfosUser.stream().filter(alb -> alb.getName().equals(album.getName())).
                        forEach(alb ->alb.setNoListen(alb.getNoListen() + finalNoListensForAlbum));
            }
            user.setAlbumsInfos(albumsInfosUser);

            //update Artists info for user
            List<ArtistInfo> artistsInfos = user.getArtistsInfos();

            if (!artistsInfos.stream().anyMatch(art -> art.getName().equals(artist.getUsername()))) {
                artistsInfos.add(new ArtistInfo(artist.getUsername(), noListensForAlbum));
            } else {
                int finalNoListensForAlbum1 = noListensForAlbum;
                artistsInfos.stream()
                        .filter(art -> art.getName().equals(artist.getUsername()))
                        .forEach(art -> art.setNoListen(art.getNoListen() + finalNoListensForAlbum1));
            }
            user.setArtistsInfos(artistsInfos);

            //update the GenreInfos for users
            List<GenreInfo> genreInfos = user.getGenresInfos();
            countTime = 0;
            for (Song song : album.getSongs()) {
                if (countTime <= passedTime) {
                    countTime += song.getDuration();
                    // load this genre in the statistcs
                    if (!genreInfos.stream().anyMatch(sg -> sg.getName().equals(song.getGenre()))) {
                        genreInfos.add(new GenreInfo(song.getGenre(), 1));
                    } else {
                        genreInfos.stream()
                                .filter(sg -> sg.getName().equals(song.getGenre()))
                                .forEach(sg -> sg.setNoListen(sg.getNoListen() + 1));

                    }
                    user.setGenresInfos(genreInfos);
                }
            }
        }
    }
    public void updateStatisticsforSongs(Song song, User user) {
        Artist artist = Admin.getInstance().getArtist(song.getArtist());
        if (artist != null) {
            //update number of listeners for artist
            List<User> artistListeners = artist.getListeners();
            if (!artistListeners.contains(user)) {
                artistListeners.add(user);
                artist.setListeners(artistListeners);
                artist.setNoListeners(artist.getNoListeners() + 1);
            }

            //update fansInfos for artist
            List<String> fansInfos = artist.getFansNames();
            if(!fansInfos.contains(user.getUsername())) {
                fansInfos.add(user.getUsername());
                artist.setFansNames(fansInfos);
            }

            //update songInfos for artist and for user
            List<SongInfo> songsInfos = artist.getSongsInfos();

            // load this song in the statistics
            if (!songsInfos.stream().anyMatch(sg -> sg.getName().equals(song.getName()))) {
                songsInfos.add(new SongInfo(song.getName(), 1));
            } else {
                songsInfos.stream()
                        .filter(sg -> sg.getName().equals(song.getName()))
                        .forEach(sg -> sg.setNoListen(sg.getNoListen() + 1));

            }
            artist.setSongsInfos(songsInfos);

            List<SongInfo> songsInfosUser = user.getSongsInfos();
            if (!songsInfosUser.stream().anyMatch(sg -> sg.getName().equals(song.getName()))) {
                songsInfosUser.add(new SongInfo(song.getName(), 1));
            } else {
                songsInfosUser.stream()
                        .filter(sg -> sg.getName().equals(song.getName()))
                        .forEach(sg -> sg.setNoListen(sg.getNoListen() + 1));
            }
            user.setSongsInfos(songsInfosUser);

            // updates album info for artist and user
            List<AlbumInfo> albumsInfos = artist.getAlbumsInfos();

            if (!albumsInfos.stream().anyMatch(albm -> albm.getName().equals(song.getAlbum()))) {
                albumsInfos.add(new AlbumInfo(song.getAlbum(), 1));
            } else {
                albumsInfos.stream().filter(albm -> albm.getName().equals(song.getAlbum())).
                        forEach(albm ->albm.setNoListen(albm.getNoListen() + 1));
            }
            artist.setAlbumsInfos(albumsInfos);

            List<AlbumInfo> albumsInfosUser = user.getAlbumsInfos();
            if (!albumsInfosUser.stream().anyMatch(albm -> albm.getName().equals(song.getAlbum()))) {
                albumsInfosUser.add(new AlbumInfo(song.getAlbum(), 1));
            } else {
                albumsInfosUser.stream().filter(albm -> albm.getName().equals(song.getAlbum())).
                        forEach(albm ->albm.setNoListen(albm.getNoListen() + 1));
            }
            user.setAlbumsInfos(albumsInfosUser);

            //update Artists info for user
            List<ArtistInfo> artistsInfos = user.getArtistsInfos();

            if (!artistsInfos.stream().anyMatch(art -> art.getName().equals(artist.getUsername()))) {
                artistsInfos.add(new ArtistInfo(artist.getUsername(), 1));
            } else {
                artistsInfos.stream()
                        .filter(art -> art.getName().equals(artist.getUsername()))
                        .forEach(art -> art.setNoListen(art.getNoListen() + 1));
            }
            user.setArtistsInfos(artistsInfos);
            //update the GenreInfos for users
            List<GenreInfo> genreInfos = user.getGenresInfos();

            // load this genre in the statistcs
            if (!genreInfos.stream().anyMatch(sg -> sg.getName().equals(song.getGenre()))) {
                genreInfos.add(new GenreInfo(song.getGenre(), 1));
            } else {
                genreInfos.stream()
                        .filter(sg -> sg.getName().equals(song.getGenre()))
                        .forEach(sg -> sg.setNoListen(sg.getNoListen() + 1));

            }
            user.setGenresInfos(genreInfos);


        }
    }

    public void updateStatistics(int lastTime, int currentTime, String userName) {
        User user = Admin.getInstance().getUser(userName);
        if (copyPlayer != null && copyPlayer.getSource() != null) {
            // the time that has passed between the last load command and current search command
            int passedTime = currentTime - lastTime;
            int countTime = 0;
            if (copyPlayer.getType().equals("podcast")) {
                Podcast podcast = (Podcast) copyPlayer.getCurrentAudioCollection();
                for (Episode episode : podcast.getEpisodes()) {
                    if (countTime < passedTime) {
                        countTime += episode.getDuration();
                        updateStatisticsForEpisodes(podcast, episode, user);
                    }
                }
            }
            if (copyPlayer.getType().equals("album")) {
                Album album = (Album) copyPlayer.getCurrentAudioCollection();
                updateStatisticsForAlbums(album, user, passedTime);
            }
            if (copyPlayer.getType().equals("song")) {
                Song song = (Song) copyPlayer.getCurrentAudioFile();
                updateStatisticsforSongs(song, user);
            }

        }
    }
    public void arrangeStatistics() {
        List<AlbumInfo> albums = statistics.getTopAlbums();
        //sortare Albume chatGPT
        List<AlbumInfo> top5Albums = albums.stream()
                .sorted(Comparator
                        .comparingInt(AlbumInfo::getNoListen)
                        .reversed()
                        .thenComparing(AlbumInfo::getName))
                .limit(5)
                .collect(Collectors.toList());
        statistics.setTopAlbums(top5Albums);

        //sortare cantece
        List<SongInfo> songs = statistics.getTopSongs();
        List<SongInfo> top5Songs = songs.stream()
                .sorted(Comparator
                        .comparingInt(SongInfo::getNoListen)
                        .reversed()
                        .thenComparing(SongInfo::getName))
                .limit(5)
                .collect(Collectors.toList());
        statistics.setTopSongs(top5Songs);

    }
    public ObjectNode formattedStatisticsUser() {
        // Convertirea obiectului WrappedHost într-un obiect JSON
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();

        WrappedUser wrappedUser = getStatistics();

        ObjectNode topArtistsNode = resultNode.putObject("topArtists");
        // Adăugarea informațiilor despre artistii de top
        for (ArtistInfo artist : wrappedUser.getTopArtists()) {
            topArtistsNode.put(artist.getName(), artist.getNoListen());
        }

        ObjectNode topGenresNode = resultNode.putObject("topGenres");
        // Adăugarea informațiilor despre genurile de top
        for (GenreInfo genre : wrappedUser.getTopGenres()) {
            topGenresNode.put(genre.getName(), genre.getNoListen());
        }

        ObjectNode topSongsNode = resultNode.putObject("topSongs");
        // Adăugarea informațiilor despre cantecele de top
        for (SongInfo song : wrappedUser.getTopSongs()) {
            topSongsNode.put(song.getName(), song.getNoListen());
        }

        ObjectNode topAlbumsNode = resultNode.putObject("topAlbums");
        // Adăugarea informațiilor despre albumele de top
        for (AlbumInfo album : wrappedUser.getTopAlbums()) {
            topAlbumsNode.put(album.getName(), album.getNoListen());
        }

        ObjectNode topEpisodesNode = resultNode.putObject("topEpisodes");
        // Adăugarea informațiilor despre episoadele de top
        for (EpisodeInfo episode : wrappedUser.getTopEpisodes()) {
            topEpisodesNode.put(episode.getName(), episode.getNoListen());
        }

        return resultNode;
    }
}
