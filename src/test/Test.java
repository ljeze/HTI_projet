package test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import img.EncodedFrame;
import img.Images;
import img.VideoEncoder;
import img.Videos;
import img.math.Complex;
import img.math.transforms.DCT;
import img.math.transforms.FFT;
import test.plot.Plot;

/**
 * Classe contenant tous les tests.
 */
public class Test
{
	/**
	 * Obtenir le chemin vers un fichier de test interne au projet.
	 * 
	 * @param filename
	 *            nom du fichier.
	 * @return chemin vers un fichier de test interne au projet.
	 */
	public static Path getTestFile(final String filename) throws IllegalArgumentException
	{
		try
		{
			return Paths.get(Test.class.getResource("files").toURI()).resolve(filename);
		} catch (URISyntaxException e)
		{
			throw new IllegalArgumentException("Le fichier spécifié est inexistant : " + e.getMessage());
		}
	}
	
	/**
	 * Tester la FFT.
	 */
	public static void testFFT()
	{
		double[] sampleVector = IntStream.range(0, 32).mapToDouble(x->x).toArray();
		Complex[] sampleVectorFFT = FFT.transform(sampleVector);
		
		System.out.println("X = " + Arrays.toString(sampleVector));
		System.out.println("FFT(X) = " + Arrays.toString(sampleVectorFFT));
		System.out.println("FFT-1(FFT(X)) = " + Arrays.toString(FFT.inverseTransform(sampleVectorFFT)));
	}
	
	/**
	 * Tester la DCT.
	 */
	public static void testDCT()
	{
		double[] sampleVector = {1, 2, 3, 4};
		double[] sampleVectorDCT = DCT.transform(sampleVector);
		/*
		sampleVectorDCT[1] *= -1;
		sampleVectorDCT[1] *= -1;
		*/
		System.out.println("X = " + Arrays.toString(sampleVector));
		System.out.println("DCT(X) = " + Arrays.toString(sampleVectorDCT));
		System.out.println("DCT-1(DCT(X)) = " + Arrays.toString(DCT.inverseTransform(sampleVectorDCT)));
	}
	
	/**
	 * Tester la lecture d'images et leur affichage.
	 * @throws FileNotFoundException
	 */
	public static void testImageRead() throws FileNotFoundException
	{
		final int[][] img = Images.readGray(Paths.get("C:\\Users\\Loic\\Pictures\\Fujiview.jpg"));
		Plot.showImg(Images.toJavaImg(img));
	}
	
	/**
	 * Tester l'encodage d'une séquence.
	 * @throws FileNotFoundException
	 */
	public static void testVideoEncoding() throws FileNotFoundException
	{
		// Exemple : charger une séquence de 5 images nommées sequence1.jpg, sequence2.jpg, ..., sequence5.jpg .
		Stream<EncodedFrame> encodedSequence = VideoEncoder.encode(
			Videos.readGray(getTestFile("mas"))
		);
		
		VideoEncoder.decode(encodedSequence)
					.map(Images::toJavaImg)
					.forEach(Plot::showImg);
	}
	
	public static void main(final String[] args)
	{
		// testFFT();
		// testDCT();
		// testImageRead();
		
		/*try
		{
			testVideoEncoding();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
