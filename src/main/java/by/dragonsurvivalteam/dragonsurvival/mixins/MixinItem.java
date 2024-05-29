package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin( Item.class )
public class MixinItem{
	@Unique
	private boolean dragon_Survival$isEdible(boolean original, Player player) {
		DragonStateHandler handler = DragonStateProvider.getHandler(player);
		if(handler != null && handler.isDragon()){
			return DragonFoodHandler.isDragonEdible((Item)(Object)this, handler.getType());
		}

		return original;
	}

	@ModifyExpressionValue (method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
	private boolean dragonIsEdible(boolean original, Level level, Player player, InteractionHand hand){
		return dragon_Survival$isEdible(original, player);
	}

	@ModifyExpressionValue (method ="use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z"))
	private boolean dragonCanEat(boolean original, Level level, Player player, InteractionHand hand){
		DragonStateHandler handler = DragonStateProvider.getHandler(player);
		if(handler != null && handler.isDragon()){
			FoodProperties dragonFoodProperties = DragonFoodHandler.getDragonFoodProperties((Item)(Object)this, handler.getType());
			if(dragonFoodProperties != null) {
				return player.canEat(dragonFoodProperties.canAlwaysEat());
			}
		}

		return original;
	}

	@ModifyExpressionValue (method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
	private boolean dragonFinishUsingItem(boolean original, ItemStack item, Level level, LivingEntity player){
		if (player instanceof Player) {
			return dragon_Survival$isEdible(original, (Player) player);
		}

		return original;
	}
}