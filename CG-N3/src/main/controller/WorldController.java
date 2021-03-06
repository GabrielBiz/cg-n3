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
import main.Vertex;
import main.World;
import main.opengl.utils.ColorUtils;
import main.view.MainWindow;
import main.view.Render;

public class WorldController implements KeyListener, MouseListener, MouseMotionListener {

	private static final int TRANSLATE = 20;
	private static final double SCALE_UP = 2;
	private static final double SCALE_DOWN = 0.5;
	private static final double ROTATE = 5;
	private final World world;
	private final Render render;

	private int colorIndex = 0;
	private Point4D initialVertexPos;
	private int currentVertexIndex = -1;
	private boolean isCtrlDown = false;
	private boolean isEditingVertex = false;

	public WorldController(final World world, final Render render) {
		this.world = world;
		this.render = render;
	}

	private void render() {
		final Camera camera = world.getCamera();
		final float[] axis = camera.axisSizes();
		render.addDrawable(world);
		render.setAxisSizes(axis);
		render.render();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (world.hasCurrentObject()) {
			GraphicObject currentObject = world.getCurrentObject();
			final Point4D currentPos = currentObject.transform.getInverseMatriz().transformPoint(worldPoint(e));

			/*
			 * Se estiver editando o objeto ou um vertice do objeto faz o
			 * vertice acompanhar o ponteiro do mouse.
			 */
			if (currentVertexIndex != -1) {
				currentObject.updateVertexPointAt(currentVertexIndex, currentPos);
			} else {
				Vertex vertexOver = currentObject.getVertexAtPos(currentPos);
				if (vertexOver != null) {
					render.addDrawable(vertexOver.bbox());
				}
			}
		}
		render();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Rectangle bounds = MainWindow.canvas.getBounds();
		double dX = bounds.getWidth() - e.getX();
		final Point4D currentPos = worldPoint(e);

		Point4D transformedPos = null;
		GraphicObject currentObject = world.getCurrentObject();
		boolean hasCurrentObject = currentObject != null;
		if (hasCurrentObject) {
			transformedPos = currentObject.transform.getInverseMatriz().transformPoint(currentPos);
		}

		// Adiciona um poligono filho ao objeto selecionado
		if (hasCurrentObject && e.isShiftDown()) {
			clearEdition();
			GraphicObject child = new GraphicObject();
			child.createVertexAt(currentPos.clone());
			currentObject.addGraphicObject(child);
			world.setCurrentObject(child);
			return;
		}

		if (currentVertexIndex != -1)

		{
			currentObject.updateVertexPointAt(currentVertexIndex, transformedPos);
			clearEdition();
			render();
			return;
		}

		if (world.hasCurrentObject())

		{
			currentVertexIndex = currentObject.getVertexIndexAtPos(transformedPos);
			if (currentVertexIndex != -1) {
				initialVertexPos = currentObject.getVertex(currentVertexIndex).getPoint();
				isEditingVertex = true;
				return;
			}
		}

		Point4D fin = framePosToWorldPos((int) (e.getX() + dX), e.getY());
		if (isCtrlDown) {
			GraphicObject object = null;
			if (world.hasCurrentObject()) {

			} else if ((object = world.findObjectAt(currentPos, fin)) == null) {
				object = new GraphicObject();
				object.createVertexAt(currentPos.clone());
				world.add(object);
				world.setCurrentObject(object);
			}
			object.createVertexAt(currentPos);
			currentVertexIndex = object.getLastVertexIndex();
		} else {
			GraphicObject object = world.findObjectAt(currentPos, fin);
			if (object != null) {
				world.setCurrentObject(object);
			} else {
				world.removeCurrentObject();
			}
		}

		render();

	}

	private void clearEdition() {
		currentVertexIndex = -1;
		isEditingVertex = false;
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
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_CONTROL) {
			isCtrlDown = true;
		}

