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

/**
 * Factory utility that creates pre-configured {@link ImageView} instances for every
 * visual asset used in the Mesos GUI (cards, buildings, totems, tiles, events).
 * All images are loaded once and stored in a thread-safe cache keyed by resource path.
 */
public class CardImageFactory {

    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    /** Utility class — not instantiable. */
    private CardImageFactory() {
    }

    /**
     * Returns the cached {@link Image} for the given classpath resource, loading it on first access.
     *
     * @param path the classpath-relative path of the image resource.
     * @return the cached {@link Image}.
     */
    private static Image cached(String path) {
        return IMAGE_CACHE.computeIfAbsent(path,
                p -> new Image(Objects.requireNonNull(CardImageFactory.class.getResourceAsStream(p))));
    }

    /**
     * Creates an {@link ImageView} for the default tile of the given player count.
     *
     * @param playerCount the number of players (determines which tile image to use).
     * @param fitHeight   the display height in pixels; aspect ratio is preserved.
     * @return a configured {@link ImageView} for the default tile.
     */
    public static ImageView defaultTileImageView(int playerCount, double fitHeight) {
        ImageView iv = new ImageView(cached("/images/Tiles/defaultTile/" + playerCount + "plDefTile.png"));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        return iv;
    }

    /**
     * Creates an {@link ImageView} for the offer tile with the given identifier.
     *
     * @param tileID    the single-character identifier of the offer tile.
     * @param fitHeight the display height in pixels; aspect ratio is preserved.
     * @return a configured {@link ImageView} for the offer tile.
     */
    public static ImageView offerTileImageView(char tileID, double fitHeight) {
        ImageView iv = new ImageView(cached("/images/Tiles/offertiles/" + tileID + "offertile.png"));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        return iv;
    }

    /**
     * Creates an {@link ImageView} for a tribe-member or event card described by the given DTO.
     * The correct image is selected from the card type and its role-specific attributes.
     * The DTO is stored in the view's {@code userData} for later retrieval.
     *
     * @param card      the card data-transfer object describing the card to render.
     * @param fitHeight the display height in pixels; aspect ratio is preserved.
     * @return a configured {@link ImageView} for the card.
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
        ImageView iv = new ImageView(cached(path));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(card);
        return iv;
    }

    /**
     * Creates an {@link ImageView} for a building card described by the given DTO.
     * The era is inferred from the building ID range (1–6 era I, 7–13 era II, 14+ era III).
     * The DTO is stored in the view's {@code userData} for later retrieval.
     *
     * @param bld       the building data-transfer object describing the building to render.
     * @param fitHeight the display height in pixels; aspect ratio is preserved.
     * @return a configured {@link ImageView} for the building.
     */
    public static ImageView buildingImageView(BuildingDTO bld, double fitHeight) {
        int id = bld.getBuildingID();
        String era = id <= 6 ? "eraOne" : id <= 13 ? "eraTwo" : "eraThree";
        ImageView iv = new ImageView(cached("/images/Card/Buildings/" + era + "/" + id + "IDbuilding.png"));
        iv.setFitHeight(fitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(bld);
        return iv;
    }

    /**
     * Returns the cached totem {@link Image} for the given color.
     *
     * @param color the totem color.
     * @return the {@link Image} corresponding to the given color.
     */
    public static Image totemImage(COLOR color) {
        return cached(totemPath(color));
    }

    /**
     * Returns the classpath-relative image path for the totem of the given color.
     *
     * @param color the totem color.
     * @return the resource path string for the totem image.
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
     * Returns the cached event card {@link Image} for the given event ID and type.
     *
     * @param eventID   the unique identifier of the event.
     * @param eventType the category of the event (hunt, sustenance, shaman, paintings).
     * @return the {@link Image} for the specified event card.
     */
    public static Image eventImage(int eventID, EVENT_TYPE eventType) {
        return cached("/images/Card/events/" + eventID + eventTypePath(eventType) + "Event.png");
    }

    /**
     * Pre-loads all twelve event card images into the cache.
     * Should be called once at startup to avoid UI stutter during the first event reveal.
     */
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

    /**
     * Returns the filename segment that identifies the event type in the image path.
     *
     * @param type the event type.
     * @return the path segment string for the given event type.
     */
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
