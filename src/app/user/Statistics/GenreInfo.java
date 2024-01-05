package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GenreInfo {
    @Setter
    String name;
    @Setter
    int noListen;

    public GenreInfo(String name, int noListen) {
        this.name = name;
        this.noListen = noListen;
    }
}
