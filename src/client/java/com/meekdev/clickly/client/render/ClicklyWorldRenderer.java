package com.meekdev.clickly.client.render;

import com.meekdev.clickly.core.models.LocationPing;
import com.meekdev.clickly.core.models.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClicklyWorldRenderer {
    private static final CopyOnWriteArrayList<LocationPing> activePings = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Waypoint> waypoints = new CopyOnWriteArrayList<>();

    private static final long PING_DURATION = 30000;
    private static final float MAX_RENDER_DISTANCE = 1000.0f;

    public static void addLocationPing(LocationPing ping) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        String currentDimension = mc.world.getRegistryKey().getValue().toString();
        if (!currentDimension.equals(ping.getDimension())) return;

        activePings.add(ping);
    }

    public static void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
    }

    public static void removeWaypoint(String waypointId) {
        waypoints.removeIf(w -> w.getId().equals(waypointId));
    }

    public static void render(MatrixStack matrices, Camera camera, float tickDelta) {
        cleanupExpiredPings();

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        Vec3d cameraPos = camera.getPos();
        String currentDimension = mc.world.getRegistryKey().getValue().toString();

        try {
            for (LocationPing ping : activePings) {
                if (ping.getDimension().equals(currentDimension)) {
                    renderLocationPing(matrices, ping, cameraPos, tickDelta);
                }
            }

            for (Waypoint waypoint : waypoints) {
                if (waypoint.getDimension().equals(currentDimension)) {
                    renderWaypoint(matrices, waypoint, cameraPos, tickDelta);
                }
            }
        } catch (Exception e) {

        }
    }

    private static void renderLocationPing(MatrixStack matrices, LocationPing ping, Vec3d cameraPos, float tickDelta) {
        double distance = cameraPos.distanceTo(new Vec3d(ping.getX(), ping.getY(), ping.getZ()));
        if (distance > MAX_RENDER_DISTANCE) return;

        long elapsed = System.currentTimeMillis() - ping.getTimestamp();
        float alpha = Math.max(0.0f, 1.0f - (float) elapsed / PING_DURATION);

        matrices.push();
        matrices.translate(
                ping.getX() - cameraPos.x,
                ping.getY() - cameraPos.y,
                ping.getZ() - cameraPos.z
        );

        renderPingBeam(matrices, alpha, elapsed);

        matrices.pop();
    }

    private static void renderWaypoint(MatrixStack matrices, Waypoint waypoint, Vec3d cameraPos, float tickDelta) {
        double distance = cameraPos.distanceTo(new Vec3d(waypoint.getX(), waypoint.getY(), waypoint.getZ()));
        if (distance > MAX_RENDER_DISTANCE) return;

        matrices.push();
        matrices.translate(
                waypoint.getX() - cameraPos.x,
                waypoint.getY() - cameraPos.y,
                waypoint.getZ() - cameraPos.z
        );

        renderWaypointMarker(matrices, waypoint.getColor(), tickDelta);

        matrices.pop();
    }

    private static void renderPingBeam(MatrixStack matrices, float alpha, long elapsed) {
        try {
            float time = elapsed / 1000.0f;
            float pulseAlpha = alpha * (0.6f + 0.4f * MathHelper.sin(time * 8.0f));

            for (int ring = 0; ring < 3; ring++) {
                float ringTime = (time + ring * 0.5f) % 2.0f / 2.0f;
                float ringRadius = 1.0f + ringTime * 6.0f;
                float ringAlpha = alpha * (1.0f - ringTime) * 0.4f;

                if (ringAlpha <= 0) continue;

                matrices.push();
                matrices.scale(ringRadius, 1.0f, ringRadius);
                matrices.pop();
            }
        } catch (Exception e) {

        }
    }

    private static void renderWaypointMarker(MatrixStack matrices, int color, float tickDelta) {
        try {
            float time = (System.currentTimeMillis() % 4000) / 4000.0f;
            float bobOffset = MathHelper.sin(time * (float) Math.PI * 2) * 0.2f;

            matrices.push();
            matrices.translate(0, 2.0f + bobOffset, 0);
            matrices.scale(0.8f, 0.8f, 0.8f);
            matrices.pop();
        } catch (Exception e) {

        }
    }

    private static void cleanupExpiredPings() {
        Iterator<LocationPing> iterator = activePings.iterator();
        long currentTime = System.currentTimeMillis();

        while (iterator.hasNext()) {
            LocationPing ping = iterator.next();
            if (currentTime - ping.getTimestamp() > PING_DURATION) {
                iterator.remove();
            }
        }
    }

    public static void clearAllPings() {
        activePings.clear();
    }

    public static void clearAllWaypoints() {
        waypoints.clear();
    }
}