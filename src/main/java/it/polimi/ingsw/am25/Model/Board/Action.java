package it.polimi.ingsw.am25.Model.Board;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Action action)) return false;
        return DrawFromTop == action.DrawFromTop && DrawFromBottom == action.DrawFromBottom;
    }
}
