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
	public static BufferedImage toJavaImg(final int[][] img)
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
		final int[][] rgbImg = readRgb(filePath);
		
		// Faire une moyenne des r, g et b pour le niveau de gris.
		for (int y = 0; y < rgbImg.length; ++y)
		{
			for (int x = 0; x < rgbImg[y].length; ++x)
			{
				rgbImg[y][x] = (
					((rgbImg[y][x]>>16)&0xFF) + // r
					((rgbImg[y][x]>>8)&0xFF)  + // g
					((rgbImg[y][x]>>0)&0xFF)	// b
				)/3;
			}
		}
		return rgbImg;
	}
}
