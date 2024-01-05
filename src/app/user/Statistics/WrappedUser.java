package app.user.Statistics;

import app.audio.Files.Song;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class WrappedUser {
    @Setter
    public List<ArtistInfo> topArtists;
    @Setter
    public List<GenreInfo> topGenres;
    @Setter
    public List<SongInfo> topSongs;
    @Setter
    public List<AlbumInfo> topAlbums;
    @Setter
    public List<EpisodeInfo> topEpisodes;

    public WrappedUser(List<ArtistInfo> topArtists, List<GenreInfo> topGenres,
                       List<SongInfo> topSongs, List<AlbumInfo> topAlbums,
                       List<EpisodeInfo> topEpisodes) {
        this.topArtists = topArtists;
        this.topGenres = topGenres;
        this.topSongs = topSongs;
        this.topAlbums = topAlbums;
        this.topEpisodes = topEpisodes;
    }

}
