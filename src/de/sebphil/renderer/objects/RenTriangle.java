package de.sebphil.renderer.objects;

import java.util.Arrays;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public class RenTriangle {

	private Point3D[] vert;
	private Color color = Color.WHITE;

	public RenTriangle(Point3D v1, Point3D v2, Point3D v3) {
		vert = new Point3D[] { v1, v2, v3 };
	}

	public RenTriangle(Point3D v1, Point3D v2, Point3D v3, Color color) {
		vert = new Point3D[] { v1, v2, v3 };
		this.color = color;
	}

	public Point3D[] getVert() {
		return Arrays.copyOf(vert, vert.length);
	}

	public Point3D getV1() {
		return vert[0];
	}

	public void setV1(Point3D v1) {
		this.vert[0] = v1;
	}

	public Point3D getV2() {
		return vert[1];
	}

	public void setV2(Point3D v2) {
		this.vert[1] = v2;
	}

	public Point3D getV3() {
		return vert[2];
	}

	public void setV3(Point3D v3) {
		this.vert[2] = v3;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
