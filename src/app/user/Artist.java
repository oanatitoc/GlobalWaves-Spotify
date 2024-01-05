package app.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import app.Admin;
import app.audio.Collections.Album;
import app.audio.Collections.AlbumOutput;
import app.audio.Files.Song;
import app.pages.ArtistPage;
import app.user.Statistics.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.tree.tiny.Statistics;

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
    private List<AlbumInfo> albumsInfos;
    @Getter
    @Setter
    private List<SongInfo> songsInfos;
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
    private boolean hasAccessedData;

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
        hasAccessedData = false;
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

    public void addMerchRevenue(final Double price) {
        merchRevenue += price;
    }
    public void arrangeStatistics(Artist artist) {
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


        List<String> topFans = statistics.getTopFans();
        List<User> fansUsers = new ArrayList<>();
        for (String fan : topFans) {
            User user = Admin.getInstance().getUser(fan);
            fansUsers.add(user);
        }
        List<FanInfo> fanInfos = new ArrayList<>();
        for (User user : fansUsers) {
            List<ArtistInfo> artistInfos = user.getArtistsInfos();
            for (ArtistInfo artistInfo : artistInfos) {
                if(artistInfo.getName().equals(artist.getUsername())) {
                    fanInfos.add(new FanInfo(user.getUsername(), artistInfo.getNoListen()));
                }
            }
        }
        List<String> fans = fanInfos.stream()
                .sorted(Comparator
                        .comparingInt(FanInfo::getNoListen)
                        .reversed()
                        .thenComparing(FanInfo::getName))
                .map(FanInfo::getName)
                .limit(5)
                .collect(Collectors.toList());
        statistics.setTopFans(fans);

    }

    public ObjectNode formattedStatisticsArtist() {
        // Convertirea obiectului WrappedHost într-un obiect JSON
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();

        WrappedArtist wrappedArtist = getStatistics();

        ObjectNode topAlbumsNode = resultNode.putObject("topAlbums");
        // Adăugarea informațiilor despre albumele de top
        for (AlbumInfo album : wrappedArtist.getTopAlbums()) {
            hasAccessedData = true;
            topAlbumsNode.put(album.getName(), album.getNoListen());
        }

        ObjectNode topSongsNode = resultNode.putObject("topSongs");
        // Adăugarea informațiilor despre cantecele de top
        for (SongInfo song : wrappedArtist.getTopSongs()) {
            hasAccessedData = true;
            topSongsNode.put(song.getName(), song.getNoListen());
        }

        ArrayNode topFansNode = resultNode.putArray("topFans");
        // Adăugarea informațiilor despre cantecele de top
        for(String name : wrappedArtist.getTopFans()) {
            hasAccessedData = true;
            topFansNode.add(name);
        }

        resultNode.put("listeners", wrappedArtist.getListeners());
        return resultNode;
    }

}
