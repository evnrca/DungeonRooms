package io.github.evnrca.dungeonrooms;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.Collections;
import java.util.Set;

/**
 * Thin wrapper around the direct WorldGuard API.
 * <p>
 * All WorldGuard imports are confined to this class. It is injected into other
 * classes via constructor, never accessed statically.
 *
 * @author evnrca
 */
public final class WorldGuardHook {

    private final RegionContainer container;
    private final RegionQuery query;

    public WorldGuardHook() {
        this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        this.query = container.createQuery();
    }

    /**
     * @return the region with the given id in the world, or {@code null}
     */
    public ProtectedRegion getRegion(World world, String id) {
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        return manager == null ? null : manager.getRegion(id);
    }

    /**
     * @return all region ids registered in the world
     */
    public Set<String> getRegionNames(World world) {
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        return manager == null ? Collections.emptySet() : manager.getRegions().keySet();
    }

    /**
     * @return all regions containing the given location
     */
    public Iterable<ProtectedRegion> getRegionsAt(Location location) {
        com.sk89q.worldedit.util.Location weLoc =
                BukkitAdapter.adapt(location);
        return query.getApplicableRegions(weLoc);
    }

    /**
     * @return {@code true} if the location is inside the named region
     */
    public boolean isInRegion(Location location, String regionId) {
        if (location == null || location.getWorld() == null || regionId == null) {
            return false;
        }
        for (ProtectedRegion region : getRegionsAt(location)) {
            if (region.getId().equals(regionId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that a configured spawn region exists in its world.
     */
    public boolean isValidSpawnRegion(World world, String regionId) {
        return world != null && getRegion(world, regionId) != null;
    }

    /**
     * @return {@code true} if the location is inside the configured spawn region
     */
    public boolean isInsideSpawnRegion(Location location, String spawnRegionId) {
        return isInRegion(location, spawnRegionId);
    }

    /**
     * @return the min and max corners of a region as a {@link BoundingBox}, or {@code null}
     */
    public BoundingBox getRegionBoundingBox(World world, String regionId) {
        ProtectedRegion region = getRegion(world, regionId);
        if (region == null) {
            return null;
        }
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        return new BoundingBox(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
    }
}
