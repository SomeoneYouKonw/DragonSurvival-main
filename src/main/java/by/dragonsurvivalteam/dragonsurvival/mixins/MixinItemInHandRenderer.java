package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( ItemInHandRenderer.class )
public class MixinItemInHandRenderer{

	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyExpressionValue( method = "applyEatTransform", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
	private int dragonUseDuration(int original, @Local(argsOnly = true) ItemStack stack){
		return DragonStateProvider.getCap(minecraft.player).map(dragonStateHandler -> dragonStateHandler.isDragon() ? DragonFoodHandler.getUseDuration(stack, dragonStateHandler.getType()) : original).orElse(original);
	}

	@Inject( at = @At( value = "HEAD" ), method = "renderPlayerArm", cancellable = true )
	private void hideArmsForDragon(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide, CallbackInfo ci){
		if(DragonUtils.isDragon(minecraft.player)){
			ci.cancel();
		}
	}

	@ModifyExpressionValue (method = "renderTwoHandedMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInvisible()Z"))
	private boolean hideArmsForDragonTwoHandedMap(boolean original){
		return !DragonUtils.isDragon(minecraft.player) && original;
	}
}