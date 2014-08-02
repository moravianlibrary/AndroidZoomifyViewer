package cz.mzk.androidzoomifyviewer.viewer;

/**
 * @author Martin Řehánek
 * 
 */
public class PointD {
	public final double x;
	public final double y;

	public PointD(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return String.format("[%.3f;%.3f]", x, y);
	}

}
