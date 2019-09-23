package de.sebphil.renderer.objects;

import de.sebphil.renderer.util.RenUtilities;
import javafx.geometry.Point3D;

public class RenCamera extends RenObject {

	private double[][] camRotMat;
	private Point3D lookDir;
	private Point3D newRight, newUp;

	public RenCamera(String name) {
		super(name);

		setRotXMat(generateRotXMat(Math.toRadians(getAngleX())));
		setRotYMat(generateRotXMat(Math.toRadians(getAngleY())));
		setRotZMat(generateRotXMat(Math.toRadians(getAngleZ())));

		camRotMat = RenUtilities.multMatMat(getRotZMat(), getRotYMat());
		camRotMat = RenUtilities.multMatMat(camRotMat, getRotXMat());
	}

	public double[][] lookAt(Point3D to) {

		double[][] camWorldMat = new double[4][4];

		Point3D tmp = new Point3D(0, 1, 0);
		Point3D pos = getPosition();

		lookDir = RenUtilities.multMatVec(camRotMat, to);
		to = getPosition().add(lookDir);

		Point3D forward = to.subtract(getPosition()).normalize();
		Point3D right = tmp.crossProduct(forward);
		Point3D up = forward.crossProduct(right);

		Point3D newForward = to.subtract(pos).normalize();
		newUp = up.subtract(newForward.multiply(up.dotProduct(newForward))).normalize();
		newRight = newUp.crossProduct(newForward);

		if ((forward.getY() >= 0.9 && forward.getY() <= 1.1) && (forward.getY() >= -0.9 && forward.getY() <= -1.1)) {
			forward = new Point3D(forward.getX(), forward.getY() + 0.15, forward.getZ());
			System.out.println("forward corrected");
		}

		camWorldMat[0][0] = newRight.getX();
		camWorldMat[0][1] = newRight.getY();
		camWorldMat[0][2] = newRight.getZ();
		camWorldMat[0][3] = 0.0;
		camWorldMat[1][0] = newUp.getX();
		camWorldMat[1][1] = newUp.getY();
		camWorldMat[1][2] = newUp.getZ();
		camWorldMat[1][3] = 0.0;
		camWorldMat[2][0] = newForward.getX();
		camWorldMat[2][1] = newForward.getY();
		camWorldMat[2][2] = newForward.getZ();
		camWorldMat[2][3] = 0.0;
		camWorldMat[3][0] = pos.getX();
		camWorldMat[3][1] = pos.getY();
		camWorldMat[3][2] = pos.getZ();
		camWorldMat[3][3] = 1.0;

		double[][] camViewMat = RenUtilities.invertLookAtMat(camWorldMat);

		return camViewMat;
	}

	public double getYaw() {
		return getAngleY();
	}

	public double getPitch() {
		return getAngleX();
	}

	public void setYaw(double yaw) {
		setAngleY(yaw);
		setRotYMat(generateRotYMat(Math.toRadians(yaw % 360)));

		camRotMat = RenUtilities.multMatMat(getRotZMat(), getRotYMat());
		camRotMat = RenUtilities.multMatMat(camRotMat, getRotXMat());
	}

	public void setPitch(double pitch) {
		setAngleX(pitch);
		setRotXMat(generateRotXMat(Math.toRadians(pitch % 360)));

		camRotMat = RenUtilities.multMatMat(getRotZMat(), getRotYMat());
		camRotMat = RenUtilities.multMatMat(camRotMat, getRotXMat());
	}

	public double[][] getCamRotMat() {
		return camRotMat;
	}

	public void setCamRotMat(double[][] camRotMat) {
		this.camRotMat = camRotMat;
	}

	public Point3D getLookDir() {
		return lookDir;
	}

	public void setLookDir(Point3D lookDir) {
		this.lookDir = lookDir;
	}

	public Point3D getNewRight() {
		return newRight;
	}

	public void setNewRight(Point3D newRight) {
		this.newRight = newRight;
	}

	public Point3D getNewUp() {
		return newUp;
	}

	public void setNewUp(Point3D newUp) {
		this.newUp = newUp;
	}

}
