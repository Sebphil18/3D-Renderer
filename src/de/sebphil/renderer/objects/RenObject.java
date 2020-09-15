package de.sebphil.renderer.objects;

import java.util.UUID;

import javafx.geometry.Point3D;

public class RenObject {

	private double angleX, angleY, angleZ;
	private double[][] rotXMat, rotYMat, rotZMat, transMat;
	private String name;
	private Point3D position;
	private UUID uuid;

	/**
	 * Constructor für ein RenObject.
	 * Dabei werden alle Membervariablen mit einem validen Wert belegt.
	 * 
	 * @param name	Name des zu erzeugenden Objektes
	 */
	public RenObject(String name) {
		this.name = name;
		this.uuid = UUID.randomUUID();
		this.position = new Point3D(0, 0, 0);
		this.rotXMat = generateRotXMat(angleX);
		this.rotYMat = generateRotXMat(angleY);
		this.rotZMat = generateRotXMat(angleZ);
	}

	/**
	 * Diese Funktion generiert eine geeignete Rotationsmatrix um die x-Achse.
	 * 
	 * @param angleX	Grad der Rotation in Radiant.
	 * @return			Gibt eine neue Rotationsmatrix zurück.
	 */
	protected static double[][] generateRotXMat(double angleX) {

		double[][] rotMat = new double[4][4];
		rotMat[0][0] = 1;
		rotMat[1][1] = Math.cos(angleX);
		rotMat[1][2] = Math.sin(angleX);
		rotMat[2][1] = -Math.sin(angleX);
		rotMat[2][2] = Math.cos(angleX);
		rotMat[3][3] = 1;
		return rotMat;
	}

	/**
	 * Diese Funktion generiert eine geeignete Rotationsmatrix um die z-Achse.
	 * 
	 * @param angleZ	Grad der Rotation in Radiant.
	 * @return			Gibt eine neue Rotationsmatrix zurück.
	 */
	private static double[][] generateRotZMat(double angleZ) {

		double[][] rotMat = new double[4][4];
		rotMat[0][0] = Math.cos(angleZ);
		rotMat[0][1] = Math.sin(angleZ);
		rotMat[1][0] = -Math.sin(angleZ);
		rotMat[1][1] = Math.cos(angleZ);
		rotMat[2][2] = 1;
		rotMat[3][3] = 1;
		return rotMat;
	}
	
	/**
	 * Diese Funktion generiert eine geeignete Rotationsmatrix um die y-Achse.
	 * 
	 * @param angleY	Grad der Rotation in Radiant.
	 * @return			Gibt eine neue Rotationsmatrix zurück.
	 */
	private static double[][] generateRotYMat(double angleY) {

		double[][] rotMat = new double[4][4];
		rotMat[0][0] = Math.cos(angleY);
		rotMat[0][2] = Math.sin(angleY);
		rotMat[2][0] = -Math.sin(angleY);
		rotMat[1][1] = 1;
		rotMat[2][2] = Math.cos(angleY);
		rotMat[3][3] = 1;
		return rotMat;
	}

	/**
	 * Legt einen neuen Winkel für die Rotation um die x-Achse fest.
	 * Diese Funktion erzeugt eine neue Rotationsmatrix für die Rotation um 
	 * die x-Achse für diese Instanz.
	 * 
	 * @param angleX	Grad der Rotation in Gradmaß
	 */
	public void setAngleX(double angleX) {
		this.angleX = angleX % 360;
		this.rotXMat = generateRotXMat(Math.toRadians(angleX % 360));
	}

	/**
	 * Legt einen neuen Winkel für die Rotation um die y-Achse fest.
	 * Diese Funktion erzeugt eine neue Rotationsmatrix für die Rotation um 
	 * die y-Achse für diese Instanz.
	 * 
	 * @param angleY	Grad der Rotation in Gradmaß
	 */
	public void setAngleY(double angleY) {
		this.angleY = angleY % 360;
		this.rotYMat = generateRotYMat(Math.toRadians(angleY % 360));
	}

	/**
	 * Legt einen neuen Winkel für die Rotation um die z-Achse fest.
	 * Diese Funktion erzeugt eine neue Rotationsmatrix für die Rotation um 
	 * die z-Achse für diese Instanz.
	 * 
	 * @param angleZ	Grad der Rotation in Gradmaß
	 */
	public void setAngleZ(double angleZ) {
		this.angleZ = angleZ % 360;
		this.rotZMat = generateRotZMat(Math.toRadians(angleZ % 360));
	}

	public double getAngleX() {
		return angleX;
	}

	public double getAngleY() {
		return angleY;
	}

	public double getAngleZ() {
		return angleZ;
	}

	public double[][] getRotXMat() {
		return rotXMat;
	}

	public void setRotXMat(double[][] rotXMat) {
		this.rotXMat = rotXMat;
	}

	public double[][] getRotYMat() {
		return rotYMat;
	}

	public void setRotYMat(double[][] rotYMat) {
		this.rotYMat = rotYMat;
	}

	public double[][] getRotZMat() {
		return rotZMat;
	}

	public void setRotZMat(double[][] rotZMat) {
		this.rotZMat = rotZMat;
	}

	public double[][] getTransMat() {
		return transMat;
	}

	public void setTransMat(double[][] transMat) {
		this.transMat = transMat;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Point3D getPosition() {
		return position;
	}

	public void setPosition(Point3D position) {
		this.position = position;
	}

	public UUID getUuid() {
		return uuid;
	}

}
