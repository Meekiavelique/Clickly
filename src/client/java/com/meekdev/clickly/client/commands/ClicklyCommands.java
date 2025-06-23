package com.meekdev.clickly.client.commands;

import com.meekdev.clickly.client.render.ClicklyWorldRenderer;
import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.models.Waypoint;
import com.meekdev.clickly.core.services.GroupService;
import com.meekdev.clickly.core.services.NetworkService;
import com.meekdev.clickly.core.services.UIService;
import com.meekdev.clickly.core.models.LocationPing;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class ClicklyCommands {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("clickly")
                .then(ClientCommandManager.literal("create")
                        .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String groupName = StringArgumentType.getString(context, "name");
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc.player == null) return 0;

                                    GroupService groupService = ServiceManager.getInstance().require(GroupService.class);
                                    UUID playerId = mc.player.getUuid();

                                    groupService.createGroup(groupName, playerId).thenAccept(group -> {
                                        mc.execute(() -> {
                                            context.getSource().sendFeedback(
                                                    Text.literal("Created group: " + group.getName())
                                                            .formatted(Formatting.GREEN)
                                                            .then(ClientCommandManager.literal("waypoint")
                                                                    .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                                                            .executes(context -> {
                                                                                String waypointName = StringArgumentType.getString(context, "name");

                                                                                if (mc.player == null || mc.world == null) return 0;

                                                                                int x = (int) mc.player.getX();
                                                                                int y = (int) mc.player.getY();
                                                                                int z = (int) mc.player.getZ();
                                                                                String dimension = mc.world.getRegistryKey().getValue().toString();

                                                                                Waypoint waypoint = new Waypoint(
                                                                                        UUID.randomUUID().toString(),
                                                                                        waypointName,
                                                                                        x, y, z,
                                                                                        dimension,
                                                                                        0xFFFF9800,
                                                                                        mc.player.getUuid(),
                                                                                        "",
                                                                                        System.currentTimeMillis(),
                                                                                        false
                                                                                );

                                                                                ClicklyWorldRenderer.addWaypoint(waypoint);

                                                                                context.getSource().sendFeedback(
                                                                                        Text.literal("Waypoint '" + waypointName + "' created at " + x + ", " + y + ", " + z)
                                                                                                .formatted(Formatting.GREEN)
                                                                                );

                                                                                return 1;
                                                                            })
                                                                    )
                                                            );
                                        });
                                    }).exceptionally(throwable -> {
                                        mc.execute(() -> {
                                            context.getSource().sendError(
                                                    Text.literal("Failed to create group: " + throwable.getMessage())
                                                            .formatted(Formatting.RED)
                                            );
                                        });
                                        return null;
                                    });

                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("join")
                        .then(ClientCommandManager.argument("code", StringArgumentType.string())
                                .executes(context -> {
                                    String inviteCode = StringArgumentType.getString(context, "code");
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc.player == null) return 0;

                                    GroupService groupService = ServiceManager.getInstance().require(GroupService.class);
                                    UUID playerId = mc.player.getUuid();
                                    String username = mc.player.getGameProfile().getName();

                                    groupService.joinGroup(inviteCode, playerId, username).thenAccept(success -> {
                                        mc.execute(() -> {
                                            if (success) {
                                                context.getSource().sendFeedback(
                                                        Text.literal("Successfully joined group!")
                                                                .formatted(Formatting.GREEN)
                                                );
                                            } else {
                                                context.getSource().sendError(
                                                        Text.literal("Failed to join group. Invalid code or group is full.")
                                                                .formatted(Formatting.RED)
                                                );
                                            }
                                        });
                                    });

                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("leave")
                        .then(ClientCommandManager.argument("groupId", StringArgumentType.string())
                                .executes(context -> {
                                    String groupId = StringArgumentType.getString(context, "groupId");
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc.player == null) return 0;

                                    GroupService groupService = ServiceManager.getInstance().require(GroupService.class);
                                    UUID playerId = mc.player.getUuid();

                                    groupService.leaveGroup(groupId, playerId).thenAccept(success -> {
                                        mc.execute(() -> {
                                            if (success) {
                                                context.getSource().sendFeedback(
                                                        Text.literal("Left group successfully")
                                                                .formatted(Formatting.YELLOW)
                                                );
                                            } else {
                                                context.getSource().sendError(
                                                        Text.literal("Failed to leave group")
                                                                .formatted(Formatting.RED)
                                                );
                                            }
                                        });
                                    });

                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("ping")
                        .executes(context -> {
                            MinecraftClient mc = MinecraftClient.getInstance();
                            if (mc.player == null || mc.world == null) return 0;

                            int x = (int) mc.player.getX();
                            int y = (int) mc.player.getY();
                            int z = (int) mc.player.getZ();

                            return sendLocationPing((FabricClientCommandSource) context, x, y, z);
                        })
                        .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                                .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                                        .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    int x = IntegerArgumentType.getInteger(context, "x");
                                                    int y = IntegerArgumentType.getInteger(context, "y");
                                                    int z = IntegerArgumentType.getInteger(context, "z");

                                                    return sendLocationPing((FabricClientCommandSource) context, x, y, z);
                                                })
                                        )
                                )
                        )
                )
                .then(ClientCommandManager.literal("chat")
                        .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String message = StringArgumentType.getString(context, "message");
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc.player == null) return 0;

                                    NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);

                                    if (!networkService.isConnected()) {
                                        context.getSource().sendError(
                                                Text.literal("Not connected to Clickly server")
                                                        .formatted(Formatting.RED)
                                        );
                                        return 0;
                                    }

                                    // For now, send to first group - this should be improved
                                    GroupService groupService = ServiceManager.getInstance().require(GroupService.class);
                                    var groups = groupService.getCachedPlayerGroups(mc.player.getUuid());

                                    if (groups.isEmpty()) {
                                        context.getSource().sendError(
                                                Text.literal("You are not in any groups")
                                                        .formatted(Formatting.RED)
                                        );
                                        return 0;
                                    }

                                    String groupId = groups.get(0).getId();
                                    networkService.sendChatMessage(groupId, message);

                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("toggle")
                        .executes(context -> {
                            UIService uiService = ServiceManager.getInstance().require(UIService.class);
                            boolean currentState = uiService.isOverlayVisible();
                            uiService.setOverlayVisible(!currentState);

                            context.getSource().sendFeedback(
                                    Text.literal("Overlay " + (currentState ? "hidden" : "shown"))
                                            .formatted(Formatting.YELLOW)
                            );

                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("groups")
                        .executes(context -> {
                            MinecraftClient mc = MinecraftClient.getInstance();
                            if (mc.player == null) return 0;

                            GroupService groupService = ServiceManager.getInstance().require(GroupService.class);
                            var groups = groupService.getCachedPlayerGroups(mc.player.getUuid());

                            if (groups.isEmpty()) {
                                context.getSource().sendFeedback(
                                        Text.literal("You are not in any groups")
                                                .formatted(Formatting.YELLOW)
                                );
                            } else {
                                context.getSource().sendFeedback(
                                        Text.literal("Your groups:")
                                                .formatted(Formatting.GOLD)
                                );

                                for (var group : groups) {
                                    context.getSource().sendFeedback(
                                            Text.literal("- " + group.getName() + " (" + group.getMemberCount() + " members)")
                                                    .formatted(Formatting.WHITE)
                                    );
                                }
                            }

                            return 1;
                        })
                )
        );
    }

    private static int sendLocationPing(FabricClientCommandSource source, int x, int y, int z) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return 0;

        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);

        if (!networkService.isConnected()) {
            source.sendError(
                    Text.literal("Not connected to Clickly server")
                            .formatted(Formatting.RED)
            );
            return 0;
        }

        GroupService groupService = ServiceManager.getInstance().require(GroupService.class);
        var groups = groupService.getCachedPlayerGroups(mc.player.getUuid());

        if (groups.isEmpty()) {
            source.sendError(
                    Text.literal("You are not in any groups")
                            .formatted(Formatting.RED)
            );
            return 0;
        }

        String groupId = groups.get(0).getId();
        String dimension = mc.world.getRegistryKey().getValue().toString();
        String server = getCurrentServerName();

        LocationPing ping = new LocationPing(
                mc.player.getUuid(),
                mc.player.getGameProfile().getName(),
                x, y, z,
                dimension,
                server,
                System.currentTimeMillis(),
                groupId
        );

        networkService.sendLocationPing(ping);

        source.sendFeedback(
                Text.literal("Location ping sent: " + x + ", " + y + ", " + z)
                        .formatted(Formatting.GREEN)
        );

        return 1;
    }

    private static String getCurrentServerName() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getCurrentServerEntry() != null) {
            return mc.getCurrentServerEntry().address;
        } else if (mc.isIntegratedServerRunning()) {
            return "Singleplayer";
        } else {
            return "Unknown";
        }
    }
}