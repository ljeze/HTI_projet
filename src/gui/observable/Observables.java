package gui.observable;

import java.util.function.Function;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Classe utilitaire pour les observables.
 */
public class Observables
{
	/**
	 * Obtenir un observable booléen indiquant si la valeur d'un observable est
	 * null ou pas.
	 * 
	 * @param obs
	 *            observateur à observer.
	 * @return observable booléen indiquant si la valeur de l'observable est
	 *         null ou pas.
	 */
	public static <T> Observable<Boolean> notNull(final Observable<T> obs)
	{
		final Observable<Boolean> nullObs = new Observable<>(obs.get() != null);
		obs.addListener(val->nullObs.set(val != null));
		return nullObs;
	}
	
	/**
	 * Assure une correspondance entre le contenu du composant textComposant et
	 * l'observable obs. La convertion entre les deux est assuré par la fonction
	 * conversionFunction.
	 * 
	 * @param textComponent
	 *            composant texte.
	 * @param obs
	 *            observable.
	 * @param conversionFunction
	 *            fonction de convertion texte<->observable.
	 */
	public static <T> void bind(final JTextComponent textComponent, final Observable<T> obs, final Function<String, T> conversionFunction)
	{
		textComponent.setText(obs.get() == null ? "" : obs.get().toString());
		
		// Observateur du texte du composant.
		final SimpleDocumentListener docListener = e->obs.setWithoutNotify(conversionFunction.apply(textComponent.getText()));
		
		// textComponent <= obs.
		obs.addListener(val->
		{
			// On retire le listener avant pour ne pas le notifier.
			textComponent.getDocument().removeDocumentListener(docListener);
			textComponent.setText(val == null ? "" : val.toString());
			textComponent.getDocument().addDocumentListener(docListener);
		});
		
		// textComponent => obs.
		textComponent.getDocument().addDocumentListener(docListener);
	}
	
	/**
	 * Interface simplifiée pour un observateur de document.
	 */
	private static interface SimpleDocumentListener extends DocumentListener
	{
		/**
		 * Méthode appelée quand le document a été modifié.
		 * @param e
		 */
		public void update(final DocumentEvent e);
		
		@Override
		public default void changedUpdate(final DocumentEvent e)
		{
			update(e);
		}
		
		@Override
		public default void insertUpdate(final DocumentEvent e)
		{
			update(e);
		}
		
		@Override
		public default void removeUpdate(final DocumentEvent e)
		{
			update(e);
		}
		
	}
}
