package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

public class ArtistInfo {
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    int noListen;
    public ArtistInfo(String name, int noListen) {
        this.name = name;
       this.noListen = noListen;
    }
}
