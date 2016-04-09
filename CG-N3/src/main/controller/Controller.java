package main.controller;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import main.Camera;
import main.GraphicObject;
import main.Point4D;
import main.World;
import main.view.MainWindow;
import main.view.Render;

public class Controller implements KeyListener, MouseListener, MouseMotionListener {

	private final World world;

	public Controller(World world) {
		this.world = world;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (world.hasCurrentObject()) {
			final GraphicObject graphicObject = world.getCurrentObject();
			final Point4D current = worldPoint(e);
			graphicObject.updateLastPoint(current);
		}
		Render.glDrawable.display();
	}

	/**
	 * Converte os pontos X e Y do frame para um Point4D do mundo.
	 * 
	 * @param x
	 *            Ponto X do frame.
	 * @param y
	 *            Ponto Y do frame.
	 * @return Point4D com os valores equivalentes a mesma posi��o do X e Y no
	 *         mundo.
	 */
	private Point4D framePosToWorldPos(int x, int y) {
		// Pega o tamanho do canvas e j� pr�-calcula metade do tamanho
		final Rectangle canvasBounds = MainWindow.canvas.getBounds();
		int canvasHalfWidth = (int) (canvasBounds.getWidth() / 2);
		final double canvasHeight = canvasBounds.getHeight();
		int canvasHalfHeight = (int) (canvasHeight / 2);

		// Aqui � transformado de orienta��o "top to bottom" e "left to right"
		// para "center to top" e "center to right"
		int xCalculated = x - canvasHalfWidth;
		int yCalculated = (int) (Math.abs(y - canvasHeight) - canvasHalfHeight);

		// Calculo aqui quanto que a c�mera est� deslocada do centro, sendo que
		// se ela estiver centralizada, esses valores retornar�o zero
		final Camera camera = world.getCamera();
		final float cameraHalfWidth = camera.getCameraHalfWidth();
		final float cameraHalfHeight = camera.getCameraHalfHeight();
		final float xAxisOffset = cameraHalfWidth - camera.axisSizes[1];
		final float yAxisOffset = cameraHalfHeight - camera.axisSizes[3];

		// Agora sim � feito uma regrinha de tr�s proporcional como o professor
		// falou, que calcula quando eu clicar na borda do canvas, tem que nos
		// dizer que valor � este dentro do nosso mundo.
		xCalculated = (int) (((xCalculated * cameraHalfWidth) / canvasHalfWidth) - xAxisOffset);
		yCalculated = (int) (((yCalculated * cameraHalfHeight) / canvasHalfHeight) - yAxisOffset);

		return new Point4D(xCalculated, yCalculated);
	}

	/**
	 * Converte um ponto do frame para um ponto do mundo.
	 * 
	 * @param e
	 *            Evento do mouse que cont�m o ponto do frame.
	 * @return Point4D do mundo equivalente ao ponto do frame.
	 */
	private Point4D worldPoint(final MouseEvent e) {
		return framePosToWorldPos(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		/*
		 * Se o Ctrl estiver pressionado significa que deve ser criado um novo
		 * pol�gono.
		 */
		if (e.isControlDown()) {
			final Point4D current = worldPoint(e);
			GraphicObject graphicObject = world.getCurrentObject();
			if (graphicObject == null) {
				System.out.println("Criado novo pol�gono");
				graphicObject = new GraphicObject();
				world.add(graphicObject);
				world.setCurrentObject(graphicObject);
				/*
				 * Quando cria um objeto adiciona um ponto, pois o �ltimo ponto
				 * do objeto � pr�ximo ponto na vis�o do usu�rio.
				 */
				graphicObject.addPoint(current.clone());
			}
			graphicObject.addPoint(current);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		updateCamera(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		final int keyCode = e.getKeyCode();
		/*
		 * Deseleciona o objeto gr�fico e remove o �ltimo ponto desse objeto.
		 */
		if (KeyEvent.VK_CONTROL == keyCode) {
			final GraphicObject currentObject = world.getCurrentObject();
			if (currentObject != null) {
				currentObject.removeLastPoint();
				Render.glDrawable.display();
				world.removeCurrentObject();
			}
		}
	}

	/**
	 * Altera zoom/panorama da camera.
	 * 
	 * @param e
	 *            Evento do teclado.
	 */
	private void updateCamera(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_I:
			zoom(50);
			break;
		case KeyEvent.VK_O:
			zoom(-50);
			break;
		case KeyEvent.VK_E:
			adjustPan(0, 50);
			break;
		case KeyEvent.VK_D:
			adjustPan(0, -50);
			break;
		case KeyEvent.VK_C:
			adjustPan(1, 50);
			break;
		case KeyEvent.VK_B:
			adjustPan(1, -50);
			break;
		}
	}

	/**
	 * Realiza zoom-in ou zoom-out na c�mera.
	 * 
	 * @param zoom
	 *            quantidade de zoom, quanto negativo � feito zoom-out, quando �
	 *            positivo � feito zoom-in.
	 */
	private void zoom(final int offset) {
		world.getCamera().zoom(offset);
		Render.glDrawable.display();
	}

	/**
	 * Desloca a camera entre os eixos.
	 * 
	 * @param axis
	 *            Eixo a ser deslocado, sendo X = 0 e Y = 1.
	 * @param offset
	 *            Valor a ser deslocado.
	 */
	private void adjustPan(final int axis, final int offset) {
		world.getCamera().pan(axis, offset);
		Render.glDrawable.display();
	}
}
