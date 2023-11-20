package useless.dragonfly.mixins.mixin;

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
import useless.dragonfly.mixins.mixininterfaces.ExtraRendering;
import useless.dragonfly.model.block.processed.BlockCube;
import useless.dragonfly.model.block.processed.BlockFace;
import useless.dragonfly.model.block.processed.BlockModel;
import useless.dragonfly.registries.TextureRegistry;

import static useless.dragonfly.DragonFly.terrainAtlasWidth;

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
	private boolean flipTexture;
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
		for (BlockCube cube: modelDragonFly.baseModel.blockCubes) {
			for (BlockFace face: cube.faces.values()) {
				tessellator.startDrawingQuads();
				tessellator.setNormal(face.getSide().getOffsetX(), face.getSide().getOffsetY(), face.getSide().getOffsetZ());
				renderModelFace(cube, face.getSide(), 0, 0, 0, TextureRegistry.getIndexOrDefault(modelDragonFly.baseModel.getTexture(cube.getFaceFromSide(face.getSide()).getTexture()), block.getBlockTextureFromSideAndMetadata(face.getSide(), meta)));
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
		if (mc.isAmbientOcclusionEnabled() && model.getAO()) {
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
		for (BlockCube cube: model.blockCubes) {
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
			renderModelFace( cube, side, x, y, z, TextureRegistry.getIndexOrDefault(model.getTexture(cube.getFaceFromSide(side).getTexture()),tex));
			return true;
		}
		return false;
	}
	@Unique
	public void renderModelFace(BlockCube cube, Side side, double x, double y, double z, int texture) {
		BlockFace face = cube.getFaceFromSide(side);
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texture = this.overrideBlockTexture;
		}
		int texX = texture % Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		int texY = texture / Global.TEXTURE_ATLAS_WIDTH_TILES * TextureFX.tileWidthTerrain;
		double atlasUMin = (texX + face.uMin() * TextureFX.tileWidthTerrain) / terrainAtlasWidth;
		double atlasUMax = (texX + face.uMax() * TextureFX.tileWidthTerrain - 0.01) / terrainAtlasWidth;
		double atlasVMin = (texY + (1 - face.vMin()) * TextureFX.tileWidthTerrain) / terrainAtlasWidth;
		double atlasVMax = (texY + (1 - face.vMax()) * TextureFX.tileWidthTerrain - 0.01) / terrainAtlasWidth;
		if (face.uMin() < 0.0 || face.uMax() > 1.0) { // Cap U value
			atlasUMin = texX / terrainAtlasWidth;
			atlasUMax = (texX + (TextureFX.tileWidthTerrain - 0.01f)) / terrainAtlasWidth;
		}
		if (face.vMin() < 0.0 || face.vMax() > 1.0) { // Cap V value
			atlasVMin = texY / terrainAtlasWidth;
			atlasVMax = (texY + (TextureFX.tileWidthTerrain - 0.01f)) / terrainAtlasWidth;
		}
		double[] uvTL = face.getVertexUV(atlasUMin, atlasVMin, atlasUMax, atlasVMax, 0);
		double[] uvBL = face.getVertexUV(atlasUMin, atlasVMin, atlasUMax, atlasVMax, 1);
		double[] uvBR = face.getVertexUV(atlasUMin, atlasVMin, atlasUMax, atlasVMax, 2);
		double[] uvTR = face.getVertexUV(atlasUMin, atlasVMin, atlasUMax, atlasVMax, 3);
		if (this.enableAO) {
			// Top Left
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.addVertexWithUV(x + face.vertices[0].x, y + face.vertices[0].y, z + face.vertices[0].z, uvTL[0], uvTL[1]);

			// Bottom Left
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.addVertexWithUV(x + face.vertices[1].x, y + face.vertices[1].y, z + face.vertices[1].z, uvBL[0], uvBL[1]);

			// Bottom Right
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight, this.colorBlueBottomRight);
			tessellator.addVertexWithUV(x + face.vertices[2].x, y + face.vertices[2].y, z + face.vertices[2].z, uvBR[0], uvBR[1]);

			// Top Right
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.addVertexWithUV(x + face.vertices[3].x, y + face.vertices[3].y, z + face.vertices[3].z, uvTR[0], uvTR[1]);
		} else {
			tessellator.addVertexWithUV(x + face.vertices[0].x, y + face.vertices[0].y, z + face.vertices[0].z, uvTL[0], uvTL[1]); // Top Left
			tessellator.addVertexWithUV(x + face.vertices[1].x, y + face.vertices[1].y, z + face.vertices[1].z, uvBL[0], uvBL[1]); // Bottom Left
			tessellator.addVertexWithUV(x + face.vertices[2].x, y + face.vertices[2].y, z + face.vertices[2].z, uvBR[0], uvBR[1]); // Bottom Right
			tessellator.addVertexWithUV(x + face.vertices[3].x, y + face.vertices[3].y, z + face.vertices[3].z, uvTR[0], uvTR[1]); // Top Right
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
		for (BlockCube cube: model.blockCubes) {
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
				renderModelFace(cube, side, x, y, z, TextureRegistry.getIndexOrDefault(model.getTexture(cube.getFaceFromSide(side).getTexture()), block.getBlockTexture(this.blockAccess, x, y, z, side)));
				renderedSomething = true;
			}
		}
		return renderedSomething;
	}
}
