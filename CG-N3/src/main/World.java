package main;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.media.opengl.GL;

/**
 * Mundo que agrupa objetos gr�ficos.
 */
public class World implements Drawable {

	private final Camera camera = new Camera();
	private final List<GraphicObject> objects = new LinkedList<>();
	private GraphicObject currentObject;

	public GraphicObject findObjectAt(final Point4D point) {
		return objects.stream().filter(o -> o.contains(o.transform.getInverseMatriz().transformPoint(point)))
				.findFirst().orElse(null);
	}

	public <Any> List<Any> getRecursive(Any node, Function<Any, List<Any>> get) {
		List<Any> all = new ArrayList<>();
		all.add(node);
		List<Any> children = get.apply(node);
		if (!children.isEmpty()) {
			for (Any c : children) {
				all.addAll(getRecursive(c, get));
			}
		}
		return all;
	}

	public GraphicObject findObjectAt(final Point4D point, final Point4D endPoint) {

		List<GraphicObject> all = new ArrayList<>();

		for (GraphicObject graphicObject : objects) {
			all.addAll(getRecursive(graphicObject, GraphicObject::getGrapicObjects));
		}

		all = all.stream().filter(o -> o.contains(o.transform.getInverseMatriz().transformPoint(point)))
				.collect(Collectors.toList());

		List<GraphicObject> inside = new ArrayList<>();
		for (GraphicObject go : all) {
			int intersects = 0;
			LinkedList<Vertex> vertices = go.getVertices();
			final int size = vertices.size();
			for (int i = 0; i < size; i++) {
				int endIndex = i < size - 1 ? i + 1 : 0;
				if (Line2D.linesIntersect(point.getX(), point.getY(), endPoint.getX(), endPoint.getY(),
						vertices.get(i).getX(), vertices.get(i).getY(), vertices.get(endIndex).getX(),
						vertices.get(endIndex).getY())) {
					intersects++;
				}
			}
			if (intersects % 2 != 0) {
				inside.add(go);
			}
		}
		if (inside.isEmpty()) {
			return null;
		}
		GraphicObject smaller = inside.get(0);
		for (GraphicObject go : inside) {
			if (go.getBBox().compareTo(smaller.getBBox()) < 0) {
				smaller = go;
			}
		}
		return smaller;
	}

	/**
	 * Altera o objeto selecionado no mundo.
	 * 
	 * @param object
	 *            Novo objeto a ser selecionado.
	 */
	public void setCurrentObject(final GraphicObject object) {
		currentObject = object;
	}

	/**
	 * Obt�m o objeto selecionado no mundo.
	 * 
	 * @return Objeto selecionado no mundo.
	 */
	public GraphicObject getCurrentObject() {
		return currentObject;
	}

	/**
	 * Deseleciona o objeto atual do mundo.
	 */
	public void removeCurrentObject() {
		currentObject = null;
	}

	/**
	 * Retorna se mundo possui um objeto selecionado.
	 * 
	 * @return <code>true</code> se o mundo possui um objeto selecionado,
	 *         <code>false</code> de outra maneira.
	 */
	public boolean hasCurrentObject() {
		return currentObject != null;
	}

	/**
	 * Obt�m a camera associada ao mundo.
	 * 
	 * @return {@link Camera} do mundo.
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * Adiciona um novo objeto gr�fico ao mundo.
	 * 
	 * @param graphicObject
	 *            Objeto a ser adicionado.
	 */
	public void add(GraphicObject graphicObject) {
		objects.add(graphicObject);
	}

	/**
	 * Remove um objeto gr�fico do mundo.
	 * 
	 * @param graphicObject
	 *            objeto a ser removido.
	 */
	public void remove(GraphicObject graphicObject) {
		objects.remove(graphicObject);
	}

	@Override
	public void draw(GL gl) {
		objects.forEach(o -> o.draw(gl));
		if (hasCurrentObject()) {
			final GraphicObject current = getCurrentObject();
			if (current.hasBBox()) {
				final BBox bbox = current.getBBox();
				bbox.draw(gl);
			}
		}
	}

}
