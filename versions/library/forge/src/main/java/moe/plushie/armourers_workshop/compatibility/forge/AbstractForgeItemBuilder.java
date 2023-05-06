package moe.plushie.armourers_workshop.compatibility.forge;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.common.IItemGroup;
import moe.plushie.armourers_workshop.api.registry.IItemBuilder;
import moe.plushie.armourers_workshop.api.registry.IRegistryBinder;
import moe.plushie.armourers_workshop.api.registry.IRegistryKey;
import moe.plushie.armourers_workshop.compatibility.client.AbstractItemStackRendererProvider;
import moe.plushie.armourers_workshop.init.environment.EnvironmentExecutor;
import moe.plushie.armourers_workshop.init.environment.EnvironmentType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

@Available("[1.18, )")
public abstract class AbstractForgeItemBuilder<T extends Item> implements IItemBuilder<T> {

    protected Item.Properties properties = new Item.Properties();
    protected IRegistryBinder<T> binder;
    protected IRegistryKey<IItemGroup> group;
    protected final Function<Item.Properties, T> supplier;

    public AbstractForgeItemBuilder(Function<Item.Properties, T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public IItemBuilder<T> bind(Supplier<AbstractItemStackRendererProvider> provider) {
        this.binder = () -> item -> {
            // here is safe call client registry.
            GameRenderer.registerItemRendererFO(item.get(), provider.get());
        };
        return this;
    }

    @Override
    public IRegistryKey<T> build(String name) {
        IRegistryKey<T> item = Registry.registerItemFO(name, () -> {
            T value = supplier.apply(properties);
            if (group != null) {
                group.get().add(() -> value);
            }
            return value;
        });
        EnvironmentExecutor.didInit(EnvironmentType.CLIENT, IRegistryBinder.perform(binder, item));
        return item;
    }
}

