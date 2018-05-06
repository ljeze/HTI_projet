package img.math;

/**
 * Nombre complexe, utilisé ici en particulier dans la FFT.
 */
public class Complex
{
	/**
	 * Partie imaginaire.
	 */
	private final double img;
	/**
	 * Partie réelle.
	 */
	private final double real;
	
	public Complex(final double img, final double real)
	{
		this.img  = img;
		this.real = real;
	}
	
	/**
	 * Obtenir la partie imaginaire de ce nombre complexe.
	 * @return partie imaginaire.
	 */
	public double imgPart()
	{
		return img;
	}
	
	/**
	 * Obtenir la partie réelle de ce nombre complexe.
	 * @return partie réelle.
	 */
	public double realPart()
	{
		return real;
	}
	
	/**
	 * Obtenir l'addition de ce nombre complexe avec celui spécifié.
	 * 
	 * @param z
	 *            nombre complexe à additionner.
	 * @return addition de ce nombre complexe avec celui spécifié.
	 */
	public Complex add(final Complex z)
	{
		return new Complex(img+z.img, real+z.real);
	}
	
	/**
	 * Obtenir la soustraction entre ce nombre complexe et celui spécifié.
	 * 
	 * @param z
	 *            nombre complexe à soustraire.
	 * @return soustraction entre ce nombre complexe et celui spécifié.
	 */
	public Complex sub(final Complex z)
	{
		return new Complex(img-z.img, real-z.real);
	}
	
	/**
	 * Obtenir la multiplication entre ce nombre complexe et celui spécifié.
	 * 
	 * @param z
	 *            nombre complexe à multiplier.
	 * @return multiplication entre ce nombre complexe et celui spécifié.
	 */
	public Complex mult(final Complex z)
	{
		return new Complex(real*z.img + img*z.real, real*z.real - img*z.img);
	}
}
