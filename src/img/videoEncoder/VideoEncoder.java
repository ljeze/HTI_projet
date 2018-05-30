package img.videoEncoder;

import java.util.function.Function;
import java.util.stream.Stream;

import img.math.Matrices;
import img.math.Vector2D;
import img.math.transforms.DCT;
import img.videoEncoder.EncodedFrame.FrameType;

/**
 * Encodeur / decodeur d'une séquence d'images.
 */
public class VideoEncoder
{
	//==========================================================================
	// Fonctions principales d'encodage / décodage.
	//==========================================================================
	/**
	 * Encoder un flux de trame.
	 * 
	 * @param frameStream
	 *            flux de trame à encoder.
	 * @return flux de trames encodées.
	 */
	public static Stream<EncodedFrame> encode(final Stream<int[][]> frameStream, final EncoderParams parameters)
	{
		return frameStream.map(new Encoder(parameters));
	}
	
	/**
	 * Decoder un flux de trame.
	 * 
	 * @param frameStream
	 *            flux de trame à décoder.
	 * @return flux de trames décodées.
	 */
	public static Stream<int[][]> decode(final Stream<EncodedFrame> frameStream, final EncoderParams parameters)
	{
		return frameStream.map(new Decoder(parameters));
	}
	
	//==========================================================================
	// Fonctions intermédiaires utiles pour l'encodage / décodage.
	//==========================================================================
	/**
	 * Obtenir le vecteur de déplacement dans le bloc spécifié entre deux
	 * trames, tel que la somme des différences dans le bloc translaté soit
	 * minimum.
	 * 
	 * @param frame1
	 *            trame 1, référence.
	 * @param frame2
	 *            trame 2, où l'on cherche le vecteur de translation.
	 * @param bx
	 *            position x réel du coin supérieur gauche du bloc dans la trame 1.
	 * @param by
	 *            position y réel du coin supérieur gauche du bloc dans la trame 1.
	 * @param blockW
	 *            largeur du bloc.
	 * @param blockH
	 *            hauteur du bloc.
	 * @return vecteur de déplacement optimal dans le bloc spécifié entre deux
	 *         trames.
	 */
	private static Vector2D getBlockMovement(final int[][] frame1, final int[][] frame2, final int bx, final int by,
			final int blockW, final int blockH)
	{
		final int h = frame1.length,
				  w = frame1[0].length;
		
		// Mesure de disimilarité minimum obtenue.
		int minDisimilarity = Integer.MAX_VALUE;
		// Vecteur de déplacement obtenu pour ce min de disimilarité.
		Vector2D minMovement = new Vector2D(0, 0);
		
		// Déplacements maximums autorisés pour ne pas sortir de l'image.
		final int minI = Math.max(-16, (bx+blockW) - w),
				  maxI = Math.min(16, bx),
				  
				  minJ = Math.max(-16, (by+blockH) - h),
				  maxJ = Math.min(16, by);
		
		// Essayer pour différents déplacement possibles en x...
		for (int i = minI; i <= maxI; ++i)
		{
			// et différents déplacement possibles en y.
			for (int j = minJ; j <= maxJ; ++j)
			{
				// Calculer la disimilarité.
				int disimilarity = 0;
				
				for (int y = by; y < by + blockH; ++y)
				{
					for (int x = bx; x < bx + blockW; ++x)
					{
						disimilarity += Math.abs(frame1[y][x] - frame2[y-j][x-i]);
					}
				}
				
				// Si on obtient un nouveau min...
				if (disimilarity < minDisimilarity)
				{
					minDisimilarity = disimilarity;
					minMovement = new Vector2D(i, j);
					
					// Si on obtient 0, on ne pourra pas avoir mieux, quitter la boucle.
					if (disimilarity == 0)
					{
						return minMovement;
					}
				}
			}
		}
		
		return minMovement;
	}
	
	/**
	 * Obtenir la carte de compensation de mouvement des blocks entre la trame
	 * précédente reconstruite et la trame actuelle.
	 * 
	 * @param prevFrameRec
	 *            trame précédente reconstruite.
	 * @param frame
	 *            trame actuelle.
	 * @param blockW
	 *            largeur des blocs.
	 * @param blockH
	 *            hauteur des blocs.
	 * @return carte de compensation de mouvement des blocks entre la trame
	 *         précédente reconstruite et la trame actuelle.
	 */
	private static Vector2D[][] getBlockMovementMap(final int[][] prevFrameRec, final int[][] frame, final int blockW,
			final int blockH)
	{
		final int nBlockH = frame.length    / blockH,
				  nBlockW = frame[0].length / blockW;
		
		final Vector2D[][] movementMap = new Vector2D[nBlockH][nBlockW];
		
		// Pour chaque bloc...
		for (int by = 0; by < nBlockH; ++by) 		// Indice position y.
		{
			for (int bx = 0; bx < nBlockW; ++bx) 	// Indice position x.
			{
				movementMap[by][bx] = getBlockMovement(frame, prevFrameRec, bx*blockW, by*blockH, blockW, blockH);
			}
		}
		
		return movementMap;
	}
	
