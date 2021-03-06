package img.math;

/**
 * Vecteur de déplacement 2D.
 */
public class Vector2D
{
	/**
	 * Coordonnée x.
	 */
	private final double x;
	/**
	 * Coordonnée y.
	 */
	private final double y;
	
	public Vector2D(final double x, final double y)
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
		return (int) x;
	}
	
	/**
	 * Obtenir la coordonnée y de ce vecteur.
	 * @return coordonnée y de ce vecteur.
	 */
	public int y()
	{
		return (int) y;
	}
	
	/**
	 * Obtenir la coordonnée x de ce vecteur.
	 * @return coordonnée x de ce vecteur.
	 */
	public double xDouble()
	{
		return x;
	}
	
	/**
	 * Obtenir la coordonnée y de ce vecteur.
	 * @return coordonnée y de ce vecteur.
	 */
	public double yDouble()
	{
		return y;
	}
	
	/**
	 * Additionner ce vecteur et un autre vecteur spécifié.
	 * 
	 * @param other
	 *            autre vecteur à additionner.
	 * @return somme des deux vecteurs.
	 */
	public Vector2D plus(final Vector2D other)
	{
		return new Vector2D(x + other.x, y + other.y);
	}
	
	/**
	 * Soustraire à ce vecteur un autre vecteur spécifié.
	 * 
	 * @param other
	 *            autre vecteur à soustraire.
	 * @return différence de ce vecteur avec l'autre vecteur.
	 */
	public Vector2D minus(final Vector2D other)
	{
		return new Vector2D(x - other.x, y - other.y);
	}
	
	public Vector2D times(final double f)
	{
		return new Vector2D(x*f, y*f);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)x;
		result = prime * result + (int)y;
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj == null)
		{
			return false;
		}
		
		if (getClass() != obj.getClass())
		{
			return false;
		}
		
		Vector2D other = (Vector2D) obj;
		if (x != other.x || y != other.y)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
}
