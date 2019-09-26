package de.sebphil.renderer.objects;

import java.util.Random;

import de.sebphil.renderer.util.NoiseGenerator2D;
import de.sebphil.renderer.util.ResGrid;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;

public class RenNoise extends RenShape {

	private double increment;
	private long seed;
	private NoiseGenerator2D noise;
	private ResGrid grid;
	private Random ran;

	public RenNoise(String name) {

		super(name);

		this.ran = new Random();
		this.noise = new NoiseGenerator2D(ran.nextLong());

		noise.setAmplitude(1);
		noise.setFrequency(1);
		noise.setFreqMult(1);
		noise.setAmplMult(1);
		noise.setLayers(1);

	}

	public void generatePolyMesh() {

		super.getPolys().clear();

		for (int x = 0; x < grid.getAmountX() - 1; x++) {
			for (int y = 0; y < grid.getAmountY() - 1; y++) {

				Point3D v1 = new Point3D((x - grid.getAmountX() / 2), grid.getVal(x, y) * 5, y);
				Point3D v2 = new Point3D(((x + 1) - grid.getAmountX() / 2), grid.getVal(x + 1, y) * 5, y);
				Point3D v3 = new Point3D(((x + 1) - grid.getAmountX() / 2), grid.getVal(x + 1, y + 1) * 5, (y + 1));
				Point3D v4 = new Point3D((x - grid.getAmountX() / 2), grid.getVal(x, y + 1) * 5, (y + 1));

				super.getPolys().add(new RenTriangle(v4, v2, v1));
				super.getPolys().add(new RenTriangle(v3, v2, v4));

			}
		}

	}

	public void drawNoise(GraphicsContext gc) {

		for (int x = 0; x < grid.getAmountX(); x++) {
			for (int y = 0; y < grid.getAmountY(); y++) {

				gc.setStroke(noise.getNoiseRGB(grid.getVal(x, y)));
				grid.fillCell(x, y, gc);

			}
		}

	}

	public double getIncrement() {
		return increment;
	}

	public void setIncrement(double increment) {
		this.increment = increment;
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

}
