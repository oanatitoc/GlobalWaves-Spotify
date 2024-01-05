package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

public class EpisodeInfo {
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    int noListen;
    public EpisodeInfo(String name) {
        this.name = name;
        noListen = 1;
    }
}
