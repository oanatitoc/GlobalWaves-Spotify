package app;

import app.audio.Collections.Album;
import app.audio.Collections.AudioCollection;
import app.audio.Collections.Playlist;
import app.audio.Collections.Podcast;
import app.audio.Files.AudioFile;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.pages.CommandNextPrev.Command;
import app.pages.FactoryPages.HomePageFactory;
import app.pages.FactoryPages.LikedContentPageFactory;
import app.pages.CommandNextPrev.NextPage;
import app.pages.CommandNextPrev.Page;
import app.pages.CommandNextPrev.PreviousPage;
import app.player.Player;
import app.user.*;
import app.user.Entities.*;
import app.user.Entities.Notifications.Notification;
import app.user.Statistics.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Admin.
 */
public final class Admin {
    private List<User> users = new ArrayList<>();
    @Getter
    private List<Artist> artists = new ArrayList<>();
    @Getter
    private List<Host> hosts = new ArrayList<>();
    private List<Song> songs = new ArrayList<>();
    private List<Podcast> podcasts = new ArrayList<>();
    private int timestamp = 0;
    private final int limit = 5;
    private final int dateStringLength = 10;
    private final int dateFormatSize = 3;
    private final int dateYearLowerLimit = 1900;
    private final int dateYearHigherLimit = 2023;
    private final int dateMonthLowerLimit = 1;
    private final int dateMonthHigherLimit = 12;
    private final int dateDayLowerLimit = 1;
    private final int dateDayHigherLimit = 31;
    private final int dateFebHigherLimit = 28;
    private static Admin instance;
    private final int maxRevenueforPremium = 1000000;
    private final double hundred = 100.0;
    private final int five = 5;


    private Admin() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Admin getInstance() {
        if (instance == null) {
            instance = new Admin();
        }
        return instance;
    }

    /**
     * Reset instance.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Sets users.
     *
     * @param userInputList the user input list
     */
    public void setUsers(final List<UserInput> userInputList) {
        for (UserInput userInput : userInputList) {
            users.add(new User(userInput.getUsername(), userInput.getAge(), userInput.getCity()));
        }
    }

    /**
     * Sets songs.
     *
     * @param songInputList the song input list
     */
    public void setSongs(final List<SongInput> songInputList) {
        for (SongInput songInput : songInputList) {
            songs.add(new Song(songInput.getName(), songInput.getDuration(), songInput.getAlbum(),
                    songInput.getTags(), songInput.getLyrics(), songInput.getGenre(),
                    songInput.getReleaseYear(), songInput.getArtist()));
        }
    }

    /**
     * Sets podcasts.
     *
     * @param podcastInputList the podcast input list
     */
    public void setPodcasts(final List<PodcastInput> podcastInputList) {
        for (PodcastInput podcastInput : podcastInputList) {
            List<Episode> episodes = new ArrayList<>();
            for (EpisodeInput episodeInput : podcastInput.getEpisodes()) {
                episodes.add(new Episode(episodeInput.getName(),
                        episodeInput.getDuration(),
                        episodeInput.getDescription()));
            }
            podcasts.add(new Podcast(podcastInput.getName(), podcastInput.getOwner(), episodes));
        }
    }

    /**
     * Gets songs.
     *
     * @return the songs
     */
    public List<Song> getSongs() {
        return new ArrayList<>(songs);
    }

    /**
     * Gets podcasts.
     *
     * @return the podcasts
     */
    public List<Podcast> getPodcasts() {
        return new ArrayList<>(podcasts);
    }

    /**
     * Gets playlists.
     *
     * @return the playlists
     */
    public List<Playlist> getPlaylists() {
        return users.stream()
                .flatMap(user -> user.getPlaylists().stream())
                .collect(Collectors.toList());
    }

    /**
     * Gets albums.
     *
     * @return the albums
     */
    public List<Album> getAlbums() {
        return artists.stream()
                .flatMap(artist -> artist.getAlbums().stream())
                .collect(Collectors.toList());
    }

    /**
     * Gets all users.
     *
     * @return the all users
     */
    public List<String> getAllUsers() {
        List<String> allUsers = new ArrayList<>();

        allUsers.addAll(users.stream().map(UserAbstract::getUsername).toList());
        allUsers.addAll(artists.stream().map(UserAbstract::getUsername).toList());
        allUsers.addAll(hosts.stream().map(UserAbstract::getUsername).toList());

        return allUsers;
    }

