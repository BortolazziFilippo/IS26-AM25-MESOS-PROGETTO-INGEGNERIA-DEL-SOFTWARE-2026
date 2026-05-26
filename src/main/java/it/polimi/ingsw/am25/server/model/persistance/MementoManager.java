package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Board.BoardView;

import java.util.Optional;

/**
 * Generic interface for classes that support the Memento pattern.
 * An implementor acts as an Originator: it can snapshot its state into a memento
 * and later restore itself from one.
 *
 * @param <T> the type of memento produced and consumed by this manager.
 */
public interface MementoManager<T> {
    /**
     * Creates a memento capturing the current state of this object.
     *
     * @return a snapshot of the current state.
     */
    T createMemento();

    /**
     * Restores this object's state from the given memento.
     *
     * @param memento the snapshot to restore from.
     */
    void restoreMemento(T memento);
}
