package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.EventDTO;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CardImageFactory {

    private CardImageFactory() {
    }

    /**
     * Creates an {@link ImageView} for the default tile corresponding to the given
     * player count.
     *
     * @param playerCount the number of players (determines which image to load).
     * @param fitHeight   the desired image height in pixels.
     * @return an {@link ImageView} configured with the image and preserved aspect ratio.
     */
    public static ImageView defaultTileImageView(int playerCount, double fitHeight) {
        ImageView iv = new ImageView(new Image(CardImageFactory.class.getResourceAsStream(
                "/images/Tiles/defaultTile/" + playerCount + "plDefTile.png")));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        return iv;
    }

    /**
     * Creates an {@link ImageView} for the offer tile identified by the given character ID.
     *
     * @param tileID    the character identifier of the offer tile.
     * @param fitHeight the desired image height in pixels.
     * @return an {@link ImageView} configured with the image and preserved aspect ratio.
     */
    public static ImageView offerTileImageView(char tileID, double fitHeight) {
        ImageView iv = new ImageView(new Image(CardImageFactory.class.getResourceAsStream(
                "/images/Tiles/offertiles/" + tileID + "offertile.png")));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        return iv;
    }

    /**
     * Creates an {@link ImageView} for a market card, selecting the correct image
     * based on the card type and its DTO attributes.
     *
     * @param card      the DTO of the card to display.
     * @param fitHeight the desired image height in pixels.
     * @return an {@link ImageView} with the card image and the DTO set as {@code userData}.
     */
    public static ImageView cardImageView(CardDTO card, double fitHeight) {
        String path = switch (card.getCardType()) {
            case GATHERER -> "/images/Card/gatherer/Gatherer.png";
            case HUNTER -> card.isHasIcon()
                    ? "/images/Card/hunters/hunterWIcon.png"
                    : "/images/Card/hunters/hunterNormal.png";
            case SHAMAN -> "/images/Card/shaman/" + shamanPath(card.getStarNumber()) + "Shaman.png";
            case INVENTOR -> "/images/Card/inventors/" + invPath(card.getInvIcon()) + "Inventor.png";
            case BUILDER -> "/images/Card/builders/" + card.getBuilderID() + "IDBuilder.png";
            case EVENT -> {
                EventDTO ev = (EventDTO) card;
                yield "/images/Card/events/" + ev.getEventID() + eventTypePath(ev.getEventType()) + "Event.png";
            }
            default -> "/images/Card/artist/Artist.png";
        };
        ImageView iv = new ImageView(new Image(CardImageFactory.class.getResourceAsStream(path)));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(card);
        return iv;
    }

    /**
     * Creates an {@link ImageView} for a building, selecting the correct era image
     * (first, second, or third) based on the building ID.
     *
     * @param bld       the DTO of the building to display.
     * @param fitHeight the desired image height in pixels.
     * @return an {@link ImageView} with the building image and the DTO set as {@code userData}.
     */
    public static ImageView buildingImageView(BuildingDTO bld, double fitHeight) {
        int id = bld.getBuildingID();
        String era = id <= 6 ? "eraOne" : id <= 13 ? "eraTwo" : "eraThree";
        ImageView iv = new ImageView(new Image(CardImageFactory.class.getResourceAsStream(
                "/images/Card/Buildings/" + era + "/" + id + "IDbuilding.png")));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(bld);
        return iv;
    }

    /**
     * Returns the classpath resource path of the totem image for the given color.
     *
     * @param color the totem color.
     * @return the absolute classpath resource path.
     */
    public static String totemPath(COLOR color) {
        return switch (color) {
            case RED -> "/images/totems/pedine_specs_redTotem.png";
            case BLUE -> "/images/totems/pedine_specs_blueTotem.png";
            case YELLOW -> "/images/totems/pedine_specs_yellowTotem.png";
            case PURPLE -> "/images/totems/pedine_specs_purpleTotem.png";
            case WHITE -> "/images/totems/pedine_specs_whiteTotem.png";
        };
    }

    /**
     * Returns the partial path segment corresponding to the given event type,
     * used when constructing the event image filename.
     *
     * @param type the event type.
     * @return the type identifier string (e.g. {@code "hunt"}, {@code "painting"}).
     */
    public static String eventTypePath(EVENT_TYPE type) {
        return switch (type) {
            case HUNT -> "hunt";
            case PAINTINGS -> "painting";
            case SHAMANIC_RIT -> "Shaman";
            case SUSTENANCE -> "sustenance";
            default -> "hunt";
        };
    }

    private static String shamanPath(SHAMAN_STAR star) {
        return switch (star) {
            case ONE -> "oneStar";
            case TWO -> "twoStar";
            case THREE -> "threeStar";
        };
    }

    private static String invPath(INV_ICON icon) {
        return switch (icon) {
            case STONE -> "stone";
            case BAIT -> "bait";
            case ARROW -> "arrow";
            case BOWL -> "bowl";
            case BREAD -> "bread";
            case NECKLACE -> "necklace";
            case GHOST -> "ghost";
            case LEATHER -> "leather";
            case ROPE -> "rope";
            case FLUTE -> "flute";
        };
    }
}
