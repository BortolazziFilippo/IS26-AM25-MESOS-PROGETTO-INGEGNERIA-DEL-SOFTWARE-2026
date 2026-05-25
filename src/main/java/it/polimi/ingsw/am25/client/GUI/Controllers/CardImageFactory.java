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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CardImageFactory {

    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private CardImageFactory() {
    }

    private static Image cached(String path) {
        return IMAGE_CACHE.computeIfAbsent(path,
                p -> new Image(Objects.requireNonNull(CardImageFactory.class.getResourceAsStream(p))));
    }

    public static ImageView defaultTileImageView(int playerCount, double fitHeight) {
        ImageView iv = new ImageView(cached("/images/Tiles/defaultTile/" + playerCount + "plDefTile.png"));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        return iv;
    }

    public static ImageView offerTileImageView(char tileID, double fitHeight) {
        ImageView iv = new ImageView(cached("/images/Tiles/offertiles/" + tileID + "offertile.png"));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        return iv;
    }

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
        ImageView iv = new ImageView(cached(path));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(card);
        return iv;
    }

    public static ImageView buildingImageView(BuildingDTO bld, double fitHeight) {
        int id = bld.getBuildingID();
        String era = id <= 6 ? "eraOne" : id <= 13 ? "eraTwo" : "eraThree";
        ImageView iv = new ImageView(cached("/images/Card/Buildings/" + era + "/" + id + "IDbuilding.png"));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(bld);
        return iv;
    }

    public static Image totemImage(COLOR color) {
        return cached(totemPath(color));
    }

    public static String totemPath(COLOR color) {
        return switch (color) {
            case RED -> "/images/totems/pedine_specs_redTotem.png";
            case BLUE -> "/images/totems/pedine_specs_blueTotem.png";
            case YELLOW -> "/images/totems/pedine_specs_yellowTotem.png";
            case PURPLE -> "/images/totems/pedine_specs_purpleTotem.png";
            case WHITE -> "/images/totems/pedine_specs_whiteTotem.png";
        };
    }

    public static Image eventImage(int eventID, EVENT_TYPE eventType) {
        return cached("/images/Card/events/" + eventID + eventTypePath(eventType) + "Event.png");
    }

    public static void preloadEventImages() {
        String[] paths = {
            "/images/Card/events/1huntEvent.png",
            "/images/Card/events/2sustenanceEvent.png",
            "/images/Card/events/3ShamanEvent.png",
            "/images/Card/events/4paintingEvent.png",
            "/images/Card/events/5huntEvent.png",
            "/images/Card/events/6sustenanceEvent.png",
            "/images/Card/events/7ShamanEvent.png",
            "/images/Card/events/8paintingEvent.png",
            "/images/Card/events/9huntEvent.png",
            "/images/Card/events/10paintingEvent.png",
            "/images/Card/events/11sustenanceEvent.png",
            "/images/Card/events/12ShamanEvent.png"
        };
        for (String p : paths) cached(p);
    }

    public static String eventTypePath(EVENT_TYPE type) {
        return switch (type) {
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
