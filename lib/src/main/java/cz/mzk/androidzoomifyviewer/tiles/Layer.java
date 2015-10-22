package cz.mzk.androidzoomifyviewer.tiles;

/**
 * @author Martin Řehánek
 * 
 */
public class Layer {

	private final int tilesVertical;
	private final int tilesHorizontal;

	public Layer(int tilesVertical, int tilesHorizontal) {
		this.tilesVertical = tilesVertical;
		this.tilesHorizontal = tilesHorizontal;
	}

	public int getTilesVertical() {
		return tilesVertical;
	}

	public int getTilesHorizontal() {
		return tilesHorizontal;
	}

}
