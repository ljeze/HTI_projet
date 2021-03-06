package test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import img.Images;
import img.Videos;
import img.math.Complex;
import img.math.Matrices;
import img.math.transforms.DCT;
import img.math.transforms.FFT;
import img.prediction.DPCM;
import img.videoEncoder.VideoEncoder;
import img.videoEncoder.io.EncodedFrame;
import img.videoEncoder.io.EncoderParams;
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
		
		// Nombre de tests échoués.
		int nFail = 0;
		
		System.out.println("______________ Tests vecteurs aléatoires ______________________");
		
		for (int i = 0; i < 500; ++i)
		{
			double[] randomVector = IntStream.range(0, 32).mapToDouble(k->(Math.random()-0.5)*10000).toArray();
			double[] randomVectorRec = DCT.inverseTransform(DCT.transform(randomVector));
			System.out.println(Arrays.toString(randomVector) + "\n" + Arrays.toString(randomVectorRec));
			// Tester si le contenu en entier est identique.
			for (int k = 0; k < randomVector.length; ++k)
			{
				if ((int)randomVector[k] != (int)randomVectorRec[k])
				{
					System.out.println("==| Echec du test |================");
					++nFail;
					continue;
				}
			}
			System.out.println("==| Succès du test |================");
		}
		
		if (nFail == 0)
		{
			System.out.println("Tous les tests ont été passés avec succès.");
		}
		else
		{
			System.out.println("Echec de " + nFail + " test.");
		}
	}
	
	/**
	 * Tester la DCT en 2D.
	 * @throws IllegalArgumentException 
	 * @throws FileNotFoundException 
	 */
	public static void testDCT2D()
	{
		final int h = 8,
				  w = 8;
		
		double[][] randomMatrix = new double[h][w];
		
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				randomMatrix[y][x] = (Math.random()-0.5)*10000;
			}
		}
		
		double[][] randomMatrixRec = DCT.inverseTransform2D(DCT.transform2D(randomMatrix));
		
		System.out.println(Arrays.deepToString(randomMatrix) + "\n" + Arrays.deepToString(randomMatrixRec));
		
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				if ((int)randomMatrix[y][x] != (int)randomMatrixRec[y][x])
				{
					System.out.println("==| Echec du test |================");
					//++nFail;
					//continue;
					return;
				}
			}
		}
		
		System.out.println("==| Succès du test |================");
	}
	
	/**
	 * Tester la DCT 2D en blocs.
	 */
	public static void testBlockDCT2D() throws FileNotFoundException, IllegalArgumentException
	{
		int[][] img = Images.readGray(getTestFile("mas082.bmp"));
		//System.out.println(Arrays.deepToString(DCT.transform2D(Matrices.toDouble(img))));
		/*
		int[][] tmp = new int[128][256];
		
		for (int i = 0; i < tmp.length; ++i)
		for (int j = 0; j < tmp[0].length; ++j)
			tmp[i][j] = img[i][j];
		
		img = tmp;
		*/
		final double[][] imgRec = DCT.inverseBlockTransform(DCT.blockTransform(Matrices.toDouble(img), 8, 8), 8, 8);
		
		for (int y = 0; y < img.length; ++y)
		{
			for (int x = 0; x < img[0].length; ++x)
			{
				img[y][x] = (int) Math.min(255, Math.max(0, Math.round(imgRec[y][x])));
			}
		}
		Plot.showImg(Images.grayToJavaImg(img));
	}
	
	/**
	 * Tester la lecture d'images et leur affichage.
	 * @throws FileNotFoundException
	 */
	public static void testImageRead() throws FileNotFoundException
	{
		final int[][] img = Images.readGray(getTestFile("mas082.bmp"));
		Plot.showImg(Images.grayToJavaImg(img));
	}
	
	public static void testDPCM()
	{
		final int w = 50,
				  h = 30;
		
		final double[][] randomMatrix = new double[h][];
		for (int l = 0; l < randomMatrix.length; ++l)
		{
			randomMatrix[l] = IntStream.range(0, w).mapToDouble(i->(double)((int)(Math.random()*500))).toArray();
		}
		
		System.out.println(Arrays.deepToString(randomMatrix));
		
		final double[][] reconstructedMatrix = DPCM.decode(DPCM.encode(randomMatrix, 1));
OUT_LOOP: for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				if (reconstructedMatrix[y][x] != randomMatrix[y][x])
				{
					System.err.println("Erreur ! à " + "(" + x + ", " + y + ")");
					break OUT_LOOP;
				}
			}
		}
		
		System.out.println("=====================\n" + Arrays.deepToString(reconstructedMatrix));
		
	}
	
	/**
	 * Tester l'encodage d'une séquence.
	 * @throws FileNotFoundException
	 */
	public static void testVideoEncoding() throws FileNotFoundException
	{
		// Exemple : charger une séquence de 4 images nommées mas082.jpg, mas083.jpg, ..., mas085.jpg .
		Stream<EncodedFrame> encodedSequence = VideoEncoder.encode(
			Videos.readGray(getTestFile("mas"))/*.map(img->
			{
				int[][] tmp = new int[128][256];
				
				for (int i = 0; i < tmp.length; ++i)
				for (int j = 0; j < tmp[0].length; ++j)
					tmp[i][j] = img[i][j];
				
				return tmp;
			})*/
		, new EncoderParams());
		
		VideoEncoder.decode(encodedSequence, new EncoderParams())
					.map(Images::grayToJavaImg)
					.forEach(Plot::showImg);
	}
	
	public static void main(final String[] args)
	{
		// testFFT();
		// testDCT();
		// testDCT2D();
		/*
		try
		{
			int[][] img = Images.readGray(getTestFile("mas082.bmp"));
			int[][] tmp = new int[128][256];
			
			for (int i = 0; i < tmp.length; ++i)
			for (int j = 0; j < tmp[0].length; ++j)
				tmp[i][j] = img[i][j];
			
			img = tmp;
			
			double[][] dct = DCT.transform2D(Matrices.toDouble(img));
			System.out.println(Arrays.deepToString(dct));
		} catch (FileNotFoundException | IllegalArgumentException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		testDPCM();
		/*
		try
		{
			//testImageRead();
			testBlockDCT2D();
			//testVideoEncoding();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}*/
	}

}
