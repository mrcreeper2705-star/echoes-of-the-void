package dev.voidechoes.storm;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

/** Saved per End world: ticks left in the current storm, or until the next one. */
public class VoidStormState extends PersistentState {
    public static final Type<VoidStormState> TYPE = new Type<>(VoidStormState::new, VoidStormState::fromNbt, null);

    public int stormTicks;      // > 0 while a storm rages
    public int clearTicks = -1; // countdown to the next storm (-1 = not rolled yet)

    public static VoidStormState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        VoidStormState s = new VoidStormState();
        s.stormTicks = nbt.getInt("storm_ticks");
        s.clearTicks = nbt.contains("clear_ticks") ? nbt.getInt("clear_ticks") : -1;
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putInt("storm_ticks", stormTicks);
        nbt.putInt("clear_ticks", clearTicks);
        return nbt;
    }
}
