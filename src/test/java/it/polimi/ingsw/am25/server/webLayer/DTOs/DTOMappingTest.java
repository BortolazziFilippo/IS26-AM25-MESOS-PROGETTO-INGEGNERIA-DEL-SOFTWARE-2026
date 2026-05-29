package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.Action;
import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.*;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DTOMappingTest {

    // ─────────────────────────── ActionDTO ───────────────────────────

    @Test
    void actionDTO_fromInts_mapsCorrectly() {
        ActionDTO dto = new ActionDTO(3, 2);
        assertEquals(3, dto.getDrawTop());
        assertEquals(2, dto.getDrawBot());
    }

    @Test
    void actionDTO_fromAction_mapsCorrectly() {
        Action action = new Action(4, 1);
        ActionDTO dto = new ActionDTO(action);
        assertEquals(4, dto.getDrawTop());
        assertEquals(1, dto.getDrawBot());
    }

    // ─────────────────────────── DefaultTileDTO ───────────────────────────

    @Test
    void defaultTileDTO_fromDomainObject_mapsFood() {
        DefaultTile tile = new DefaultTile(5);
        DefaultTileDTO dto = new DefaultTileDTO(tile);
        assertEquals(5, dto.foodPerSlotPosition());
    }

    // ─────────────────────────── OffertileDTO ───────────────────────────

    @Test
    void offertileDTO_fromOfferTile_mapsAllFields() {
        OfferTile tile = new OfferTile(2, 3, 'B');
        OffertileDTO dto = new OffertileDTO(tile);
        assertEquals('B', dto.getOfferTileID());
        assertEquals(2, dto.getDrawTop());
        assertEquals(3, dto.getDrawBot());
    }

    // ─────────────────────────── CardDTO ───────────────────────────

    @Test
    void cardDTO_fromArtistCard_mapsEraAndType() {
        ArtistCard card = new ArtistCard(ERA.ERA_II, CARD_TYPE.ARTIST);
        CardDTO dto = new CardDTO(card);
        assertEquals(ERA.ERA_II, dto.getEra());
        assertEquals(CARD_TYPE.ARTIST, dto.getCardType());
    }

    @Test
    void cardDTO_fromGathererCard_mapsEraAndType() {
        GathererCard card = new GathererCard(ERA.ERA_I, CARD_TYPE.GATHERER);
        CardDTO dto = new CardDTO(card);
        assertEquals(ERA.ERA_I, dto.getEra());
        assertEquals(CARD_TYPE.GATHERER, dto.getCardType());
    }

    @Test
    void cardDTO_fromHuntersCard_mapsHasIcon() {
        HuntersCard withIcon    = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true);
        HuntersCard withoutIcon = new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false);
        assertTrue(new CardDTO(withIcon).isHasIcon());
        assertFalse(new CardDTO(withoutIcon).isHasIcon());
    }

    @Test
    void cardDTO_fromShamanCard_mapsStar() {
        ShamanCard card = new ShamanCard(ERA.ERA_III, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE);
        CardDTO dto = new CardDTO(card);
        assertEquals(SHAMAN_STAR.THREE, dto.getStarNumber());
    }

    @Test
    void cardDTO_fromInventorCard_mapsIcon() {
        InventorCard card = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.ARROW);
        CardDTO dto = new CardDTO(card);
        assertEquals(INV_ICON.ARROW, dto.getInvIcon());
    }

    @Test
    void cardDTO_fromBuilderCard_mapsDiscountPPAndID() {
        BuilderCard card = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 6, 10, 3);
        CardDTO dto = new CardDTO(card);
        assertEquals(6,  dto.getFoodDiscount());
        assertEquals(10, dto.getFinalPrestigePoint());
        assertEquals(3,  dto.getBuilderID());
    }

    // ─────────────────────────── BuildingDTO ───────────────────────────

    @Test
    void buildingDTO_fromBuildingCard_mapsAllFields() {
        BuildingCard card = new BuildingCard(ERA.ERA_II, CARD_TYPE.BUILDING, 5, 8, 20, EVENT_TYPE.END_ROUND);
        BuildingDTO dto = new BuildingDTO(card);
        assertEquals(ERA.ERA_II,         dto.getEra());
        assertEquals(CARD_TYPE.BUILDING, dto.getCardType());
        assertEquals(8,  dto.getFoodCost());
        assertEquals(20, dto.getEndGamePP());
        assertEquals(5,  dto.getBuildingID());
    }

    // ─────────────────────────── EventDTO ───────────────────────────

    @Test
    void eventDTO_fromEventCard_mapsEventIDAndType() {
        EventCard card = new EventCard(ERA.ERA_I, CARD_TYPE.EVENT, 3, EVENT_TYPE.SHAMANIC_RIT);
        EventDTO dto = new EventDTO(card);
        assertEquals(3,                    dto.getEventID());
        assertEquals(EVENT_TYPE.SHAMANIC_RIT, dto.getEventType());
        assertEquals(ERA.ERA_I,            dto.getEra());
    }

    @Test
    void eventDTO_fromExplicitArgs_mapsAllFields() {
        EventDTO dto = new EventDTO(7, ERA.ERA_III, EVENT_TYPE.HUNT);
        assertEquals(EVENT_TYPE.HUNT, dto.getEventType());
        assertEquals(ERA.ERA_III,     dto.getEra());
    }

    // ─────────────────────────── PlayerDTO ───────────────────────────

    @Test
    void playerDTO_fromPlayer_mapsAllFields() {
        Player player = new Player("Tizio", COLOR.RED);
        player.manageFoodAndPP(5);
        player.managePP(12);
        PlayerDTO dto = new PlayerDTO(player);
        assertEquals("Tizio",    dto.getNickName());
        assertEquals(COLOR.RED,  dto.getColorTotem());
        assertEquals(5,          dto.getFood());
        assertEquals(12,         dto.getPrestigePoint());
        assertTrue(dto.getCardDtoList().isEmpty());
    }

    @Test
    void playerDTO_addCardToTribe_appendsCard() {
        PlayerDTO dto = new PlayerDTO("X", 0, 0, COLOR.BLUE);
        CardDTO card = new CardDTO(ERA.ERA_I, CARD_TYPE.ARTIST);
        dto.addCardToTribe(card);
        assertEquals(1, dto.getCardDtoList().size());
        assertSame(card, dto.getCardDtoList().get(0));
    }
}
