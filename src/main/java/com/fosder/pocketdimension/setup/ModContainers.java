package com.fosder.pocketdimension.setup;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.container.DimensionalWorkbenchContainer;
import com.fosder.pocketdimension.container.StabilizerMachineContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, PocketDimensionMod.MOD_ID);

    public static final RegistryObject<ContainerType<DimensionalWorkbenchContainer>> DIMENSIONAL_WORKBENCH = CONTAINERS.register(
            "dimensional_workbench",
            () -> IForgeContainerType.create((windowId, inventory, data) ->
                    new DimensionalWorkbenchContainer(windowId, inventory, IWorldPosCallable.NULL)
            )
    );

    public static final RegistryObject<ContainerType<StabilizerMachineContainer>> STABILIZER_MACHINE = CONTAINERS.register(
            "stabilizer_machine",
            () -> IForgeContainerType.create((windowId, inventory, data) ->
                    new StabilizerMachineContainer(windowId, inventory, IWorldPosCallable.NULL)
            )
    );

    private ModContainers() {}

    public static void register(IEventBus bus) {
        CONTAINERS.register(bus);
    }
}
