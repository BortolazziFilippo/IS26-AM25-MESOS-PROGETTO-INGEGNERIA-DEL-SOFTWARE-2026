package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.Action;

import java.io.Serializable;

public class ActionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    int drawTop;
    int drawBot;
    /**
     * Creates a new action dto instance.
     * @param action parameter action.
     */
    public ActionDTO(Action action){
        this.drawBot=action.drawBot;
        this.drawTop=action.drawTop;
    }
    /**
     * Creates a new action dto instance.
     * @param drawTop parameter drawTop.
     * @param drawBot parameter drawBot.
     */
    public ActionDTO(int drawTop,int drawBot){
        this.drawBot=drawBot;
        this.drawTop=drawTop;
    }

    /**
     * Returns draw top.
     * @return the result of the operation.
     */
    public int getDrawTop() {
        return drawTop;
    }

    /**
     * Returns draw bot.
     * @return the result of the operation.
     */
    public int getDrawBot() {
        return drawBot;
    }
}
