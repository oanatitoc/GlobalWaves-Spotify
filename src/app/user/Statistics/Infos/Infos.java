package app.user.Statistics.Infos;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Infos {
    @Setter
    private String name;
    @Setter
    private int noListen;
    public Infos(final String name, final int noListen) {
        this.name = name;
        this.noListen = noListen;
    }
}
