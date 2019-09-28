package de.sebphil.renderer.objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import de.sebphil.renderer.util.RenUtilities;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public class RenShape extends RenObject {

	private double maxX, maxY, maxZ, minX, minY, minZ;
	private double[][] sizeMat;
	private Point3D size, translation;
	private Color color;
	private List<RenTriangle> polys;

	public RenShape(String name) {
		super(name);
		this.color = Color.WHITE;
		this.sizeMat = new double[4][4];
		this.polys = new ArrayList<RenTriangle>();
		this.translation = new Point3D(0, 0, 0);
		this.size = new Point3D(1, 1, 1);
	}

	public RenShape(String name, Color color) {
		super(name);
		this.color = color;
		this.sizeMat = new double[4][4];
		this.polys = new ArrayList<RenTriangle>();
		this.translation = new Point3D(0, 0, 0);
		this.size = new Point3D(1, 1, 1);
	}

	public RenShape copy() {

		RenShape copyShape = new RenShape(getName() + "Copy", getColor());

		copyShape.setAngleX(getAngleX());
		copyShape.setAngleY(getAngleY());
		copyShape.setAngleZ(getAngleZ());

		copyShape.setPosition(getPosition());
		copyShape.setTranslation(getTranslation());
		copyShape.setSize(getSize());

		copyShape.setPolys(getPolys());

		copyShape.setMinX(getMinX());
		copyShape.setMinY(getMinY());
		copyShape.setMinZ(getMinZ());

		copyShape.setMaxX(getMaxX());
		copyShape.setMaxY(getMaxY());
		copyShape.setMaxZ(getMaxZ());

		return copyShape;
	}

	public void importObj(File file) {

		List<Point3D> vertecies = new ArrayList<Point3D>();
		List<RenTriangle> impPolys = new ArrayList<RenTriangle>();

		try {
			Scanner scanner = new Scanner(file);

			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();

				if (line.startsWith("v")) {

					String[] args = line.split(" ");

					vertecies.add(
							new Point3D(Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3])));

				} else if (line.startsWith("f")) {

					String[] args = line.split(" ");

					RenTriangle tri = new RenTriangle(vertecies.get(Integer.valueOf(args[1]) - 1),
							vertecies.get(Integer.valueOf(args[2]) - 1), vertecies.get(Integer.valueOf(args[3]) - 1));

					impPolys.add(tri);

					for (int i = 0; i < 3; i++) {

						Point3D v = tri.getVert()[i];
						if (v.getX() > maxX)
							maxX = v.getX();
						else if (v.getX() < minX)
							minX = v.getX();

						if (v.getY() > maxY)
							maxY = v.getY();
						else if (v.getY() < minY)
							minY = v.getY();

						if (v.getZ() > maxZ)
							maxZ = v.getZ();
						else if (v.getZ() < minZ)
							minZ = v.getZ();

					}
				}
			}

			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.polys.addAll(impPolys);
	}

	protected static double[][] generateSizeMat(Point3D size) {
		double[][] sizeMat = new double[4][4];
		sizeMat[0][0] = size.getX();
		sizeMat[1][1] = size.getY();
		sizeMat[2][2] = size.getZ();
		sizeMat[3][3] = 1.0;
		sizeMat[3][0] = 1.0;
		sizeMat[3][1] = 1.0;
		sizeMat[3][2] = 1.0;
		return sizeMat;
	}

	protected static double[][] generateTransMat(Point3D translation) {
		double[][] transMat = new double[4][4];
		transMat[0][0] = 1.0;
		transMat[1][1] = 1.0;
		transMat[2][2] = 1.0;
		transMat[3][3] = 1.0;
		transMat[3][0] = translation.getX();
		transMat[3][1] = translation.getY();
		transMat[3][2] = translation.getZ();
		return transMat;
	}

	protected static double[][] generateTransformationMat(RenShape shape) {
		double[][] transf = RenUtilities.multMatMat(RenObject.generateRotZMat(Math.toRadians(shape.getAngleZ())),
				RenObject.generateRotYMat(Math.toRadians(shape.getAngleY())));
		transf = RenUtilities.multMatMat(transf, RenObject.generateRotXMat(Math.toRadians(shape.getAngleX())));
		transf = RenUtilities.multMatMat(RenShape.generateTransMat(shape.getTranslation()), transf);
		return transf;
	}

	public void setSize(Point3D size) {
		this.size = size;
		this.sizeMat = generateSizeMat(size);
	}

	public double[][] getSizeMat() {
		return sizeMat;
	}

	public void setSizeMat(double[][] sizeMat) {
		this.sizeMat = sizeMat;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		for(RenTriangle tri : getPolys()) {
			tri.setColor(color);
		}
	}

	public List<RenTriangle> getPolys() {
		return polys;
	}

	public void setPolys(List<RenTriangle> polys) {
		this.polys = polys;
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMaxZ() {
		return maxZ;
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMinZ() {
		return minZ;
	}

	public Point3D getSize() {
		return size;
	}

	public Point3D getTranslation() {
		return translation;
	}

	public void setTranslation(Point3D translation) {
		this.translation = translation;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	public void setMaxZ(double maxZ) {
		this.maxZ = maxZ;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public void setMinZ(double minZ) {
		this.minZ = minZ;
	}

}
