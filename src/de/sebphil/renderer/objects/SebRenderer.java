package de.sebphil.renderer.objects;

import java.nio.IntBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import de.sebphil.renderer.util.RenUtilities;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class SebRenderer {

	private int[] framebuffer;
	private double near, far, fov, scale, width, height, aspectratio, scaleX, scaleY;
	private double[] depthBuffer;
	private double[][] projMat;
	private PixelFormat<IntBuffer> format;

	/**
	 * constructs new SebRenderer:
	 * 	The SebRenderer contains a RenScene, depth-buffer, screen-buffer and values for the projection-matrix like fov, aspectratio.
	 * @param width - width of image
	 * @param height - height of image
	 */
	public SebRenderer(double width, double height) {

		this.width = width;
		this.height = height;
		this.aspectratio = width / height;

		this.near = 0.1;
		this.far = 500;
		this.fov = 60;

		this.scaleX = 360;
		this.scaleY = 360;

		this.framebuffer = new int[(int) (width * height)];
		this.depthBuffer = new double[(int) (width * height)];

		this.projMat = new double[4][4];
		generateProjMat();

		this.format = PixelFormat.getIntArgbPreInstance();

		refreshBuffer();
	}

	/**
	 * draws frame-buffer to PixelWriter
	 * @param writer - writer of canvas
	 */
	public void draw(PixelWriter writer) {
		writer.setPixels(0, 0, (int) width, (int) height, format, framebuffer, 0, (int) width);
	}

	/**
	 * Updates frame- and depth-buffer (renders scene)
	 * @param scene - Scene to render
	 */
	public void update(RenScene scene) {

		refreshBuffer();

		RenCamera camera = scene.getCamera();

		Point3D to = new Point3D(0, 0, 1);

		double[][] camView = camera.lookAt(to);

		for (RenShape shape : scene.getShapes()) {

			double[][] worldMat = RenShape.generateWorldMat(shape);

			for (RenTriangle tri : shape.getPolys()) {

				Point3D[] vert = tri.getVert();

				// World-Space

				for (int i = 0; i < vert.length; i++) {

					vert[i] = RenUtilities.multMatVec(worldMat, vert[i]);
					vert[i] = vert[i].add(shape.getPosition());

				}

				Point3D line1 = new Point3D(vert[1].getX() - vert[0].getX(), vert[1].getY() - vert[0].getY(),
						vert[1].getZ() - vert[0].getZ());
				Point3D line2 = new Point3D(vert[2].getX() - vert[0].getX(), vert[2].getY() - vert[0].getY(),
						vert[2].getZ() - vert[0].getZ());

				Point3D normal = line1.crossProduct(line2).normalize();

				double dot = normal.dotProduct(vert[0].subtract(camera.getPosition()));

				if (dot < 0) {

					// World-Space -> View-Space

					for (int i = 0; i < vert.length; i++) {
						vert[i] = RenUtilities.multMatVec(camView, vert[i]);
					}

					RenTriangle[] triangles = clipToPlane(new Point3D(0, 0, near), new Point3D(0, 0, 1), vert,
							tri.getColor());

					if (triangles.length == 0)
						continue;

					for (RenTriangle projTri : triangles) {

						Point3D[] projVert = projTri.getVert();

						// View-Space -> (Clip-Space) -> NDC-Space

						for (int i = 0; i < projVert.length; i++) {

							projVert[i] = RenUtilities.multVecVec(new Point3D(scaleX, scaleY, 1), projVert[i]);
							projVert[i] = RenUtilities.multMatVec(projMat, projVert[i]);
							
							// Move Vec. to middle of screen
							projVert[i] = projVert[i].add(new Point3D(width / 2, height / 2, 0));

						}

						Color color2 = projTri.getColor();
						
						//Clipping
						
						BlockingQueue<RenTriangle> queue = new LinkedBlockingQueue<RenTriangle>();
						queue.add(new RenTriangle(projVert[0], projVert[1], projVert[2]));
						int trianglesToRaster = 1;

						// !!Screen-Space!!
						
						//all 4 plans
						for (int i = 0; i < 4; i++) {

							while (trianglesToRaster > 0) {
								RenTriangle testTri = queue.poll();
								
								//max. 1 new triangle (2 total)
								RenTriangle[] clipped = new RenTriangle[2];
								trianglesToRaster--;
								
								if (i == 0) {
									//clip to top (normal points in screen downwards) ((0, 0, 0), da oben y = 0)
									clipped = clipToPlane(new Point3D(0, 0, 0), new Point3D(0, 1, 0), testTri.getVert(),
											color2);
								} else if (i == 1) {
									//clip to bottom (normal points in screen upwards) ((0, height-1, 0), da unten y = height)
									clipped = clipToPlane(new Point3D(0, height - 1, 0), new Point3D(0, -1, 0),
											testTri.getVert(), color2);
								} else if (i == 2) {
									//clip to left
									clipped = clipToPlane(new Point3D(0, 0, 0), new Point3D(1, 0, 0), testTri.getVert(),
											color2);
								} else if (i == 3) {
									//clip to right
									clipped = clipToPlane(new Point3D(width - 1, 0, 0), new Point3D(-1, 0, 0),
											testTri.getVert(), color2);
								}

								for (int n = 0; n < clipped.length; n++) {
									queue.offer(clipped[n]);
								}

							}
							trianglesToRaster = queue.size();
						}

						while (queue.size() > 0) {

							RenTriangle triClip = queue.poll();

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

							rasterizeTri(triClip.getVert(), color);
						}

					}

				}

			}

		}

	}

	/**
	 * clears frame- and depth-buffer
	 */
	private void refreshBuffer() {
		for (int i = 0; i < framebuffer.length; i++) {
			framebuffer[i] = 0xFF000000;
			depthBuffer[i] = Double.MIN_VALUE;
		}
	}

	/**
	 * Clips triangle to plane
	 * @param planeP - point on plane
	 * @param planeN - plane-normal
	 * @param vert - vertices to clip
	 * @param color - color of triangle
	 * @return returns clipped triangle
	 */
	private RenTriangle[] clipToPlane(Point3D planeP, Point3D planeN, Point3D[] vert, Color color) {
		
		/*
		 * planeN - 1. pane normal
		 * planeP - 2. Point on pane
		 */
		
		planeN = planeN.normalize();

		int insideCount = 0;
		int outsideCount = 0;
		Point3D[] inside = new Point3D[3];
		Point3D[] outside = new Point3D[3];

		double dist1 = getDistance(vert[0], planeN, planeP);
		double dist2 = getDistance(vert[1], planeN, planeP);
		double dist3 = getDistance(vert[2], planeN, planeP);

		//sorting vertices according to if there are inside / outside of pane
		if (dist1 >= 0) {
			inside[insideCount++] = vert[0];
		} else {
			outside[outsideCount++] = vert[0];
		}

		if (dist2 >= 0) {
			inside[insideCount++] = vert[1];
		} else {
			outside[outsideCount++] = vert[1];
		}

		if (dist3 >= 0) {
			inside[insideCount++] = vert[2];
		} else {
			outside[outsideCount++] = vert[2];
		}

		//do nothing, cause not in view
		if (insideCount == 0) {

			return new RenTriangle[] {};

		} else if (insideCount == 1 && outsideCount == 2) {
			//1 inside / 2 outside
			return new RenTriangle[] {
					new RenTriangle(inside[0], lineIntersectPlane(planeP, planeN, inside[0], outside[0]),
							lineIntersectPlane(planeP, planeN, inside[0], outside[1]), color) };

		} else if (insideCount == 2 && outsideCount == 1) {
			//2 inside / 1 outside
			return new RenTriangle[] {
					new RenTriangle(inside[0], inside[1], lineIntersectPlane(planeP, planeN, inside[0], outside[0]),
							color),
					new RenTriangle(inside[1], lineIntersectPlane(planeP, planeN, inside[0], outside[0]),
							lineIntersectPlane(planeP, planeN, inside[1], outside[0]), color) };

		} else if (insideCount == 3 && outsideCount == 0) {
			//all vertices in view
			return new RenTriangle[] { new RenTriangle(vert[0], vert[1], vert[2], color) };
		}

		return null;
	}

	/**
	 * rasterizes a triangle
	 * @param vert - vertices of triangle
	 * @param color - color of triangle
	 */
	private void rasterizeTri(Point3D[] vert, Color color) {

		double a = edgeFunction(vert[0], vert[1], new Point2D(vert[2].getX(), vert[2].getY()));
		double dZ1 = vert[1].getZ() - vert[0].getZ();
		double dZ2 = vert[2].getZ() - vert[0].getZ();

		int maxX = (int) Math.max(vert[0].getX(), Math.max(vert[1].getX(), vert[2].getX()));
		int maxY = (int) Math.max(vert[0].getY(), Math.max(vert[1].getY(), vert[2].getY()));
		int minX = (int) Math.min(vert[0].getX(), Math.min(vert[1].getX(), vert[2].getX()));
		int minY = (int) Math.min(vert[0].getY(), Math.min(vert[1].getY(), vert[2].getY()));

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {

				int index = (int) (y * width + x);
				Point2D p = new Point2D(x, y);

				double w1 = edgeFunction(vert[1], vert[2], p);
				double w2 = edgeFunction(vert[2], vert[0], p);
				double w3 = edgeFunction(vert[0], vert[1], p);

				boolean inTri = (w1 <= 0 && w2 <= 0 && w3 <= 0) || (w1 >= 0 && w2 >= 0 && w3 >= 0);
				if (inTri) {

					w1 /= a;
					w2 /= a;
					w3 /= a;

					double z = vert[0].getZ() + w1 * dZ1 + w3 * dZ2;
					z = 1 / z;

					if (depthBuffer[index] < z) {
						depthBuffer[index] = z;
						framebuffer[index] = color.hashCode();
					}

				}
			}
		}

	}

	/**
	 * 
	 * @param planeP - point on plane
	 * @param planeN - plane-normal
	 * @param lineStart - line-start (Point)
	 * @param lineEnd - line-end (Point)
	 * @return returns the intersection Point of the line with the plane
	 */
	private Point3D lineIntersectPlane(Point3D planeP, Point3D planeN, Point3D lineStart, Point3D lineEnd) {
		
		//planeP - point on plane
		//planeN - plane normal
		
		Point3D planeNormal = planeN.normalize();
		double planeD = -planeNormal.dotProduct(planeP);
		double ad = lineStart.dotProduct(planeNormal);
		double bd = lineEnd.dotProduct(planeNormal);
		double t = (-planeD - ad) / (bd - ad);

		Point3D line = lineEnd.subtract(lineStart);
		Point3D intersection = line.multiply(t);

		return lineStart.add(intersection);
	}

	/**
	 * 
	 * @param point3d - point to check
	 * @param planeN - plane-normal
	 * @param planeP - point on plane
	 * @return returns distance from point3d to the plane
	 */
	private double getDistance(Point3D point3d, Point3D planeN, Point3D planeP) {
		return (planeN.getX() * point3d.getX() + planeN.getY() * point3d.getY() + planeN.getZ() * point3d.getZ()
				- planeN.dotProduct(planeP));
	}

	/**
	 * returns negative value, if point is on right side of line descriped by v1 and v2; 
	 * returns positive value, if point is on left side
	 * @param v1 - line-start
	 * @param v2 - line-end
	 * @param p - point to test
	 * @return returns the value of the edgeFunction for given values
	 */
	private double edgeFunction(Point3D v1, Point3D v2, Point2D p) {
		return (p.getX() - v1.getX()) * (v2.getY() - v1.getY()) - (p.getY() - v1.getY()) * (v2.getX() - v1.getX());
	}

	/**
	 * shades a given Color with a specific value (Color * shade)
	 * @param color - base color
	 * @param shade - shade value
	 * @return returns shaded Color
	 */
	private Color shade(Color color, double shade) {

		double red = color.getRed() * shade;
		double green = color.getGreen() * shade;
		double blue = color.getBlue() * shade;
		double opacity = color.getOpacity() * shade;

		return new Color(opacity, Math.abs(red), Math.abs(green), Math.abs(blue));

	}

	/**
	 * updates projection-matrix
	 */
	private void generateProjMat() {

		scale = Math.tan(Math.toRadians(fov) / 2) * near;

		double right = scale;
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
	 * resizes image (frame- & depth-buffer)
	 * @param width - new width
	 * @param height - new height
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

	public double[] getDepthBuffer() {
		return depthBuffer;
	}

	public void setDepthBuffer(double[] depthBuffer) {
		this.depthBuffer = depthBuffer;
	}

	public double[][] getProjMat() {
		return projMat;
	}

	public void setProjMat(double[][] projMat) {
		this.projMat = projMat;
	}

	public PixelFormat<IntBuffer> getFormat() {
		return format;
	}

	public void setFormat(PixelFormat<IntBuffer> format) {
		this.format = format;
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
