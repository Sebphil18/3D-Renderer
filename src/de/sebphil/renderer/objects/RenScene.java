package de.sebphil.renderer.objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;

public class RenScene {

	private double ambient;
	private RenCamera camera;
	private ObservableList<RenShape> shapes;
	private ObservableList<Point3D> lights;

	/**
	 * Constructs new RenScene:
	 * 	A RenScene contains all objects placed in the 3D World including shapes, lights and the camera.
	 * 	It also has an value of the ambient lighting (ambient).
	 */
	public RenScene() {
		this.ambient = 0.05;
		this.camera = new RenCamera("camera");
		this.shapes = FXCollections.observableArrayList();
		this.lights = FXCollections.observableArrayList();
	}

	public double getAmbient() {
		return ambient;
	}

	public void setAmbient(double ambient) {
		this.ambient = ambient;
	}

	public RenCamera getCamera() {
		return camera;
	}

	public void setCamera(RenCamera camera) {
		this.camera = camera;
	}

	public ObservableList<RenShape> getShapes() {
		return shapes;
	}

	public ObservableList<Point3D> getLights() {
		return lights;
	}

}
