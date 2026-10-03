package com.fosder.pocketdimension.integration.geckolib;

import net.minecraft.tileentity.TileEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

/** Estado animable del núcleo de Colapso Oscuro. */
public class GeckoDarkDimensionalRiftTileEntity extends TileEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public GeckoDarkDimensionalRiftTileEntity() {
        super(GeckoAnimatedTileEntities.DARK_DIMENSIONAL_RIFT.get());
    }

    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(
                "animation.pocketdimension.dark_rift.collapse",
                EDefaultLoopTypes.LOOP
        ));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<GeckoDarkDimensionalRiftTileEntity>(
                this,
                "collapse_controller",
                0,
                this::predicate
        ));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
