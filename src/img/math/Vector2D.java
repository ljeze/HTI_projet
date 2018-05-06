package img.math;

/**
 * Vecteur de déplacement 2D.
 */
public class Vector2D
{
	/**
	 * Coordonnée x.
	 */
	private final int x;
	/**
	 * Coordonnée y.
	 */
	private final int y;
	
	public Vector2D(final int x, final int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Obtenir la coordonnée x de ce vecteur.
	 * @return coordonnée x de ce vecteur.
	 */
	public int x()
	{
		return x;
	}
	
	/**
	 * Obtenir la coordonnée y de ce vecteur.
	 * @return coordonnée y de ce vecteur.
	 */
	public int y()
	{
		return y;
	}
}
