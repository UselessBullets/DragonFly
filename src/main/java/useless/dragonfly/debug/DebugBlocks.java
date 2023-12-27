package useless.dragonfly.debug;

import net.minecraft.client.render.block.color.BlockColorWater;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockStairs;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.item.Item;
import turniplabs.halplibe.helper.BlockBuilder;
import turniplabs.halplibe.helper.ItemHelper;
import turniplabs.halplibe.helper.TextureHelper;
import useless.dragonfly.debug.block.BlockModel;
import useless.dragonfly.debug.block.metastates.StairsMetaStateInterpreter;
import useless.dragonfly.helper.ModelHelper;
import useless.dragonfly.model.block.BlockModelDragonFly;
import useless.dragonfly.model.item.data.ItemModelData;
import useless.dragonfly.utilities.NamespaceId;
import useless.dragonfly.utilities.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static useless.dragonfly.DragonFly.MOD_ID;

public class DebugBlocks {
	private static int blockId = 1000;
	private static int itemID = 19000;
public static final Block testBlock = new BlockBuilder(MOD_ID)
	.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/TestBlock.json")))
	.build(new BlockModel("testblock" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/TestBlock.json")));
	public static final Block testBlock2 = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/TestBlock2.json")))
		.build(new BlockModel("testblock" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/TestBlock2.json")));
	public static final Block testBlock3 = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/TestBlock3.json")))
		.build(new BlockModel("testblock" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/TestBlock3.json")));
	public static final Block testBlock6 = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/directionPyramid.json")))
		.build(new BlockModel("testblock" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/directionPyramid.json")));
	public static final Block testBlock7 = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/stool.json")))
		.build(new BlockModel("testblock" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/stool.json")));
	public static final Block harris = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/harris.json")))
		.build(new BlockModel("testblock" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/harris.json")));
	public static final Block slope = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/slope.json")))
		.build(new BlockModel("testblock" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/slope.json")));
	public static final Block stairs = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(NamespaceId.coreNamespaceId, "block/cut_copper_stairs.json"),
			ModelHelper.getOrCreateBlockState(MOD_ID, "test_stairs.json"), new StairsMetaStateInterpreter(), true, 0.25f))
		.build(new BlockStairs(Block.dirt,blockId++)).withLitInteriorSurface(true);
	public static final Block trel = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/bean_trellis_bottom_0.json")))
		.build(new BlockModel("trel" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/bean_trellis_bottom_0.json")));
	public static final Block trel1 = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/bean_trellis_bottom_1.json")))
		.build(new BlockModel("trel" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/bean_trellis_bottom_1.json")));
	public static final Block trel2 = new BlockBuilder(MOD_ID)
		.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(MOD_ID, "block/bean_trellis_bottom_2.json")))
		.build(new BlockModel("trel" + blockId, blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(MOD_ID, "block/bean_trellis_bottom_2.json")));

	public static void init() {
		try {
			for (String string : getResourceFiles("assets/minecraft/model/block/")) {
				System.out.println(string);
				if (string.contains("cauldron")){
					new BlockBuilder(MOD_ID)
						.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(NamespaceId.coreNamespaceId, "block/" + string)))
						.setBlockColor(new BlockColorWater())
						.build(new BlockModel(string.replace(".json", ""), blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(NamespaceId.coreNamespaceId, "block/" + string)));
				} else {
					new BlockBuilder(MOD_ID)
						.setBlockModel(new BlockModelDragonFly(ModelHelper.getOrCreateBlockModel(NamespaceId.coreNamespaceId, "block/" + string)))
						.setHardness(1)
						.build(new BlockModel(string.replace(".json", ""), blockId++, Material.dirt, ModelHelper.getOrCreateBlockModel(NamespaceId.coreNamespaceId, "block/" + string)));
				}

				System.out.println(string + " created");
			}
			for (String string : getResourceFiles("assets/minecraft/blockstates/")) {
				System.out.println(string);
				try {
					System.out.println(ModelHelper.getOrCreateBlockState(NamespaceId.coreNamespaceId, string));
				}
				catch (Exception e){
					System.out.println(e);
				}
			}
			for (String string : getResourceFiles("assets/minecraft/model/item/")) {
				System.out.println(string);
				ItemModelData data = ModelHelper.getOrCreateItemModel(NamespaceId.coreNamespaceId, "item/" + string);
				int[] tex = new int[]{4, 10};
				if (data.textures.containsKey("layer0")){
					String texString = data.textures.get("layer0");
					if (texString.contains(":item/")){
						tex = TextureHelper.getOrCreateItemTexture(NamespaceId.coreNamespaceId, texString.replace("minecraft:item/", "").replace("minecraft:block/", "") + ".png");
					} else if (texString.contains(":block/")) {
						tex = TextureHelper.getOrCreateBlockTexture(NamespaceId.coreNamespaceId, texString.replace("minecraft:item/", "").replace("minecraft:block/", "") + ".png");
					}

				}
				ModelHelper.setItemModel(ItemHelper.createItem(MOD_ID, new Item(string.replace(".json", ""),itemID++), string.replace(".json", "")).setIconCoord(tex[0], tex[1]), data);
				System.out.println(string + " created");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	private static List<String> getResourceFiles(String path) throws IOException {
		List<String> filenames = new ArrayList<>();

		try (
			InputStream in = Utilities.getResourceAsStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String resource;

			while ((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
		}

		return filenames;
	}
}
