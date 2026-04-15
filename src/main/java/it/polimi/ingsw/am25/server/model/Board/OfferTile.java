package it.polimi.ingsw.am25.server.model.Board;

/**
 * An offer tile that players choose during the placing phase.
 * It defines how many cards the occupying player may draw from the top and bottom lists.
 */
public class OfferTile extends Tile{
    private final Action actionAvailable;
    private final char offerTileID;

    /**
     * Creates an offer tile with the given draw counts and identifier.
     *
     * @param drawTop    number of top-list draws available
     * @param drawBot    number of bottom-list draws available
     * @param offerTileID single-character identifier for this tile (e.g. 'A', 'B', …)
     */
    public OfferTile(int drawTop, int drawBot, char offerTileID){
        this.actionAvailable = new Action(drawTop, drawBot);
        this.offerTileID = offerTileID;
        super(null);
    }
    /**
     * Copy constructor — creates a defensive copy of another offer tile.
     *
     * @param offerTile the tile to copy
     */
    public OfferTile(OfferTile offerTile){
        this.actionAvailable = new Action(offerTile.getActionAvailable());
        this.offerTileID = offerTile.offerTileID;
        super(null);
    }
    /**
     * Returns the single-character identifier of this offer tile.
     *
     * @return the tile ID character
     */
    public char getOfferTileID() {
        return offerTileID;
    }
    /**
     * Returns the remaining draw actions available on this tile.
     *
     * @return the {@link Action} tracking remaining draws
     */
    public Action getActionAvailable() {
        return actionAvailable;
    }


}
