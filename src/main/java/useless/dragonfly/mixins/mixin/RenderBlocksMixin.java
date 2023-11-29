package useless.dragonfly.mixins.mixin;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.core.block.Block;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import useless.dragonfly.model.block.BlockModelDragonFly;
import useless.dragonfly.model.block.BlockModelRenderer;

@Mixin(value = RenderBlocks.class, remap = false)
public abstract class RenderBlocksMixin {

	@Inject(method = "renderBlockOnInventory(Lnet/minecraft/core/block/Block;IF)V", at = @At("HEAD"), cancellable = true)
	public void redirectRenderer(Block block, int metadata, float brightness, CallbackInfo ci){
		if (BlockModelDispatcher.getInstance().getDispatch(block) instanceof BlockModelDragonFly){
			GL11.glColor4f(1,1,1,1);
			BlockModelDragonFly blockModelDragonFly = (BlockModelDragonFly) BlockModelDispatcher.getInstance().getDispatch(block);
			BlockModelRenderer.renderModelInventory(blockModelDragonFly, block, metadata, brightness);
			ci.cancel();
		}
	}
}
