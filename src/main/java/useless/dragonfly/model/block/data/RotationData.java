package useless.dragonfly.model.block.data;

import com.google.gson.annotations.SerializedName;

public class RotationData {
	@SerializedName("origin")
	public double[] origin = new double[3];
	@SerializedName("axis")
	public String axis = null;
	@SerializedName("angle")
	public float angle = 0;
	@SerializedName("rescale")
	public boolean rescale = false;
}
