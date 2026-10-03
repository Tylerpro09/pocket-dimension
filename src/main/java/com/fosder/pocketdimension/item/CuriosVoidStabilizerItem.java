package com.fosder.pocketdimension.item;

import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.world.PocketDimension;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Integración opcional con Curios para convertir el Estabilizador del Vacío en un accesorio útil.
 *
 * La clase solo se instancia cuando Curios está cargado; sin Curios el registro usa el Item normal
 * y el mod sigue funcionando sin esa dependencia.
 */
public final class CuriosVoidStabilizerItem extends Item implements ICurioItem {
    private static final int RECOVERY_INTERVAL_TICKS = 20 * 10;
    private static final int RECOVERY_ENERGY = 2;

    public CuriosVoidStabilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip,
                                 ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("tooltip.pocketdimension.void_stabilizer_curios"));
    }

    @Override
    public boolean canEquip(String identifier, LivingEntity livingEntity, ItemStack stack) {
        return "charm".equals(identifier) || "curio".equals(identifier);
    }

    @Override
    public void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        if (!(livingEntity instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) livingEntity;
        if (player.tickCount % RECOVERY_INTERVAL_TICKS != 0) return;
        if (!PocketDimension.isPocketWorld(player.level)) return;
        if (!PocketStabilityData.isMachineLinked(player)) return;

        PocketStabilityData.addEnergy(player, RECOVERY_ENERGY);
    }
}
