package moe.plushie.armourers_workshop.core.item;

import moe.plushie.armourers_workshop.api.common.IItemModelProperty;
import moe.plushie.armourers_workshop.api.common.IItemPropertiesProvider;
import moe.plushie.armourers_workshop.core.capability.SkinWardrobe;
import moe.plushie.armourers_workshop.core.client.bake.SkinBakery;
import moe.plushie.armourers_workshop.core.data.SkinBlockPlaceContext;
import moe.plushie.armourers_workshop.core.data.slot.SkinSlotType;
import moe.plushie.armourers_workshop.core.data.ticket.Tickets;
import moe.plushie.armourers_workshop.core.skin.Skin;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.core.skin.SkinLoader;
import moe.plushie.armourers_workshop.core.skin.SkinTypes;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.init.ModItems;
import moe.plushie.armourers_workshop.utils.Constants;
import moe.plushie.armourers_workshop.utils.TranslateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;

import manifold.ext.rt.api.auto;

public class SkinItem extends BlockItem implements IItemPropertiesProvider {

    public SkinItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public static ItemStack replace(ItemStack targetStack, ItemStack sourceStack) {
        CompoundTag sourceNBT = null;
        if (!sourceStack.isEmpty()) {
            sourceNBT = sourceStack.getTagElement(Constants.Key.SKIN);
        }
        if (sourceNBT != null && sourceNBT.size() != 0) {
            targetStack.addTagElement(Constants.Key.SKIN, sourceNBT.copy());
        } else {
            CompoundTag targetNBT = targetStack.getTag();
            if (targetNBT != null) {
                targetNBT.remove(Constants.Key.SKIN);
            }
        }
        return targetStack;
    }

    public static ItemStack replace(ItemStack targetStack, SkinDescriptor descriptor) {
        if (targetStack.isEmpty()) {
            return descriptor.asItemStack();
        }
        if (targetStack.is(ModItems.SKIN_TEMPLATE.get())) {
            return descriptor.asItemStack();
        }
        if (descriptor.isEmpty()) {
            CompoundTag targetNBT = targetStack.getTag();
            if (targetNBT != null) {
                targetNBT.remove(Constants.Key.SKIN);
            }
        } else {
            targetStack.addTagElement(Constants.Key.SKIN, descriptor.serializeNBT());
        }
        return targetStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        SkinDescriptor descriptor = SkinDescriptor.of(itemStack);
        SkinSlotType slotType = SkinSlotType.of(descriptor.getType());
        if (descriptor.isEmpty() || slotType == null) {
            return InteractionResultHolder.pass(itemStack);
        }
        SkinWardrobe wardrobe = SkinWardrobe.of(player);
        if (wardrobe == null || !wardrobe.isEditable(player)) {
            return InteractionResultHolder.pass(itemStack);
        }
        int slot = wardrobe.getFreeSlot(slotType);
        if (!wardrobe.getItem(slotType, slot).isEmpty()) {
            return InteractionResultHolder.pass(itemStack);
        }
        // we need to wait for the server check.
        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        }
        wardrobe.setItem(slotType, slot, itemStack.copy());
        wardrobe.broadcast();
        itemStack.shrink(1);
        return InteractionResultHolder.consume(itemStack.copy());
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack itemStack = context.getItemInHand();
        SkinDescriptor descriptor = SkinDescriptor.of(itemStack);
        if (descriptor.getType() != SkinTypes.BLOCK) {
            return InteractionResult.PASS;
        }
        // we need expand the context info.
        return super.place(new SkinBlockPlaceContext(context));
    }

    @Override
    public Component getName(ItemStack itemStack) {
        Skin skin = SkinLoader.getInstance().getSkin(itemStack);
        if (skin != null && !skin.getCustomName().trim().isEmpty()) {
            return TranslateUtils.formatted(skin.getCustomName());
        }
        if (skin != null) {
            return TranslateUtils.Name.of(skin.getType());
        }
        return super.getName(itemStack);
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    public void createModelProperties(BiConsumer<ResourceLocation, IItemModelProperty> builder) {
        builder.accept(ModConstants.key("loading"), (itemStack, level, entity, id) -> {
            auto descriptor = SkinDescriptor.of(itemStack);
            auto bakedSkin = SkinBakery.getInstance().loadSkin(descriptor, Tickets.INVENTORY);
            if (bakedSkin != null) {
                return 0;
            }
            return descriptor.getType().getId() / 1000f;
        });
    }
}
