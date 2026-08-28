package io.github.evnrca.dungeonrooms;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Entity;

/**
 * Thin wrapper around the direct MythicMobs API.
 * <p>
 * All MythicMobs imports are confined to this class. It is injected into other
 * classes via constructor, never accessed statically.
 *
 * @author evnrca
 */
public final class MythicMobsHook {

    private final MythicBukkit mythic;

    public MythicMobsHook() {
        this.mythic = MythicBukkit.inst();
    }

    /**
     * @return {@code true} if the entity was spawned by MythicMobs
     */
    public boolean isMythicMob(Entity entity) {
        return mythic.getMobManager().isActiveMob(entity.getUniqueId());
    }

    /**
     * @return the configured display name of the MythicMob, or {@code null} if unknown
     */
    public String getMythicMobName(Entity entity) {
        var mob = mythic.getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mob == null) {
            return null;
        }
        String display = mob.getDisplayName();
        return display == null ? mob.getType().getInternalName() : display;
    }
}