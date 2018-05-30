package img.videoEncoder;

/**
 * Représente les paramètres de l'encodeur vidéo.
 */
public class EncoderParams
{
	/**
	 * Matrice de poids pour la quantification.
	 * Matrice de poids par défaut;
	 */
	private int[][] quantifWeights = 
	{
		{ 8 , 17, 18, 19, 21, 23, 25, 27},
		{ 17, 18, 19, 21, 23, 25, 27, 28},
		{ 20, 21, 22, 23, 24, 26, 28, 30},
		{ 21, 22, 23, 24, 26, 28, 30, 32},
		{ 22, 23, 24, 26, 28, 30, 32, 35},
		{ 23, 24, 26, 28, 30, 32, 35, 38},
		{ 25, 26, 28, 30, 32, 35, 38, 41},
		{ 27, 28, 30, 32, 35, 38, 41, 45},
	};
	
	/**
	 * Taille des blocs utilisés pour la compensation de mouvement.
	 */
	private int movementBlockSize = 8;
	/**
	 * Taille des blocs utilisés pour la DCT en bloc.
	 */
	private int dctBlockSize = 8;
	/**
	 * Echelle de quantification.
	 */
	private int quantifScale = 3;
	
	/**
	 * Définir la taille des blocks dct. Fonction temporaire, à faire : mettre
	 * plutôt en argument de decode et rajouter attribut encodingInfo à
	 * encodedFrame.
	 * 
	 * @param dctBlockSize
	 * @return paramètres de l'encodeur.
	 */
	public EncoderParams dctBlockSize(final int dctBlockSize)
	{
		this.dctBlockSize = dctBlockSize;
		return this;
	}
	
	/**
	 * Définir la taille des blocks pour la compensation de mouvement. Fonction
	 * temporaire, à faire : mettre plutôt en argument de decode et rajouter
	 * attribut encodingInfo à encodedFrame.
	 * 
	 * @param movementBlockSize
	 * @return paramètres de l'encodeur.
	 */
	public EncoderParams movementBlockSize(final int movementBlockSize)
	{
		this.movementBlockSize = movementBlockSize;
		return this;
	}
	
	/**
	 * Définir l'échelle de quantification.
	 * 
	 * @param quantifScale
	 *            échelle de quantification.
	 * @return paramètres de l'encodeur.
	 */
	public EncoderParams quantifierScale(final int quantifScale)
	{
		this.quantifScale = quantifScale;
		return this;
	}
	
	/**
	 * Obtenir la taille des bloc de prédiction de mouvement.
	 * @return taille des bloc de prédiction de mouvement.
	 */
	public int getMovementBlockSize()
	{
		return movementBlockSize;
	}
	
	/**
	 * Obtenir la taille des bloc de DCT.
	 * @return taille des bloc de DCT.
	 */
	public int getDctBlockSize()
	{
		return dctBlockSize;
	}
	
	/**
	 * Obtenir l'échelle de quantification.
	 * @return échelle de quantification.
	 */
	public int getQuantificationScale()
	{
		return quantifScale;
	}
	
	/**
	 * Obtenir la matrice des poids de quantification.
	 * @return matrice des poids de quantification.
	 */
	public int[][] getQuantificationWeights()
	{
		return quantifWeights;
	}
}
