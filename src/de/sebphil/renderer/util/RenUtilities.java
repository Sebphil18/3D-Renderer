package de.sebphil.renderer.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class RenUtilities {

	public static boolean isNumeric(String arg, boolean point, boolean e) {

		if (arg.isEmpty())
			return false;
		if ((arg.contains("+") && !arg.startsWith("+")) || (arg.contains("-") && !arg.startsWith("-"))) {
			return false;
		}

		char[] chars = arg.toCharArray();

		if (chars.length == 1) {
			if (!Character.isDigit(chars[0]))
				return false;
		}

		for (char c : chars) {

			if (!Character.isDigit(c) && c != '-' && c != '+') {
				if (point && c == '.') {
					point = false;
					continue;
				} else if (e && c == 'E' && !arg.endsWith("E")) {
					e = false;
					continue;
				}
				return false;
			}
		}

		return true;
	}

	public static Point3D multVecVec(Point3D vec1, Point3D vec2) {
		return new Point3D(vec1.getX() * vec2.getX(), vec1.getY() * vec2.getY(), vec1.getZ() * vec2.getZ());
	}

	public static Point3D multMatVec(double[][] matrix, Point3D vector) {

		double[] result = new double[3];

		result[0] = vector.getX() * matrix[0][0] + vector.getY() * matrix[1][0] + vector.getZ() * matrix[2][0]
				+ matrix[3][0];
		result[1] = vector.getX() * matrix[0][1] + vector.getY() * matrix[1][1] + vector.getZ() * matrix[2][1]
				+ matrix[3][1];
		result[2] = vector.getX() * matrix[0][2] + vector.getY() * matrix[1][2] + vector.getZ() * matrix[2][2]
				+ matrix[3][2];

		double w = vector.getX() * matrix[0][3] + vector.getY() * matrix[1][3] + vector.getZ() * matrix[2][3]
				+ matrix[3][3];
		if (w != 0.0f) {
			result[0] /= w;
			result[1] /= w;
			result[2] /= w;
		}

		return new Point3D(result[0], result[1], result[2]);
	}

	public static double[][] multMatMat(double[][] m1, double[][] m2) {

		double[][] result = new double[m1.length][m2[0].length];

		for (int i = 0; i < result[0].length; i++) {
			for (int j = 0; j < result.length; j++) {

				double s = 0;

				for (int k = 0; k < m2.length; k++)
					s += m1[j][k] * m2[k][i];

				result[j][i] = s;
			}
		}

		return result;
	}

	public static double[][] invertLookAtMat(double[][] mat) {

		double[][] result = new double[4][4];
		result[0][0] = mat[0][0];
		result[0][1] = mat[1][0];
		result[0][2] = mat[2][0];
		result[0][3] = 0.0f;
		result[1][0] = mat[0][1];
		result[1][1] = mat[1][1];
		result[1][2] = mat[2][1];
		result[1][3] = 0.0f;
		result[2][0] = mat[0][2];
		result[2][1] = mat[1][2];
		result[2][2] = mat[2][2];
		result[2][3] = 0.0f;
		result[3][0] = -(mat[3][0] * result[0][0] + mat[3][1] * result[1][0] + mat[3][2] * result[2][0]);
		result[3][1] = -(mat[3][0] * result[0][1] + mat[3][1] * result[1][1] + mat[3][2] * result[2][1]);
		result[3][2] = -(mat[3][0] * result[0][2] + mat[3][1] * result[1][2] + mat[3][2] * result[2][2]);
		result[3][3] = 1.0f;
		return result;
	}

	public static void showErrorMessage(String errorMsg, Label error, Color color) {

		error.setText(errorMsg);
		error.setTextFill(color);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new KeyValue(error.opacityProperty(), 1)));
		timeline.getKeyFrames()
				.add(new KeyFrame(Duration.millis(errorMsg.length() * 200), new KeyValue(error.opacityProperty(), 0)));

		timeline.play();

	}
	
	public static void shiftArrDown(double[] array, int width, int height) {
		
		for(int y=0;y<height-1;y++) {
		
			for(int x=0;x<width;x++) {
				array[y * width + x] = array[(y+1) * width + x];
			}
			
		}
		
	}
	
	public static void shiftArrUp(double[] array, int width, int height) {
		
		for(int y=height-1;y>0;--y) {
			
			for(int x=0;x<width;x++) {
				
				array[y * width + x] = array[(y-1) * width + x];
				
			}
			
		}
		
	}
	
	public static void shiftArrLeft(double[] array, int width, int height) {
		
		for(int x=0;x<width-1;x++) {
			
			for(int y=0;y<height;y++) {
				
				array[y * width + x] = array[y * width + (x+1)];
				
			}
			
		}
		
	}

}
