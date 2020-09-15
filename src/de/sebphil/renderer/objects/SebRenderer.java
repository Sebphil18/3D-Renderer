package de.sebphil.renderer.objects;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import de.sebphil.renderer.util.RenUtilities;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class SebRenderer {

	private int[] framebuffer;
	private double near, far, fov, scale, width, height, aspectratio;
	private double[] depthBuffer;
	private double[][] projMat;
	private PixelFormat<IntBuffer> format;

	/**
	 * Constructor für den SebRenderer.
	 * 
	 * Diese Klasse ist für das Rendern von einer Szene sowie das Darstellen
	 * dieser verantwortlich. Sie verfügt über einen Frame- und Depthbuffer
	 * sowie Eigenschafter der Projektionsmatrix. Es wird die
	 * OpenGL-Projektionsmatrix verwendet.
	 * Beim Aufruf dieses Constructors werden geeignete Werte für diese Projektionsmatrix erzeugt
	 * sowie ein passender Frame- und Depthbuffer erzeugt.
	 * 
	 * @param width		Breite der Fläche, welche gerendert werden soll
	 * @param height	Höhe der Fläche, welche gerendert werden soll
	 */
	public SebRenderer(double width, double height) {

		this.width = width;
		this.height = height;
		this.aspectratio = width / height;

		this.near = 0.1;
		this.far = 100;
		this.fov = 60;

		this.framebuffer = new int[(int) (width * height)];
		this.depthBuffer = new double[(int) (width * height)];

		this.projMat = new double[4][4];
		generateProjMat();

		this.format = PixelFormat.getIntArgbPreInstance();

		refreshBuffer();
	}

	/**
	 * Zeichnet den Framebuffer mithilfe des angegebenen PixelWriter.
	 * 
	 * @param writer	PixelWriter von der Zeichenfläche, auf welcher das erzeugte Bild dargestellt werden soll
	 */
	public void draw(PixelWriter writer) {
		writer.setPixels(0, 0, (int) width, (int) height, format, framebuffer, 0, (int) width);
	}

	/**
	 * Rendert eine Szene, zeichnet diese allerdings noch nicht auf eine
	 * ausgewählte Fläche.
	 * (siehe dazu: 
	 * @see de.sebphil.renderer.objects.SebRenderer#draw(PixelWriter) draw
	 * )
	 * 
	 * @param scene	Szene, welcher gerendert werden soll
	 */
	public void update(RenScene scene) {
		
		// Buffer müssen geleert werden, da sonst Artefakte von vorgangenem Frame auftauchen
		refreshBuffer();
		
		// Kamera
		RenCamera camera = scene.getCamera();

		Point3D to = new Point3D(0, 0, 1);
		
		double w = width / 2;
		double h = height / 2;
		
		double[][] camView = camera.lookAt(to);
		
		// Rendern der Figuren
		for (RenShape shape : scene.getShapes()) {
			
			// Worldmatrix für aktuelle Figur erstellen
			double[][] worldMat = RenShape.generateWorldMat(shape);
			
			// Jedes Dreieck der Figur abarbeiten
			for (RenTriangle tri : shape.getPolys()) {

				Point3D[] vert = tri.getVert();

				// Eckpuntke in World-Space transformieren

				for (int i = 0; i < vert.length; i++) {

					vert[i] = RenUtilities.multMatVec(worldMat, vert[i]);
					vert[i] = vert[i].add(shape.getPosition());

				}
				
				// Flächen-Normale des aktuellen Dreieckes ermitteln
				
				Point3D line1 = new Point3D(vert[1].getX() - vert[0].getX(), vert[1].getY() - vert[0].getY(),
						vert[1].getZ() - vert[0].getZ());
				Point3D line2 = new Point3D(vert[2].getX() - vert[0].getX(), vert[2].getY() - vert[0].getY(),
						vert[2].getZ() - vert[0].getZ());

				Point3D normal = line1.crossProduct(line2).normalize();
				
				// Face-Culling
				
				double dot = normal.dotProduct(vert[0].subtract(camera.getPosition()));

				if (dot < 0) {

					// Eckpunkte des aktuellen Dreieckes in View-Space transformieren

					for (int i = 0; i < vert.length; i++) 
						vert[i] = RenUtilities.multMatVec(camView, vert[i]);
					
					// Eckpunkte des aktuellen Dreieckes gegen die near Clipping-Ebene clippen
					
					RenTriangle[] triangles = clipToPlane(new Point3D(0, 0, 0.5+near), new Point3D(0, 0, 1), vert,
							tri.getColor());
					
					if (triangles.length == 0)
						continue;
					
					// Restliche Dreiecke werden weiter verarbeitet
					
					for (RenTriangle projTri : triangles) {
						
						Point3D[] projVert = projTri.getVert();
						
						/*
						 * Aktuelle Eckpunkte in NDC-Space transformieren.
						 * Dabei werden diese zunächst in den Clip-Space transformiert und anschließend
						 * sofort in den NDC-Space mithilfe der Funktion 'multMatVec'.
						 * View-Space -> (Clip-Space) -> NDC-Space
						 */

						for (int i = 0; i < projVert.length; i++)
							projVert[i] = RenUtilities.multMatVec(projMat, projVert[i]);
						
						// Das aktuelle Dreieck wird nun gegen alle restliche Clipping-Ebenen geclippt.
						
						Color color2 = projTri.getColor();
						List<RenTriangle> clipTriangles = new ArrayList<RenTriangle>();
						
						clipTriangles.add(new RenTriangle(projVert[0], projVert[1], projVert[2]));
						
						int trianglesToClip = 1;
						
						for (int i = 0; i < 5; i++) {
							
							trianglesToClip = clipTriangles.size();
							
							while (trianglesToClip > 0) {
								
								RenTriangle testTri = clipTriangles.get(0);
								clipTriangles.remove(0);
								
								RenTriangle[] clipped = new RenTriangle[0];
								trianglesToClip--;
								
								
								// Aufgrund von Ungenauigkeit muss in einigen Fällen 0.999... verwendet werden.
								
								if (i == 0) {
									//right
									clipped = clipToPlane(new Point3D(0.9999, 0, 0), new Point3D(-1, 0, 0), testTri.getVert(),
											color2);
									
								} else if (i == 1) {
									//left
									clipped = clipToPlane(new Point3D(-1, 0, 0), new Point3D(1, 0, 0),
											testTri.getVert(), color2);
								} else if (i == 2) {
									//bottom
									clipped = clipToPlane(new Point3D(0, 0.998, 0), new Point3D(0, -1, 0),
											testTri.getVert(), color2);
								} else if (i == 3) {
									//up
									clipped = clipToPlane(new Point3D(0, -1, 0), new Point3D(0, 1, 0),
											testTri.getVert(), color2);
								} else if (i == 4) {
									//far
									clipped = clipToPlane(new Point3D(0, 0, 1), new Point3D(0, 0, -1),
											testTri.getVert(), color2);
								}

								for (int n = 0; n < clipped.length; n++) 
									clipTriangles.add(clipped[n]);

							}
						}
						
						// Alle, durch das Clipping entstandenen, Dreiecke in den Depth- und Framebuffer eintragen.
						
						for (int i=0; i < clipTriangles.size(); i++) {

							RenTriangle triClip = clipTriangles.get(i);
							
							Point3D[] verts = triClip.getVert();
							
							// Aktuelles Dreieck in den Screen-Space transformieren.
							
							for(int j=0;j<verts.length;j++) 
								verts[j] = new Point3D(verts[j].getX() * w + w, verts[j].getY() * h + h, projVert[j].getZ());
							
							clipTriangles.get(i).setV1(verts[0]);
							clipTriangles.get(i).setV2(verts[1]);
							clipTriangles.get(i).setV3(verts[2]);
							
							// Aktuelles Dreieck schattieren (Unter Einfluss der Lichtquellen).
							
							double r = 0, g = 0, b = 0, o = 0;
							
							for (Point3D dirLight : scene.getLights()) {

								dirLight = dirLight.normalize();

								double dotLight = normal.dotProduct(dirLight);
								double shade = Math.max(scene.getAmbient(), dotLight);

								Color shadeCol = shade(triClip.getColor(), shade);

								r += shadeCol.getRed();
								g += shadeCol.getGreen();
								b += shadeCol.getBlue();
								o += shadeCol.getOpacity();
							}

							if (r > 1)
								r = 1;
							if (g > 1)
								g = 1;
							if (b > 1)
								b = 1;
							if (o > 1)
								o = 1;
							
							Color color = new Color(r, g, b, o);
							
							// Aktuelles Dreieck rasterieren
							
							rasterizeTri(triClip.getVert(), color);
						}

					}

				}

			}

		}

	}

	/**
	 * Stellt den Ursprungszustand des Framebuffers sowie Depthbuffers wieder her.
	 * (Framebuffer wird mit der Farbe Schwarz und der Depthbuffer mit Double.Min_Value aufgefüllt)
	 */
	private void refreshBuffer() {
		for (int i = 0; i < framebuffer.length; i++) {
			framebuffer[i] = 0xFF000000;
			depthBuffer[i] = Double.MIN_VALUE;
		}
	}
	
	/**
	 * Clippt ein Dreieck gegen eine Ebene mithilfe des Sutherland-Hodgman Algorithmus.
	 * 
	 * @param planeP 	Punkt auf Ebene
	 * @param planeN 	Ebenen-Normale
	 * @param vert 		Eckpunkt des Dreiecks
	 * @param color 	Farbe des Dreiecks
	 * @return returns 	Gibt die geclippten Dreiecke zurück (0 bis (einschließlich) 2 Dreiecke)
	 */
	private RenTriangle[] clipToPlane(Point3D planeP, Point3D planeN, Point3D[] vert, Color color) {
		
		// Sutherland-Hodgman Algorithmus
		
		List<Point3D> output = new ArrayList<Point3D>();
		
		for(int i=0;i<vert.length;i++) {
			
			Point3D currentPoint = vert[i];
			Point3D prevPoint = vert[(i + vert.length - 1) % vert.length];
			Point3D interstection = lineIntersectPlane(planeP, planeN, currentPoint, prevPoint);
			
			double dist1 = getDistance(currentPoint, planeN, planeP);
			double dist2 = getDistance(prevPoint, planeN, planeP);
			
			if(dist1 >= 0) {
				
				if(dist2 < 0) {
					output.add(interstection);
				}
				
				output.add(currentPoint);
				
			}else if(dist2 >= 0) {
				output.add(interstection);
			}
			
		}
		
		// Konstruieren der Dreiecke (wenn Eckpunkte vorhanden)
		
		if(output.isEmpty())
			return new RenTriangle[] {};
		else if(output.size() == 3) {
			return new RenTriangle[] {
					new RenTriangle(output.get(0), output.get(1), output.get(2), color),
					};
		}else if(output.size() == 4) {
			return new RenTriangle[] {
					new RenTriangle(output.get(2), output.get(0), output.get(1), color),
					new RenTriangle(output.get(2), output.get(0), output.get(3), color),
					};
		}
		
		return null;
	}
	
	/**
	 * Rasteriert ein Dreieck, d.h. das Dreieck wird in den Depth- und Framebuffer
	 * eingetragen.
	 * 
	 * @param vert Scheitelpunkt des Dreiecks
	 * @param color Farbe des Dreiecks (mit Schattierung)
	 */
	private void rasterizeTri(Point3D[] vert, Color color) {

		double a = edgeFunction(vert[0], vert[1], new Point2D(vert[2].getX(), vert[2].getY()));
		double dZ1 = vert[1].getZ() - vert[0].getZ();
		double dZ2 = vert[2].getZ() - vert[0].getZ();

		int maxX = (int) Math.max(vert[0].getX(), Math.max(vert[1].getX(), vert[2].getX()));
		int maxY = (int) Math.max(vert[0].getY(), Math.max(vert[1].getY(), vert[2].getY()));
		int minX = (int) Math.min(vert[0].getX(), Math.min(vert[1].getX(), vert[2].getX()));
		int minY = (int) Math.min(vert[0].getY(), Math.min(vert[1].getY(), vert[2].getY()));

		// Jedes Pixel der zu zeichnenden Fläche wird untersucht, ob es von dem beschriebenen Dreieck beeinflusst wird.
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {

				int index = (int) (y * width + x);
				Point2D p = new Point2D(x, y);

				double w1 = edgeFunction(vert[1], vert[2], p);
				double w2 = edgeFunction(vert[2], vert[0], p);
				double w3 = edgeFunction(vert[0], vert[1], p);
				
				/*
				 * Überprüfen, ob Pixel von beschriebenen Dreieck beeinflusst wird
				 * true, wenn Pixel ist in Dreieck (einschließlich Grenzen des Dreiecks)
				 * false, wenn Pixel ist außerhalb
				 */
				boolean inTri = (w1 <= 0 && w2 <= 0 && w3 <= 0) || (w1 >= 0 && w2 >= 0 && w3 >= 0);
				
				if (inTri) {
					
					// z-Koordiante des Pixels bestimmen
					
					w1 /= a;
					w2 /= a;
					w3 /= a;

					// Depthtest vollführen
					
					double z = vert[0].getZ() + w1 * dZ1 + w3 * dZ2;
					z = 1 / z;

					if (depthBuffer[index] < z) {
						// Wert in Depth- und Framebuffer aktualisieren
						depthBuffer[index] = z;
						framebuffer[index] = color.hashCode();
					}

				}
			}
		}

	}

	/**
	 * Errechnet den Schnittpunkt einer Geraden mit einer Ebene
	 * 
	 * @param planeP Punkt auf Ebene
	 * @param planeN Ebenen-Normale
	 * @param lineStart Punkt auf Geraden
	 * @param lineEnd Punkt auf Geraden
	 * @return returns Gibt den Schnittpunkt von der beschriebenen Gerade und Ebene zurück (wenn vorhanden).
	 */
	private Point3D lineIntersectPlane(Point3D planeP, Point3D planeN, Point3D lineStart, Point3D lineEnd) {
		
		planeN = planeN.normalize();
		
		double planeDot = -planeN.dotProduct(planeP);
		
		double a = lineStart.dotProduct(planeN);
		double b = lineEnd.dotProduct(planeN);
		double c = (-planeDot - a) / (b - a);

		Point3D line = lineEnd.subtract(lineStart);
		Point3D intersection = line.multiply(c);

		return lineStart.add(intersection);
	}

	/**
	 * 
	 * @param point3d Punkt, welcher bearbeitet werden soll
	 * @param planeN Ebenen-Normale
	 * @param planeP Punkt auf Ebene
	 * @return returns Gibt die Distanz des beschriebenen Punktes zu der Ebene zurück 
	 * (wenn Punkt auf Seite von Normale liegt: positiv; wenn auf Ebene: 0; sonst negativ).
	 */
	private double getDistance(Point3D point3d, Point3D planeN, Point3D planeP) {
		return (planeN.getX() * point3d.getX() + planeN.getY() * point3d.getY() + planeN.getZ() * point3d.getZ()
				- planeN.dotProduct(planeP));
	}

	/**
	 * gibt einen negativen Wert zurück, wenn Punkt auf der rechten Seite der beschriebenen Geraden (durch v1 und v2) liegt.
	 * gibt einen positiven Wert zurück, wenn Punkt auf der linken Seite der beschriebenen Geraden (durch v1 und v2) liegt.
	 * 
	 * @param v1 Geraden Punkt
	 * @param v2 zweiter Geraden Punkt
	 * @param p Punkt, welcher bearbeitet werden soll
	 * @return returns gibt den Wert der Edge-Function zurück
	 */
	private double edgeFunction(Point3D v1, Point3D v2, Point2D p) {
		return (p.getX() - v1.getX()) * (v2.getY() - v1.getY()) - (p.getY() - v1.getY()) * (v2.getX() - v1.getX());
	}

	/**
	 * Schattiert eine Farbe mit einem bestimmten Wert
	 * 
	 * @param color Grundfarbe
	 * @param shade Wert, mit welchem schattiert wird
	 * @return returns schattierte Farbe
	 */
	private Color shade(Color color, double shade) {

		double red = color.getRed() * shade;
		double green = color.getGreen() * shade;
		double blue = color.getBlue() * shade;
		double opacity = color.getOpacity() * shade;

		return new Color(opacity, Math.abs(red), Math.abs(green), Math.abs(blue));

	}

	/**
	 * Aktualisiert die (Werte für die) Projektionsmatrix
	 */
	private void generateProjMat() {

		scale = Math.tan(Math.toRadians(fov) / 2) * near;

		double right = scale * aspectratio;
		double top = scale;
		double left = -right;
		double bottom = -top;
		
		projMat[0][0] = -2 * near / (right - left);
		projMat[1][1] = 2 * near / (top - bottom);
		projMat[2][0] = (right + left) / (right - left);
		projMat[2][1] = (top + bottom) / (top - bottom);
		
		projMat[2][2] = -(far+near) / (far - near);
		projMat[2][3] = -1;
		
		projMat[3][2] = 2 * far * near / (far - near);
	}

	/**
	 * Ändert die Größe des Frame- und Depthbuffers.
	 * 
	 * @param width neue Breite
	 * @param height neue Höhe
	 */
	public void resize(double width, double height) {

		this.width = width;
		this.height = height;
		this.aspectratio = width / height;

		this.framebuffer = new int[(int) (width * height)];
		this.depthBuffer = new double[(int) (width * height)];

		refreshBuffer();

		generateProjMat();
	}

	/**
	 * Legt ein neues FieldOfView fest.
	 * 
	 * @param fov FieldofView in Gradmaß
	 */
	public void setFov(double fov) {
		this.fov = fov;
		scale = Math.tan(Math.toRadians(fov) / 2);

		generateProjMat();
	}

	public void setNear(double near) {
		this.near = near;

		generateProjMat();
	}

	public void setFar(double far) {
		this.far = far;

		generateProjMat();
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getAspectratio() {
		return aspectratio;
	}

	public void setAspectratio(double aspectratio) {
		this.aspectratio = aspectratio;
	}

	public double getNear() {
		return near;
	}

	public double getFar() {
		return far;
	}

	public double getFov() {
		return fov;
	}

}
