package img.videoEncoder;

import java.util.function.Function;

import img.videoEncoder.EncodedFrame.FrameType;
import static img.videoEncoder.VideoEncoder.*;

/**
 * Pipeline de décodage vidéo.
 */
public class VideoDecodingPipeline implements Function<EncodedFrame, int[][]>
{
	/**
	 * Paramètres d'encodage.
	 */
	private final EncoderParams parameters;
	/**
	 * Trame précédente reconstruite.
	 */
	private int[][] prevFrameRec;
	
	public VideoDecodingPipeline(final EncoderParams parameters)
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
			prevFrameRec = inverseTransformErrors(frame.getTransformedErrors(), parameters.getDctBlockSize());
			return prevFrameRec;
		}
		
		// La trame est une matrice d'erreurs de prédiction.
		int[][] errors = inverseTransformErrors(frame.getTransformedErrors(), parameters.getDctBlockSize());
		// On calcul la trame actuelle reconstruite.
		int[][] frameRec = reconstruct(prevFrameRec, errors, frame.getBlockMovementMap(), parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		prevFrameRec = frameRec;
		return frameRec;
	}
}