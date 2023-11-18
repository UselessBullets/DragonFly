package useless.dragonfly.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderBlockCache;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextureFX;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.core.Global;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import useless.dragonfly.model.block.BlockModelDragonFly;
import useless.dragonfly.DragonFly;
import useless.dragonfly.mixininterfaces.ExtraRendering;
import useless.dragonfly.model.block.processed.BlockCube;
import useless.dragonfly.model.block.processed.BlockFace;
import useless.dragonfly.model.block.processed.BlockModel;
import useless.dragonfly.registries.TextureRegistry;

@Mixin(value = RenderBlocks.class, remap = false)
public abstract class RenderBlocksMixin implements ExtraRendering {
	@Shadow
	private Minecraft mc;

	@Shadow
	private World world;

	@Shadow
	private boolean enableAO;

	@Shadow
	private WorldSource blockAccess;

	@Shadow
	private RenderBlockCache cache;

	@Shadow
	public abstract float getBlockBrightness(WorldSource blockAccess, int x, int y, int z);

	@Shadow
	private boolean renderAllFaces;

	@Shadow
	private int overrideBlockTexture;

	@Shadow
	public boolean overbright;

	@Shadow
	private int field_22352_G;

	@Shadow
	private float colorRedTopRight;

	@Shadow
	private float colorRedBottomRight;

	@Shadow
	private float colorRedBottomLeft;

	@Shadow
	private float colorGreenTopRight;

	@Shadow
	private float colorRedTopLeft;

	@Shadow
	private float colorGreenBottomRight;

	@Shadow
	private float colorGreenBottomLeft;

	@Shadow
	private float colorGreenTopLeft;

	@Shadow
	private float colorBlueTopRight;

	@Shadow
	private float colorBlueBottomRight;

	@Shadow
	private float colorBlueBottomLeft;

	@Shadow
	private float colorBlueTopLeft;

	@Shadow
	@Final
	private static float[] SIDE_LIGHT_MULTIPLIER;

	@Shadow
	private int uvRotateBottom;

	@Shadow
	private int uvRotateTop;

	@Shadow
	private boolean flipTexture;

	@Shadow
	private int uvRotateEast;

	@Shadow
	private int uvRotateWest;

	@Shadow
	private int uvRotateNorth;

