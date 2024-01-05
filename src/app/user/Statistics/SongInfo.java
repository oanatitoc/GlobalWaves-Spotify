package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

public class SongInfo {
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    int noListen;
    public SongInfo(String name, int noListen) {
        this.name = name;
        this.noListen = noListen;
    }
}
