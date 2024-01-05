package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class WrappedArtist {
    @Setter
    public List<AlbumInfo> topAlbums;
    @Setter
    public List<SongInfo> topSongs;
    @Setter
    public List<String> topFans;
    @Setter
    public int listeners;

    public WrappedArtist(List<AlbumInfo> topAlbums, List<SongInfo> topSongs, List<String> topFans, int listeners) {
        this.topAlbums = topAlbums;
        this.topSongs = topSongs;
        this.topFans = topFans;
        this.listeners = listeners;
    }
}
