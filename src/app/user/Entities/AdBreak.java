package app.user.Entities;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class AdBreak {
    @Setter
    public boolean AdBreakInQueue;
    @Setter
    private int timestamp;

    public AdBreak(final int timestamp) {
        this.AdBreakInQueue = true;
        this.timestamp = timestamp;
    }
}
