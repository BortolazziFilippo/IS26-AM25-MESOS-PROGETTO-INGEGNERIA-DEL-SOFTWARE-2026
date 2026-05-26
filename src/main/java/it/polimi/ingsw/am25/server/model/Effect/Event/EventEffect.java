package it.polimi.ingsw.am25.server.model.Effect.Event;

/**
 * Abstract base class for all Mesos event effects.
 * Concrete subclasses implement the effect logic for each {@link it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE}.
 */
public abstract class EventEffect implements EventEffectInterface {

    /** Default constructor for subclasses. */
    protected EventEffect() {}
}
