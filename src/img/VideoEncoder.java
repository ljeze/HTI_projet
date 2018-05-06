package img;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Encodeur / decodeur d'une séquence d'images.
 */
public class VideoEncoder
{
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
	 * Prédire une trame à partir de la précédente.
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
	 * précédente.
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
	 * Encodeur de trame.
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
			
			// On calcul les erreurs de prédiction entre la trame actuelle initiale et la trame précédente reconstruite.
			int[][] predError = predict(prevFrameRec, frame);
			// On calcul la trame actuelle reconstruite.
			int[][] frameRec = reconstruct(prevFrameRec, predError);
			
			prevFrameRec = frameRec;
			return new EncodedFrame(predError);
		}
	}
	
	/**
	 * Decodeur de trames.
	 */
	private static class Decoder implements Function<EncodedFrame, int[][]>
	{
		/**
		 * Trame précédente reconstruite.
		 */
		private int[][] prevFrameRec;
		
		// Trame actuelle initiale (non reconstruite).
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
