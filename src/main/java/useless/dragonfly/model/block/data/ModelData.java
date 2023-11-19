package useless.dragonfly.model.block.data;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.util.helper.Side;

import java.util.HashMap;

public class ModelData {
	@SerializedName("parent")
	public String parent = null; // Currently unused
	@SerializedName("ambientocclusion")
	public boolean ambientocclusion = true;
	@SerializedName("elements")
	public CubeData[] elements = new CubeData[0];
	@SerializedName("textures")
	public HashMap<String, String> textures = new HashMap<>();
	public static final HashMap<String, Side> keyToSide = new HashMap<>();
	public static final HashMap<Side, String> sideToKey = new HashMap<>();
	private static void registerSide(Side side, String key){
		keyToSide.put(key, side);
		sideToKey.put(side, key);
	}
	static {
		registerSide(Side.BOTTOM, "down");
		registerSide(Side.TOP, "up");
		registerSide(Side.NORTH, "north");
		registerSide(Side.SOUTH, "south");
		registerSide(Side.WEST, "west");
		registerSide(Side.EAST, "east");
	}
}
