/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.nms.v1_13_R2;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import me.filoghost.fcommons.logging.ErrorCollector;
import me.filoghost.fcommons.logging.Log;
import me.filoghost.fcommons.reflection.ReflectField;
import me.filoghost.holographicdisplays.common.nms.EntityID;
import me.filoghost.holographicdisplays.common.nms.FallbackEntityIDGenerator;
import me.filoghost.holographicdisplays.common.nms.NMSErrors;
import me.filoghost.holographicdisplays.common.nms.NMSManager;
import me.filoghost.holographicdisplays.common.nms.NMSPacketList;
import me.filoghost.holographicdisplays.common.nms.PacketListener;
import me.filoghost.holographicdisplays.nms.v1_13_R2.protocollib.WrapperPlayServerSpawnEntity;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.NetworkManager;
import net.minecraft.server.v1_13_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_13_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_13_R2.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_DESTROY;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_METADATA;
import static com.comphenix.protocol.PacketType.Play.Server.MAP_CHUNK;
import static com.comphenix.protocol.PacketType.Play.Server.POSITION;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY;
import static com.comphenix.protocol.PacketType.Play.Server.UNLOAD_CHUNK;

public class VersionNMSManager implements NMSManager {

    private static final ReflectField<Integer> ENTITY_ID_COUNTER_FIELD = ReflectField.lookup(int.class, Entity.class, "entityCount");
    private static Supplier<Integer> fallbackEntityIDGenerator;
    private static Supplier<Integer> entityIDGenerator;
    private static Plugin holographicDisplays;

    private static AtomicLong ticksCount = new AtomicLong();

