package gui;

import java.awt.image.BufferedImage;

import gui.observable.Observable;
import test.plot.Plot;

/**
 * Ensemble d'images résultantes du codage d'une trame.
 */
public class CodingResults
{
	/**
	 * Trame originale.
	 */
	public final Observable<BufferedImage> originalImg;
	/**
	 * Carte des vecteurs de mouvement.
	 */
	public final Observable<BufferedImage> movementImg;
	/**
	 * Image reconstruite.
	 */
	public final Observable<BufferedImage> reconstImg;
	/**
	 * Carte des erreurs de prédiction.
	 */
	public final Observable<BufferedImage> errorsImg;
	
	public CodingResults()
	{
		originalImg = new Observable<>();
		movementImg = new Observable<>();
		reconstImg  = new Observable<>();
		errorsImg = new Observable<>();
	}
}
