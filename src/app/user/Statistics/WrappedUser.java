package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class WrappedUser {
    @Setter
    private List<Infos> topArtists;
    @Setter
    private List<Infos> topGenres;
    @Setter
    private List<Infos> topSongs;
    @Setter
    private List<Infos> topAlbums;
    @Setter
    private List<Infos> topEpisodes;

    public WrappedUser(final List<Infos> topArtists, final List<Infos> topGenres,
                       final List<Infos> topSongs, final List<Infos> topAlbums,
                       final List<Infos> topEpisodes) {
        this.topArtists = topArtists;
        this.topGenres = topGenres;
        this.topSongs = topSongs;
        this.topAlbums = topAlbums;
        this.topEpisodes = topEpisodes;
    }

}