	@Shadow
	private int uvRotateSouth;
	@Inject(method = "renderBlockOnInventory(Lnet/minecraft/core/block/Block;IF)V", at = @At("HEAD"), cancellable = true)
	public void redirectRenderer(Block block, int metadata, float brightness, CallbackInfo ci){
		if (BlockModelDispatcher.getInstance().getDispatch(block) instanceof BlockModelDragonFly){
			BlockModelDragonFly blockModelDragonFly = (BlockModelDragonFly) BlockModelDispatcher.getInstance().getDispatch(block);
			renderModelInventory(blockModelDragonFly, block, metadata, brightness);
			ci.cancel();
		}
	}
	@Unique
	public void renderModelInventory(BlockModelDragonFly modelDragonFly, Block block, int meta, float brightness){
		float yOffset = 0.5f;
		Tessellator tessellator = Tessellator.instance;
		GL11.glTranslatef(-0.5f, 0.0f - yOffset, -0.5f);
		for (BlockCube cube: modelDragonFly.baseModel.elements) {
			for (BlockFace face: cube.faces.values()) {
				tessellator.startDrawingQuads();
				tessellator.setNormal(face.getSide().getOffsetX(), face.getSide().getOffsetY(), face.getSide().getOffsetZ());
				renderModelFaceBySide(cube, face.getSide(), block, 0, 0, 0, block.getBlockTextureFromSideAndMetadata(face.getSide(), meta));
				tessellator.draw();
			}
		}
		GL11.glTranslatef(0.5f, 0.5f, 0.5f);
	}
	@Unique
	public boolean renderModelNormal(BlockModel model, Block block, int x, int y, int z) {
		int color = BlockColorDispatcher.getInstance().getDispatch(block).getWorldColor(this.world, x, y, z);
		float red = (float)(color >> 16 & 0xFF) / 255.0f;
		float green = (float)(color >> 8 & 0xFF) / 255.0f;
		float blue = (float)(color & 0xFF) / 255.0f;
		if (mc.isAmbientOcclusionEnabled()) {
			return this.renderStandardModelWithAmbientOcclusion(model, block, x, y, z, red, green, blue);
		}
		return this.renderStandardModelWithColorMultiplier(model, block, x, y, z, red, green, blue);
	}
	@Unique
	public boolean renderModelNoCulling(BlockModel model, Block block, int x, int y, int z){
		this.renderAllFaces = true;
		boolean result = this.renderModelNormal(model, block, x, y, z);
		this.renderAllFaces = false;
		return result;
	}
	@Unique
	public boolean renderModelBlockUsingTexture(BlockModel model, Block block, int x, int y, int z, int textureIndex){
		this.overrideBlockTexture = textureIndex;
		boolean result = this.renderModelNormal(model, block, x, y, z);
		this.overrideBlockTexture = -1;
		return result;
	}
	@Unique
	public boolean renderStandardModelWithAmbientOcclusion(BlockModel model, Block block, int x, int y, int z, float r, float g, float b) {
		this.enableAO = true;
		int meta = this.blockAccess.getBlockMetadata(x, y, z);
		this.cache.setupCache(block, this.blockAccess, x, y, z);
		boolean somethingRendered = false;
		for (BlockCube cube: model.elements) {
			somethingRendered |= renderModelSide(model, cube, block, x, y, z, r, g, b, Side.BOTTOM, meta, cube.yMin(), 0, 0, 1, cube.zMax(), cube.zMin(), -1, 0, 0, 1.0F - cube.xMin(), 1.0F - cube.xMax());
			somethingRendered |= renderModelSide(model, cube, block, x, y, z, r, g, b, Side.TOP, meta, 1.0F - cube.yMax(), 0, 0, 1, cube.zMax(), cube.zMin(), 1, 0, 0, cube.xMax(), cube.xMin());
			somethingRendered |= renderModelSide(model, cube, block, x, y, z, r, g, b, Side.NORTH, meta, cube.zMin(), -1, 0, 0, 1.0F - cube.xMin(), 1.0F - cube.xMax(), 0, 1, 0, cube.yMax(), cube.yMin());
			somethingRendered |= renderModelSide(model, cube, block, x, y, z, r, g, b, Side.SOUTH, meta, 1.0F - cube.zMax(), 0, 1, 0, cube.yMax(), cube.yMin(), -1, 0, 0, 1.0F - cube.xMin(), 1.0F - cube.xMax());
			somethingRendered |= renderModelSide(model, cube, block, x, y, z, r, g, b, Side.WEST, meta, cube.xMin(), 0, 0, 1, cube.zMax(), cube.zMin(), 0, 1, 0, cube.yMax(), cube.yMin());
			somethingRendered |= renderModelSide(model, cube, block, x, y, z, r, g, b, Side.EAST, meta, 1.0F - cube.xMax(), 0, 0, 1, cube.zMax(), cube.zMin(), 0, -1, 0, 1.0F - cube.yMin(), 1.0F - cube.yMax());
		}
		this.enableAO = false;
		return somethingRendered;
	}
	@Unique
	public boolean renderSide(BlockModel model, BlockCube cube, Side side, boolean renderOuterSide){
		if (model.hasFaceToRender(side)){
			if (cube.isOuterFace(side)){
				if (!renderOuterSide){
					return false;
				}
			}
			if (!cube.isFaceVisible(side)){
				return false;
			}
		}
		return true;
	}
	@Unique
	public boolean renderModelSide(BlockModel model, BlockCube cube, Block block, int x, int y, int z, float r, float g, float b, Side side, int meta, float depth, int topX, int topY, int topZ, float topP, float botP, int lefX, int lefY, int lefZ, float lefP, float rigP) {
		if (cube.getFaceFromSide(side) == null) return false;
		int dirX = side.getOffsetX();
		int dirY = side.getOffsetY();
		int dirZ = side.getOffsetZ();

		boolean renderOuterSide = block.shouldSideBeRendered(this.blockAccess, x + dirX, y + dirY, z + dirZ, side.getId(), meta);

		if (!this.renderAllFaces){
			if (!renderSide(model, cube, side, renderOuterSide)) return false;
		}


		float lightTL;
		float lightBL;
		float lightBR;
		float lightTR;
		if (this.overbright) {
			lightTR = 1.0f;
			lightBR = 1.0f;
			lightBL = 1.0f;
			lightTL = 1.0f;
		} else if (this.field_22352_G <= 0) {
			lightBR = lightTR = this.cache.getBrightness(dirX, dirY, dirZ);
			lightBL = lightTR;
			lightTL = lightTR;
		} else {
			float dirB = this.cache.getBrightness(dirX, dirY, dirZ);
			boolean lefT = this.cache.getOpacity(dirX + lefX, dirY + lefY, dirZ + lefZ);
			boolean botT = this.cache.getOpacity(dirX - topX, dirY - topY, dirZ - topZ);
			boolean topT = this.cache.getOpacity(dirX + topX, dirY + topY, dirZ + topZ);
			boolean rigT = this.cache.getOpacity(dirX - lefX, dirY - lefY, dirZ - lefZ);
			float lB = this.cache.getBrightness(dirX + lefX, dirY + lefY, dirZ + lefZ);
			float bB = this.cache.getBrightness(dirX - topX, dirY - topY, dirZ - topZ);
			float tB = this.cache.getBrightness(dirX + topX, dirY + topY, dirZ + topZ);
			float rB = this.cache.getBrightness(dirX - lefX, dirY - lefY, dirZ - lefZ);
			float blB = botT && lefT ? lB : this.cache.getBrightness(dirX + lefX - topX, dirY + lefY - topY, dirZ + lefZ - topZ);
			float tlB = topT && lefT ? lB : this.cache.getBrightness(dirX + lefX + topX, dirY + lefY + topY, dirZ + lefZ + topZ);
			float brB = botT && rigT ? rB : this.cache.getBrightness(dirX - lefX - topX, dirY - lefY - topY, dirZ - lefZ - topZ);
			float trB = topT && rigT ? rB : this.cache.getBrightness(dirX - lefX + topX, dirY - lefY + topY, dirZ - lefZ + topZ);
			lightTL = (tlB + lB + tB + dirB) / 4.0f;
			lightTR = (tB + dirB + trB + rB) / 4.0f;
			lightBR = (dirB + bB + rB + brB) / 4.0f;
			lightBL = (lB + blB + dirB + bB) / 4.0f;
			if ((double)depth > 0.01) {
				dirB = this.cache.getBrightness(0, 0, 0);
				lefT = this.cache.getOpacity(lefX, lefY, lefZ);
				botT = this.cache.getOpacity(-topX, -topY, -topZ);
				topT = this.cache.getOpacity(topX, topY, topZ);
				rigT = this.cache.getOpacity(-lefX, -lefY, -lefZ);
				lB = this.cache.getBrightness(lefX, lefY, lefZ);
				bB = this.cache.getBrightness(-topX, -topY, -topZ);
				tB = this.cache.getBrightness(topX, topY, topZ);
				rB = this.cache.getBrightness(-lefX, -lefY, -lefZ);
				blB = botT && lefT ? lB : this.cache.getBrightness(lefX - topX, lefY - topY, lefZ - topZ);
				tlB = topT && lefT ? lB : this.cache.getBrightness(lefX + topX, lefY + topY, lefZ + topZ);
				brB = botT && rigT ? rB : this.cache.getBrightness(-lefX - topX, -lefY - topY, -lefZ - topZ);
				trB = topT && rigT ? rB : this.cache.getBrightness(-lefX + topX, -lefY + topY, -lefZ + topZ);
				lightTL = (tlB + lB + tB + dirB) / 4.0f * depth + lightTL * (1.0f - depth);
				lightTR = (tB + dirB + trB + rB) / 4.0f * depth + lightTR * (1.0f - depth);
				lightBR = (dirB + bB + rB + brB) / 4.0f * depth + lightBR * (1.0f - depth);
				lightBL = (lB + blB + dirB + bB) / 4.0f * depth + lightBL * (1.0f - depth);
			}
		}
		if (this.overbright) {
			this.colorRedTopRight = r;
			this.colorRedBottomRight = r;
			this.colorRedBottomLeft = r;
			this.colorRedTopLeft = r;
			this.colorGreenTopRight = g;
			this.colorGreenBottomRight = g;
			this.colorGreenBottomLeft = g;
			this.colorGreenTopLeft = g;
			this.colorBlueTopRight = b;
			this.colorBlueBottomRight = b;
			this.colorBlueBottomLeft = b;
			this.colorBlueTopLeft = b;
		} else {
			this.colorRedBottomRight = this.colorRedTopRight = r * SIDE_LIGHT_MULTIPLIER[side.getId()];
			this.colorRedBottomLeft = this.colorRedTopRight;
			this.colorRedTopLeft = this.colorRedTopRight;
			this.colorGreenBottomRight = this.colorGreenTopRight = g * SIDE_LIGHT_MULTIPLIER[side.getId()];
			this.colorGreenBottomLeft = this.colorGreenTopRight;
			this.colorGreenTopLeft = this.colorGreenTopRight;
			this.colorBlueBottomRight = this.colorBlueTopRight = b * SIDE_LIGHT_MULTIPLIER[side.getId()];
			this.colorBlueBottomLeft = this.colorBlueTopRight;
			this.colorBlueTopLeft = this.colorBlueTopRight;
		}
		float tl = topP * lightTL + (1.0f - topP) * lightBL;
		float tr = topP * lightTR + (1.0f - topP) * lightBR;
		float bl2 = botP * lightTL + (1.0f - botP) * lightBL;
		float br = botP * lightTR + (1.0f - botP) * lightBR;
		float ltl = lefP * tl + (1.0f - lefP) * tr;
		float lbl = lefP * bl2 + (1.0f - lefP) * br;
		float lbr = rigP * bl2 + (1.0f - rigP) * br;
		float ltr = rigP * tl + (1.0f - rigP) * tr;
		this.colorRedTopLeft *= ltl;
		this.colorGreenTopLeft *= ltl;
		this.colorBlueTopLeft *= ltl;
		this.colorRedBottomLeft *= lbl;
		this.colorGreenBottomLeft *= lbl;
		this.colorBlueBottomLeft *= lbl;
		this.colorRedBottomRight *= lbr;
		this.colorGreenBottomRight *= lbr;
		this.colorBlueBottomRight *= lbr;
		this.colorRedTopRight *= ltr;
		this.colorGreenTopRight *= ltr;
		this.colorBlueTopRight *= ltr;
		int tex = this.overbright ? block.getBlockOverbrightTexture(this.blockAccess, x, y, z, side.getId()) : block.getBlockTexture(this.blockAccess, x, y, z, side);
		if (tex >= 0) {
			renderModelFaceBySide(cube, side, block, x, y, z, tex);
			return true;
		}
		return false;
	}
	@Unique
	public void renderBottomFace(BlockCube cube, Block block, double x, double y, double z, int texture) {
		BlockFace face = cube.getFaceFromSide(Side.BOTTOM);
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texture = this.overrideBlockTexture;
		}
		int texX = texture % Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		int texY = texture / Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		double renderU2 = ((double)texX + face.uMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderU4 = ((double)texX + face.uMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV2 = ((double)texY + face.vMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV4 = ((double)texY + face.vMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		if (face.uMin() < 0.0 || face.uMax() > 1.0) {
			renderU2 = ((float)texX + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((float)texX + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		if (face.vMin() < 0.0 || face.vMax() > 1.0) {
			renderV2 = ((float)texY + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((float)texY + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		double renderU3 = renderU4;
		double renderU1 = renderU2;
		double renderV3 = renderV2;
		double renderV1 = renderV4;
		if (this.uvRotateBottom == 2) {
			renderU2 = ((double)texX + block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)texX + block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = renderV2;
			renderV1 = renderV4;
			renderU3 = renderU2;
			renderU1 = renderU4;
			renderV2 = renderV4;
			renderV4 = renderV3;
		} else if (this.uvRotateBottom == 1) {
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)texY + block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)texY + block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = renderU4;
			renderU1 = renderU2;
			renderU2 = renderU3;
			renderU4 = renderU1;
			renderV3 = renderV4;
			renderV1 = renderV2;
		} else if (this.uvRotateBottom == 3) {
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = renderU4;
			renderU1 = renderU2;
			renderV3 = renderV2;
			renderV1 = renderV4;
		}
		double renderMinX = x + cube.xMin();
		double renderMaxX = x + cube.xMax();
		double renderY = y + cube.yMin();
		double renderMinZ = z + cube.zMin();
		double renderMaxZ = z + cube.zMax();
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.addVertexWithUV(renderMinX, renderY, renderMaxZ, renderU1, renderV1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.addVertexWithUV(renderMinX, renderY, renderMinZ, renderU2, renderV2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight, this.colorBlueBottomRight);
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMinZ, renderU3, renderV3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMaxZ, renderU4, renderV4);
		} else {
			tessellator.addVertexWithUV(renderMinX, renderY, renderMaxZ, renderU1, renderV1);
			tessellator.addVertexWithUV(renderMinX, renderY, renderMinZ, renderU2, renderV2);
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMinZ, renderU3, renderV3);
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMaxZ, renderU4, renderV4);
		}
	}
	@Unique
	public void renderTopFace(BlockCube cube, Block block, double x, double y, double z, int texture) {
		BlockFace face = cube.getFaceFromSide(Side.TOP);
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texture = this.overrideBlockTexture;
		}
		int texX = texture % Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		int texY = texture / Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		double renderU3 = ((double)texX + face.uMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderU1 = ((double)texX + face.uMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV3 = ((double)texY + face.vMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV1 = ((double)texY + face.vMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		if (face.uMin() < 0.0 || face.uMax() > 1.0) {
			renderU3 = ((float)texX + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = ((float)texX + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		if (face.vMin() < 0.0 || face.vMax() > 1.0) {
			renderV3 = ((float)texY + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = ((float)texY + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		double renderU2 = renderU1;
		double renderU4 = renderU3;
		double renderV2 = renderV3;
		double renderV4 = renderV1;
		if (this.uvRotateTop == 1) {
			renderU3 = ((double)texX + block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = ((double)texX + block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = renderV3;
			renderV4 = renderV1;
			renderU2 = renderU3;
			renderU4 = renderU1;
			renderV3 = renderV1;
			renderV1 = renderV2;
		} else if (this.uvRotateTop == 2) {
			renderU3 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = ((double)texY + block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = ((double)texY + block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU2 = renderU1;
			renderU4 = renderU3;
			renderU3 = renderU2;
			renderU1 = renderU4;
			renderV2 = renderV1;
			renderV4 = renderV3;
		} else if (this.uvRotateTop == 3) {
			renderU3 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU2 = renderU1;
			renderU4 = renderU3;
			renderV2 = renderV3;
			renderV4 = renderV1;
		}
		double renderMinX = x + cube.xMin();
		double renderMaxX = x + cube.xMax();
		double renderY = y + cube.yMax();
		double renderMinZ = z + cube.zMin();
		double renderMaxZ = z + cube.zMax();
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMaxZ, renderU1, renderV1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMinZ, renderU2, renderV2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight, this.colorBlueBottomRight);
			tessellator.addVertexWithUV(renderMinX, renderY, renderMinZ, renderU3, renderV3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.addVertexWithUV(renderMinX, renderY, renderMaxZ, renderU4, renderV4);
		} else {
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMaxZ, renderU1, renderV1);
			tessellator.addVertexWithUV(renderMaxX, renderY, renderMinZ, renderU2, renderV2);
			tessellator.addVertexWithUV(renderMinX, renderY, renderMinZ, renderU3, renderV3);
			tessellator.addVertexWithUV(renderMinX, renderY, renderMaxZ, renderU4, renderV4);
		}
	}
	@Unique
	public void renderNorthFace(BlockCube cube, Block block, double x, double y, double z, int texture) {
		BlockFace face = cube.getFaceFromSide(Side.NORTH);
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texture = this.overrideBlockTexture;
		}
		int texX = texture % Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		int texY = texture / Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		double renderU2 = ((double)texX + face.uMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderU4 = ((double)texX + face.uMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMax() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMin() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		if (this.flipTexture) {
			double d7 = renderU2;
			renderU2 = renderU4;
			renderU4 = d7;
		}
		if (face.uMin() < 0.0 || face.uMax() > 1.0) {
			renderU2 = ((float)texX + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((float)texX + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		if (face.vMin() < 0.0 || face.vMax() > 1.0) {
			renderV2 = ((float)texY + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((float)texY + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		double renderU1 = renderU4;
		double renderU3 = renderU2;
		double renderV1 = renderV2;
		double renderV3 = renderV4;
		if (this.uvRotateEast == 2) {
			renderU2 = ((double)texX + block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)texX + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = renderV2;
			renderV3 = renderV4;
			renderU1 = renderU2;
			renderU3 = renderU4;
			renderV2 = renderV4;
			renderV4 = renderV1;
		} else if (this.uvRotateEast == 1) {
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)texY + block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)texY + block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = renderU4;
			renderU3 = renderU2;
			renderU2 = renderU1;
			renderU4 = renderU3;
			renderV1 = renderV4;
			renderV3 = renderV2;
		} else if (this.uvRotateEast == 3) {
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)texY + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)texY + block.minY * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = renderU4;
			renderU3 = renderU2;
			renderV1 = renderV2;
			renderV3 = renderV4;
		}
		double renderXMin = x + cube.xMin();
		double renderXMax = x + cube.xMax();
		double renderYMin = y + cube.yMin();
		double renderYMax = y + cube.yMax();
		double renderZ = z + cube.zMin();
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.addVertexWithUV(renderXMin, renderYMax, renderZ, renderU1, renderV1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.addVertexWithUV(renderXMax, renderYMax, renderZ, renderU2, renderV2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight, this.colorBlueBottomRight);
			tessellator.addVertexWithUV(renderXMax, renderYMin, renderZ, renderU3, renderV3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.addVertexWithUV(renderXMin, renderYMin, renderZ, renderU4, renderV4);
		} else {
			tessellator.addVertexWithUV(renderXMin, renderYMax, renderZ, renderU1, renderV1);
			tessellator.addVertexWithUV(renderXMax, renderYMax, renderZ, renderU2, renderV2);
			tessellator.addVertexWithUV(renderXMax, renderYMin, renderZ, renderU3, renderV3);
			tessellator.addVertexWithUV(renderXMin, renderYMin, renderZ, renderU4, renderV4);
		}
	}
	@Unique
	public void renderSouthFace(BlockCube cube, Block block, double x, double y, double z, int texture) {
		BlockFace face = cube.getFaceFromSide(Side.SOUTH);
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texture = this.overrideBlockTexture;
		}
		int texX = texture % Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		int texY = texture / Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		double renderU1 = ((double)texX + face.uMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderU3 = ((double)texX + face.uMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV1 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMax() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV3 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMin() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		if (this.flipTexture) {
			double d7 = renderU1;
			renderU1 = renderU3;
			renderU3 = d7;
		}
		if (face.uMin() < 0.0 || face.uMax() > 1.0) {
			renderU1 = ((float)texX + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = ((float)texX + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		if (face.vMin() < 0.0 || face.vMax() > 1.0) {
			renderV1 = ((float)texY + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = ((float)texY + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		double renderU4 = renderU3;
		double renderU2 = renderU1;
		double renderV4 = renderV1;
		double renderV2 = renderV3;
		if (this.uvRotateWest == 1) {
			renderU1 = ((double)texX + block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = ((double)texX + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = renderV1;
			renderV2 = renderV3;
			renderU4 = renderU1;
			renderU2 = renderU3;
			renderV1 = renderV3;
			renderV3 = renderV4;
		} else if (this.uvRotateWest == 2) {
			renderU1 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = ((double)texY + block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = ((double)texY + block.maxX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = renderU3;
			renderU2 = renderU1;
			renderU1 = renderU4;
			renderU3 = renderU2;
			renderV4 = renderV3;
			renderV2 = renderV1;
		} else if (this.uvRotateWest == 3) {
			renderU1 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minX * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxX * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = ((double)texY + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = ((double)texY + block.minY * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = renderU3;
			renderU2 = renderU1;
			renderV4 = renderV1;
			renderV2 = renderV3;
		}
		double renderXMin = x + cube.xMin();
		double renderXMax = x + cube.xMax();
		double renderYMin = y + cube.yMin();
		double renderYMax = y + cube.yMax();
		double renderZ = z + cube.zMax();
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.addVertexWithUV(renderXMin, renderYMax, renderZ, renderU1, renderV1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.addVertexWithUV(renderXMin, renderYMin, renderZ, renderU2, renderV2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight, this.colorBlueBottomRight);
			tessellator.addVertexWithUV(renderXMax, renderYMin, renderZ, renderU3, renderV3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.addVertexWithUV(renderXMax, renderYMax, renderZ, renderU4, renderV4);
		} else {
			tessellator.addVertexWithUV(renderXMin, renderYMax, renderZ, renderU1, renderV1);
			tessellator.addVertexWithUV(renderXMin, renderYMin, renderZ, renderU2, renderV2);
			tessellator.addVertexWithUV(renderXMax, renderYMin, renderZ, renderU3, renderV3);
			tessellator.addVertexWithUV(renderXMax, renderYMax, renderZ, renderU4, renderV4);
		}
	}
	@Unique
	public void renderWestFace(BlockCube cube, Block block, double x, double y, double z, int texture) {
		BlockFace face = cube.getFaceFromSide(Side.WEST);
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texture = this.overrideBlockTexture;
		}
		int texX = texture % Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		int texY = texture / Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		double renderU2 = ((double)texX + face.uMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderU4 = ((double)texX + face.uMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMax() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMin() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		if (this.flipTexture) {
			double d7 = renderU2;
			renderU2 = renderU4;
			renderU4 = d7;
		}
		if (face.uMin() < 0.0 || face.uMax() > 1.0) {
			renderU2 = ((float)texX + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((float)texX + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		if (face.vMin() < 0.0 || face.vMax() > 1.0) {
			renderV2 = ((float)texY + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((float)texY + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		double renderU1 = renderU4;
		double renderU3 = renderU2;
		double renderV1 = renderV2;
		double renderV3 = renderV4;
		if (this.uvRotateNorth == 1) {
			renderU2 = ((double)texX + block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)texX + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV1 = renderV2;
			renderV3 = renderV4;
			renderU1 = renderU2;
			renderU3 = renderU4;
			renderV2 = renderV4;
			renderV4 = renderV1;
		} else if (this.uvRotateNorth == 2) {
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)texY + block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)texY + block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = renderU4;
			renderU3 = renderU2;
			renderU2 = renderU1;
			renderU4 = renderU3;
			renderV1 = renderV4;
			renderV3 = renderV2;
		} else if (this.uvRotateNorth == 3) {
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)texY + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)texY + block.minY * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU1 = renderU4;
			renderU3 = renderU2;
			renderV1 = renderV2;
			renderV3 = renderV4;
		}
		double renderX = x + cube.xMin();
		double renderYMin = y + cube.yMin();
		double renderYMax = y + cube.yMax();
		double renderZMin = z + cube.zMin();
		double renderZMax = z + cube.zMax();
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMax, renderU1, renderV1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMin, renderU2, renderV2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight, this.colorBlueBottomRight);
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMin, renderU3, renderV3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMax, renderU4, renderV4);
		} else {
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMax, renderU1, renderV1);
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMin, renderU2, renderV2);
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMin, renderU3, renderV3);
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMax, renderU4, renderV4);
		}
	}
	@Unique
	public void renderEastFace(BlockCube cube, Block block, double x, double y, double z, int texture) {
		BlockFace face = cube.getFaceFromSide(Side.EAST);
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texture = this.overrideBlockTexture;
		}
		int texX = texture % Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		int texY = texture / Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		double renderU4 = ((double)texX + face.uMin() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderU2 = ((double)texX + face.uMax() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMax() * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		double renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - face.vMin() * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		if (this.flipTexture) {
			double d7 = renderU4;
			renderU4 = renderU2;
			renderU2 = d7;
		}
		if (face.uMin() < 0.0 || face.uMax() > 1.0) {
			renderU4 = ((float)texX + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU2 = ((float)texX + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		if (face.vMin() < 0.0 || face.vMax() > 1.0) {
			renderV4 = ((float)texY + 0.0f) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((float)texY + ((float)TextureFX.tileWidthTerrain - 0.01f)) / (float)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
		}
		double renderU3 = renderU2;
		double renderU1 = renderU4;
		double renderV3 = renderV4;
		double renderV1 = renderV2;
		if (this.uvRotateSouth == 2) {
			renderU4 = ((double)texX + block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)(texY + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU2 = ((double)texX + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)(texY + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV3 = renderV4;
			renderV1 = renderV2;
			renderU3 = renderU4;
			renderU1 = renderU2;
			renderV4 = renderV2;
			renderV2 = renderV3;
		} else if (this.uvRotateSouth == 1) {
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)texY + block.maxZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)texY + block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = renderU2;
			renderU1 = renderU4;
			renderU4 = renderU3;
			renderU2 = renderU1;
			renderV3 = renderV2;
			renderV1 = renderV4;
		} else if (this.uvRotateSouth == 3) {
			renderU4 = ((double)(texX + TextureFX.tileWidthTerrain) - block.minZ * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU2 = ((double)(texX + TextureFX.tileWidthTerrain) - block.maxZ * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV4 = ((double)texY + block.maxY * (double)TextureFX.tileWidthTerrain) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderV2 = ((double)texY + block.minY * (double)TextureFX.tileWidthTerrain - 0.01) / (double)(TextureFX.tileWidthTerrain * Global.TEXTURE_ATLAS_WIDTH_TILES);
			renderU3 = renderU2;
			renderU1 = renderU4;
			renderV3 = renderV4;
			renderV1 = renderV2;
		}
		double renderX = x + cube.xMax();
		double renderYMin = y + cube.yMin();
		double renderYMax = y + cube.yMax();
		double renderZMin = z + cube.zMin();
		double renderZMax = z + cube.zMax();
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMax, renderU1, renderV1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMin, renderU2, renderV2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight, this.colorBlueBottomRight);
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMin, renderU3, renderV3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMax, renderU4, renderV4);
		} else {
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMax, renderU1, renderV1);
			tessellator.addVertexWithUV(renderX, renderYMin, renderZMin, renderU2, renderV2);
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMin, renderU3, renderV3);
			tessellator.addVertexWithUV(renderX, renderYMax, renderZMax, renderU4, renderV4);
		}
	}
	@Unique
	public boolean renderStandardModelWithColorMultiplier(BlockModel model, Block block, int x, int y, int z, float r, float g, float b) {
		this.enableAO = false;
		int meta = this.blockAccess.getBlockMetadata(x, y, z);
		Tessellator tessellator = Tessellator.instance;
		boolean renderedSomething = false;
		float cBottom = 0.5f;
		float cTop = 1.0f;
		float cNorthSouth = 0.8f;
		float cEastWest = 0.6f;
		float rTop = cTop * r;
		float gTop = cTop * g;
		float bTop = cTop * b;
		float rBottom = cBottom;
		float rNorthSouth = cNorthSouth;
		float rEastWest = cEastWest;
		float gBottom = cBottom;
		float gNorthSouth = cNorthSouth;
		float gEastWest = cEastWest;
		float bBottom = cBottom;
		float bNorthSouth = cNorthSouth;
		float bEastWest = cEastWest;
		rBottom *= r;
		rNorthSouth *= r;
		rEastWest *= r;
		gBottom *= g;
		gNorthSouth *= g;
		gEastWest *= g;
		bBottom *= b;
		bNorthSouth *= b;
		bEastWest *= b;
		float blockBrightness = this.getBlockBrightness(this.blockAccess, x, y, z);
		for (BlockCube cube: model.elements) {
			for (Side side: DragonFly.sides) {
				if (cube.getFaceFromSide(side) == null) continue;
				int _x = x + side.getOffsetX();
				int _y = y + side.getOffsetY();
				int _z = z + side.getOffsetZ();

				if (!this.renderAllFaces){
					if (!renderSide(model, cube, side, block.shouldSideBeRendered(this.blockAccess, _x, _y, _z, side.getId(), meta))) continue;
				}

				float sideBrightness;
				if (!cube.isOuterFace(side) && !block.blockMaterial.isLiquid()){
					sideBrightness = blockBrightness;
				} else {
					sideBrightness = getBlockBrightness(this.blockAccess, _x, _y, _z);
				}

				float red;
				float green;
				float blue;

				switch (side){
					case TOP:
						red = rTop;
						green = gTop;
						blue = bTop;
						break;
					case BOTTOM:
						red = rBottom;
						green = gBottom;
						blue = bBottom;
						break;
					case NORTH:
					case SOUTH:
						red = rNorthSouth;
						green = gNorthSouth;
						blue = bNorthSouth;
						break;
					case WEST:
					case EAST:
						red = rEastWest;
						green = gEastWest;
						blue = bEastWest;
						break;
					default:
						throw new RuntimeException("Specified side does not exist on a cube!!!");
				}
				tessellator.setColorOpaque_F(red * sideBrightness, green * sideBrightness, blue * sideBrightness);
				renderModelFaceBySide(cube, side, block, x, y, z, block.getBlockTexture(this.blockAccess, x, y, z, side));
				renderedSomething = true;
			}
		}
		return renderedSomething;
	}
	@Unique
	public void renderModelFaceBySide(BlockCube cube, Side side, Block block, double x, double y, double z, int texture){
		texture = TextureRegistry.getIndexOrDefault(cube.getFaceFromSide(side).texture, texture);
		switch (side){
			case TOP:
				renderTopFace(cube, block, x, y, z, texture);
				break;
			case BOTTOM:
				renderBottomFace(cube, block, x, y, z, texture);
				break;
			case NORTH:
				renderNorthFace(cube, block, x, y, z, texture);
				break;
			case SOUTH:
				renderSouthFace(cube, block, x, y, z, texture);
				break;
			case WEST:
				renderWestFace(cube, block, x, y, z, texture);
				break;
			case EAST:
				renderEastFace(cube, block, x, y, z, texture);
				break;
			default:
				throw new RuntimeException("Specified side does not exist on a cube!!!");
		}
	}
}
