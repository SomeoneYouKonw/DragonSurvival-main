package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.HashMap;

public class ClientGrowthHudHandler {
	private static final HashMap<String, ResourceLocation> CACHE = new HashMap<>();

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = ConfigSide.CLIENT, category = {"ui", "growth"}, key = "growthXOffset", comment = "Offset the x position of the item growth icon in relation to its normal position" )
	public static Integer growthXOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = ConfigSide.CLIENT, category = {"ui", "growth"}, key = "growthYOffset", comment = "Offset the y position of the item growth icon in relation to its normal position" )
	public static Integer growthYOffset = 0;

	public static void renderGrowth(final DragonStateHandler handler, final PoseStack poseStack, int width, int height) {
		Player localPlayer = Minecraft.getInstance().player;

		if (localPlayer == null || localPlayer.isSpectator()) {
			return;
		}

		ItemStack stack = localPlayer.getMainHandItem();

		if (stack.isEmpty()) {
			return;
		}

		int increment = DragonGrowthHandler.getIncrement(stack.getItem(), handler.getLevel());

		if (increment != 0 && (handler.getSize() < ServerConfig.maxGrowthSize && increment > 0 || increment < 0 && handler.getSize() >= DragonLevel.NEWBORN.size + 1)) {
			float curSize = (float) handler.getSize();
			float nextSize = (float) (handler.getSize() + increment);
			float progress = 0;
			float nextProgess = 0;

			if (handler.getLevel() == DragonLevel.NEWBORN) {
				progress = (curSize - DragonLevel.NEWBORN.size) / (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size);
				nextProgess = (nextSize - DragonLevel.NEWBORN.size) / (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size);
			} else if (handler.getLevel() == DragonLevel.YOUNG) {
				progress = (curSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
				nextProgess = (nextSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
			} else if (handler.getLevel() == DragonLevel.ADULT && handler.getSize() < 40) {
				progress = (curSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
				nextProgess = (nextSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
			} else if (handler.getLevel() == DragonLevel.ADULT && handler.getSize() >= 40) {
				progress = (float) ((curSize - 40) / (ServerConfig.maxGrowthSize - 40));
				nextProgess = (float) ((nextSize - 40) / (ServerConfig.maxGrowthSize - 40));
			}

			progress = Math.min(1.0f, progress);
			nextProgess = Math.min(1.0f, nextProgess);

			int radius = 17;
			int thickness = 5;
			int circleX = width / 2 - radius;
			int circleY = height - 90;

			circleX += growthXOffset;
			circleY += growthYOffset;

			RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
			Color color = new Color(99, 99, 99);

			RenderSystem.disableTexture();

			RenderSystem.setShaderColor(color.brighter().getRed() / 255.0f, color.brighter().getBlue() / 255.0f, color.brighter().getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(poseStack, circleX + radius, circleY + radius, radius, 6, 1, 0);

			RenderSystem.enableTexture();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);

			if (nextProgess > progress) {
				int num = 1;
				double perSide = 1.0 / 6.0;

				if (nextProgess < progress + perSide) {
					nextProgess = (float) (progress + perSide);
					num = 2;
				}

				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_" + num + ".png"));
				RenderingUtils.drawTexturedCircle(poseStack, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);

				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_" + handler.getTypeName().toLowerCase() + ".png"));
				RenderingUtils.drawTexturedCircle(poseStack, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);
			} else if (increment < 0) {
				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_3.png"));
				RenderingUtils.drawTexturedCircle(poseStack, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);

				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_" + handler.getTypeName().toLowerCase() + ".png"));
				RenderingUtils.drawTexturedCircle(poseStack, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);
			}

			RenderSystem.disableTexture();
			RenderSystem.setShaderColor(color.getRed() / 255.0f, color.getBlue() / 255.0f, color.getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(poseStack, circleX + radius, circleY + radius, radius - thickness, 6, 1, 0);
			RenderSystem.enableTexture();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);

			RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/growth_" + handler.getTypeName().toLowerCase() + "_" + (handler.getLevel().ordinal() + 1) + ".png"));
			Screen.blit(poseStack, circleX + 6, circleY + 6, 0, 0, 20, 20, 20, 20);
		}
	}

	private static ResourceLocation getOrCreate(final String path) {
		return CACHE.computeIfAbsent(path, key -> new ResourceLocation(DragonSurvivalMod.MODID, path));
	}
}