package it.polimi.ingsw.am25.Model.Tile;

public class Action {
    public int DrawFromTop;
    public int DrawFromBottom;

    public Action(int drawFromTop, int drawFromBottom){
        this.DrawFromTop = drawFromTop;
        this.DrawFromBottom = drawFromBottom;
    }

    public int getDrawFromBottom() {
        return DrawFromBottom;
    }

    public int getDrawFromTop() {
        return DrawFromTop;
    }
}
