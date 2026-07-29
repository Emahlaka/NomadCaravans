package com.sappyeddie.nomadcaravans.client.render;

import net.minecraft.util.Mth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TusselPhysics {
    private static final Map<UUID, TusselPhysics> STATES = new ConcurrentHashMap<>();

    private float swingX, swingZ, prevSwingX, prevSwingZ;
    private float velX, velZ;
    private float prevYaw, prevPitch;
    private long lastTick = -1;
    private boolean initialized = false;

    public static TusselPhysics get(UUID wearerId) {
        return STATES.computeIfAbsent(wearerId, id -> new TusselPhysics());
    }

    public void tickIfNeeded(long gameTime, float yaw, float pitch) {
        if (gameTime == lastTick)
            return;

        lastTick = gameTime;
        prevSwingX = swingX;
        prevSwingZ = swingZ;

        if (!initialized) {
            prevYaw = yaw;
            prevPitch = pitch;
            initialized = true;
            return;
        }

        float targetX = Mth.clamp((pitch - prevPitch) * 1.2f, -15f, 15f);
        float targetZ = Mth.clamp(Mth.wrapDegrees(yaw - prevYaw) * 1.2f, -15f, 15f);

        velX = (velX + (targetX - swingX) * 0.35f) * 0.7f;
        velZ = (velZ + (targetZ - swingZ) * 0.35f) * 0.7f;
        swingX += velX;
        swingZ += velZ;

        prevYaw = yaw;
        prevPitch = pitch;
    }

    public float getSwingX(float partialTick) { return Mth.lerp(partialTick, prevSwingX, swingX); }
    public float getSwingZ(float partialTick) { return Mth.lerp(partialTick, prevSwingZ, swingZ); }
}
