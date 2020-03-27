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
	 * Constructs new RenObject:
	 * 	The RenObject is base for every element placed in a RenScene besides lights. It contains a name, uuid, position and rotation of an object.
	 * @param name - name of RenObject
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
	 * generates a new rotation-matrix around the x-Axis
	 * @param angleX - angle of rotation
	 * @return returns new rotation-x-matrix
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
	 * generates a new rotation-matrix around the z-Axis
	 * @param angleZ - angle of rotation
	 * @return returns new rotation-z-matrix
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
	 * generates a new rotation-matrix around the y-Axis
	 * @param angleY - angle of rotation
	 * @return returns new rotation-y-matrix
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
	 * sets angle of rotation around the x-Axis and updates rotation-x-matrix
	 * @param angleX - angle of rotation
	 */
	public void setAngleX(double angleX) {
		this.angleX = angleX % 360;
		this.rotXMat = generateRotXMat(Math.toRadians(angleX % 360));
	}

	/**
	 * sets angle of rotation around the y-Axis and updates rotation-y-matrix
	 * @param angleY - angle of rotation
	 */
	public void setAngleY(double angleY) {
		this.angleY = angleY % 360;
		this.rotYMat = generateRotYMat(Math.toRadians(angleY % 360));
	}

	/**
	 * sets angle of rotation around the z-Axis and updates rotation-z-matrix
	 * @param angleZ - angle of rotation
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
