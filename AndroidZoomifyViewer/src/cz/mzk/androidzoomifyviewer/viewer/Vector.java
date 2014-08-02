package cz.mzk.androidzoomifyviewer.viewer;

/**
 * @author Martin Řehánek
 * 
 */
public class Vector {

	public static final Vector ZERO_VECTOR = new Vector(0, 0);
	public final int x;
	public final int y;

	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vector plus(int x, int y) {
		return new Vector(this.x + x, this.y + y);
	}

	public Vector plusX(int x) {
		return new Vector(this.x + x, this.y);
	}

	public Vector plusY(int y) {
		return new Vector(this.x, this.y + y);
	}

	public static Vector sum(Vector... vectors) {
		int x = 0;
		int y = 0;
		for (Vector vector : vectors) {
			x += vector.x;
			y += vector.y;
		}
		return new Vector(x, y);
	}
	
	public String toString() {
		return "[" + x + ";" + y + "]";
	}

}
