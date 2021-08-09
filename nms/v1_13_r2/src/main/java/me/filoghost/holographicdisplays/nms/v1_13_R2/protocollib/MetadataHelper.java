/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.filoghost.holographicdisplays.nms.v1_13_R2.protocollib;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftChatMessage;

import java.util.Optional;

public class MetadataHelper {

    private final int itemSlotIndex;
    private final int entityStatusIndex;
    private final int airLevelWatcherIndex;
    private final int customNameIndex;
    private final int customNameVisibleIndex;
    private final int noGravityIndex;
    private final int armorStandStatusIndex;
    private final int slimeSizeIndex;
    private Serializer itemSerializer;
    private Serializer intSerializer;
    private Serializer byteSerializer;
    private Serializer stringSerializer;
    private Serializer booleanSerializer;
    private Serializer chatComponentSerializer;


    public MetadataHelper() {
        if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_17_R1)) {
            itemSlotIndex = 8;
        } else if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_14_R1)) {
            itemSlotIndex = 7;
        } else if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_10_R1)) {
            itemSlotIndex = 6;
        } else if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_9_R1)) {
            itemSlotIndex = 5;
        } else {
            itemSlotIndex = 10;
        }

        if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_17_R1)) {
            armorStandStatusIndex = 15;
        } else if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_15_R1)) {
            armorStandStatusIndex = 14;
        } else {
            armorStandStatusIndex = 11;
        }

        entityStatusIndex = 0;
        airLevelWatcherIndex = 1;
        customNameIndex = 2;
        customNameVisibleIndex = 3;
        noGravityIndex = 5;
        slimeSizeIndex = 15;

        if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_9_R1)) {
            itemSerializer = Registry.get(MinecraftReflection.getItemStackClass());
            intSerializer = Registry.get(Integer.class);
            byteSerializer = Registry.get(Byte.class);
            stringSerializer = Registry.get(String.class);
            booleanSerializer = Registry.get(Boolean.class);
        }

        if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_13_R1)) {
            chatComponentSerializer = Registry.get(MinecraftReflection.getIChatBaseComponentClass(), true);
        }
    }

    private static void requireMinimumVersion(NMSVersion minimumVersion) {
        if (!NMSVersion.isGreaterEqualThan(minimumVersion)) {
            throw new UnsupportedOperationException("Method only available from NMS version " + minimumVersion);
        }
    }

    public void setEntityStatus(WrappedDataWatcher dataWatcher, byte statusBitmask) {
        requireMinimumVersion(NMSVersion.v1_9_R1);
        dataWatcher.setObject(new WrappedDataWatcherObject(entityStatusIndex, byteSerializer), statusBitmask);
    }

    public void setCustomNameNMSObject(WrappedWatchableObject customNameWatchableObject, Object customNameNMSObject) {
        if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_13_R1)) {
            customNameWatchableObject.setValue(Optional.ofNullable(customNameNMSObject));
        } else {
            customNameWatchableObject.setValue(customNameNMSObject);
        }
    }

    public void setCustomNameNMSObject(WrappedDataWatcher dataWatcher, String customName) {
        requireMinimumVersion(NMSVersion.v1_9_R1);

        if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_13_R1)) {
            dataWatcher.setObject(
                    new WrappedDataWatcherObject(customNameIndex, chatComponentSerializer),
                    Optional.of(CraftChatMessage.fromString(customName, false)[0]));
        } else {
            dataWatcher.setObject(new WrappedDataWatcherObject(customNameIndex, stringSerializer), customName);
        }
    }

    public void setCustomNameVisible(WrappedDataWatcher dataWatcher, boolean customNameVisible) {
        requireMinimumVersion(NMSVersion.v1_9_R1);
        dataWatcher.setObject(new WrappedDataWatcherObject(customNameVisibleIndex, booleanSerializer), customNameVisible);
    }

    public void setNoGravity(WrappedDataWatcher dataWatcher, boolean noGravity) {
        requireMinimumVersion(NMSVersion.v1_9_R1);
        dataWatcher.setObject(new WrappedDataWatcherObject(noGravityIndex, booleanSerializer), noGravity);
    }

    public void setArmorStandStatus(WrappedDataWatcher dataWatcher, byte statusBitmask) {
        requireMinimumVersion(NMSVersion.v1_9_R1);
        dataWatcher.setObject(new WrappedDataWatcherObject(armorStandStatusIndex, byteSerializer), statusBitmask);
    }

    public void setItemMetadata(WrappedDataWatcher dataWatcher, Object nmsItemStack) {
        if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_9_R1)) {
            if (NMSVersion.isGreaterEqualThan(NMSVersion.v1_11_R1)) {
                dataWatcher.setObject(new WrappedDataWatcherObject(itemSlotIndex, itemSerializer), nmsItemStack);
            } else {
                dataWatcher.setObject(
                        new WrappedDataWatcherObject(itemSlotIndex, itemSerializer),
                        com.google.common.base.Optional.of(nmsItemStack));
            }
            dataWatcher.setObject(new WrappedDataWatcherObject(airLevelWatcherIndex, intSerializer), 300);
            dataWatcher.setObject(new WrappedDataWatcherObject(entityStatusIndex, byteSerializer), (byte) 0);
        } else {
            dataWatcher.setObject(itemSlotIndex, nmsItemStack);
            dataWatcher.setObject(airLevelWatcherIndex, 300);
            dataWatcher.setObject(entityStatusIndex, (byte) 0);
        }
    }

    public void setSlimeSize(WrappedDataWatcher dataWatcher, int size) {
        requireMinimumVersion(NMSVersion.v1_15_R1);
        dataWatcher.setObject(new WrappedDataWatcherObject(slimeSizeIndex, intSerializer), size);
    }

}
