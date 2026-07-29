package com.sappyeddie.nomadcaravans.client;

import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.WanderingTraderYakEntity;
import com.sappyeddie.nomadcaravans.entity.WildYakEntity;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class NomadDynamicLights {

    private static final int LUMINANCE = 14;
    private static final double LANTERN_HEIGHT = 1.2;

    private static final Map<Integer, YakLantern> BEHAVIORS = new HashMap<>();
    private static DynamicLightBehaviorManager manager;
    private static ClientLevel lastLevel;
    private static boolean unavailable;

    private NomadDynamicLights() {
    }

    public static boolean carriesLantern(Entity entity) {
        return entity instanceof WildYakEntity || entity instanceof WanderingTraderYakEntity;
    }

    public static void tick(Minecraft client) {
        if (unavailable) {
            return;
        }

        if (client.level != lastLevel) {
            lastLevel = client.level;
            manager = null;
            BEHAVIORS.clear();
        }
        if (client.level == null) {
            return;
        }
        if (manager == null) {
            manager = resolveManager();
            if (manager == null) {
                return;
            }
        }

        for (YakLantern behavior : BEHAVIORS.values()) {
            behavior.seen = false;
        }

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!carriesLantern(entity)) {
                continue;
            }
            YakLantern behavior = BEHAVIORS.get(entity.getId());
            if (behavior == null) {
                behavior = new YakLantern();
                BEHAVIORS.put(entity.getId(), behavior);
                manager.add(behavior);
            }
            behavior.update(entity);
            behavior.seen = true;
        }

        Iterator<Map.Entry<Integer, YakLantern>> it = BEHAVIORS.entrySet().iterator();
        while (it.hasNext()) {
            YakLantern behavior = it.next().getValue();
            if (!behavior.seen) {
                behavior.markRemoved();
                it.remove();
            }
        }
    }

    private static DynamicLightBehaviorManager resolveManager() {
        try {
            Class<?> ldl = Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
            Object instance = ldl.getMethod("get").invoke(null);
            return (DynamicLightBehaviorManager) ldl.getMethod("dynamicLightBehaviorManager").invoke(instance);
        } catch (ClassNotFoundException e) {
            unavailable = true;
            return null;
        } catch (ReflectiveOperationException | ClassCastException e) {
            unavailable = true;
            NomadCaravans.LOGGER.warn("LambDynamicLights is present but its behavior API did not resolve; "
                    + "caravan lanterns will not emit dynamic light.", e);
            return null;
        }
    }

    private static final class YakLantern implements DynamicLightBehavior {

        private double x;
        private double y;
        private double z;
        private double prevX = Double.NaN;
        private double prevY = Double.NaN;
        private double prevZ = Double.NaN;
        private int luminance;
        private int prevLuminance = -1;
        private boolean removed;
        private boolean seen;

        void update(Entity entity) {
            this.x = entity.getX();
            this.y = entity.getY() + LANTERN_HEIGHT;
            this.z = entity.getZ();
            this.luminance = LUMINANCE;
        }

        void markRemoved() {
            this.removed = true;
            this.luminance = 0;
        }

        @Override
        public double lightAtPos(BlockPos pos, double falloffRatio) {
            if (this.luminance <= 0 || this.removed) {
                return 0.0;
            }
            double dx = pos.getX() + 0.5 - this.x;
            double dy = pos.getY() + 0.5 - this.y;
            double dz = pos.getZ() + 0.5 - this.z;
            return Math.max(this.luminance - Math.sqrt(dx * dx + dy * dy + dz * dz) * falloffRatio, 0.0);
        }

        @Override
        public BoundingBox getBoundingBox() {
            int bx = (int) Math.floor(this.x);
            int by = (int) Math.floor(this.y);
            int bz = (int) Math.floor(this.z);
            return new BoundingBox(bx, by, bz, bx + 1, by + 1, bz + 1);
        }

        @Override
        public boolean hasChanged() {
            boolean changed = this.x != this.prevX || this.y != this.prevY || this.z != this.prevZ
                    || this.luminance != this.prevLuminance;
            if (changed) {
                this.prevX = this.x;
                this.prevY = this.y;
                this.prevZ = this.z;
                this.prevLuminance = this.luminance;
            }
            return changed;
        }

        @Override
        public boolean isRemoved() {
            return this.removed;
        }
    }
}
