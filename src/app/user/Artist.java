package app.user;

import java.util.ArrayList;
import java.util.List;

import app.audio.Collections.Album;
import app.audio.Collections.AlbumOutput;
import app.audio.Files.Song;
import app.pages.ArtistPage;

/**
 * The type Artist.
 */
public final class Artist extends ContentCreator {
    private ArrayList<Album> albums;
    private ArrayList<Merchandise> merch;
    private ArrayList<Event> events;
    private Double merchRevenue;
    private Double songRevenue;
    private int ranking;
    private String mostProfitableSong;
    private int noPlays;

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
        super.setPage(new ArtistPage(this));
    }

    /**
     * Gets albums.
     *
     * @return the albums
     */
    public ArrayList<Album> getAlbums() {
        return albums;
    }

    /**
     * Gets merch.
     *
     * @return the merch
     */
    public ArrayList<Merchandise> getMerch() {
        return merch;
    }

    /**
     * Gets events.
     *
     * @return the events
     */
    public ArrayList<Event> getEvents() {
        return events;
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
    public Double getMerchRevenue() {
        return merchRevenue;
    }

    public void setMerchRevenue(final Double merchRevenue) {
        this.merchRevenue = merchRevenue;
    }

    public Double getSongRevenue() {
        return songRevenue;
    }

    public void setSongRevenue(final Double songRevenue) {
        this.songRevenue = songRevenue;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(final int ranking) {
        this.ranking = ranking;
    }

    public String getMostProfitableSong() {
        return mostProfitableSong;
    }

    public void setMostProfitableSong(final String mostProfitableSong) {
        this.mostProfitableSong = mostProfitableSong;
    }
    public void addMerchRevenue(final Double price) {
        merchRevenue += price;
    }
    public void addSongRevenue(final Double price) {
        songRevenue += price;
    }

    public int getNoPlays() {
        return noPlays;
    }

    public void setNoPlays(final int noPlays) {
        this.noPlays = noPlays;
    }
}
