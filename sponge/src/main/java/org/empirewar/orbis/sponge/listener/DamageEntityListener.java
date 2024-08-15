package org.empirewar.orbis.sponge.listener;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.vehicle.Vehicle;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;

public class DamageEntityListener {

    private final Orbis orbis;

    public DamageEntityListener(Orbis orbis) {
        this.orbis = orbis;
    }

    @Listener(order = Order.EARLY)
    public void onDamage(DamageEntityEvent event) {
        final Entity attacked = event.entity();
        final RegionisedWorld world =
                orbis.getRegionisedWorld(attacked.serverLocation().world().key());
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> query =
                world.query(RegionQuery.Position.builder()
                        .position(
                                attacked.position().x(),
                                attacked.position().y(),
                                attacked.position().z()));

        if (!(attacked instanceof Living)) return;

        if (!query.query(RegionQuery.Flag.builder(DefaultFlags.INVULNERABILITY))
                .result()
                .orElse(true)) {
            event.setCancelled(true);
        }
    }
}
