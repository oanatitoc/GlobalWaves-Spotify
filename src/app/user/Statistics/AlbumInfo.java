package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AlbumInfo {
    @Setter
    String name;
    @Setter
    int noListen;
    public AlbumInfo(String name, int noListen) {
        this.name = name;
        this.noListen = noListen;
    }
}
