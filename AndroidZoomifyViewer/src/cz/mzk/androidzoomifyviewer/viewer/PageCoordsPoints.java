package cz.mzk.androidzoomifyviewer.viewer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Řehánek
 * 
 */
public class PageCoordsPoints {

	private final Point center;
	private final List<Point> corners;
	private final List<Point> clickedPoints;

	private Point initialZoomCenter;

	public PageCoordsPoints(int pageWidth, int pageHeight) {
		this.center = new Point(pageWidth / 2, pageHeight / 2);
		this.corners = initCorners(pageWidth, pageHeight);
		this.clickedPoints = new ArrayList<Point>();
	}

	private List<Point> initCorners(int pageWidth, int pageHeight) {
		List<Point> corners = new ArrayList<Point>();
		corners.add(new Point(0, 0));
		corners.add(new Point(pageWidth, 0));
		corners.add(new Point(pageWidth, pageHeight));
		corners.add(new Point(0, pageHeight));
		return corners;
	}

	public Point getCenter() {
		return center;
	}

	public List<Point> getCorners() {
		return corners;
	}

	public List<Point> getClickedPoints() {
		return clickedPoints;
	}

	public void addOtherPoint(Point point) {
		clickedPoints.add(point);
	}

	public Point getInitialZoomCenter() {
		return initialZoomCenter;
	}

	public void setInitialZoomCenter(Point initialZoomCenter) {
		this.initialZoomCenter = initialZoomCenter;
	}

}
