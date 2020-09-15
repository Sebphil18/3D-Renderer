package de.sebphil.renderer.objects;

import java.util.Random;

import de.sebphil.renderer.util.NoiseGenerator2D;
import de.sebphil.renderer.util.ResGrid;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public class RenNoise extends RenShape {

	private double offsetY, maxHeight, minHeight, scale;
	private long seed;
	private boolean mask;
	private NoiseGenerator2D noise, maskNoise;
	private ResGrid grid;
	private Random ran;

	/**
	 * Constructor für RenNoise.
	 * 
	 * Diese Klasse erbt von der Klasse RenShape.
	 * @see de.sebphil.renderer.objects.RenShape
	 * 
	 * @param name	Name des zu erzeugenden Objektes
	 */
	public RenNoise(String name) {

		super(name);

		this.ran = new Random();
		this.noise = new NoiseGenerator2D(ran.nextLong());
		this.maskNoise = new NoiseGenerator2D(ran.nextLong());
		this.mask = false;
		this.maxHeight = 100;
		this.minHeight = -100;
		this.scale = 1;
		
		maskNoise.setAmplitude(0.5);
		maskNoise.setFreqMult(0.5);
		maskNoise.setAmplMult(1.8);
		maskNoise.setOctaves(5);
		
	}

	/**
	 * Generiert ein Gitter aus Dreiecken basierend auf dem ResGrid von diesem Objekt.
	 * Mit der Variable 'scale' kann die Größe der einzelnen Zellen bestimmt werden.
	 * 
	 * @param colored
	 * <ul>
	 * <li>wenn true:	Dreiecke werden basierend auf ihrer y-Koordiante gefärbt.
	 * <li>wenn false:	Dreiecke werden nicht basierend auf iherer y-Koordinate eingefärbt.
	 * </ul>
	 */
	public void generatePolyMesh(boolean colored) {
		
		/*
		 * Es müssen alle Dreiecke dieses Objektes gelöscht werden, da evtl. noch
		 * einige noch geladen sind.
		 */
		super.getPolys().clear();
		
		//Erstellt das Gitter aus Dreiecken
		for (int x = 0; x < grid.getAmountX() - 1; x++) {
			for (int y = 0; y < grid.getAmountY() - 1; y++) {
				
				double val1 = grid.getVal(x, y) * 5 * scale;
				double val2 = grid.getVal(x + 1, y) * 5 * scale;
				double val3 = grid.getVal(x + 1, y + 1) * 5 * scale;
				double val4 = grid.getVal(x, y + 1) * 5 * scale;
				
				Point3D v1 = new Point3D(
						(x - grid.getAmountX() / 2) * scale, 
						val1, 
						y * scale);
				
				Point3D v2 = new Point3D(((x + 1) - grid.getAmountX() / 2) * scale, 
						val2, 
						y * scale);
				
				Point3D v3 = new Point3D(((x + 1) - grid.getAmountX() / 2) * scale, 
						val3, 
						(y + 1) * scale);
				
				Point3D v4 = new Point3D((x - grid.getAmountX() / 2) * scale, 
						val4, 
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

	public NoiseGenerator2D getMaskNoise() {
		return maskNoise;
	}

	public void setMaskNoise(NoiseGenerator2D maskNoise) {
		this.maskNoise = maskNoise;
	}

	public boolean isMask() {
		return mask;
	}

	public void setMask(boolean mask) {
		this.mask = mask;
	}

}
