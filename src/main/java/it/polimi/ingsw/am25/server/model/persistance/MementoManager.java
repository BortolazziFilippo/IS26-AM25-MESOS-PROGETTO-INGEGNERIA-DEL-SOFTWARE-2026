package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Board.BoardView;

import java.util.Optional;

public interface MementoManager<T> {
    T createMemento();
    void restoreMemento(T memento);
}
