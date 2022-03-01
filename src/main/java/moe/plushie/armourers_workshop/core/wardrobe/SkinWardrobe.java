package moe.plushie.armourers_workshop.core.wardrobe;

import com.google.common.cache.Cache;
import moe.plushie.armourers_workshop.core.api.common.ISkinWardrobe;
import moe.plushie.armourers_workshop.core.entity.MannequinEntity;
import moe.plushie.armourers_workshop.core.network.NetworkHandler;
import moe.plushie.armourers_workshop.core.network.packet.UpdateWardrobePacket;
import moe.plushie.armourers_workshop.core.utils.SkinSlotType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unused")
public class SkinWardrobe implements ISkinWardrobe, INBTSerializable<CompoundNBT> {

    private final HashSet<EquipmentSlotType> armourFlags = new HashSet<>();
    private final HashMap<SkinSlotType, Integer> skinSlots = new HashMap<>();

    private final Inventory inventory = new Inventory(SkinSlotType.getTotalSize());

    private final WeakReference<Entity> entity;
    private final SkinWardrobeState state;

    private int id; // a.k.a entity id

    public SkinWardrobe(Entity entity) {
        this.id = entity.getId();
        this.state = new SkinWardrobeState(inventory);
        this.entity = new WeakReference<>(entity);
        this.inventory.addListener(inventory -> state.invalidateAll());
    }

    @Nullable
    public static SkinWardrobe of(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        Object key = entity.getId();
        Cache<Object, LazyOptional<SkinWardrobe>> caches = SkinWardrobeStorage.getCaches(entity);
        LazyOptional<SkinWardrobe> wardrobe = caches.getIfPresent(key);
        if (wardrobe != null) {
            return wardrobe.resolve().orElse(null);
        }
        wardrobe = entity.getCapability(SkinWardrobeProvider.WARDROBE_KEY);
        wardrobe.addListener(self -> caches.invalidate(key));
        caches.put(key, wardrobe);
        return wardrobe.resolve().orElse(null);
    }

    public int getFreeSlot(SkinSlotType slotType) {
        int unlockedSize = getUnlockedSize(slotType);
        for (int i = 0; i < unlockedSize; ++i) {
            if (inventory.getItem(slotType.getIndex() + i).isEmpty()) {
                return i + 1;
            }
        }
        return Integer.MAX_VALUE;
    }

    public ItemStack getItem(SkinSlotType slotType, int slot) {
        if (slot >= getUnlockedSize(slotType)) {
            return ItemStack.EMPTY;
        }
        return inventory.getItem(slotType.getIndex() + slot);
    }

    public void setItem(SkinSlotType slotType, int slot, ItemStack itemStack) {
        if (slot >= getUnlockedSize(slotType)) {
            return;
        }
        inventory.setItem(slotType.getIndex() + slot, itemStack);
    }

    public void clear() {
        inventory.clearContent();
    }

    public void sendToAll() {
        NetworkHandler.getInstance().sendToAll(UpdateWardrobePacket.sync(this));
    }

    public void sendToServer() {
        NetworkHandler.getInstance().sendToServer(UpdateWardrobePacket.sync(this));
    }

    public void broadcast(ServerPlayerEntity player) {
        NetworkHandler.getInstance().sendTo(UpdateWardrobePacket.sync(this), player);
    }

    public boolean shouldRenderEquipment(EquipmentSlotType slotType) {
        return !armourFlags.contains(slotType);
    }

    public void setRenderEquipment(EquipmentSlotType slotType, boolean enable) {
        if (enable) {
            armourFlags.remove(slotType);
        } else {
            armourFlags.add(slotType);
        }
    }

    public int getUnlockedSize(SkinSlotType slotType) {
        if (slotType == SkinSlotType.DYE) {
            return 8;
        }
        if (getEntity() instanceof MannequinEntity) {
            if (!slotType.isArmor()) {
                return 0;
            }
        }
        return slotType.getSize();
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Nullable
    public Entity getEntity() {
        return entity.get();
    }

    public int getId() {
        Entity entity = getEntity();
        if (entity != null) {
            id = entity.getId();
        }
        return id;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        SkinWardrobeStorage.saveSkinSlots(skinSlots, nbt);
        SkinWardrobeStorage.saveVisibility(armourFlags, nbt);
        SkinWardrobeStorage.saveInventoryItems(inventory, nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SkinWardrobeStorage.loadSkinSlots(skinSlots, nbt);
        SkinWardrobeStorage.loadVisibility(armourFlags, nbt);
        SkinWardrobeStorage.loadInventoryItems(inventory, nbt);
        state.invalidateAll();
    }

    @OnlyIn(Dist.CLIENT)
    public SkinWardrobeState snapshot() {
        Entity entity = getEntity();
        if (entity != null) {
            if (state.tick(entity)) {
                state.reload(entity);
            }
        }
        return state;
    }
}

