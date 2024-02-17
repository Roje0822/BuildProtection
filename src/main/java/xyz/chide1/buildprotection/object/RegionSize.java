package xyz.chide1.buildprotection.object;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum RegionSize {
    BIG("대형"),
    NORMAL("중형"),
    SMALL("소형");

    @Getter
    private String key;
}
