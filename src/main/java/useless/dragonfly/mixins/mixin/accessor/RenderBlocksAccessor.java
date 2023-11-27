package useless.dragonfly.mixins.mixin.accessor;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.core.world.WorldSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RenderBlocks.class, remap = false)
public interface RenderBlocksAccessor {
	@Accessor
	WorldSource getBlockAccess();
}