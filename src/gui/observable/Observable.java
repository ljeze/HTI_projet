package gui.observable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Observable générique. On pourrait utiliser les property de javafx mais pour
 * des raisons de compatibilités, on évite d'utilser javafx.
 * 
 * @param <T>
 *            type de l'observable.
 */
public class Observable<T>
{
	/**
	 * Valeur de cet observable.
	 */
	private T value;
	/**
	 * Liste des observateurs.
	 */
	private final List<Consumer<T>> listeners;
	
	public Observable(final T initialValue)
	{
		listeners = new ArrayList<>();
		value = initialValue;
	}
	
	public Observable()
	{
		this(null);
	}
	
	/**
	 * Définir la nouvelle valeur de cet observable, et en avertir tous les
	 * observateurs.
	 * 
	 * @param value
	 *            nouvelle valeur.
	 */
	public void set(final T value)
	{
		this.value = value;
		notifyChange();
	}
	
	/**
	 * Définir la nouvelle valeur de cet observable sans en avertir les
	 * observateurs.
	 * 
	 * @param value
	 *            nouvelle valeur.
	 */
	public void setWithoutNotify(final T value)
	{
		this.value = value;
	}
	
	/**
	 * Obtenir la valeur de cet observable.
	 */
	public T get()
	{
		return value;
	}
	
	/**
	 * Averti et appelle tous les observateurs.
	 */
	private void notifyChange()
	{
		for (final Consumer<T> listener : listeners)
		{
			listener.accept(value);
		}
	}
	
	/**
	 * Ajouter un observateur à cet observable. Il sera appelé dès que sa valeur
	 * changera.
	 * 
	 * @param listener
	 *            observateur.
	 */
	public void addListener(final Consumer<T> listener)
	{
		listeners.add(listener);
	}
	
}
