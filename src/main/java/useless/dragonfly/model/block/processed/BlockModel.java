package useless.dragonfly.model.block.processed;

import net.minecraft.core.util.helper.Side;
import useless.dragonfly.DragonFly;
import useless.dragonfly.helper.ModelHelper;
import useless.dragonfly.model.block.data.ModelData;
import useless.dragonfly.registries.TextureRegistry;

import java.util.HashMap;

public class BlockModel {
	public boolean[] hasFaceToRenderOnSide;
	public BlockCube[] blockCubes;
	protected ModelData modelData;
	protected BlockModel parentModel;
	protected HashMap<String, String> textureMap = new HashMap<>();
	public boolean hasFaceToRender(Side side){
		return hasFaceToRenderOnSide[side.getId()];
	}
	public BlockModel(ModelData modelData){
		this.modelData = modelData;

		if (modelData.parent != null && !modelData.parent.equals("block/block")){
			String namespace;
			String modelName;
			if (modelData.parent.contains(":")){
				namespace = modelData.parent.split(":")[0];
				modelName = modelData.parent.split(":")[1];
			} else {
				namespace = TextureRegistry.coreNamepaceId;
				modelName = modelData.parent;
			}
			parentModel = ModelHelper.getOrCreateBlockModel(namespace, modelName );

			textureMap.putAll(parentModel.textureMap);
		}

		textureMap.putAll(modelData.textures);

		loadModel();
	}
	protected void loadModel(){

		if (parentModel != null && modelData.elements == null){
			this.blockCubes = parentModel.blockCubes;
		} else {
			this.blockCubes = new BlockCube[modelData.elements.length];
			for (int i = 0; i < blockCubes.length; i++) {
				blockCubes[i] = new BlockCube(modelData.elements[i]);
			}
		}

		initializeTextures();

		hasFaceToRenderOnSide = new boolean[6];
		for (BlockCube cube: blockCubes) {
			cube.process();
			for (int i = 0; i < hasFaceToRenderOnSide.length; i++) {
				hasFaceToRenderOnSide[i] |= cube.isOuterFace(Side.getSideById(i));
			}
		}
		for (BlockCube cube: blockCubes) {
			cube.processVisibleFaces(this);
		}
	}
	protected void initializeTextures(){
		for (String texture: modelData.textures.values()) {
			if (TextureRegistry.containsTexture(texture)) continue;
			String[] nameSpaceSplit = texture.split(":");
			if (nameSpaceSplit[0].equals(TextureRegistry.coreNamepaceId) || nameSpaceSplit.length != 2) continue;

			String[] dirSplit = nameSpaceSplit[1].split("/");
			if (dirSplit[0].equals("block")){
				TextureRegistry.registerModBlockTexture(nameSpaceSplit[0], nameSpaceSplit[1].replace("block/", ""));
			}
			if (dirSplit[0].equals("item")){
				TextureRegistry.registerModItemTexture(nameSpaceSplit[0], nameSpaceSplit[1].replace("block/", ""));
			}
		}
	}
	public String getTexture(String faceTexKey){
		String result = textureMap.get(faceTexKey.substring(1));
		if (result != null && result.contains("#")){
			return getTexture(result);
		}
		return result;
	}
	public boolean getAO(){
		return modelData.ambientocclusion;
	}
}