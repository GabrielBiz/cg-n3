package main;

import main.opengl.utils.ColorUtils;

public class Vertex {

	private static final int VERTEX_BBOX_SIZE = 10;
	private static final float[] VERTEX_BBOX_COLOR = ColorUtils.PUMPKIN_ORANGE.clone();

	private final Point4D point;
	private final BBox bbox;

	public Vertex(Point4D point, Transform transform) {
		this.point = point;
		int minX = point.getX() - VERTEX_BBOX_SIZE;
		int minY = point.getY() - VERTEX_BBOX_SIZE;
		int maxX = point.getX() + VERTEX_BBOX_SIZE;
		int maxY = point.getY() + VERTEX_BBOX_SIZE;
		this.bbox = new BBox(minX, minY, maxX, maxY, VERTEX_BBOX_COLOR, transform);
	}

	public Point4D getPoint() {
		return point;
	}

	public BBox bbox() {
		return bbox;
	}

	public boolean contains(Point4D point) {
		return bbox.contains(point);
	}

	public int getX() {
		return point.getX();
	}

	public int getY() {
		return point.getY();
	}
}
