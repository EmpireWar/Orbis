package org.empirewar.orbis.bukkit.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.empirewar.orbis.bukkit.OrbisBukkit;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3d;

public record EntityDamageListener(OrbisBukkit orbis) implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(player.getWorld()));
        final Location location = player.getLocation();
        final Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());

        final RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(DefaultFlags.INVULNERABILITY);
        builder.player(player.getUniqueId());
        final boolean canAct = world.query(RegionQuery.Position.builder().position(pos))
                .query(builder)
                .result()
                .orElse(false);

        if (!canAct) {
            event.setCancelled(true);
        }
    }
}