    /**
     * Gets user.
     *
     * @param username the username
     * @return the user
     */
    public User getUser(final String username) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets artist.
     *
     * @param username the username
     * @return the artist
     */
    public Artist getArtist(final String username) {
        return artists.stream()
                .filter(artist -> artist.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets host.
     *
     * @param username the username
     * @return the host
     */
    public Host getHost(final String username) {
        return hosts.stream()
                .filter(artist -> artist.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Update timestamp.
     *
     * @param newTimestamp the new timestamp
     */
    public void updateTimestamp(final int newTimestamp) {
        int elapsed = newTimestamp - timestamp;
        timestamp = newTimestamp;

        if (elapsed == 0) {
            return;
        } else if (elapsed < 0) {
            throw new IllegalArgumentException("Invalid timestamp" + newTimestamp);
        }

        users.forEach(user -> user.simulateTime(elapsed));
    }

    private UserAbstract getAbstractUser(final String username) {
        ArrayList<UserAbstract> allUsers = new ArrayList<>();

        allUsers.addAll(users);
        allUsers.addAll(artists);
        allUsers.addAll(hosts);

        return allUsers.stream()
                .filter(userPlatform -> userPlatform.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add new user string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addNewUser(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String type = commandInput.getType();
        int age = commandInput.getAge();
        String city = commandInput.getCity();

        UserAbstract currentUser = getAbstractUser(username);
        if (currentUser != null) {
            return "The username %s is already taken.".formatted(username);
        }

        if (type.equals("user")) {
            users.add(new User(username, age, city));
        } else if (type.equals("artist")) {
            artists.add(new Artist(username, age, city));
        } else {
            hosts.add(new Host(username, age, city));
        }

        return "The username %s has been added successfully.".formatted(username);
    }

    /**
     * Delete user string.
     *
     * @param username the username
     * @return the string
     */
    public String deleteUser(final String username) {
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        }

        if (currentUser.userType().equals("user")) {
            return deleteNormalUser((User) currentUser);
        }

        if (currentUser.userType().equals("host")) {
            return deleteHost((Host) currentUser);
        }

        return deleteArtist((Artist) currentUser);
    }

    private String deleteNormalUser(final User user) {
        if (user.getPlaylists().stream().anyMatch(playlist -> users.stream().map(User::getPlayer)
                .filter(player -> player != user.getPlayer())
                .map(Player::getCurrentAudioCollection)
                .filter(Objects::nonNull)
                .anyMatch(collection -> collection == playlist))) {
            return "%s can't be deleted.".formatted(user.getUsername());
        }

        user.getLikedSongs().forEach(Song::dislike);
        user.getFollowedPlaylists().forEach(Playlist::decreaseFollowers);

        users.stream().filter(otherUser -> otherUser != user)
                .forEach(otherUser -> otherUser.getFollowedPlaylists()
                        .removeAll(user.getPlaylists()));

        users.remove(user);
        return "%s was successfully deleted.".formatted(user.getUsername());
    }

    private String deleteHost(final Host host) {
        if (host.getPodcasts().stream().anyMatch(podcast -> getAudioCollectionsStream()
                .anyMatch(collection -> collection == podcast))
                || users.stream().anyMatch(user -> user.getCurrentPage() == host.getPage())) {
            return "%s can't be deleted.".formatted(host.getUsername());
        }

        host.getPodcasts().forEach(podcast -> podcasts.remove(podcast));
        hosts.remove(host);

        return "%s was successfully deleted.".formatted(host.getUsername());
    }

    private String deleteArtist(final Artist artist) {
        if (artist.getAlbums().stream().anyMatch(album -> album.getSongs().stream()
                .anyMatch(song -> getAudioFilesStream().anyMatch(audioFile -> audioFile == song))
                || getAudioCollectionsStream().anyMatch(collection -> collection == album))
                || users.stream().anyMatch(user -> user.getCurrentPage() == artist.getPage())) {
            return "%s can't be deleted.".formatted(artist.getUsername());
        }

        users.forEach(user -> artist.getAlbums().forEach(album -> album.getSongs().forEach(song -> {
            user.getLikedSongs().remove(song);
            user.getPlaylists().forEach(playlist -> playlist.removeSong(song));
        })));

        songs.removeAll(artist.getAllSongs());
        artists.remove(artist);
        return "%s was successfully deleted.".formatted(artist.getUsername());
    }

    /**
     * Add album string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addAlbum(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String albumName = commandInput.getName();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        if (currentArtist.getAlbums().stream()
                .anyMatch(album -> album.getName().equals(albumName))) {
            return "%s has another album with the same name.".formatted(username);
        }
        // we add a notification in the notification list of all users that have subscribed to
        // this artist.
        for (User user : users) {
            Subscribe userSubscribes = user.getSubscribes();
            if (userSubscribes != null) {
                // we check if there is the name of the artist in the list of names that the user
                // has subscribed
                List<String> names = userSubscribes.getNames();
                if (names.contains(commandInput.getUsername())) {
                    Notification notification = new Notification("New Album", "New Album from "
                            + commandInput.getUsername() + ".");
                    notification.addObserver(user.getListManager());
                    notification.notifyObservers();
                }
            }
        }
        List<Song> newSongs = commandInput.getSongs().stream()
                .map(songInput -> new Song(songInput.getName(),
                        songInput.getDuration(),
                        albumName,
                        songInput.getTags(),
                        songInput.getLyrics(),
                        songInput.getGenre(),
                        songInput.getReleaseYear(),
                        currentArtist.getUsername()))
                .toList();

        Set<String> songNames = new HashSet<>();
        if (!newSongs.stream().filter(song -> !songNames.add(song.getName()))
                .collect(Collectors.toSet()).isEmpty()) {
            return "%s has the same song at least twice in this album.".formatted(username);
        }

        songs.addAll(newSongs);
        currentArtist.getAlbums().add(new Album(albumName,
                commandInput.getDescription(),
                username,
                newSongs,
                commandInput.getReleaseYear(),
                commandInput.getTimestamp()));
        return "%s has added new album successfully.".formatted(username);
    }

    /**
     * Remove album string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removeAlbum(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String albumName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        Album searchedAlbum = currentArtist.getAlbum(albumName);
        if (searchedAlbum == null) {
            return "%s doesn't have an album with the given name.".formatted(username);
        }

        if (getAudioCollectionsStream().anyMatch(collection -> collection == searchedAlbum)) {
            return "%s can't delete this album.".formatted(username);
        }

        for (Song song : searchedAlbum.getSongs()) {
            if (getAudioCollectionsStream().anyMatch(collection -> collection.containsTrack(song))
                    || getAudioFilesStream().anyMatch(audioFile -> audioFile == song)) {
                return "%s can't delete this album.".formatted(username);
            }
        }

        for (Song song: searchedAlbum.getSongs()) {
            users.forEach(user -> {
                user.getLikedSongs().remove(song);
                user.getPlaylists().forEach(playlist -> playlist.removeSong(song));
            });
            songs.remove(song);
        }

        currentArtist.getAlbums().remove(searchedAlbum);
        return "%s deleted the album successfully.".formatted(username);
    }

    /**
     * Add podcast string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addPodcast(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String podcastName = commandInput.getName();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        if (currentHost.getPodcasts().stream()
                .anyMatch(podcast -> podcast.getName().equals(podcastName))) {
            return "%s has another podcast with the same name.".formatted(username);
        }

        List<Episode> episodes = commandInput.getEpisodes().stream()
                .map(episodeInput ->
                        new Episode(episodeInput.getName(),
                                episodeInput.getDuration(),
                                episodeInput.getDescription()))
                .collect(Collectors.toList());

        Set<String> episodeNames = new HashSet<>();
        if (!episodes.stream().filter(episode -> !episodeNames.add(episode.getName()))
                .collect(Collectors.toSet()).isEmpty()) {
            return "%s has the same episode in this podcast.".formatted(username);
        }

        Podcast newPodcast = new Podcast(podcastName, username, episodes);
        currentHost.getPodcasts().add(newPodcast);
        podcasts.add(newPodcast);

        return "%s has added new podcast successfully.".formatted(username);
    }


    /**
     * Remove podcast string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removePodcast(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String podcastName = commandInput.getName();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        Podcast searchedPodcast = currentHost.getPodcast(podcastName);

        if (searchedPodcast == null) {
            return "%s doesn't have a podcast with the given name.".formatted(username);
        }

        if (getAudioCollectionsStream().anyMatch(collection -> collection == searchedPodcast)) {
            return "%s can't delete this podcast.".formatted(username);
        }

        currentHost.getPodcasts().remove(searchedPodcast);
        podcasts.remove(searchedPodcast);
        return "%s deleted the podcast successfully.".formatted(username);
    }

    /**
     * Add event string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addEvent(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String eventName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        if (currentArtist.getEvent(eventName) != null) {
            return "%s has another event with the same name.".formatted(username);
        }

        String date = commandInput.getDate();

        if (!checkDate(date)) {
            return "Event for %s does not have a valid date.".formatted(username);
        }
        for (User user : users) {
            Subscribe userSubscribes = user.getSubscribes();
            if (userSubscribes != null) {
                // we check if there is the name of the artist in the list of names that the user
                // has subscribed
                List<String> names = userSubscribes.getNames();
                if (names.contains(commandInput.getUsername())) {
                    Notification notification = new Notification("New Event", "New Event from "
                            + commandInput.getUsername() + ".");
                    notification.addObserver(user.getListManager());
                    notification.notifyObservers();
                }
            }
        }
        currentArtist.getEvents().add(new Event(eventName,
                commandInput.getDescription(),
                commandInput.getDate()));
        return "%s has added new event successfully.".formatted(username);
    }

    /**
     * Remove event string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removeEvent(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String eventName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        Event searchedEvent = currentArtist.getEvent(eventName);
        if (searchedEvent == null) {
            return "%s doesn't have an event with the given name.".formatted(username);
        }

        currentArtist.getEvents().remove(searchedEvent);
        return "%s deleted the event successfully.".formatted(username);
    }

    private boolean checkDate(final String date) {
        if (date.length() != dateStringLength) {
            return false;
        }

        List<String> dateElements = Arrays.stream(date.split("-", dateFormatSize)).toList();

        if (dateElements.size() != dateFormatSize) {
            return false;
        }

        int day = Integer.parseInt(dateElements.get(0));
        int month = Integer.parseInt(dateElements.get(1));
        int year = Integer.parseInt(dateElements.get(2));

        if (day < dateDayLowerLimit
                || (month == 2 && day > dateFebHigherLimit)
                || day > dateDayHigherLimit
                || month < dateMonthLowerLimit || month > dateMonthHigherLimit
                || year < dateYearLowerLimit || year > dateYearHigherLimit) {
            return false;
        }

        return true;
    }

    /**
     * Add merch string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addMerch(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }
        Artist currentArtist = (Artist) currentUser;
        if (currentArtist.getMerch().stream()
                .anyMatch(merch -> merch.getName().equals(commandInput.getName()))) {
            return "%s has merchandise with the same name.".formatted(currentArtist.getUsername());
        } else if (commandInput.getPrice() < 0) {
            return "Price for merchandise can not be negative.";
        }
        for (User user : users) {
            Subscribe userSubscribes = user.getSubscribes();
            if (userSubscribes != null) {
                // we check if there is the name of the artist in the list of names that the user
                // has subscribed
                List<String> names = userSubscribes.getNames();
                if (names.contains(commandInput.getUsername())) {
                    Notification notification = new Notification("New Merchandise",
                            "New Merchandise from " + commandInput.getUsername() + ".");
                    notification.addObserver(user.getListManager());
                    notification.notifyObservers();
                }
            }
        }
        currentArtist.getMerch().add(new Merchandise(commandInput.getName(),
                commandInput.getDescription(),
                commandInput.getPrice(),
                commandInput.getTimestamp()));
        return "%s has added new merchandise successfully.".formatted(username);
    }

    /**
     * Add announcement string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addAnnouncement(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String announcementName = commandInput.getName();
        String announcementDescription = commandInput.getDescription();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        Announcement searchedAnnouncement = currentHost.getAnnouncement(announcementName);
        if (searchedAnnouncement != null) {
            return "%s has already added an announcement with this name.";
        }

        currentHost.getAnnouncements().add(new Announcement(announcementName,
                announcementDescription));
        return "%s has successfully added new announcement.".formatted(username);
    }

    /**
     * Remove announcement string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removeAnnouncement(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String announcementName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        Announcement searchAnnouncement = currentHost.getAnnouncement(announcementName);
        if (searchAnnouncement == null) {
            return "%s has no announcement with the given name.".formatted(username);
        }

        currentHost.getAnnouncements().remove(searchAnnouncement);
        return "%s has successfully deleted the announcement.".formatted(username);
    }

    /**
     * Change page string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String changePage(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String nextPage = commandInput.getNextPage();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("user")) {
            return "%s is not a normal user.".formatted(username);
        }

        User user = (User) currentUser;
        if (!user.isStatus()) {
            return "%s is offline.".formatted(user.getUsername());
        }

        switch (nextPage) {
            case "Home" -> user.setCurrentPageFactory(new HomePageFactory(user));
            case "LikedContent" -> user.setCurrentPageFactory(new LikedContentPageFactory(user));
            case "Artist", "Host" -> user.setCurrentPageFactory(user.findArtistOrHostPage());
            default -> {
                return "%s is trying to access a non-existent page.".formatted(username);
            }
        }
        user.setCurrentPage(user.getCurrentPageFactory().createPage());
        // adding the new Page in the list of user's accessed pages
        List<Page> usersPages = user.getPages();
        usersPages.add(user.getCurrentPage());
        user.setPages(usersPages);
        user.setCurrentIndex(usersPages.size() - 1);

        return "%s accessed %s successfully.".formatted(username, nextPage);
    }

    /**
     * Print current page string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String printCurrentPage(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("user")) {
            return "%s is not a normal user.".formatted(username);
        }

        User user = (User) currentUser;
        if (!user.isStatus()) {
            return "%s is offline.".formatted(user.getUsername());
        }

        return user.getCurrentPage().printCurrentPage();
    }

    /**
     * Switch status string.
     *
     * @param username the username
     * @return the string
     */
    public String switchStatus(final String username) {
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        }

        if (currentUser.userType().equals("user")) {
            ((User) currentUser).switchStatus();
            return username + " has changed status successfully.";
        } else {
            return username + " is not a normal user.";
        }
    }

    /**
     * Gets online users.
     *
     * @return the online users
     */
    public List<String> getOnlineUsers() {
        return users.stream().filter(User::isStatus).map(User::getUsername).toList();
    }

    private Stream<AudioCollection> getAudioCollectionsStream() {
        return users.stream().map(User::getPlayer)
                .map(Player::getCurrentAudioCollection).filter(Objects::nonNull);
    }

    private Stream<AudioFile> getAudioFilesStream() {
        return users.stream().map(User::getPlayer)
                .map(Player::getCurrentAudioFile).filter(Objects::nonNull);
    }

    /**
     * Gets top 5 album list.
     *
     * @return the top 5 album list
     */
    public List<String> getTop5AlbumList() {
        List<Album> albums = artists.stream().map(Artist::getAlbums)
                .flatMap(List::stream).toList();

        final Map<Album, Integer> albumLikes = new HashMap<>();
        albums.forEach(album -> albumLikes.put(album, album.getSongs().stream()
                .map(Song::getLikes).reduce(0, Integer::sum)));

        return albums.stream().sorted((o1, o2) -> {
            if ((int) albumLikes.get(o1) == albumLikes.get(o2)) {
                return o1.getName().compareTo(o2.getName());
            }
            return albumLikes.get(o2) - albumLikes.get(o1);
        }).limit(limit).map(Album::getName).toList();
    }

    /**
     * Gets top 5 artist list.
     *
     * @return the top 5 artist list
     */
    public List<String> getTop5ArtistList() {
        final Map<Artist, Integer> artistLikes = new HashMap<>();
        artists.forEach(artist -> artistLikes.put(artist, artist.getAllSongs().stream()
                .map(Song::getLikes).reduce(0, Integer::sum)));

        return artists.stream().sorted(Comparator.comparingInt(artistLikes::get).reversed())
                .limit(limit).map(Artist::getUsername).toList();
    }

    /**
     * Gets top 5 songs.
     *
     * @return the top 5 songs
     */
    public List<String> getTop5Songs() {
        List<Song> sortedSongs = new ArrayList<>(songs);
        sortedSongs.sort(Comparator.comparingInt(Song::getLikes).reversed());
        List<String> topSongs = new ArrayList<>();
        int count = 0;
        for (Song song : sortedSongs) {
            if (count >= limit) {
                break;
            }
            topSongs.add(song.getName());
            count++;
        }
        return topSongs;
    }

    /**
     * Gets top 5 playlists.
     *
     * @return the top 5 playlists
     */
    public List<String> getTop5Playlists() {
        List<Playlist> sortedPlaylists = new ArrayList<>(getPlaylists());
        sortedPlaylists.sort(Comparator.comparingInt(Playlist::getFollowers)
                .reversed()
                .thenComparing(Playlist::getTimestamp, Comparator.naturalOrder()));
        List<String> topPlaylists = new ArrayList<>();
        int count = 0;
        for (Playlist playlist : sortedPlaylists) {
            if (count >= limit) {
                break;
            }
            topPlaylists.add(playlist.getName());
            count++;
        }
        return topPlaylists;
    }

    /**
     * This method finds out if a user has accessed an artist's or host's page and if yes,
     * it performs an operation of subscription/unsubscription on them
     *
     * @param commandInput the command input
     * @return the message indicating the success or failure to subscribe/unsubscribe
     */
    public String subscribe(final CommandInput commandInput) {
        User user = getUser(commandInput.getUsername());

        if (user == null) {
            return "The username %s doesn't exist.".formatted(commandInput.getUsername());
        }

        if (user.userType().equals("user")) {
            // Iterate through all artists and hosts from system to find if the user is on
            // someone's page

            for (Artist artist : getArtists()) {
                if (user.getCurrentPage() == artist.getPage()) {
                    return user.performSubscribe(artist.getUsername(), timestamp);
                }
            }

            for (Host host : getHosts()) {
                if (user.getCurrentPage() == host.getPage()) {
                    return user.performSubscribe(host.getUsername(), timestamp);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves and returns the notifications for the specified user
     * Updates the last notified time for the user
     *
     * @param commandInput the command input
     * @return the JSON array node containing the user's notifications
     */
    public ArrayNode getNotifications(final CommandInput commandInput) {
        User user = Admin.getInstance().getUser(commandInput.getUsername());
        user.setLastNotifiedTime(commandInput.getTimestamp());
        return user.getListManager().displayNotifications();
    }

    /**
     * Add the merch price in the merch revenue for that artist
     *
     * @param commandInput the command input
     * @return a message indicating the success of buying the merch
     */
    public String buyMerch(final CommandInput commandInput) {
        User user = Admin.getInstance().getUser(commandInput.getUsername());
        if (user == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }
        // Check if the user is on an artist's page
        for (Artist artist : artists) {
            if (user.getCurrentPage().equals(artist.getPage())) {
                // Check if the Merch exists
                List<Merchandise> merchandises = artist.getMerch();
                for (Merchandise merch : merchandises) {
                    if (merch.getName().equals(commandInput.getName())) {
                        user.getMerches().add(merch);
                        artist.addMerchRevenue((double) merch.getPrice());
                        return commandInput.getUsername() + " has added new merch successfully.";
                    }
                }
                return "The merch " + commandInput.getName() + " doesn't exist.";
            }
        }
        return "Cannot buy merch from this page.";
    }

    /**
     * Get the merchandises' names from the list of Merchandise objects of user
     *
     * @param commandInput the command input
     * @return the list of merchandises
     */
    public List<String> seeMerch(final CommandInput commandInput) {
        User user = Admin.getInstance().getUser(commandInput.getUsername());
        List<String> merchNames = user.getMerches().stream().map(Merchandise::getName)
                .collect(Collectors.toList());

        return merchNames;
    }

    /**
     * This method updates the user's list of recommendations
     *
     * @param commandInput the command input
     * @return a message with the success of the operation
     */
    public String updateRecommendations(final CommandInput commandInput) {
        User user = getUser(commandInput.getUsername());

        if (commandInput.getRecommendationType().equals("random_song")) {
            user.updateRecommendationsRandomSong();
        }
        if (commandInput.getRecommendationType().equals("random_playlist")) {
            user.updateRecommendationsRandomPlaylist(timestamp);
        }
        if (commandInput.getRecommendationType().equals("fans_playlist")) {
           user.updateRecommendationsFansPlaylist(timestamp);
        }
        return "The recommendations for user " + user.getUsername() +
                " have been updated successfully.";

    }

    /**
     * Moves the user to the previous page, updating the current index.
     *
     * @param commandInput The input containing the username.
     * @return A message indicating the success or failure of navigating to the previous page.
     */
    public String previousPage(final CommandInput commandInput) {
        User user = getInstance().getUser(commandInput.getUsername());

        // Check if there are no more pages to go back
        if (user.getCurrentIndex() == 0) {
            return "There are no pages left to go back.";
        }

        // Execute the previous page command and update user information
        Command previousCommand = new PreviousPage(user);
        previousCommand.execute();

        return "The user %s has navigated successfully to the previous page."
                .formatted(user.getUsername());
    }


    /**
     * Moves the user to the next page, updating the current index.
     *
     * @param commandInput The input containing the username.
     * @return A message indicating the success or failure of navigating to the next page.
     */
    public String nextPage(final CommandInput commandInput) {
        User user = getInstance().getUser(commandInput.getUsername());

        // Check if there are no more pages to go forward
        if (user.getCurrentIndex() == user.getPages().size() - 1) {
            return "There are no pages left to go forward.";
        }

        // Execute the next page command and update user information
        Command nextCommand = new NextPage(user);
        nextCommand.execute();

        return "The user %s has navigated successfully to the next page."
                .formatted(user.getUsername());
    }


    /**
     * Updates the statistics for both the user who is playing a podcast and for the host
     *
     * @param podcast the podcast that is being played
     * @param episode the current episode
     * @param user the user who plays the podcast
     */
    public void updateStatisticsForEpisodes(final Podcast podcast, final Episode episode,
                                            final User user) {
        Host host = Admin.getInstance().getHost(podcast.getOwner());
        if (host != null) {
            // update the statistics for the host
            List<User> hostListeners = host.getListeners();
            if (!hostListeners.contains(user)) {
                hostListeners.add(user);
                host.setListeners(hostListeners);
                host.setNoListeners(host.getNoListeners() + 1);
            }
            updateInfoList(host.getEpisodesInfos(), episode.getName(), 1);

        }
        // update the statistics for the user
        updateInfoList(user.getEpisodesInfos(), episode.getName(), 1);
    }

    /**
     * Updates the number of listeners and the list of listeners' names for the artist
     *
     * @param artist the artist
     * @param user the user who is going to be added (or not) in the list of listeners
     */
    public void updateListeners(final Artist artist, final User user) {
        List<User> artistListeners = artist.getListeners();
        if (!artistListeners.contains(user)) {
            artistListeners.add(user);
            artist.setListeners(artistListeners);
            artist.setNoListeners(artist.getNoListeners() + 1);
        }
    }
    /**
     * Updates the artist's list of fans' names
     *
     * @param artist the artist
     * @param user the user whose name is going to be added (or not) in the list of fans
     */
    public void updateFans(final Artist artist, final User user) {
        List<String> fansInfos = artist.getFansNames();
        if (!fansInfos.contains(user.getUsername())) {
            fansInfos.add(user.getUsername());
            artist.setFansNames(fansInfos);
        }
    }

    /**
     * Updates the info list for each field that has to be populated for users and artists
     *
     * @param infos the list of infos (songInfos, albumInfos, etc.)
     * @param name the name of the item that has to be added
     * @param noToIncrement the number of incrementation for that item
     */
    public void updateInfoList(final List<Infos> infos, final String name,
                               final int noToIncrement) {
        if (!infos.stream().anyMatch(sg -> sg.getName().equals(name))) {
            infos.add(new Infos(name, noToIncrement));
        } else {
            infos.stream()
                    .filter(sg -> sg.getName().equals(name))
                    .forEach(sg -> sg.setNoListen(sg.getNoListen() + noToIncrement));

        }
    }

    /**
     * Update all statistics both for the user and album's artist
     *
     * @param album the album that is played or was last played
     * @param user the user who loaded the album
     * @param loadTime the time when the album was loaded
     * @param lastUpdatedTime the last time when statistics for this album were set
     * @param currentTime the current time
     */
    public void updateStatisticsForAlbums(final Album album, final User user, final int loadTime,
                                          final int lastUpdatedTime, final int currentTime) {
        Artist artist = Admin.getInstance().getArtist(album.getOwner());
        if (artist != null) {
            //update the listeners of the artist
            updateListeners(artist, user);

            //update the fansInfos for artist
            updateFans(artist, user);

            // if there has been an update between load time and current time, then we have to
            // ignore the songs that have been already added in the statistics. This is why we
            // iterate through the list of song in order to find the last index of song registered
            int index = 0;

            // The time passed from when the album was loaded to the last updated time
            int alreadyRegisteredTime = lastUpdatedTime - loadTime;

            // A counter that we use to find the index of the last song registered
            int counterForSongsRegistered = 0;
            if (alreadyRegisteredTime != 0) {
                for (int i = 0; i < album.getSongs().size(); i++) {
                    counterForSongsRegistered += album.getSongs().get(i).getDuration();
                    if (counterForSongsRegistered > alreadyRegisteredTime) {
                        index = i + 1;
                        break;
                    }
                }
            }

            // The remained time to analyze from the last update to the present
            int remainedTime = currentTime - loadTime - counterForSongsRegistered;

            // A counter that we use in order to know how many songs can be played in remainedTime
            int countTime = 0;

            // The number of songs that we count
            int noListensForAlbum = 0;

            for (int i = index; i < album.getSongs().size(); i++) {
                Song song = album.getSongs().get(i);

                // We update the statistics until countTime is bigger than the remained time
                if (countTime <= remainedTime && (counterForSongsRegistered < currentTime)) {
                    countTime += song.getDuration();
                    noListensForAlbum++;

                    //update songInfos for artist and for user
                    updateInfoList(artist.getSongsInfos(), song.getName(), 1);
                    updateInfoList(user.getSongsInfos(), song.getName(), 1);

                    // update songInfos for premium and free users
                    if (user.isPremium()) {
                        updateInfoList(user.getSongsInfosPremium(), song.getName(), 1);
                    } else {
                        updateInfoList(user.getSongsInfosFree(), song.getName(), 1);
                    }

                    //update the GenreInfos for users
                    updateInfoList(user.getGenresInfos(), song.getGenre(), 1);

                }
            }

            // update album info for artist and user
            updateInfoList(artist.getAlbumsInfos(), album.getName(), noListensForAlbum);
            updateInfoList(user.getAlbumsInfos(), album.getName(), noListensForAlbum);


            // update artists info for user
            updateInfoList(user.getArtistsInfos(), artist.getUsername(), noListensForAlbum);

        }
    }

    /**
     * Updates all the statistics about this song both for user and for the song's artist
     *
     * @param song the song played
     * @param user the user who played the song
     */
    public void updateStatisticsForSong(final Song song, final User user) {
        Artist artist = Admin.getInstance().getArtist(song.getArtist());
        if (artist != null) {
            //update the listeners for artist
            updateListeners(artist, user);

            //update the fansInfos for artist
            updateFans(artist, user);

            //update songInfos for artist and for user
            updateInfoList(artist.getSongsInfos(), song.getName(), 1);
            updateInfoList(user.getSongsInfos(), song.getName(), 1);

            // update songInfos for premium and free users
            if (user.isPremium()) {
                updateInfoList(user.getSongsInfosPremium(), song.getName(), 1);
            } else {
                updateInfoList(user.getSongsInfosFree(), song.getName(), 1);
            }

            // update album info for artist and user
            updateInfoList(artist.getAlbumsInfos(), song.getAlbum(), 1);
            updateInfoList(user.getAlbumsInfos(), song.getAlbum(), 1);

            // update artists info for user
            updateInfoList(user.getArtistsInfos(), artist.getUsername(), 1);

            //update the GenreInfos for users
            updateInfoList(user.getGenresInfos(), song.getGenre(), 1);
        }
    }

    /**
     * This method verifies the last copy player of the user in order to update all the
     * statistics given by the player type
     *
     * @param currentTime the current time when this method is called
     * @param user the user whose copy player is being analysed
     */
    public void updateStatistics(final int currentTime, final User user) {
        // Verify if a copy Player exists for this user
        if (user.getCopyPlayer() != null && user.getCopyPlayer().getSource() != null) {

            int loadTime = user.getCopyPlayer().getLoadTimestamp();
            int lastUpdatedTime = user.getCopyPlayer().getUpdatedTimestamp();

            // the time that has passed between the last load command and current search command
            int passedTime = currentTime - lastUpdatedTime;

            // Check if the source is a podcast
            if (user.getCopyPlayer().getType().equals("podcast")) {
                // A counter where we accumulate time from the episodes that have been seen
                int countTime = 0;

                Podcast podcast = (Podcast) user.getCopyPlayer().getCurrentAudioCollection();
                for (Episode episode : podcast.getEpisodes()) {
                    if (countTime <= passedTime) {
                        countTime += episode.getDuration();
                        updateStatisticsForEpisodes(podcast, episode, user);
                    }
                }
            }

            // Check if the source is an album
            if (user.getCopyPlayer().getType().equals("album")) {
                Album album = (Album) user.getCopyPlayer().getCurrentAudioCollection();
                updateStatisticsForAlbums(album, user, loadTime, lastUpdatedTime, currentTime);
            }

            // Check if the source is a song
            if (user.getCopyPlayer().getType().equals("song")) {
                Song song = (Song) user.getCopyPlayer().getCurrentAudioFile();
                if (lastUpdatedTime == loadTime) {
                    updateStatisticsForSong(song, user);
                }
            }
        }
    }

    /**
     * Sets the statistics of the user/artist/host in the required format
     *
     * @param commandInput the command input
     * @return the object node with the required statistics
     */
    public ObjectNode wrapped(final CommandInput commandInput) {
        // Before this command we update all statistics
        for (User user : users) {
            if (user.getCopyPlayer() != null && user.getCopyPlayer().getUpdatedTimestamp()
                    != commandInput.getTimestamp()) {
                updateStatistics(commandInput.getTimestamp(), user);
                user.getCopyPlayer().setUpdatedTimestamp(commandInput.getTimestamp());
            }
        }

        // Check if the name in the command input belongs to an artist
        if (artists.stream().anyMatch(artist -> artist.getUsername()
                .equals(commandInput.getUsername()))) {
            Artist artist = getArtist(commandInput.getUsername());

            // Set the statistics in WrappedArtist format
            artist.setStatistics(new WrappedArtist(artist.getAlbumsInfos(), artist.getSongsInfos(),
                    artist.getFansNames(), artist.getNoListeners()));
            artist.arrangeStatistics(artist);

            return artist.formattedStatisticsArtist();
        }

        // Check if the name in the command input belongs to a host
        if (hosts.stream().anyMatch(host -> host.getUsername()
                .equals(commandInput.getUsername()))) {
            Host host = getHost(commandInput.getUsername());

            // Set the statistics in WrappedHost format
            host.setStatistics(new WrappedHost(host.getEpisodesInfos(), host.getNoListeners()));

            return host.formattedStatisticsHost();
        }

        User user = getUser(commandInput.getUsername());

        // Set the statistics in a WrappedUser format
        user.setStatistics(new WrappedUser(user.getArtistsInfos(), user.getGenresInfos(),
                user.getSongsInfos(), user.getAlbumsInfos(), user.getEpisodesInfos()));
        user.arrangeStatistics();

        return user.formattedStatisticsUser();
    }

    /**
     * Set the premium status of a user (if possible)
     *
     * @param commandInput the command input
     * @return a message indicating whether the process was successful or not
     */
    public String buyPremium(final CommandInput commandInput) {
        User user = Admin.instance.getUser(commandInput.getUsername());
        if (user == null) {
            return "The username %s doesn't exist.".formatted(commandInput.getUsername());
        }
        if (user.isPremium()) {
            return "%s is already a premium user.".formatted(user.getUsername());
        }

        // Update statistics for user before buying Premium status so that the current song
        // will not be counted for revenue
        if (user.getCopyPlayer() != null && user.getCopyPlayer().getUpdatedTimestamp()
                != commandInput.getTimestamp()) {
            updateStatistics(commandInput.getTimestamp(), user);
            user.getCopyPlayer().setUpdatedTimestamp(commandInput.getTimestamp());
        }

        user.setPremium(true);
        return "%s bought the subscription successfully.".formatted(user.getUsername());
    }

    /**
     * Cancels the premium status of a user (if possible)
     *
     * @param commandInput the command input
     * @return a message indicating whether the cancellation was successful or not
     */
    public String cancelPremium(final CommandInput commandInput) {
        User user = Admin.instance.getUser(commandInput.getUsername());
        if (user == null) {
            return "The username %s doesn't exist.".formatted(commandInput.getUsername());
        }
        if (!user.isPremium()) {
            return "%s is not a premium user.".formatted(user.getUsername());
        }

        // update statistics for user before canceling Premium status so that the current song
        // will be counted for revenue
        if (user.getCopyPlayer() != null && user.getCopyPlayer().getUpdatedTimestamp()
                != commandInput.getTimestamp()) {
            updateStatistics(commandInput.getTimestamp(), user);
            user.getCopyPlayer().setUpdatedTimestamp(commandInput.getTimestamp());
        }

        user.setPremium(false);
        return "%s cancelled the subscription successfully.".formatted(user.getUsername());
    }

    /**
     * Calculates the revenue generated for a specific song by the given user.
     *
     * @param user the user for whom the revenue is calculated.
     * @param songName the name of the song for which the revenue is calculated.
     * @return the calculated revenue for the specified song and user.
     */
    public double calculateRevenueForSong(final User user, final String songName) {
        int totalNumberOfListens = 0;
        int numberOfListensForSong = 0;
        List<Infos> songinfos = user.getSongsInfosPremium();
        for (Infos song : songinfos) {
            totalNumberOfListens += song.getNoListen();
            if (song.getName().equals(songName)) {
                numberOfListensForSong = song.getNoListen();
            }
        }
        return ((double) maxRevenueforPremium / totalNumberOfListens) * numberOfListensForSong;
    }

    /**
     * Finds the most profitable song for a given artist among the provided list of distinct songs.
     * Calculates the total songRevenue for this artist
     *
     * @param artist the artist for whom the most profitable song is being determined.
     * @param distinctSongs the list of distinct songs to consider for profitability.
     * @return The total revenue generated by all distinct songs.
     */
    public double findMostProfitable(final Artist artist, final List<Song> distinctSongs) {
        double maxRevenue = 0.0;
        double songRevenue = 0.0;
        for (Song song : distinctSongs) {
            songRevenue += song.getRevenue();
            if (song.getRevenue() > maxRevenue
                    || (song.getRevenue() == maxRevenue && song.getRevenue() != 0
                    && song.getName().compareTo(artist.getMostProfitableSong()) < 0)) {
                maxRevenue = song.getRevenue();
                artist.setMostProfitableSong(song.getName());
            }
        }
        return songRevenue;
    }

    /**
     * Updates the revenues for all songs and artists based on user's premium song plays.
     * Iterates through the premium songs of all users, calculates the revenue for each song,
     * and updates the song revenues. Then, for each artist, determines the most profitable song
     * among their distinct songs and sets the total song revenue for the artist.
     */
    public void updateSongsRevenues() {
        // Iterate through the list of songsInfoPremium for all users
        // Creating the song object from the name field from each songInfo
        // Setting the songRevenue for each song created
        getAllUsers().stream()
                .map(this::getUser)
                .filter(Objects::nonNull)
                .forEach(user -> user.getSongsInfosPremium().stream()
                        .filter(songInfo -> getSongs().stream()
                                .anyMatch(song -> song.getName().equals(songInfo.getName())))
                        .forEach(songInfo -> {
                            double revenue = calculateRevenueForSong(user, songInfo.getName());
                            getSongs().stream()
                                    .filter(song -> song.getName().equals(songInfo.getName()))
                                    .forEach(song -> song.setRevenue(song.getRevenue() + revenue));
                        }));

        for (Artist artist : artists) {
            List<Song> allSongs = new ArrayList<>(artist.getAllSongs());

            // Getting only the distinct songs from this artist (an artist can have the same song
            // in more than one album)
            List<Song> distinctSongs = new ArrayList<>(allSongs.stream()
                    .collect(Collectors.toMap(Song::getName, Function.identity(),
                            (existing, replacement) -> existing))
                    .values()
                    .stream()
                    .collect(Collectors.toList()));

            // Find out which song is the most profitable song of this artist and
            // calculate the total songRevenue for this artist
            double songRevenue = findMostProfitable(artist, distinctSongs);

            songRevenue = Math.round(songRevenue * hundred) / hundred;
            artist.setSongRevenue(songRevenue);
        }
    }

    /**
     * Adds an ad in the queue in order to be listened after the current played source
     *
     * @param commandInput the command input
     * @return A message indicating the result of the ad break insertion.
     */
    public String adBreak(final CommandInput commandInput) {
        User user = getUser(commandInput.getUsername());
        if (user == null) {
            return "The username %s doesn't exist.".formatted(commandInput.getUsername());
        }
        if (user.getPlayer().getSource() == null) {
            return "%s is not playing any music.".formatted(user.getUsername());
        }

        // Update the statistics up to now
        if (user.getCopyPlayer() != null && user.getCopyPlayer().getUpdatedTimestamp()
                != commandInput.getTimestamp()) {
            updateStatistics(commandInput.getTimestamp(), user);
            user.getCopyPlayer().setUpdatedTimestamp(commandInput.getTimestamp());
        }

        // adding a break in queue for user
        int currentTimestamp = commandInput.getTimestamp();
        if (user.getPlayer().getSource() != null) {
            int remainedTime = user.getPlayerStats().getRemainedTime();
            currentTimestamp += remainedTime;
            user.setAdBreak(new AdBreak(currentTimestamp));
        }

        return "Ad inserted successfully.";
    }

}
