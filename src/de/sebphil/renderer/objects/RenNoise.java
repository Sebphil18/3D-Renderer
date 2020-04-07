package de.sebphil.renderer.objects;

import java.util.Random;

import de.sebphil.renderer.util.NoiseGenerator2D;
import de.sebphil.renderer.util.ResGrid;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public class RenNoise extends RenShape {

	private double offsetY, maxHeight, minHeight, scale;
	private long seed;
	private boolean dynamic;
	private NoiseGenerator2D noise;
	private ResGrid grid;
	private Random ran;

	/**
	 * Constructs new RenNoise
	 * @param name - name of Object
	 */
	public RenNoise(String name) {

		super(name);

		this.ran = new Random();
		this.noise = new NoiseGenerator2D(ran.nextLong());
		this.dynamic = false;
		this.maxHeight = 100;
		this.minHeight = -100;
		this.scale = 1;

		noise.setAmplitude(1);
		noise.setFreqMult(1);
		noise.setAmplMult(1);
		noise.setOctaves(1);

	}

	/**
	 * generates PolyMesh of 2D Noise
	 * @param colored - if set to true the polyMesh will be colored by height; if set to false don't
	 */
	public void generatePolyMesh(boolean colored) {

		super.getPolys().clear();
		
		for (int x = 0; x < grid.getAmountX() - 1; x++) {
			for (int y = 0; y < grid.getAmountY() - 1; y++) {
				
				Point3D v1 = new Point3D(
						(x - grid.getAmountX() / 2) * scale, 
						grid.getVal(x, y) * 5 * scale, 
						y * scale);
				
				Point3D v2 = new Point3D(((x + 1) - grid.getAmountX() / 2) * scale, 
						grid.getVal(x + 1, y) * 5 * scale, 
						y * scale);
				
				Point3D v3 = new Point3D(((x + 1) - grid.getAmountX() / 2) * scale, 
						grid.getVal(x + 1, y + 1) * 5 * scale, 
						(y + 1) * scale);
				
				Point3D v4 = new Point3D((x - grid.getAmountX() / 2) * scale, 
						grid.getVal(x, y + 1) * 5 * scale, 
						(y + 1) * scale);
				
				
				Color color = super.getColor();

				if (colored)
					color = noise.getNoiseRGB(grid.getVal(x, y));
				
				super.getPolys().add(new RenTriangle(v4, v2, v1, color));
				super.getPolys().add(new RenTriangle(v3, v2, v4, color));
				
			}
		}

	}

	public NoiseGenerator2D getNoise() {
		return noise;
	}

	public void setNoise(NoiseGenerator2D noise) {
		this.noise = noise;
	}

	public ResGrid getGrid() {
		return grid;
	}

	public void setGrid(ResGrid grid) {
		this.grid = grid;
	}

	public Random getRan() {
		return ran;
	}

	public void setRan(Random ran) {
		this.ran = ran;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
		noise.setSeed(seed);
	}

	public double getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(double offsetY) {
		if (offsetY < 0)
			return;
		this.offsetY = offsetY;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public double getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(double maxHeight) {
		this.maxHeight = maxHeight;
	}

	public double getMinHeight() {
		return minHeight;
	}

	public void setMinHeight(double minHeight) {
		this.minHeight = minHeight;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

}
