package img.videoEncoder;

import static img.videoEncoder.VideoEncoder.computeBlockMovementMap;
import static img.videoEncoder.VideoEncoder.computeErrors;
import static img.videoEncoder.VideoEncoder.inverseTransformBlockMovementMap;
import static img.videoEncoder.VideoEncoder.inverseTransformErrors;
import static img.videoEncoder.VideoEncoder.reconstructI;
import static img.videoEncoder.VideoEncoder.reconstructP;
import static img.videoEncoder.VideoEncoder.transformBlockMovementMap;
import static img.videoEncoder.VideoEncoder.transformErrors;

import java.util.Arrays;
import java.util.function.Function;

import img.Images;
import img.math.Matrices;
import img.math.Vector2D;
import img.videoEncoder.io.EncodedFrame;
import img.videoEncoder.io.EncodedFrame.FrameType;
import img.videoEncoder.io.EncoderParams;
import test.plot.Plot;

/**
 * Pipeline d'encodage vidéo.
 */
public class VideoEncodingPipeline implements Function<int[][], EncodedFrame>
{
	/**
	 * Paramètres d'encodage.
	 */
	private final EncoderParams parameters;
	/**
	 * Trame précédente reconstruite.
	 */
	private int[][] prevFrameRec;
	/**
	 * Trame précédente.
	 */
	private int[][] prevFrame;
	
	public VideoEncodingPipeline(final EncoderParams parameters)
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
		/**
		 * Matrice des erreurs de prédiction.
		 */
		final int[][] errors;
		/**
		 * Matrice des erreurs transformée.
		 */
		final double[][] transformedErrors;
		/**
		 * Trame actuelle reconstruite.
		 */
		final int[][] frameRec;
		
		// Si l'on est sur la première trame.
		if (prevFrameRec == null)
		{
			// La matrice d'erreurs de prédiction est l'image même.
			errors = frame;
			
			// On calcul les coefficients DCT de ces erreurs (l'image) et on applique la quantification.
			transformedErrors = transformErrors(errors, parameters.getDctBlockSize(),
					parameters.getQuantificationWeights(), parameters.getQuantificationScale(), FrameType.I);
			
			// On reconstruit la trame.
			frameRec = reconstructI(inverseTransformErrors(transformedErrors, parameters.getDctBlockSize(), parameters.getQuantificationWeights(), parameters.getQuantificationScale(), FrameType.I));
			
			prevFrameRec = frameRec;
			prevFrame    = frame;
			
			// L'envoyer sans prédiction.
			return EncodedFrame.intraFrame(transformedErrors);
		}
		
		final Vector2D[][] transformedBlockMovementMap;
		
		// On calcul la carte de compensation de mouvement des blocs.
		final Vector2D[][] blockMovementMap = computeBlockMovementMap(prevFrame, frame, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		// On calcul les erreurs de prédiction entre la trame actuelle initiale et la trame précédente reconstruite.
		errors = computeErrors(prevFrameRec, frame, blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		// On calcul la carte de compensation de mouvement transformée.
		transformedBlockMovementMap = transformBlockMovementMap(blockMovementMap);
		
		// On calcul les coefficients DCT de ces erreurs et on applique la quantification puis prédiction DPCM.
		transformedErrors = transformErrors(errors, parameters.getDctBlockSize(),
				parameters.getQuantificationWeights(), parameters.getQuantificationScale(), FrameType.P);
		
		//Plot.showImg(Images.grayToJavaImg(Matrices.map(inverseTransformErrors(transformedErrors, parameters.getDctBlockSize(), parameters.getQuantificationWeights(), parameters.getQuantificationScale(), FrameType.P), -255, 255, 0, 255)));
		// On calcul la trame actuelle reconstruite.
		frameRec = reconstructP(prevFrameRec, inverseTransformErrors(transformedErrors, parameters.getDctBlockSize(), parameters.getQuantificationWeights(), parameters.getQuantificationScale(), FrameType.P), 
											 inverseTransformBlockMovementMap(transformedBlockMovementMap),
									   parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		prevFrameRec = frameRec;
		prevFrame    = frame;
		return EncodedFrame.predictedFrame(transformedErrors, transformedBlockMovementMap);
	}
}