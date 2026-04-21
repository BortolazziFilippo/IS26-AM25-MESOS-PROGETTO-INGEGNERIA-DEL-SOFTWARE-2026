package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.Action;

import java.io.Serializable;

public class ActionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    int drawTop;
    int drawBot;
    public ActionDTO(Action action){
        this.drawBot=action.drawBot;
        this.drawTop=action.drawTop;
    }
    public ActionDTO(int drawTop,int drawBot){
        this.drawBot=drawBot;
        this.drawTop=drawTop;
    }

    public int getDrawTop() {
        return drawTop;
    }

    public int getDrawBot() {
        return drawBot;
    }
}
