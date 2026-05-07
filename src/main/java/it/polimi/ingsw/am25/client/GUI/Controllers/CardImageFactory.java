package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.server.model.Enums.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CardImageFactory {

    private CardImageFactory() {}

    public static ImageView cardImageView(CardDTO card, double fitHeight) {
        String path = switch (card.getCardType()) {
            case GATHERER -> "/images/Card/gatherer/Gatherer.png";
            case HUNTER   -> card.isHasIcon()
                    ? "/images/Card/hunters/hunterWIcon.png"
                    : "/images/Card/hunters/hunterNormal.png";
            case SHAMAN   -> "/images/Card/shaman/" + shamanPath(card.getStarNumber()) + "Shaman.png";
            case INVENTOR -> "/images/Card/inventors/" + invPath(card.getInvIcon()) + "Inventor.png";
            case BUILDER  -> "/images/Card/builders/" + card.getBuilderID() + "IDBuilder.png";
            case EVENT    -> {
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

    public static String totemPath(COLOR color) {
        return switch (color) {
            case RED    -> "/images/totems/pedine_specs_redTotem.png";
            case BLUE   -> "/images/totems/pedine_specs_blueTotem.png";
            case YELLOW -> "/images/totems/pedine_specs_yellowTotem.png";
            case PURPLE -> "/images/totems/pedine_specs_purpleTotem.png";
            case WHITE  -> "/images/totems/pedine_specs_whiteTotem.png";
        };
    }

    public static String eventTypePath(EVENT_TYPE type) {
        return switch (type) {
            case HUNT        -> "hunt";
            case PAINTINGS   -> "painting";
            case SHAMANIC_RIT -> "Shaman";
            case SUSTENANCE  -> "sustenance";
            default          -> "hunt";
        };
    }

    private static String shamanPath(SHAMAN_STAR star) {
        return switch (star) {
            case ONE   -> "oneStar";
            case TWO   -> "twoStar";
            case THREE -> "threeStar";
        };
    }

    private static String invPath(INV_ICON icon) {
        return switch (icon) {
            case STONE    -> "stone";
            case BAIT     -> "bait";
            case ARROW    -> "arrow";
            case BOWL     -> "bowl";
            case BREAD    -> "bread";
            case NECKLACE -> "necklace";
            case GHOST    -> "ghost";
            case LEATHER  -> "leather";
            case ROPE     -> "rope";
            case FLUTE    -> "flute";
        };
    }
}
