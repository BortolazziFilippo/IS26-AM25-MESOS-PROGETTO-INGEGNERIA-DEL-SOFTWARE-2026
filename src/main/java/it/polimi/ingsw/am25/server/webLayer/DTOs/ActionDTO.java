package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.Action;

import java.io.Serializable;

/**
 * DTO carrying the number of top-row and bottom-row draws available to the current player
 * on their chosen offer tile.
 */
public class ActionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    int drawTop;
    int drawBot;

    /**
     * @param action the Action whose draw counts to copy.
     */
    public ActionDTO(Action action){
        this.drawBot=action.drawBot;
        this.drawTop=action.drawTop;
    }

    /**
     * @param drawTop number of top-row draws available.
     * @param drawBot number of bottom-row draws available.
     */
    public ActionDTO(int drawTop,int drawBot){
        this.drawBot=drawBot;
        this.drawTop=drawTop;
    }

    /** @return the number of top-row draws available. */
    public int getDrawTop() {
        return drawTop;
    }

    /** @return the number of bottom-row draws available. */
    public int getDrawBot() {
        return drawBot;
    }
}
