package app.user.Statistics;

import lombok.Getter;
import lombok.Setter;

public class FanInfo {
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    int noListen;
    public FanInfo(String name, int noListen) {
        this.name = name;
        this.noListen = noListen;
    }
}
