package xyz.chide1.buildprotection.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConfigType {

    REGION("region"),
    WORLD("world"),
    WORLD_GUARD("worldGuard"),
    REGION_LIMIT("regionLimit"),
    REGION_LOCATION_LIMIT("regionLocationLimit");

    public final String key;
}
