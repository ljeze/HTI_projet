package img.math.transforms;

import img.math.Complex;

/**
 * Classe utilitaire pour la tranformée de Fourier rapide (FFT).
 */
public class FFT
{
	/**
	 * Faire une transformée de fourrier rapide (FFT) du vecteur spécifié.<br>
	 * <b>La taille du vecteur doit être une puissance de deux.</b>
	 * 
	 * @param vector
	 *            vecteur à transformer.
	 * @return FFT du vecteur.
	 * @throws IllegalArgumentException
	 *             si la taille du vecteur n'est pas une puissance de 2.
	 */
	public static Complex[] transform(final double[] vector) throws IllegalArgumentException
	{
		// Vérifier si la taille du vecteur n'est pas une puissance de deux.
		
		// On utilise la représentation binaire du nombre pour vérifier
		// rapidement.
		if ((vector.length & (vector.length - 1)) != 0)
		{
			throw new IllegalArgumentException("La taille du vecteur (taille de " + vector.length + ") n'est pas une puissance de deux.");
		}
		
		// Transformer le vecteur de double en vecteur de nombres complexes.
		final Complex[] initialVector = new Complex[vector.length];
		
		for (int i = 0; i < vector.length; ++i)
		{
			initialVector[i] = new Complex(0, vector[i]);
		}
		
		return recursiveTransform(initialVector);
	}
	
	/**
	 * Faire une transformée inverse de fourrier rapide (FFT) du vecteur
	 * spécifié.<br>
	 * <b>La taille du vecteur doit être une puissance de deux.</b>
	 * 
	 * @param vector
	 *            vecteur à transformer inversement.
	 * @return FFT inverse du vecteur.
	 * @throws IllegalArgumentException
	 *             si la taille du vecteur n'est pas une puissance de 2.
	 */
	public static Complex[] inverseTransform(final Complex[] vectorFFT) throws IllegalArgumentException
	{
		// Vérifier si la taille du vecteur n'est pas une puissance de deux.
		
		// On utilise la représentation binaire du nombre pour vérifier
		// rapidement.
		if ((vectorFFT.length & (vectorFFT.length - 1)) != 0)
		{
			throw new IllegalArgumentException("La taille du vecteur (taille de " + vectorFFT.length + ") n'est pas une puissance de deux.");
		}
		
		final Complex[] vectorComplex = recursiveInverseTransform(vectorFFT);
		
		// Transformer le vecteur de double en vecteur de nombres complexes.
		/*final double[] vector = new double[vectorComplex.length];
		
		for (int i = 0; i < vector.length; ++i)
		{
			vector[i] = vectorComplex[i];
		}
		*/
		
		return vectorComplex;
	}
	
	
	/**
	 * Sous échantillonnage / décimation du vecteur spécifié d'un facteur 1/2
	 * avec un décalage donné. Ainsi,
	 * <ul>
	 * <li>Si le décalage est de 0 => on ne prendra que les termes d'indice
	 * pair</li>
	 * <li>Si le décalage est de 1 => on ne prendra que les termes d'indice
	 * impair</li>
	 * </ul>
	 * 
	 * @param vector
	 *            vecteur à sous échantillonner.
	 * @param offset
	 *            décalage pour le 1er échantillon.
	 * @return
	 */
	private static Complex[] decimate(final Complex[] vector, final int offset)
	{
		final Complex[] decimatedVector = new Complex[vector.length/2];
		for (int i = 0; i < decimatedVector.length; ++i)
		{
			decimatedVector[i] = vector[offset + i*2];
		}
		return decimatedVector;
	}
	
	/**
	 * Fonction réccursive pour calculer la FFT d'un vecteur donné. <br>
	 * L'algorithme utilisé est l'algorithme de Cooley-Tukey. (<a href=
	 * "https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm">Source1</a>, 
	 * <a href="http://chamilo2.grenet.fr/inp/courses/PHELMAA2SICOM4PMSTNS/document/BE/BE2/FFT.pdf">Source 2</a>)<br>
	 * <b>La taille du vecteur doit être une puissance de deux.</b>
	 * 
	 * @param vector
	 *            vecteur complexe à transformer.
	 * @return vecteur transformé.
	 */
	private static Complex[] recursiveTransform(final Complex[] vector)
	{
		final int n = vector.length;
		
		// Vecteur indécomposable d'un seul élément.
		if (n == 1)
		{
			return vector;
		}
		
		// On effectue maintenant la FFT sur les termes pairs et impairs.
		final Complex[] evenFFT, oddFFT;
		
		// # Termes pairs :
		evenFFT = recursiveTransform(decimate(vector, 0));
		
		// # Termes impairs :
		oddFFT = recursiveTransform(decimate(vector, 1));
		
		// On rassemble les deux résultats.
		final Complex[] vectorFFT = new Complex[n];
		
		for (int j = 0; j < n/2; ++j)
		{
			// Argument de l'exponentielle.
			double expArg = -2*j* Math.PI/n;
			// Facteur (omega_j * oddFFT[i]) avec omega_j = exp(-2*i*pi*j/n)) 
			Complex omegaOdd = Complex.exp(expArg).mult(oddFFT[j]);
			
			vectorFFT[j] = evenFFT[j].add(omegaOdd);
			vectorFFT[j + n/2] = evenFFT[j].sub(omegaOdd);
		}
		
		return vectorFFT;
	}
	
	/**
	 * Fonction réccursive pour calculer l'inverse de la FFT d'un vecteur donné.
	 * <br>
	 * <b>La taille du vecteur doit être une puissance de deux.</b>
	 * 
	 * @param vector
	 *            vecteur complexe auquel appliquer la transformation inverse.
	 * @return vecteur transformé inverse.
	 */
	public static Complex[] recursiveInverseTransform(final Complex[] vectorFFT)
	{
		final int n = vectorFFT.length;
		
		Complex[] vector = new Complex[n];
		// Conjuguer le vecteur.
		for (int i = 0; i < n; ++i)
		{
			vector[i] = vectorFFT[i].conjugate();
		}
		
		// Calculer sa FFT.
		vector = recursiveTransform(vector);
		
		for (int i = 0; i < n; ++i)
		{
			vector[i] = vector[i].conjugate().mult(1.0/n);
		}
		
		return vector;
	}
	
}
