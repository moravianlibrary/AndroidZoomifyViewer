package cz.mzk.androidzoomifyviewer.tiles;

/**
 * @author Martin Řehánek
 * 
 */
public class ImageProperties {

	private final int width;
	private final int height;
	private final int numtiles; // pro kontrolu
	private final int tileSize;
	private final int level;

	public ImageProperties(int width, int height, int numtiles, int tilesize) {
		this.width = width;
		this.height = height;
		this.numtiles = numtiles;
		this.tileSize = tilesize;
		int xTiles = (int) Math.ceil(width/tileSize);
		int yTiles = (int) Math.ceil(height/tileSize);
		this.level = xTiles * yTiles;
	}
	
//	public int getWidthInMaxZoomEstimate(){
//		int xTiles = (int) Math.ceil(width/tileSize);
//		
//		
//	}
	
	public int getLevel(){
		return level;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getNumtiles() {
		return numtiles;
	}

	public int getTileSize() {
		return tileSize;
	}

	public Orientation getOrientation() {
		if (width > height) {
			return Orientation.LANDSCAPE;
		} else {
			return Orientation.PORTRAIT;
		}
	}

	@Override
	public String toString() {
		return "ImageProperties [width=" + width + ", height=" + height + ", numtiles=" + numtiles + ", tileSize="
				+ tileSize + "]";
	}

}
