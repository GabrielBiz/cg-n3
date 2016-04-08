package main;

import javax.media.opengl.glu.GLU;

public class Camera {

	private GLU glu;
	private float[] axisSizes = {-400.0f, 400.0f, -400.0f, 400.0f};
	private float[] axisMaxSizes = {-5000.0f, 5000.0f, -5000.0f, 5000.0f};
	private float[] axisMinSizes = {-100.0f, 100.0f, -100.0f, 100.0f};

	public Camera(float minX, float maxX, float minY, float maxY) {
		axisSizes[0] = minX;
		axisSizes[1] = maxX;
		axisSizes[2] = minY;
		axisSizes[3] = maxY;
		glu = new GLU();
	}
	
	/**
	 * Desloca a c�mera entre os eixos
	 * @param axis eixo que ir� deslocar, sendo X = 0 e Y = 1
	 * @param pan quantidade de deslocamento
	 */
	public void pan(int axis, float pan) {
		if (axis == 0) {
			modifyPan(0, pan);
			modifyPan(1, pan);
		} else if (axis == 1) {
			modifyPan(2, pan);
			modifyPan(3, pan);
		}
	}

	/**
	 * Realiza zoom-in ou zoom-out na c�mera
	 * @param zoom quantidade de zoom, quanto negativo � feito zoom-out, quando � positivo � feito zoom-in
	 */
	public void zoom(float zoom) {
		if (canModifyAxis(0, zoom)
				&& canModifyAxis(1, -zoom)
				&& canModifyAxis(2, zoom)
				&& canModifyAxis(3, -zoom)) {
			axisSizes[0] += zoom;
			axisSizes[1] += -zoom;
			axisSizes[2] += zoom;
			axisSizes[3] += -zoom;
		}
	}
	
	/**
	 * Define a matriz de proje��o
	 */
	public void display() {
		glu.gluOrtho2D(axisSizes[0], axisSizes[1], axisSizes[2], axisSizes[3]);
	}
	
	private void modifyPan(int axis, float zoom) {
		axisMaxSizes[axis] += zoom;
		axisMinSizes[axis] += zoom;
		axisSizes[axis] += zoom;
	}
	
	private boolean canModifyAxis(int axis, float zoom) {
		if (axis == 0 || axis == 2) {
			if (axisSizes[axis] < axisMaxSizes[axis] && zoom < 0) {
				return false;
			} else if (axisSizes[axis] > axisMinSizes[axis] && zoom > 0) {
				return false;
			}
		} else if (axis == 1 || axis == 3) {
			if (axisSizes[axis] < axisMinSizes[axis] && zoom < 0) {
				return false;
			} else if (axisSizes[axis] > axisMaxSizes[axis] && zoom > 0) {
				return false;
			}
		}
		return true;
	}

}
