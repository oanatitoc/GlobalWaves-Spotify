package app.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.Admin;
import app.audio.Collections.Album;
import app.audio.Collections.AlbumOutput;
import app.audio.Files.Song;
import app.pages.ArtistPage;
import app.user.Entities.Event;
import app.user.Entities.Merchandise;
import app.user.Statistics.*;
import app.user.Statistics.Infos.Infos;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Artist.
 */
public final class Artist extends ContentCreator {
    @Getter
    @Setter
    private ArrayList<Album> albums;
    @Getter
    @Setter
    private ArrayList<Merchandise> merch;
    @Getter
    @Setter
    private ArrayList<Event> events;
    @Getter
    @Setter
    private Double merchRevenue;
    @Getter
    @Setter
    private Double songRevenue;
    @Getter
    @Setter
    private int ranking;
    @Getter
    @Setter
    private String mostProfitableSong;
    @Getter
    @Setter
    private int noPlays;
    @Getter
    @Setter
    private WrappedArtist statistics;
    @Getter
    @Setter
    private List<Infos> albumsInfos;
    @Getter
    @Setter
    private List<Infos> songsInfos;
    @Getter
    @Setter
    private List<String> fansNames;
    @Getter
    @Setter
    private List<User> listeners;
    @Getter
    @Setter
    private int noListeners = 0;
    @Getter
    private boolean accessedData;
    @Getter
    @Setter
    private int noListen = 0;
    private final int five = 5;

    /**
     * Instantiates a new Artist.
     *
     * @param username the username
     * @param age      the age
     * @param city     the city
     */
    public Artist(final String username, final int age, final String city) {
        super(username, age, city);
        albums = new ArrayList<>();
        merch = new ArrayList<>();
        events = new ArrayList<>();
        merchRevenue = 0.0;
        songRevenue = 0.0;
        mostProfitableSong = "N/A";
        ranking = 1;
        noPlays = 0;
        albumsInfos = new ArrayList<>();
        songsInfos = new ArrayList<>();
        fansNames = new ArrayList<>();
        listeners = new ArrayList<>();
        accessedData = false;
        super.setPage(new ArtistPage(this));
    }


    /**
     * Gets event.
     *
     * @param eventName the event name
     * @return the event
     */
    public Event getEvent(final String eventName) {
        for (Event event : events) {
            if (event.getName().equals(eventName)) {
                return event;
            }
        }

        return null;
    }

    /**
     * Gets album.
     *
     * @param albumName the album name
     * @return the album
     */
    public Album getAlbum(final String albumName) {
        for (Album album : albums) {
            if (album.getName().equals(albumName)) {
                return album;
            }
        }

        return null;
    }

    /**
     * Gets all songs.
     *
     * @return the all songs
     */
    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        albums.forEach(album -> songs.addAll(album.getSongs()));

        return songs;
    }

    /**
     * Show albums array list.
     *
     * @return the array list
     */
    public ArrayList<AlbumOutput> showAlbums() {
        ArrayList<AlbumOutput> albumOutput = new ArrayList<>();
        for (Album album : albums) {
            albumOutput.add(new AlbumOutput(album));
        }

        return albumOutput;
    }

    /**
     * Get user type
     *
     * @return user type string
     */
    public String userType() {
        return "artist";
    }

    /**
     * Add the price of the merchRevenue
     *
     * @param price the merch's price
     */
    public void addMerchRevenue(final Double price) {
        merchRevenue += price;
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
     * This method arranges all artist's items calling the previous method.
     * The items in each list implement the Infos interface.
     */
    public void arrangeStatistics(final Artist artist) {
        arrangeTopItems(statistics.getTopAlbums());
        arrangeTopItems(statistics.getTopSongs());

        // Extracting the users from the list with the fans' names
        List<User> fansUsers = statistics.getTopFans()
                                            .stream()
                                            .map(Admin.getInstance()::getUser)
                                            .toList();

        // Creating the fanInfos list based on the users that have listened this artist
        List<Infos> fanInfos = fansUsers.stream()
                .flatMap(user -> user.getArtistsInfos().stream()
                        .filter(artistInfo -> artistInfo.getName().equals(artist.getUsername()))
                        .map(artistInfo -> new Infos(user.getUsername(),
                                artistInfo.getNoListen())))
                .toList();

        // Setting the list of fans' names in statistics
        List<String> fans = fanInfos.stream()
                .sorted(Comparator
                        .comparingInt(Infos::getNoListen)
                        .reversed()
                        .thenComparing(Infos::getName))
                .map(Infos::getName)
                .limit(five)
                .collect(Collectors.toList());
        statistics.setTopFans(fans);

    }

    /**
     * The method creates the object node with artist's statistics based on the
     * information accumulated up to the moment of the "wrapped" command
     *
     * @return the object node containing the statistics
     */
    public ObjectNode formattedStatisticsArtist() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();

        // Retrieving artist's statistics
        WrappedArtist wrappedArtist = getStatistics();

        // Verifying if artist's sources have been accessed
        accessedData = Stream.of(
                        wrappedArtist.getTopSongs(),
                        wrappedArtist.getTopAlbums(),
                        wrappedArtist.getTopFans()
                )
                .anyMatch(thisList -> thisList != null && !thisList.isEmpty());

        /// Creating a JSON object for top albums and adding information
        ObjectNode topAlbumsNode = resultNode.putObject("topAlbums");
        wrappedArtist.getTopAlbums().forEach(album -> topAlbumsNode.put(album.getName(),
                album.getNoListen()));


        // Creating a JSON object for top songs and adding information
        ObjectNode topSongsNode = resultNode.putObject("topSongs");
        wrappedArtist.getTopSongs().forEach(song -> topSongsNode.put(song.getName(),
                song.getNoListen()));

        // Creating a JSON object for top fans and adding information
        ArrayNode topFansNode = resultNode.putArray("topFans");
        wrappedArtist.getTopFans().forEach(topFansNode::add);

        // Adding the number of listeners
        resultNode.put("listeners", wrappedArtist.getListeners());
        return resultNode;
    }

}
