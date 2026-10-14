package com.example.selectionclient.selection;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.AABB;

/** Uses the game's gizmo collector so highlights work with both rendering backends. */
public final class SelectionRenderer {
    private SelectionRenderer() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SelectionManager manager = SelectionManager.getInstance();
            if (client.level == null || !manager.isComplete()) return;
            var min = manager.getMin();
            var max = manager.getMax();
            try (var ignored = client.collectPerTickGizmos()) {
                Gizmos.cuboid(new AABB(min.getX(), min.getY(), min.getZ(),
                        max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0),
                        GizmoStyle.strokeAndFill(0xFF33CCFF, 2.0f, 0x403399FF));
            }
        });
    }
}
