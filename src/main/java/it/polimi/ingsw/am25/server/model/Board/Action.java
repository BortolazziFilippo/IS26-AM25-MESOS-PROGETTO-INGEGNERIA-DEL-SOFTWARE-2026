package it.polimi.ingsw.am25.server.model.Board;
/**
 * Represents the actions available to a player on a given offer tile:
 * how many cards they may draw from the top list and how many from the bottom list.
 */
public class Action {
    public int drawTop;
    public int drawBot;
    /**
     * Creates a new Action with the given draw counts.
     *
     * @param drawTop        number of cards the player may draw from the top list
     * @param drawFromBottom number of cards the player may draw from the bottom list
     */
    public Action(int drawTop, int drawFromBottom){
        this.drawTop = drawTop;
        this.drawBot = drawFromBottom;
    }
    /**
     * Copy constructor.
     *
     * @param action the Action to copy
     */
    public Action(Action action){
        this.drawTop = action.drawTop;
        this.drawBot = action.drawBot;
    }
    /**
     * Returns the remaining number of bottom-list draws.
     *
     * @return remaining bottom draws
     */
    public int getDrawFromBottom() {
        return drawBot;
    }
    /**
     * Returns the remaining number of top-list draws.
     *
     * @return remaining top draws
     */
    public int getDrawTop() {
        return drawTop;
    }
    /**
     * Decrements the remaining top-list draw count by one.
     */
    public void subtractOneTopAction(){
        this.drawTop-=1;
    }
    /**
     * Decrements the remaining bottom-list draw count by one.
     */
    public void subtractOneBotAction(){
        this.drawBot-=1;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Action action)) return false;
        return drawTop == action.drawTop && drawBot == action.drawBot;
    }
}
