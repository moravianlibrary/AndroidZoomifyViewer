package cz.mzk.androidzoomifyviewer.tiles;

/**
 * @author Martin Řehánek
 * 
 */
public class TileId {

	private final int layer;
	private final int x;
	private final int y;

	public TileId(int layer, int x, int y) {
		super();
		this.layer = layer;
		this.x = x;
		this.y = y;
	}

	public int getLayer() {
		return layer;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String toString() {
		return "" + layer + ':' + x + ':' + y;
	}

	public static TileId valueOf(String string) {
		String[] tokens = string.split(":");
		int layer = Integer.valueOf(tokens[0]);
		int x = Integer.valueOf(tokens[1]);
		int y = Integer.valueOf(tokens[2]);
		return new TileId(layer, x, y);
	}

}
