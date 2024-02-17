package xyz.chide1.buildprotection.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class AllowedBlock {
    private List<Material> ownerMaterial;
    private List<Material> generalMaterial;
}
