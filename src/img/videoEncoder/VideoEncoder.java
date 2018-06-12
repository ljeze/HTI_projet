package img.videoEncoder;

import java.util.stream.Stream;

import img.math.Matrices;
import img.math.Vector2D;
import img.math.transforms.DCT;
import img.prediction.DPCM;
import img.videoEncoder.io.EncodedFrame;
import img.videoEncoder.io.EncodedFrame.FrameType;
import img.videoEncoder.io.EncoderParams;

/**
 * Possède toutes les fonctions d'encodage / décodage utilisées dans le pipeline
 * de l'encodeur vidéo.
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
		return frameStream.map(new VideoEncodingPipeline(parameters));
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
		return frameStream.map(new VideoDecodingPipeline(parameters));
	}
	
	//==========================================================================
	// Fonctions intermédiaires utiles pour l'encodage / décodage.
	//==========================================================================
	
	/**
	 * Transformer la carte de compensation de mouvement par une prédiction
	 * DPCM.
	 * 
	 * @param blockMovementMap
	 *            carte de compensation de mouvement.
	 * @return carte de compensation de mouvement transformée par une prédiction
	 *         DPCM.
	 */
	/*package*/ static Vector2D[][] transformBlockMovementMap(final Vector2D[][] blockMovementMap)
	{
		return DPCM.encode(blockMovementMap, 1);
	}
	
	/**
	 * Obtenir la carte de compensation de mouvement <b>quantifiée</b> à partir
	 * de la prédiction DPCM de la matrice de originale.
	 * 
	 * @param transformedBlockMovementMap
	 *            prédiction DPCM carte de compensation de mouvement.
	 * @return carte de compensation de mouvement.
	 */
	public static Vector2D[][] inverseTransformBlockMovementMap(final Vector2D[][] transformedBlockMovementMap)
	{
		return DPCM.decode(transformedBlockMovementMap);
	}
	
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
	private static Vector2D computeBlockMovement(final int[][] frame1, final int[][] frame2, final int bx, final int by,
			final int blockW, final int blockH)
	{
		final int h = frame1.length,
				  w = frame1[0].length;
		
		// Mesure de disimilarité minimum obtenue.
		int minDisimilarity = Integer.MAX_VALUE;
		// Vecteur de déplacement obtenu pour ce min de disimilarité.
		Vector2D minMovement = new Vector2D(0, 0);
		
		// Déplacements maximums autorisés pour ne pas sortir de l'image.
		final int minI = Math.max(-2*blockW, (bx+blockW) - w),
				  maxI = Math.min(2*blockW, bx),
				  
				  minJ = Math.max(-2*blockH, (by+blockH) - h),
				  maxJ = Math.min(2*blockH, by);
		
		int disimilarity = 0;
		for (int y = by; y < by + blockH; ++y)
		{
			for (int x = bx; x < bx + blockW; ++x)
			{
				disimilarity += Math.abs(frame1[y][x] - frame2[y][x]);
			}
		}
		if (disimilarity == 0)
		{
			return new Vector2D(0, 0);
		}
		
		// Essayer pour différents déplacement possibles en x...
		for (int i = minI; i <= maxI; ++i)
		{
			// et différents déplacement possibles en y.
			for (int j = minJ; j <= maxJ; ++j)
			{
				// Calculer la disimilarité.
				disimilarity = 0;
				
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
	 * précédente et la trame actuelle.
	 * 
	 * @param prevFrame
	 *            trame précédente.
	 * @param frame
	 *            trame actuelle.
	 * @param blockW
	 *            largeur des blocs.
	 * @param blockH
	 *            hauteur des blocs.
	 * @return carte de compensation de mouvement des blocks entre la trame
	 *         précédente reconstruite et la trame actuelle.
	 */
	/*package*/ static Vector2D[][] computeBlockMovementMap(final int[][] prevFrame, final int[][] frame, final int blockW,
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
				movementMap[by][bx] = computeBlockMovement(frame, prevFrame, bx*blockW, by*blockH, blockW, blockH);
			}
		}
		
		return movementMap;
	}
	
	/**
	 * Obtenir la prédiction DPCM de la matrice de coefficients de la DCT par
	 * bloc quantifiée des erreurs de prédiction spécifiées.
	 * 
	 * @param errors
	 *            carte des erreurs de prédiction.
	 * @param dctBlockSize
	 *            taille des blocs DCT.
	 * @param quantifWeights
	 *            matrice des poids de quantification pour un bloc de la dct.
	 * @param quantifScale
	 *            échelle de quantification.
	 * @param frameType
	 *            type de la trame que l'on va envoyer.
	 * @return prédiction DPCM de la matrice de coefficients de la DCT par bloc
	 *         quantifiée des erreurs de prédiction spécifiées.
	 */
	/*package*/ static double[][] transformErrors(final int[][] errors, final int dctBlockSize,
			final int[][] quantifWeights, final double quantifScale, final FrameType frameType)
	{
		final int h = errors.length,
				  w = errors[0].length;
		
		double[][] transformedErrors = DCT.blockTransform(Matrices.toDouble(errors), dctBlockSize, dctBlockSize);
		
		// Quantification coefficients.

		// On sépare la boucle trame intra / prédite pour accélérer et ne pas
		// faire la vérification à chaque itération.
		
		switch (frameType)
		{
			// # Trame intra
			case I :
				for (int y = 0; y < h; y++)
				{
					for (int x = 0; x < w; x++)
					{
						transformedErrors[y][x] = Math.round(((transformedErrors[y][x]*16.0)/quantifWeights[y%dctBlockSize][x%dctBlockSize]) / 
												(2.0*quantifScale));
						//transformedErrors[y][x] = Math.round(transformedErrors[y][x] / 1.5)*1.5;
					}
				}
				break;
				
			// # Trame prédite
			case P :
				for (int y = 0; y < h; ++y)
				{
					for (int x = 0; x < w; ++x)
					{
						transformedErrors[y][x] = Math.round((transformedErrors[y][x]*16.0 / quantifWeights[y%dctBlockSize][x%dctBlockSize] - 
													Math.signum(transformedErrors[y][x])*quantifScale) / 
												(2*quantifScale));
					}
				}
				break;
		}
		
		// Prédiction DPCM sur ces coefficients.
		transformedErrors = DPCM.encode(transformedErrors, 1);
		
		return transformedErrors;
	}
	
	/**
	 * Obtenir la carte des erreurs de prédiction <b>quantifiée</b> à partir de
	 * la prédiction DPCM de la matrice de coefficient DCT par bloc.
	 * 
	 * @param transformedErrors
	 *            prédiction DPCM matrice des coefficents de la DCT par bloc des
	 *            erreurs de prédiction.
	 * @param dctBlockSize
	 *            taille des blocs DCT.
	 * @return carte des erreurs de prédiction à partir de la matrice de
	 *         coefficient DCT par bloc.
	 */
	public static int[][] inverseTransformErrors(final double[][] transformedErrors, final int dctBlockSize,
			final int[][] quantifWeights, final double quantifScale, final FrameType frameType)
	{
		final int h = transformedErrors.length,
				  w = transformedErrors[0].length;
		
		// On effectue le décodage DPCM.
		final double[][] dctErrors = DPCM.decode(transformedErrors);
		
		// Quantification inverse.
		switch (frameType)
		{
			case I :
				for (int y = 0; y < h; ++y)
				{
					for (int x = 0; x < w; ++x)
					{
						dctErrors[y][x] = (dctErrors[y][x] * 2*quantifScale) * quantifWeights[y%dctBlockSize][x%dctBlockSize] / 16;
					}
				}
				break;
				
			case P :
				for (int y = 0; y < h; ++y)
				{
					for (int x = 0; x < w; ++x)
					{
						dctErrors[y][x] = (dctErrors[y][x] * 2*quantifScale - Math.signum(transformedErrors[y][x])*quantifScale) * quantifWeights[y%dctBlockSize][x%dctBlockSize] / 16;
					}
				}
				break;
		}
		
		final double[][] predErrorDouble = DCT.inverseBlockTransform(dctErrors, dctBlockSize, dctBlockSize);
		final int[][] predError = new int[h][w];
		
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				//predError[y][x] = (int) Math.round(predErrorDouble[y][x]);
				predError[y][x] = (int) Math.max(0, Math.min(Math.round(predErrorDouble[y][x]), 255));
			}
		}
		
		return predError;
	}
	
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
	/*package*/ static int[][] computeErrors(final int[][] prevFrameRec, final int[][] frame, final Vector2D[][] blockMovementMap,
			final int blockW, final int blockH)
	{
		final int h = frame.length,
				  w = frame[0].length;
		
		final int[][] frameErrors = new int[h][w];
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				// Vecteur de déplacement du bloc contenant le pixel (x, y).
				final Vector2D blockMovement = blockMovementMap[y/blockH][x/blockW];
				frameErrors[y][x] = frame[y][x] - prevFrameRec[y - blockMovement.y()][x - blockMovement.x()];
				
//				frameErrors[y][x] = Math.max(-255, Math.min(frame[y][x] - prevFrameRec[y - blockMovement.y()][x - blockMovement.x()], 255));
			}
		}
		return frameErrors;
	}
	
	/**
	 * Reconstruire une trame P à partir des erreurs de prédictions et de la trame
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
	/*package*/ static int[][] reconstructP(final int[][] prevFrameRec, final int[][] predError,
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
				
				frameRec[y][x] = Math.max(0, Math.min(prevFrameRec[y - blockMovement.y()][x - blockMovement.x()] + predError[y][x], 255));
			}
		}
		return frameRec;
	}
	
	/**
	 * Reconstruire une trame I à partir des erreurs de prédictions.
	 * 
	 * @param predError
	 *            erreurs de prédiction.
	 * @return trame reconstruite.
	 */
	/*package*/ static int[][] reconstructI(final int[][] predError)
	{
		final int h = predError.length,
				  w = predError[0].length;
		
		final int[][] frameRec = new int[h][w];
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				frameRec[y][x] = Math.max(0, Math.min(predError[y][x], 255));
			}
		}
		return frameRec;
	}
}
