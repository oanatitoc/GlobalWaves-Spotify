package app;

import app.audio.Collections.Album;
import app.audio.Collections.AudioCollection;
import app.audio.Collections.Playlist;
import app.audio.Collections.Podcast;
import app.audio.Files.AudioFile;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.pages.Command;
import app.pages.NextPage;
import app.pages.Page;
import app.pages.PreviousPage;
import app.player.Player;
import app.user.*;
import app.user.Statistics.WrappedArtist;
import app.user.Statistics.WrappedHost;
import app.user.Statistics.WrappedUser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;
import lombok.Getter;

import java.util.*;
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
        // we add a notification in the notification list of all users that have subscribed to this artist.
        for (User user : users) {
            Subscribe userSubscribes = user.getSubscribes();
            if (userSubscribes != null) {
                // we check if there is the name of the artist in the list of names that the user has subscribed
                List<String> names = userSubscribes.getNames();
                if (names.contains(commandInput.getUsername())) {
                    Notification notification = new Notification("New Album", "New Album from " + commandInput.getUsername() + ".");
                    userSubscribes.getNotifications().add(notification);
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
                // we check if there is the name of the artist in the list of names that the user has subscribed
                List<String> names = userSubscribes.getNames();
                if (names.contains(commandInput.getUsername())) {
                    Notification notification = new Notification("New Event", "New Event from " + commandInput.getUsername() + ".");
                    userSubscribes.getNotifications().add(notification);
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
                // we check if there is the name of the artist in the list of names that the user has subscribed
                List<String> names = userSubscribes.getNames();
                if (names.contains(commandInput.getUsername())) {
                    Notification notification = new Notification("New Merchandise", "New Merchandise from " + commandInput.getUsername() + ".");
                    userSubscribes.getNotifications().add(notification);
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
            case "Home" -> user.setCurrentPage(user.getHomePage());
            case "LikedContent" -> user.setCurrentPage(user.getLikedContentPage());
            case "Artist", "Host" -> user.setCurrentPage(user.findArtistHostPage());
            default -> {
                return "%s is trying to access a non-existent page.".formatted(username);
            }
        }
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

    public String subscribe(final CommandInput commandInput) {
        User user = getUser(commandInput.getUsername());

        if (user == null) {
            return "The username %s doesn't exist.".formatted(commandInput.getUsername());
        }

        if (user.userType().equals("user")) {
            // Iterate through all artists and hosts from system to find if the user is someone's page
            for (Artist artist : getArtists()) {
                if (user.getCurrentPage() == artist.getPage()) {
                    Subscribe userSubscribes = user.getSubscribes();
                    if(userSubscribes != null) {
                        List<String> names = userSubscribes.getNames();
                        for (String name : names) {
                            if (name.equals(artist.getUsername())) {
                                // we remove the name and the notifications relatet to it
                                names.remove(name);
                                List<Notification> notifications = userSubscribes.getNotifications();
                                for (Notification notification : notifications) {
                                    if (notification.getDescription().contains(name)) {
                                        notifications.remove(notification);
                                    }
                                }
                                return user.getUsername() + " unsubscribed from " + artist.getUsername() + " successfully.";
                            }
                        }
                    }
                    userSubscribes.getNames().add(artist.getUsername());
                    user.setLastNotifiedTime(commandInput.getTimestamp());
                    return user.getUsername() + " subscribed to " + artist.getUsername() + " successfully.";
                }
            }
            for (Host host : getHosts()) {
                if (user.getCurrentPage() == host.getPage()) {
                    Subscribe userSubscribes = user.getSubscribes();
                    List<String> names = userSubscribes.getNames();
                    for (String name : names) {
                        if (name.equals(host.getUsername())) {
                            // we remove the name and the notifications relatet to it
                            names.remove(name);
                            List<Notification> notifications = userSubscribes.getNotifications();
                            for (Notification notification : notifications) {
                                if (notification.getDescription().contains(name)) {
                                    notifications.remove(notification);
                                }
                            }
                            return user.getUsername() + " unsubscribed from " + host.getUsername() + " successfully.";
                        }
                    }
                    names.add(host.getUsername());
                    user.setLastNotifiedTime(commandInput.getTimestamp());
                    return user.getUsername() + " subscribed to " + host.getUsername() + " successfully.";
                }
            }
        }
        return null;
    }

    public List<Notification> getNotifications(final CommandInput commandInput) {
        User user = Admin.getInstance().getUser(commandInput.getUsername());
        Subscribe userSubscribe = user.getSubscribes();
        List<Notification> notifications = new ArrayList<>(userSubscribe.getNotifications());
        userSubscribe.getNotifications().clear();
        user.setLastNotifiedTime(commandInput.getTimestamp());
        return notifications;
    }

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
    public List<String> seeMerch(final CommandInput commandInput) {
        User user = Admin.getInstance().getUser(commandInput.getUsername());
        List<String> merchNames = user.getMerches().stream().map(Merchandise::getName).collect(Collectors.toList());

        return merchNames;
    }

    public String updateRecommendations(final CommandInput commandInput) {
        User user = getUser(commandInput.getUsername());

        if (commandInput.getRecommendationType().equals("random_song")) {
            // find the current song that the user is playing
            String songName = user.getPlayer().getCurrentAudioFile().getName();
            for (Song song : getSongs()) {
                if (song.getName().equals(songName)) {
                    int remainedTime = user.getPlayerStats().getRemainedTime();
                    if (song.getDuration() - remainedTime >= 30) {
                        List<Song> sameGenreSongs = getSongs().stream()
                                .filter(thisSong -> thisSong.getGenre().equals(song.getGenre()))
                                .collect(Collectors.toList());

                        if (!sameGenreSongs.isEmpty()) {
                            // Generate a random index in the range [0, sameGenreSongs.size() - 1]
                            int randomIndex = new Random(song.getDuration() - remainedTime).nextInt(sameGenreSongs.size());

                            // We choose the song corresponding to the randomly generated index.
                            Song randomSong = sameGenreSongs.get(randomIndex);

                            // we add the song in the songsRecommendations list of the user
                            ArrayList<Song> userSongs = user.getSongsRecommendations();
                            userSongs.add(randomSong);
                            user.setSongsRecommendations(userSongs);
                            user.setLastRecommendation(randomSong);
                            user.setLastRecommendationType("song");
                        }

                        return "The recommendations for user " + user.getUsername() + " have been updated successfully.";
                    }
                }
            }
        }
        if (commandInput.getRecommendationType().equals("random_playlist")) {
            // finding out the top 3 genres
            List<Song> likedSongs = user.getLikedSongs();
            List<Playlist> usersPlaylists = user.getPlaylists();
            List<Playlist> followedPlaylists = user.getFollowedPlaylists();

            // Creating the ArrayList with the names of the first 3 genres
            Map<String, Integer> genreCountMap = new HashMap<>();

            // Count genres in liked songs
            countGenres(likedSongs, genreCountMap);

            // Count genres in user's playlists
            for (Playlist playlist : usersPlaylists) {
                countGenres(playlist.getSongs(), genreCountMap);
            }

            // Count genres in followed playlists
            for (Playlist playlist : followedPlaylists) {
                countGenres(playlist.getSongs(), genreCountMap);
            }

            // Sort genres by count in descending order
            List<Map.Entry<String, Integer>> sortedGenres =
                    genreCountMap.entrySet().stream()
                            .sorted(Comparator.comparingInt(Map.Entry::getValue))
                            .limit(3)
                            .toList();

            // Extract genre names from the sorted list
            List<String> topGenres = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : sortedGenres) {
                topGenres.add(entry.getKey());
            }

            Map<String, List<Song>> topSongsByGenre = new HashMap<>();

            for (String genre : topGenres) {
                List<Song> songsByGenre = filterSongsByGenre(songs, genre);
                List<Song> topSongs;

                switch (topGenres.indexOf(genre)) {
                    case 0:
                        topSongs = songsByGenre.subList(0, Math.min(5, songsByGenre.size()));
                        break;
                    case 1:
                        topSongs = songsByGenre.subList(0, Math.min(3, songsByGenre.size()));
                        break;
                    case 2:
                        topSongs = songsByGenre.subList(0, Math.min(2, songsByGenre.size()));
                        break;
                    default:
                        topSongs = new ArrayList<>();
                }
                topSongsByGenre.put(genre, topSongs);

            }
            user.createPlaylist(user.getUsername() + "'s recommendations", commandInput.getTimestamp());
            Playlist newPlaylist = user.getPlaylists().get(user.getPlaylists().size() - 1);
            for (List<Song> songs : topSongsByGenre.values()) {
                for (Song song : songs) {
                    newPlaylist.addSong(song);
                }
            }
            ArrayList<Playlist> userPlaylists = user.getPlaylistsRecommendations();
            userPlaylists.add(newPlaylist);
            user.setPlaylistsRecommendations(userPlaylists);
            user.setLastRecommendation(newPlaylist);
            user.setLastRecommendationType("playlist");
            return "The recommendations for user " + user.getUsername() + " have been updated successfully.";
        }
        if (commandInput.getRecommendationType().equals("fans_playlist")) {
            String songName = user.getPlayer().getCurrentAudioFile().getName();
            String artistName = new String();
            for (Song song : songs) {
                if (song.getName().equals(songName)) {
                    artistName = song.getArtist();
                }
            }
            user.createPlaylist(artistName + " Fan Club recommendations", commandInput.getTimestamp());
            Playlist newPlaylist = user.getPlaylists().get(user.getPlaylists().size() - 1);

            ArrayList<Playlist> userPlaylists = user.getPlaylistsRecommendations();
            userPlaylists.add(newPlaylist);
            user.setPlaylistsRecommendations(userPlaylists);
            user.setLastRecommendation(newPlaylist);
            user.setLastRecommendationType("playlist");
            return "The recommendations for user " + user.getUsername() + " have been updated successfully.";
        }
        return null;

    }
    private static void countGenres(List<Song> songs, Map<String, Integer> genreCountMap) {
        for (Song song : songs) {
            String genre = song.getGenre();
            genreCountMap.put(genre, genreCountMap.getOrDefault(genre, 0) + 1);
        }
    }
    private static List<Song> filterSongsByGenre(List<Song> songs, String genre) {
        return songs.stream()
                .filter(song -> genre.equals(song.getGenre()))
                .toList();
    }
    public String previousPage(CommandInput commandInput) {
        User user = getInstance().getUser(commandInput.getUsername());
        if (user.getCurrentIndex() == 0) {
            return "There are no pages left to go back.";
        }
        Command previousCommand = new PreviousPage(user);
        previousCommand.execute();
        return "The user %s has navigated successfully to the previous page.".formatted(user.getUsername());
    }
    public String nextPage(CommandInput commandInput) {
        User user = getInstance().getUser(commandInput.getUsername());
        if (user.getCurrentIndex() == user.getPages().size() - 1) {
            return "There are no pages left to go forward.";
        }
        Command nextCommand = new NextPage(user);
        nextCommand.execute();
        return "The user %s has navigated successfully to the next page.".formatted(user.getUsername());
    }
    public ObjectNode wrapped(CommandInput commandInput) {
        // check if there is the username in the command input belongs to an artist
        // chatGPT
        for (User user : users) {
            user.updateStatistics(user.getLastTimestamp(), commandInput.getTimestamp(), user.getUsername());
            user.setCopyPlayer(null);
        }
        if (artists.stream().anyMatch(artist -> artist.getUsername().equals(commandInput.getUsername()))) {
            Artist artist = getArtist(commandInput.getUsername());
            artist.setStatistics(new WrappedArtist(artist.getAlbumsInfos(), artist.getSongsInfos(),
                    artist.getFansNames(), artist.getNoListeners()));
            artist.arrangeStatistics();
            return artist.formattedStatisticsArtist();
        }
        if (hosts.stream().anyMatch(host -> host.getUsername().equals(commandInput.getUsername()))) {
            Host host = getHost(commandInput.getUsername());
            host.setStatistics(new WrappedHost(host.getEpisodesInfos(), host.getNoListeners()));
            return host.formattedStatisticsHost();
        }
        User user = getUser(commandInput.getUsername());
        user.setStatistics(new WrappedUser(user.getArtistsInfos(), user.getGenresInfos(), user.getSongsInfos(),
                user.getAlbumsInfos(), user.getEpisodesInfos()));
        user.arrangeStatistics();
        return user.formattedStatisticsUser();
    }

}
