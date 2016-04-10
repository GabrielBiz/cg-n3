package main;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;

/**
 * Mundo que agrupa objetos gr�ficos.
 */
public class World implements Drawable {

	private final Camera camera = new Camera();
	private final List<GraphicObject> objects = new LinkedList<>();
	private GraphicObject currentObject;
	private Vertex currentVertex;

	public GraphicObject findObjectAt(final Point4D point) {
		return objects.stream().filter(o -> o.contains(point)).findFirst().orElse(null);
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
	 * Obt�m a v�rtice selecionada no mundo
	 * 
	 * @return v�rtice selecionada no mundo
	 */
	public Vertex getCurrentVertex() {
		return currentVertex;
	}

	/**
	 * Altera a v�rtice selecionada no mundo
	 * 
	 * @param currentVertex
	 *            v�rtice selecionada
	 */
	public void setCurrentVertex(Vertex currentVertex) {
		this.currentVertex = currentVertex;
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
