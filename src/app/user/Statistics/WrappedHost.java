package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class WrappedHost {
    @Setter
    public List<EpisodeInfo> topEpisodes;
    @Setter
    public int listeners;
    public WrappedHost(List<EpisodeInfo> topEpisodes, int listeners) {
        this.topEpisodes = topEpisodes;
        this.listeners = listeners;
    }
}
