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
	private Point3D size, translation;
	private Color color;
	private List<RenTriangle> polys;

	/**
	 * Constructor für ein RenShape.
	 * 
	 * Diese Klasse erbt von der Klasse RenObject.
	 * @see de.sebphil.renderer.objects.RenObject
	 * 
	 * @param name	Name des zu erzeugenden Objektes
	 */
	public RenShape(String name) {
		super(name);
		this.color = Color.WHITE;
		this.polys = new ArrayList<RenTriangle>();
		this.translation = new Point3D(0, 0, 0);
		this.size = new Point3D(1, 1, 1);
	}

	/**
	 * Constructor für ein RenShape.
	 * 
	 * Diese Klasse erbt von der Klasse RenObject.
	 * @see de.sebphil.renderer.objects.RenObject
	 * 
	 * @param name	Name des zu erzeugenden Objektes
	 * @param color	Farbe, welche das gesamte Objekt haben wird
	 */
	public RenShape(String name, Color color) {
		super(name);
		this.color = color;
		this.polys = new ArrayList<RenTriangle>();
		this.translation = new Point3D(0, 0, 0);
		this.size = new Point3D(1, 1, 1);
	}

	/**
	 * Erstellt eine neue Kopie dieser Instanz mit einer neuen UUID und dem Namen: name + "Copy".
	 * 
	 * @return Kopie dieser Instanz
	 */
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

	/**
	 * Lädt einer einfachen Datei des Formates Wavefront (.obj) und lädt die Vertexdaten in diese Instanz.
	 * 
	 * @param file Datei, welche geladen werden soll
	 */
	public void importObj(File file) {
		
		/*
		 * vertices - Liste für die Koordinaten der einzelnen Eckpunkte
		 * impPolys - Liste für die Dreiecke, welche sich aus den Eckpunkten ergeben
		 */
		List<Point3D> vertices = new ArrayList<Point3D>();
		List<RenTriangle> impPolys = new ArrayList<RenTriangle>();

		try {
			
			Scanner scanner = new Scanner(file);

			/*
			 * Ermittle jede Zeile mit einem "v" oder "f" am Anfang und trage
			 * dementsprechend ein neuen Eckpunkt oder ein neues Dreieck in die
			 * jeweilige Liste ein.
			 */
			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();

				if (line.startsWith("v")) {
					
					//Trage einen neuen Eckpunkt in die Liste vertices ein.
					
					String[] args = line.split(" ");
					
					vertices.add(
							new Point3D(Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3])));

				} else if (line.startsWith("f")) {
					
					//Trage ein neues Dreieck in die Liste impPolys ein.
					
					String[] args = line.split(" ");

					RenTriangle tri = new RenTriangle(vertices.get(Integer.valueOf(args[1]) - 1),
							vertices.get(Integer.valueOf(args[2]) - 1), vertices.get(Integer.valueOf(args[3]) - 1));

					impPolys.add(tri);
					
					//Aktualisiere die Maximalwerte des Objektes, wenn nötig.
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

		//Füge alle importierten Dreiecke zu diesem RenShape hinzu.
		this.polys.addAll(impPolys);
	}
	
	/**
	 * Generiert eine neue Skalierungsmatrix.
	 * 
	 * @param size	Skalierung
	 * @return		Gibt eine neue Skalierungsmatrix mit der angegebenen Skalierung zurück
	 */
	protected static double[][] generateSizeMat(Point3D size) {
		double[][] sizeMat = new double[4][4];
		sizeMat[0][0] = size.getX();
		sizeMat[1][1] = size.getY();
		sizeMat[2][2] = size.getZ();
		sizeMat[3][3] = 1.0;
		return sizeMat;
	}

	/**
	 * Generiert eine neue Translationsmatrix.
	 * 
	 * @param translation	Translation
	 * @return				Gibt eine neue Trabslationsmatrix zurück mit der angegebenen Translation
	 */
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

	/**
	 * Generiert eine neue "Welt"-Matrix (Worldmatrix / Modelmatrix)
	 * 
	 * @param shape	RenShape, von welchem die "Welt"-Matrix erstellt werden soll
	 * @return		Gibt die "Welt"-Matrix des angegebenen RenShape zurück.
	 */
	protected static double[][] generateWorldMat(RenShape shape) {
		
		double[][] worldMat = RenUtilities.multMatMat(RenShape.generateTransMat(shape.getTranslation()), shape.getRotXMat());
		worldMat = RenUtilities.multMatMat(worldMat, shape.getRotYMat());
		worldMat = RenUtilities.multMatMat(worldMat, shape.getRotZMat());
		worldMat = RenUtilities.multMatMat(RenShape.generateSizeMat(shape.getSize()), worldMat);
		
		return worldMat;
	}

	public void setSize(Point3D size) {
		this.size = size;
	}

	public Color getColor() {
		return color;
	}
	
	/**
	 * Legt die Farbe dieses Objektes fest.
	 * (Legt die Farbe von allen Dreiecken, welche sich zur Zeit in der Liste "polys" befinden, fest.)
	 * 
	 * @param color Farbe
	 */
	public void setColor(Color color) {
		this.color = color;
		for (RenTriangle tri : getPolys()) {
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
