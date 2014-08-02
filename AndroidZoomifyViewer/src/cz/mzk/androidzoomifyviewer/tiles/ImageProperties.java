package cz.mzk.androidzoomifyviewer.tiles;

/**
 * @author Martin Řehánek
 * 
 */
public class ImageProperties {

	private final int width;
	private final int height;
	private final int numtiles; // pro kontrolu
	private final int numimages; // na nic
	private final String version; // na nic (vzdy 1.8)
	private final int tileSize;

	public ImageProperties(int width, int height, int numtiles, int numimages, String version, int tileSize) {
		this.width = width;
		this.height = height;
		this.numtiles = numtiles;
		this.numimages = numimages;
		this.version = version;
		this.tileSize = tileSize;
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

	public int getNumimages() {
		return numimages;
	}

	public String getVersion() {
		return version;
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
		return "ImageProperties [width=" + width + ", height=" + height + ", numtiles=" + numtiles + ", numimages="
				+ numimages + ", version=" + version + ", tileSize=" + tileSize + "]";
	}
	
	
	
	

}
