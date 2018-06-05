package img;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * Classe utilitaire de lecture et écriture d'images.
 */
public class Images
{
	/**
	 * Convertit une matrice d'entiers en niveaux de gris en une image
	 * buffererisée java.
	 * 
	 * @param img
	 *            matrice d'entiers en niveaux de gris.
	 * @return image buffererisée java.
	 */
	public static BufferedImage grayToJavaImg(final int[][] img)
	{
		final int w = img[0].length,
				  h = img.length;
		
		final BufferedImage javaImg = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		
		// Convertir le niveau de gris en entier rgb.
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				javaImg.setRGB(x, y, (img[y][x] << 0) |
									 (img[y][x] << 8) |
									 (img[y][x] << 16));
			}
		}
		
		/*final BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		ret.setRGB(0, 0, w, h, javaImg.getRGB(0, 0, w, h, null, 0, w), 0, w);
		*/
		return javaImg;
	}

	/**
	 * Lit une image couleur et retourne une matrice d'entiers dont les 3
	 * derniers octets sont les composantes R, G et B.
	 * 
	 * @param filePath
	 *            chemin du fichier.
	 * @return matrice d'entiers dont les 3 derniers octets sont les composantes
	 *         R, G et B.
	 * @throws FileNotFoundException
	 *             si le fichier est malformé ou inexistant.
	 */
	public static int[][] readRgb(final Path filePath) throws FileNotFoundException
	{
		try
		{
			final BufferedImage img = ImageIO.read(filePath.toFile());
			final int w = img.getWidth(),
					  h = img.getHeight();
			
			final int[][] rgbArray = new int[h][w];
			
			for (int y = 0; y < h; ++y)
			{
				img.getRGB(0, y, w, 1, rgbArray[y], 0, w);
			}
			
			return rgbArray;
		} catch (IOException e)
		{
			throw new FileNotFoundException("Fichier inexistant ou malformé.");
		}
	}
	
	/**
	 * Lit une image en niveaux de gris et retourne une matrice d'entiers 
	 * 
	 * @param filePath
	 *            chemin du fichier.
	 * @return matrice d'entiers.
	 * @throws FileNotFoundException
	 *             si le fichier est malformé ou inexistant.
	 */
	public static int[][] readGray(final Path filePath) throws FileNotFoundException 
	{
		try
		{
			final BufferedImage img = ImageIO.read(filePath.toFile());
			final int w = img.getWidth(),
					  h = img.getHeight();
			/*BufferedImage grayImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
			
			ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			ColorConvertOp colorConvert = new ColorConvertOp(gray, null);
			colorConvert.filter(img, grayImg);*/
			
			final int[][] grayscaleArray = new int[h][w];
			
			for (int y = 0; y < h; ++y)
			{
				img.getRGB(0, y, w, 1, grayscaleArray[y], 0, w);
			}
			
			// Convertir rgb en niveaux de gris.
			for (int y = 0; y < h; ++y)
			{
				for (int x = 0; x < w; ++x)
				{
					grayscaleArray[y][x] = grayscaleArray[y][x]& 0xFF;
				}
			}
			
			return grayscaleArray;
		} catch (IOException e)
		{
			throw new FileNotFoundException("Fichier inexistant ou malformé.");
		}
	}
}