		if (isEditingVertex) {
			/*
			 * Tecla ESC para parar de editar o v�rtice.
			 */
			final GraphicObject graphicObject = world.getCurrentObject();
			if (keyCode == KeyEvent.VK_ESCAPE) {
				/*
				 * Se estava editando um vertice volta o ponto dele para a
				 * posi��o antiga.
				 */
				graphicObject.updateVertexPointAt(currentVertexIndex, initialVertexPos);
				clearEdition();
			} else if (keyCode == KeyEvent.VK_R) {
				graphicObject.removeVertexAt(currentVertexIndex);
				clearEdition();
			}
			render();
			return;
		}

		alterCurrentObject(e);
		updateCamera(e);
		updateCurrentObjectColor(e);
		render();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_CONTROL) {
			isCtrlDown = false;
		}

		/*
		 * Remove o ponto do mouse do objeto atual, caso ele exista.
		 */
		if (KeyEvent.VK_CONTROL == keyCode && !isEditingVertex) {
			final GraphicObject currentObject = world.getCurrentObject();
			if (currentObject != null) {
				currentObject.removeVertexAt(currentVertexIndex);
				clearEdition();
				render();
			}
		}
	}

	/**
	 * Altera o objeto atual, caso ele exista.
	 * 
	 * @param e
	 *            Evento do mouse.
	 */
	private void alterCurrentObject(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		/*
		 * Est� com um objeto selecionado e n�o est� editando v�rtices
		 */
		if (world.hasCurrentObject() && currentVertexIndex == -1) {
			final GraphicObject currentObject = world.getCurrentObject();

			switch (keyCode) {
			/* Adicionar novo v�rtice */
			case KeyEvent.VK_CONTROL:
				/*
				 * Duplica o �ltimo ponto do objeto atual, esse ponto � vai ser
				 * o ponto do mouse.
				 */
				final Point4D mousePoint = currentObject.getLastVertex().getPoint().clone();
				currentObject.createVertexAt(mousePoint);
				currentVertexIndex = currentObject.getLastVertexIndex();
				break;

			/* Excluir objeto */
			case KeyEvent.VK_R:
				world.remove(currentObject);
				world.removeCurrentObject();
				break;

			case KeyEvent.VK_UP:
				currentObject.translate(0, TRANSLATE);
				break;

			case KeyEvent.VK_DOWN:
				currentObject.translate(0, -TRANSLATE);
				break;

			case KeyEvent.VK_LEFT:
				currentObject.translate(-TRANSLATE, 0);
				break;

			case KeyEvent.VK_RIGHT:
				currentObject.translate(TRANSLATE, 0);
				break;

			case KeyEvent.VK_F1:
				currentObject.rotateZ(Math.toRadians(ROTATE));
				break;

			case KeyEvent.VK_F2:
				currentObject.rotateZ(Math.toRadians(-ROTATE));
				break;

			case KeyEvent.VK_F3:
				currentObject.scaleXY(SCALE_UP);
				break;

			case KeyEvent.VK_F4:
				currentObject.scaleXY(SCALE_DOWN);
				break;
			}
		}
	}

	/**
	 * Altera a cor do objeto atual.
	 * <p>
	 * Fun��o das teclas:
	 * <ul>
	 * <li>Tecla 1 soma +0.05 na cor Vermelha;</li>
	 * <li>Tecla 2 soma +0.05 na cor Verde;</li>
	 * <li>Tecla 3 soma +0.05 na cor Azul.</li>
	 * </ul>
	 * 
	 * @param e
	 *            Evento do mouse.
	 */
	private void updateCurrentObjectColor(final KeyEvent e) {
		if (!world.hasCurrentObject()) {
			return;
		}
		GraphicObject current = world.getCurrentObject();
		switch (e.getKeyCode()) {
		case KeyEvent.VK_1:
			current.incRed();
			break;
		case KeyEvent.VK_2:
			current.incGreen();
			break;
		case KeyEvent.VK_3:
			current.incBlue();
			break;
		case KeyEvent.VK_4:
			current.setColor(ColorUtils.colors[colorIndex]);
			colorIndex++;
			if (colorIndex > 13) {
				colorIndex = 0;
			}
			break;
		}
		render();
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
		final float xAxisOffset = cameraHalfWidth - camera.getAxis(1);
		final float yAxisOffset = cameraHalfHeight - camera.getAxis(3);

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
		render();
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
		render();
	}
}
