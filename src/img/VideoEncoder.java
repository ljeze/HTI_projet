package img;

import java.util.function.Function;
import java.util.stream.Stream;

import img.math.Vector2D;

/**
 * Encodeur / decodeur d'une séquence d'images.
 */
public class VideoEncoder
{
	/**
	 * Taille des blocs utilisés pour la compensation de mouvement.
	 */
	private static final int BLOCK_SIZE = 8;
	
	/**
	 * Encoder un flux de trame.
	 * 
	 * @param frameStream
	 *            flux de trame à encoder.
	 * @return flux de trames encodées.
	 */
	public static Stream<EncodedFrame> encode(final Stream<int[][]> frameStream)
	{
		return frameStream.map(new Encoder());
	}
	
	/**
	 * Decoder un flux de trame.
	 * 
	 * @param frameStream
	 *            flux de trame à décoder.
	 * @return flux de trames décodées.
	 */
	public static Stream<int[][]> decode(final Stream<EncodedFrame> frameStream)
	{
		return frameStream.map(new Decoder());
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
		 * Trame précédente reconstruite.
		 */
		private int[][] prevFrameRec;
		
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
				prevFrameRec = frame;
				// L'envoyer sans prédiction.
				return new EncodedFrame(prevFrameRec);
			}
			
			// On calcul la carte de compensation de mouvement des blocs.
			Vector2D[][] blockMovementMap = getBlockMovementMap(prevFrameRec, frame, BLOCK_SIZE, BLOCK_SIZE);
			
			// On calcul les erreurs de prédiction entre la trame actuelle initiale et la trame précédente reconstruite.
			int[][] predError = predict(prevFrameRec, frame, blockMovementMap, BLOCK_SIZE, BLOCK_SIZE);
			// On calcul la trame actuelle reconstruite.
			int[][] frameRec = reconstruct(prevFrameRec, predError, blockMovementMap, BLOCK_SIZE, BLOCK_SIZE);
			
			prevFrameRec = frameRec;
			return new EncodedFrame(predError, blockMovementMap);
		}
	}
	
	/**
	 * Decodeur de trames avec compensation de mouvement.
	 */
	private static class Decoder implements Function<EncodedFrame, int[][]>
	{
		/**
		 * Trame précédente reconstruite.
		 */
		private int[][] prevFrameRec;
		
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
			// Première trame.
			if (prevFrameRec == null)
			{
				prevFrameRec = frame.getPredictionErrors();
				return prevFrameRec;
			}
			
			// La trame est une matrice d'erreurs de prédiction.
			int[][] predError = frame.getPredictionErrors();
			// On calcul la trame actuelle reconstruite.
			int[][] frameRec = reconstruct(prevFrameRec, predError, frame.getBlockMovementMap(), BLOCK_SIZE, BLOCK_SIZE);
			
			prevFrameRec = frameRec;
			return frameRec;
		}
	}
	
	
	//===================================================================================================
	// Prédicteurs sans compensation de mouvement.
	// 	TODO: Peut être par la suite essayer de factoriser ceux avec et sans.
	//===================================================================================================
	
	/**
	 * Prédire une trame à partir de la précédente, sans compensation de mouvement.
	 * 
	 * @param prevFrameRec
	 *            trame précédente reconstruite.
	 * @param frame
	 *            trame à prédire.
	 * @return matrice des erreurs.
	 */
	private static int[][] predict(final int[][] prevFrameRec, final int[][] frame)
	{
		final int h = frame.length,
				  w = frame[0].length;
		
		final int[][] framePred = new int[h][w];
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				framePred[y][x] = frame[y][x] - prevFrameRec[y][x];
			}
		}
		return framePred;
	}
	
	/**
	 * Reconstruire une trame à partir des erreurs de prédictions et de la trame
	 * précédente, sans compensation de mouvement.
	 * 
	 * @param prevFrameRec
	 *            trame précédente reconstruite.
	 * @param predError
	 *            erreurs de prédiction.
	 * @return trame reconstruite.
	 */
	private static int[][] reconstruct(final int[][] prevFrameRec, final int[][] predError)
	{
		final int h = prevFrameRec.length,
				  w = prevFrameRec[0].length;
		
		final int[][] frameRec = new int[h][w];
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				frameRec[y][x] = prevFrameRec[y][x] + predError[y][x];
			}
		}
		return frameRec;
	}
	
	/**
	 * Encodeur de trame sans compensation de mouvement.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private static class BasicEncoder implements Function<int[][], EncodedFrame>
	{
		/**
		 * Trame précédente reconstruite.
		 */
		private int[][] prevFrameRec;
		
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
				prevFrameRec = frame;
				// L'envoyer sans prédiction.
				return new EncodedFrame(prevFrameRec);
			}
			
			// On calcul les erreurs de prédiction entre la trame actuelle initiale et la trame précédente reconstruite.
			int[][] predError = predict(prevFrameRec, frame);
			// On calcul la trame actuelle reconstruite.
			int[][] frameRec = reconstruct(prevFrameRec, predError);
			
			prevFrameRec = frameRec;
			return new EncodedFrame(predError);
		}
	}
	
	/**
	 * Decodeur de trames sans compensation de mouvement.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private static class BasicDecoder implements Function<EncodedFrame, int[][]>
	{
		/**
		 * Trame précédente reconstruite.
		 */
		private int[][] prevFrameRec;
		
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
			// Première trame.
			if (prevFrameRec == null)
			{
				prevFrameRec = frame.getPredictionErrors();
				return prevFrameRec;
			}
			
			// La trame est une matrice d'erreurs de prédiction.
			int[][] predError = frame.getPredictionErrors();
			// On calcul la trame actuelle reconstruite.
			int[][] frameRec = reconstruct(prevFrameRec, predError);
			
			prevFrameRec = frameRec;
			return frameRec;
		}
	}
}
