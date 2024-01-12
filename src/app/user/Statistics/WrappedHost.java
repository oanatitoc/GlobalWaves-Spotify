package app.user.Statistics;

import app.user.Statistics.Infos.Infos;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class WrappedHost {
    @Setter
    private List<Infos> topEpisodes;
    @Setter
    private int listeners;
    public WrappedHost(final List<Infos> topEpisodes, final int listeners) {
        this.topEpisodes = topEpisodes;
        this.listeners = listeners;
    }
}
