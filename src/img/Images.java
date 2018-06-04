package img;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import img.math.Vector2D;

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
	 * Convertit une carte de vecteurs en une image.
	 * 
	 * @param vectorMap
	 *            carte de vecteurs.
	 * @param dx
	 *            espacement dx entre les vecteurs.
	 * @param dy
	 *            espacement dy entre les vecteurs.
	 * @param vectColor
	 *            couleur des vecteurs.
	 * @param backgroundColor
	 *            couleur du fond.
	 * @param scale
	 *            facteur de redimensionnement.
	 * @return image représentant la carte de vecteurs.
	 */
	public static BufferedImage vectorMapToJavaImg(final Vector2D[][] vectorMap, final int dx, final int dy,
			final Color vectColor, final Color backgroundColor, final double scale)
	{
		final int nW = vectorMap[0].length,
				  nH = vectorMap.length;
		
		final int dxScaled = (int) (dx * scale),
				  dyScaled = (int) (dy * scale);
		
		final BufferedImage img = new BufferedImage(nW * dxScaled, nH * dyScaled, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = img.createGraphics();
		
		g.setColor(backgroundColor);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		
		g.setColor(vectColor);
		for (int y = 0; y < nH; ++y)
		{
			for (int x = 0; x < nW; ++x)
			{
				drawVector(g, x*dxScaled, y*dyScaled, (int)(vectorMap[y][x].x() * scale), (int)(vectorMap[y][x].y() * scale));
			}
		}
		
		// Libérer les resources.
		g.dispose();
		
		return img;
	}

	/**
	 * Dessiner un vecteur partant d'une origine (x, y) et de composantes (dx, dy).
	 * 
	 * @param g
	 *            environnement graphique.
	 * @param x
	 *            origine x.
	 * @param y
	 *            origine y.
	 * @param dx
	 *            composante x.
	 * @param dy
	 *            composante y.
	 */
	private static void drawVector(final Graphics2D g, final int x, final int y, final int dx, final int dy)
	{
		final double arrowAngle  = Math.PI/8,
					 arrowLength = 4;
		final double vectorAngle = Math.atan2(dy, dx);
		
		final int tipX = x+dx,
				  tipY = y+dy;
		
		g.drawLine(x, y, tipX, tipY);
		
		if (dx != 0 || dy != 0)
		{
			g.drawLine(tipX, tipY, 
			     (int)(tipX + arrowLength*Math.cos(vectorAngle + (Math.PI-arrowAngle))),
				 (int)(tipY + arrowLength*Math.sin(vectorAngle + (Math.PI-arrowAngle))));
			g.drawLine(tipX, tipY, 
			     (int)(tipX + arrowLength*Math.cos(vectorAngle - (Math.PI-arrowAngle))),
				 (int)(tipY + arrowLength*Math.sin(vectorAngle - (Math.PI-arrowAngle))));
		}
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
