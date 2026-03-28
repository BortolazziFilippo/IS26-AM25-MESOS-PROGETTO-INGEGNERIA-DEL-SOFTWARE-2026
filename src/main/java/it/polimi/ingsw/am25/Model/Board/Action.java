package it.polimi.ingsw.am25.Model.Board;

public class Action {
    public int drawTop;
    public int drawBot;

    public Action(int drawTop, int drawFromBottom){
        this.drawTop = drawTop;
        this.drawBot = drawFromBottom;
    }

    public int getDrawFromBottom() {
        return drawBot;
    }

    public int getDrawTop() {
        return drawTop;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Action action)) return false;
        return drawTop == action.drawTop && drawBot == action.drawBot;
    }
}
