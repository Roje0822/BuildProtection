package xyz.chide1.buildprotection.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class ProtectionRegion {
    private UUID builder;
    private List<UUID> owners;
    private Location minLocation;
    private Location maxLocation;
    private Location head;
    private RegionSize size;
    private UUID uuid;
    private World world;
    private UUID entityUUID;
}
