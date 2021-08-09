/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.filoghost.holographicdisplays.nms.v1_13_R2.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.primitives.Ints;

import java.util.List;

public class WrapperPlayServerMount extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MOUNT;

    public WrapperPlayServerMount() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerMount(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Retrieve the player entity ID being attached.
     *
     * @return The current Entity ID
     */
    public int getVehicleId() {
        return handle.getIntegers().read(0);
    }

    /**
     * Set the player entity ID being attached.
     *
     * @param value - new value.
     */
    public void setVehicleId(int value) {
        handle.getIntegers().write(0, value);
    }

    /**
     * Retrieve the IDs of the entities that will be destroyed.
     *
     * @return The current entities.
     */
    public List<Integer> getPassengers() {
        return Ints.asList(handle.getIntegerArrays().read(0));
    }

    /**
     * Set the entities that will be destroyed.
     *
     * @param value - new value.
     */
    public void setPassengers(int[] entities) {
        handle.getIntegerArrays().write(0, entities);
    }

    /**
     * Set the entities that will be destroyed.
     *
     * @param value - new value.
     */
    public void setPassengers(List<Integer> entities) {
        setPassengers(Ints.toArray(entities));
    }

}