	/**
	 * Obtenir la matrice de coefficients de la DCT par bloc des erreurs de
	 * prédiction spécifiées.
	 * 
	 * @param predError
	 *            carte des erreurs de prédiction.
	 * @param dctBlockSize
	 *            taille des blocs DCT.
	 * @param quantifWeights
	 *            matrice des poids de quantification pour un bloc de la dct.
	 * @param firstFrame
	 *            true si cette trame est la première, ie: "Intra".
	 * @return matrice de coefficients de la DCT par bloc des erreurs de
	 *         prédiction spécifiées.
	 */
	private static double[][] getPredictionErrorCoeff(final int[][] predError, final int dctBlockSize,
			final int[][] quantifWeights, final boolean firstFrame)
	{
		final int h = predError.length,
				  w = predError[0].length;
		
		double[][] predErrorCoeffs = DCT.blockTransform(Matrices.toDouble(predError), dctBlockSize, dctBlockSize);
		
		// Taille / échelle du quantificateur.
		final double quantifScale = 1;
		
		// Quantification coefficients.

		// On sépare la boucle trame intra / prédite pour accélérer et ne pas
		// faire la vérification à chaque itération.
		if (firstFrame)	// # Trame intra
		{
			for (int y = 0; y < h; y++)
			{
				for (int x = 0; x < w; x++)
				{
					predErrorCoeffs[y][x] = ((predErrorCoeffs[y][x]*16.0)/quantifWeights[y%dctBlockSize][x%dctBlockSize]) / 
											(2.0*quantifScale);
				}
			}
		}
		else			// # Trame prédite
		{
			for (int y = 0; y < h; ++y)
			{
				for (int x = 0; x < w; ++x)
				{
					predErrorCoeffs[y][x] = 
							(predErrorCoeffs[y][x]*16.0/quantifWeights[y%dctBlockSize][x%dctBlockSize] - 
												Math.signum(predErrorCoeffs[y][x])*quantifScale) / 
											(2*quantifScale);
				}
			}
		}
		return predErrorCoeffs;
	}
	
