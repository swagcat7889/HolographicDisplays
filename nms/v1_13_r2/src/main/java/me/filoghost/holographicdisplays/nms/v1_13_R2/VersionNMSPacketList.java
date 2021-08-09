/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.nms.v1_13_R2;

import me.filoghost.holographicdisplays.common.nms.AbstractNMSPacketList;
import me.filoghost.holographicdisplays.common.nms.EntityID;
import me.filoghost.holographicdisplays.common.nms.IndividualCustomName;
import me.filoghost.holographicdisplays.common.nms.IndividualNMSPacket;
import me.filoghost.holographicdisplays.nms.v1_13_R2.protocollib.MetadataHelper;
import me.filoghost.holographicdisplays.nms.v1_13_R2.protocollib.PacketHelper;
import org.bukkit.inventory.ItemStack;

class VersionNMSPacketList extends AbstractNMSPacketList {

    private static final PacketHelper PACKET_HELPER = new PacketHelper(new MetadataHelper());

    @Override
    public void addArmorStandSpawnPackets(EntityID entityID, double positionX, double positionY, double positionZ) {
        add(player ->
                PACKET_HELPER.sendSpawnArmorStandPacket(player, entityID, positionX, positionY, positionZ, null));
    }

    @Override
    public void addArmorStandSpawnPackets(EntityID entityID, double positionX, double positionY, double positionZ, String customName) {
        add(player ->
                PACKET_HELPER.sendSpawnArmorStandPacket(player, entityID, positionX, positionY, positionZ, customName));
    }

    @Override
    public void addArmorStandSpawnPackets(EntityID entityID, double positionX, double positionY, double positionZ, IndividualCustomName individualCustomName) {
        add(new IndividualNMSPacket(player ->
                player1 -> PACKET_HELPER.sendSpawnArmorStandPacket(
                        player1,
                        entityID,
                        positionX,
                        positionY,
                        positionZ,
                        individualCustomName.get(player1))));
    }

    @Override
    public void addArmorStandNameChangePackets(EntityID entityID, String customName) {

    }

    @Override
    public void addArmorStandNameChangePackets(EntityID entityID, IndividualCustomName individualCustomName) {

    }

    @Override
    public void addItemSpawnPackets(EntityID entityID, double positionX, double positionY, double positionZ, ItemStack itemStack) {
        add(new EntitySpawnNMSPacket(entityID, EntityTypeID.ITEM, positionX, positionY, positionZ));
        add(EntityMetadataNMSPacket.builder(entityID)
                .setItemStack(itemStack)
                .build()
        );
    }

    @Override
    public void addItemStackChangePackets(EntityID entityID, ItemStack itemStack) {
        add(EntityMetadataNMSPacket.builder(entityID)
                .setItemStack(itemStack)
                .build()
        );
    }

    @Override
    public void addSlimeSpawnPackets(EntityID entityID, double positionX, double positionY, double positionZ) {
        add(EntityLivingSpawnNMSPacket.builder(entityID, EntityTypeID.SLIME, positionX, positionY, positionZ)
                .setInvisible()
                .setSlimeSmall() // Required for a correct client-side collision box
                .build()
        );
    }

    @Override
    public void addEntityDestroyPackets(EntityID... entityIDs) {
        for (EntityID entityID : entityIDs) {
            add(player -> PACKET_HELPER.sendDestroyEntityPacket(player, entityID));
        }
    }

    @Override
    public void addTeleportPackets(EntityID entityID, double positionX, double positionY, double positionZ) {
        add(new EntityTeleportNMSPacket(entityID, positionX, positionY, positionZ));
    }

    @Override
    public void addMountPackets(EntityID vehicleEntityID, EntityID passengerEntityID) {
        add(new EntityMountNMSPacket(vehicleEntityID, passengerEntityID));
    }

}
