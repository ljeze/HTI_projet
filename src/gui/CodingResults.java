package gui;

import java.awt.image.BufferedImage;

import gui.observable.Observable;
import img.math.Vector2D;

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
	public final Observable<Vector2D[][]> movementMap;
	/**
	 * Image reconstruite.
	 */
	public final Observable<BufferedImage> reconstImg;
	/**
	 * Carte des erreurs de prédiction.
	 */
	public final Observable<BufferedImage> errorsImg;
	
	/**
	 * Entropie de l'image originale.
	 */
	public final Observable<Double> originalEntropy;
	/**
	 * Entropie de l'image des erreurs.
	 */
	public final Observable<Double> errorsEntropy;
	/**
	 * Entropie de la carte de mouvement.
	 */
	public final Observable<Double> movementMapEntropy;
	
	public CodingResults()
	{
		originalImg = new Observable<>();
		movementMap = new Observable<>();
		reconstImg  = new Observable<>();
		errorsImg = new Observable<>();
		
		originalEntropy = new Observable<>(0.0);
		errorsEntropy = new Observable<>(0.0);
		movementMapEntropy = new Observable<>(0.0);
	}
}