	/**
	 * Obtenir la carte des erreurs de prédiction <b>quantifiée</b> à partir de
	 * la matrice de coefficient DCT par bloc.
	 * 
	 * @param predError
	 *            matrice des coefficents de la DCT par bloc des erreurs de
	 *            prédiction.
	 * @param dctBlockSize
	 *            taille des blocs DCT.
	 * @return carte des erreurs de prédiction à partir de la matrice de
	 *         coefficient DCT par bloc.
	 */
	private static int[][] getPredictionErrorMap(final double[][] predErrorDCT, final int dctBlockSize)
	{
		final int h = predErrorDCT.length,
				  w = predErrorDCT[0].length;
		
		final double[][] predErrorDouble = DCT.inverseBlockTransform(predErrorDCT, dctBlockSize, dctBlockSize);
		final int[][] predError = new int[h][w];
		
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				predError[y][x] = (int) Math.max(-255, Math.min(predErrorDouble[y][x], 255));
			}
		}
		
		return predError;
	}
	
	//===================================================================================================
	// Prédicteurs avec compensation de mouvement.
	//===================================================================================================
	/**
	 * Prédire une trame à partir de la précédente, avec compensation de
	 * mouvement.
	 * 
	 * @param prevFrameRec
	 *            trame précédente reconstruite.
	 * @param frame
	 *            trame à prédire.
	 * @param blockMovementMap
	 *            carte de déplacement des blocs.
	 * @param blockW
	 *            largeur des blocs.
	 * @param blockH
	 *            hauteur des blocs.
	 * @return matrice des erreurs.
	 */
	private static int[][] predict(final int[][] prevFrameRec, final int[][] frame, final Vector2D[][] blockMovementMap,
			final int blockW, final int blockH)
	{
		final int h = frame.length,
				  w = frame[0].length;
		
		final int[][] framePred = new int[h][w];
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				// Vecteur de déplacement du bloc contenant le pixel (x, y).
				final Vector2D blockMovement = blockMovementMap[y/blockH][x/blockW];
				
				framePred[y][x] = frame[y][x] - prevFrameRec[y - blockMovement.y()][x - blockMovement.x()];
			}
		}
		return framePred;
	}
	
	/**
	 * Reconstruire une trame à partir des erreurs de prédictions et de la trame
	 * précédente, avec compensation de mouvement.
	 * 
	 * @param prevFrameRec
	 *            trame précédente reconstruite.
	 * @param predError
	 *            erreurs de prédiction.
	 * @param blockMovementMap
	 *            carte de déplacement des blocs.
	 * @param blockW
	 *            largeur des blocs.
	 * @param blockH
	 *            hauteur des blocs.
	 * @return trame reconstruite.
	 */
	private static int[][] reconstruct(final int[][] prevFrameRec, final int[][] predError,
			final Vector2D[][] blockMovementMap, final int blockW, final int blockH)
	{
		final int h = prevFrameRec.length,
				  w = prevFrameRec[0].length;
		
		final int[][] frameRec = new int[h][w];
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				// Vecteur de déplacement du bloc contenant le pixel (x, y).
				final Vector2D blockMovement = blockMovementMap[y/blockH][x/blockW];
				
				frameRec[y][x] = prevFrameRec[y - blockMovement.y()][x - blockMovement.x()] + predError[y][x];
			}
		}
		return frameRec;
	}
	
	/**
	 * Encodeur de trame avec compensation de mouvement.
	 */
	private static class Encoder implements Function<int[][], EncodedFrame>
	{
		/**
		 * Paramètres d'encodage.
		 */
		private final EncoderParams parameters;
		/**
		 * Trame précédente reconstruite.
		 */
		private int[][] prevFrameRec;
		
		public Encoder(final EncoderParams parameters)
		{
			this.parameters = parameters;
		}
		
		/**
		 * Encode une trame.
		 * 
		 * @param frame
		 *            trame actuelle initiale (non reconstruite).
		 * @return trame encodée.
		 */
		@Override
		public EncodedFrame apply(final int[][] frame)
		{
			// Si l'on est sur la première trame.
			if (prevFrameRec == null)
			{
				// On calcul les coefficients DCT de ces erreurs (l'image) et on applique la quantification.
				double[][] predErrorCoeff = getPredictionErrorCoeff(frame, parameters.getDctBlockSize(), parameters.getQuantificationWeights(), true);
				prevFrameRec = getPredictionErrorMap(predErrorCoeff, parameters.getDctBlockSize());
				// L'envoyer sans prédiction.
				return EncodedFrame.intraFrame(predErrorCoeff);
			}
			
			// On calcul la carte de compensation de mouvement des blocs.
			Vector2D[][] blockMovementMap = getBlockMovementMap(prevFrameRec, frame, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
			
			// On calcul les erreurs de prédiction entre la trame actuelle initiale et la trame précédente reconstruite.
			int[][] predError = predict(prevFrameRec, frame, blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
			
			// On calcul les coefficients DCT de ces erreurs et on applique la quantification.
			double[][] predErrorCoeff = getPredictionErrorCoeff(predError, parameters.getDctBlockSize(), parameters.getQuantificationWeights(), false);
			
			// On calcul la trame actuelle reconstruite.
			int[][] frameRec = reconstruct(prevFrameRec, getPredictionErrorMap(predErrorCoeff, parameters.getDctBlockSize()), blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
			
			prevFrameRec = frameRec;
			return EncodedFrame.predictedFrame(predErrorCoeff, blockMovementMap);
		}
	}
	
	/**
	 * Décodeur de trames avec compensation de mouvement.
	 */
	private static class Decoder implements Function<EncodedFrame, int[][]>
	{
		/**
		 * Paramètres d'encodage.
		 */
		private final EncoderParams parameters;
		/**
		 * Trame précédente reconstruite.
		 */
		private int[][] prevFrameRec;
		
		public Decoder(final EncoderParams parameters)
		{
			this.parameters = parameters;
		}
		
		/**
		 * Décode une trame.
		 * 
		 * @param frame
		 *            trame encodée.
		 * @return trame décodée.
		 */
		@Override
		public int[][] apply(final EncodedFrame frame)
		{
			// Trame Intra.
			if (frame.getType() == FrameType.I)
			{
				prevFrameRec = getPredictionErrorMap(frame.getPredictionErrorCoeffs(), parameters.getDctBlockSize());
				return prevFrameRec;
			}
			
			// La trame est une matrice d'erreurs de prédiction.
			int[][] predError = getPredictionErrorMap(frame.getPredictionErrorCoeffs(), parameters.getDctBlockSize());
			// On calcul la trame actuelle reconstruite.
			int[][] frameRec = reconstruct(prevFrameRec, predError, frame.getBlockMovementMap(), parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
			
			prevFrameRec = frameRec;
			return frameRec;
		}
	}
}