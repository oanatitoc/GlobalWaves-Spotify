package app.user.Statistics;

import app.user.Statistics.Infos.Infos;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class WrappedArtist {
    @Setter
    private List<Infos> topAlbums;
    @Setter
    private List<Infos> topSongs;
    @Setter
    private List<String> topFans;
    @Setter
    private int listeners;

    public WrappedArtist(final List<Infos> topAlbums, final List<Infos> topSongs,
                         final List<String> topFans, final int listeners) {
        this.topAlbums = topAlbums;
        this.topSongs = topSongs;
        this.topFans = topFans;
        this.listeners = listeners;
    }
}