    public VersionNMSManager(ErrorCollector errorCollector) {
        this.fallbackEntityIDGenerator = new FallbackEntityIDGenerator();
        this.entityIDGenerator = getEntityIDGenerator(errorCollector);
        holographicDisplays = Bukkit.getPluginManager().getPlugin("HolographicDisplays");

        // Force initialization of class to eventually throw exceptions early
        DataWatcherKey.ENTITY_STATUS.getIndex();

        Bukkit.getScheduler().runTaskTimer(holographicDisplays, () -> {
            ticksCount.incrementAndGet();
        }, 1, 1);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                holographicDisplays,
                ListenerPriority.NORMAL,
                Arrays.asList(
                        POSITION,
                        MAP_CHUNK,
                        UNLOAD_CHUNK,
                        SPAWN_ENTITY,
                        ENTITY_DESTROY,
                        ENTITY_METADATA
                ),
                ListenerOptions.ASYNC) {

            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                if (event.getPacketType() == MAP_CHUNK) {
                    int x = packet.getIntegers().read(0);
                    int z = packet.getIntegers().read(1);

                    if (x == 20 && z == 11) {
                        System.out.println(getPrefix() + "Sending map chunk packet: x=" + x + ", z=" + z + ", full=" + packet.getBooleans().read(0));

//                        event.setCancelled(true);
//
//                        Bukkit.getScheduler().runTaskLater(holographicDisplays, () -> {
//                            try {
//                                System.out.println(getPrefix() + "Now sending packet 20 tick later");
//                                ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet, false);
//                            } catch (InvocationTargetException e) {
//                                System.out.println("Cannot send packet");
//                                e.printStackTrace();
//                            }
//                        }, 20L);
                    }

                } else if (event.getPacketType() == UNLOAD_CHUNK) {
                    int x = packet.getIntegers().read(0);
                    int z = packet.getIntegers().read(1);
                    if (x == 20 && z == 11) {
                        System.out.println(getPrefix() + "Sending unload chunk packet: x=" + x + ", z=" + z);
                    }

                } else if (event.getPacketType() == SPAWN_ENTITY) {
                    WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(packet);
                    System.out.println(getPrefix() + "Sending entity spawn packet: id=" + wrapper.getEntityID() + ", type=" + wrapper.getType());

                } else if (event.getPacketType() == ENTITY_DESTROY) {
                    System.out.println(getPrefix() + "Sending entity destroy packet: ids=" + Arrays.toString(packet.getIntegerArrays().read(0)));

                } else if (event.getPacketType() == POSITION) {
                    System.out.println(getPrefix() + "Sending position packet: x=" + packet.getDoubles().read(0)
                            + ", y=" + packet.getDoubles().read(1)
                            + ", z=" + packet.getDoubles().read(2));
                } else {
                    System.out.println(getPrefix() + "Sending packet " + event.getPacketType().name() + ": id=" + packet.getIntegers().read(0));
                }
            }
        });
    }

    public static void sendChunkRefresh(Player sender) {
        PacketPlayOutUnloadChunk packet = new PacketPlayOutUnloadChunk(20, 11);
        PlayerConnection playerConnection = ((CraftPlayer) sender).getHandle().playerConnection;
        playerConnection.sendPacket(packet);
        sender.sendMessage("Unload");
        Bukkit.getScheduler().runTaskLater(holographicDisplays, () -> {
            PacketPlayOutMapChunk packet2 = new PacketPlayOutMapChunk(((CraftChunk) sender.getWorld().getChunkAt(20, 11)).getHandle(), 65535);
            playerConnection.sendPacket(packet2);
            sender.sendMessage("Load");
        }, 20L);
    }

    public static void sendChunkUnload(Player sender) {
        PacketPlayOutUnloadChunk packet = new PacketPlayOutUnloadChunk(20, 11);
        PlayerConnection playerConnection = ((CraftPlayer) sender).getHandle().playerConnection;
        playerConnection.sendPacket(packet);
    }

    public static void sendChunkLoad(Player sender) {
        PacketPlayOutMapChunk packet2 = new PacketPlayOutMapChunk(((CraftChunk) sender.getWorld().getChunkAt(20, 11)).getHandle(), 65535);
        PlayerConnection playerConnection = ((CraftPlayer) sender).getHandle().playerConnection;
        playerConnection.sendPacket(packet2);
    }

    private String getPrefix() {
        return "[" + Thread.currentThread().getName() + "] [tick " + ticksCount + "] ";
    }

    private Supplier<Integer> getEntityIDGenerator(ErrorCollector errorCollector) {
        try {
            testStaticFieldReadWrite(ENTITY_ID_COUNTER_FIELD);

            return () -> {
                try {
                    int nmsEntityIDCounter = ENTITY_ID_COUNTER_FIELD.getStatic();
                    ENTITY_ID_COUNTER_FIELD.setStatic(nmsEntityIDCounter + 1);
                    return nmsEntityIDCounter;
                } catch (ReflectiveOperationException e) {
                    // Should not happen, access is tested beforehand
                    return fallbackEntityIDGenerator.get();
                }
            };
        } catch (ReflectiveOperationException e) {
            errorCollector.add(e, NMSErrors.EXCEPTION_GETTING_ENTITY_ID_GENERATOR);
            return fallbackEntityIDGenerator;
        }
    }

    private <T> void testStaticFieldReadWrite(ReflectField<T> field) throws ReflectiveOperationException {
        T value = field.getStatic();
        field.setStatic(value);
    }

    @Override
    public EntityID newEntityID() {
        return new EntityID(entityIDGenerator);
    }

    @Override
    public NMSPacketList createPacketList() {
        return new VersionNMSPacketList();
    }

    @Override
    public void injectPacketListener(Player player, PacketListener packetListener) {
        modifyPipeline(player, (ChannelPipeline pipeline) -> {
            ChannelHandler currentListener = pipeline.get(InboundPacketHandler.HANDLER_NAME);
            if (currentListener != null) {
                pipeline.remove(InboundPacketHandler.HANDLER_NAME);
            }
            pipeline.addBefore("packet_handler", InboundPacketHandler.HANDLER_NAME, new InboundPacketHandler(player, packetListener));
        });
    }

    @Override
    public void uninjectPacketListener(Player player) {
        modifyPipeline(player, (ChannelPipeline pipeline) -> {
            ChannelHandler currentListener = pipeline.get(InboundPacketHandler.HANDLER_NAME);
            if (currentListener != null) {
                pipeline.remove(InboundPacketHandler.HANDLER_NAME);
            }
        });
    }

    /*
     * Modifying the pipeline in the main thread can cause deadlocks, delays and other concurrency issues,
     * which can be avoided by using the event loop. Thanks to ProtocolLib for this insight.
     */
    private void modifyPipeline(Player player, Consumer<ChannelPipeline> pipelineModifierTask) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        NetworkManager networkManager = playerConnection.a();
        Channel channel = networkManager.channel;

        channel.eventLoop().execute(() -> {
            try {
                pipelineModifierTask.accept(channel.pipeline());
            } catch (Exception e) {
                Log.warning(NMSErrors.EXCEPTION_MODIFYING_CHANNEL_PIPELINE, e);
            }
        });
    }

}
