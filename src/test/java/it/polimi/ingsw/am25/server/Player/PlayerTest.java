package it.polimi.ingsw.am25.server.Player;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Effect.Building.PPPerCharType;
import it.polimi.ingsw.am25.server.model.Effect.Building.TwentyFivePPEndGame;
import it.polimi.ingsw.am25.server.model.Enums.*;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Player.Totem;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotEnoughFoodException;
import it.polimi.ingsw.am25.server.model.persistance.PlayerMemento;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    Player player;
    @BeforeEach
    void createPlayer(){
        this.player = new Player("Lorem Ipsum", COLOR.BLUE);
    }
    @Test
    void manageFoodAndPP_variousAmounts_modifiesFoodAndPP() {

        //AddingFood
        player.manageFoodAndPP(+7);
        assertEquals(7,player.getFood());
        //removing food
        player.manageFoodAndPP(-7);
        assertEquals(0,player.getFood());
        //removing food under 0 should remove double PP
        player.manageFoodAndPP(-3);
        assertEquals(-6,player.getPrestigePoint());
        assertEquals(0,player.getFood());
    }

    @Test
    void tryBuyBuilding_insufficientAndSufficientFood_throwsOrSucceeds() throws NotEnoughFoodException {
        player.manageFoodAndPP(5);
        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING,1,10,10, EVENT_TYPE.END_ROUND);
        assertThrows(NotEnoughFoodException.class,()->player.tryBuyBuilding(buildingCard));
        player.manageFoodAndPP(5);
        player.tryBuyBuilding(buildingCard);
        assertEquals(0,player.getFood());
        assertIterableEquals(List.of(new BuildingCard(ERA.ERA_I,CARD_TYPE.BUILDING,1,10,10,EVENT_TYPE.END_ROUND)), player.getBuildingCards());    }

    @Test
    void managePP_variousAmounts_modifiesPrestigePoints() {
        player.managePP(+7);
        assertEquals(7,player.getPrestigePoint());
        player.managePP(-14);
        assertEquals(-7,player.getPrestigePoint());
    }

    @Test
    void addCardToTribe_multipleCardTypes_addsAll() {
        assertEquals(0, player.getNumberOfCard());
        player.addCardToTribe(new InventorCard( ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.ARROW));
        assertEquals(1,player.getNumberOfCard());
        player.addCardToTribe(new ArtistCard( ERA.ERA_III, CARD_TYPE.ARTIST));
        player.addCardToTribe(new GathererCard( ERA.ERA_I, CARD_TYPE.GATHERER));
        player.addCardToTribe(new BuilderCard( ERA.ERA_II, CARD_TYPE.BUILDER,10,10, 0));

        List<Card> listToCompare= new ArrayList<>();

        listToCompare.add(new InventorCard( ERA.ERA_II, CARD_TYPE.INVENTOR,INV_ICON.ARROW));
        listToCompare.add(new ArtistCard( ERA.ERA_III, CARD_TYPE.ARTIST));
        listToCompare.add(new GathererCard( ERA.ERA_I, CARD_TYPE.GATHERER));
        listToCompare.add(new BuilderCard( ERA.ERA_II, CARD_TYPE.BUILDER,10,10, 0));
        assertIterableEquals(listToCompare,player.getTribe());
    }

    @Test
    void getBuilderDiscount_multipleBuilders_sumsTotalDiscount() {
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,6,10, 0));
        assertEquals(6,player.getBuilderDiscount());
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,10,10, 0));
        assertEquals(16,player.getBuilderDiscount());
    }

    @Test
    void getGatherDiscount_multipleGatherers_sumsTotalDiscount() {
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        assertEquals(3,player.getGatherDiscount());
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        assertEquals(9,player.getGatherDiscount());
    }

    @Test
    void getArtistNumber_multipleArtists_returnsCount() {
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        assertEquals(3,player.getArtistNumber());
    }
    @Test
    void  getShamanStarTotal_multipleShaman_sumsTotalStars(){
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE));
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE));
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO));
        assertEquals(6,player.getShamanStarTotal());
    }

    @Test
    void getHunterNumber_variousCards_returnsCount() {
        assertEquals(0, player.getHunterNumber());
        player.addCardToTribe(new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true));
        player.addCardToTribe(new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false));
        assertEquals(2, player.getHunterNumber());
    }

    @Test
    void getNumberOfDifferentInventorIcon_multipleIconTypes_countsDistinct() {
        player.addCardToTribe(new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.BREAD));
        player.addCardToTribe(new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD));
        assertEquals(1, player.getNumberOfDifferentInventorIcon());

        player.addCardToTribe(new InventorCard(ERA.ERA_III, CARD_TYPE.INVENTOR, INV_ICON.ARROW));
        assertEquals(2, player.getNumberOfDifferentInventorIcon());
    }

    @Test
    void getNickname_afterConstruction_returnsCorrectName() {
        assertEquals("Lorem Ipsum", player.getNickname());
    }

    @Test
    void getTotem_afterConstruction_returnsCorrectColor() {
        assertEquals(COLOR.BLUE, player.getTotem().color());
    }

    @Test
    void equals_sameName_areEqualDifferentNameAreNot() {
        Player same = new Player("Lorem Ipsum", COLOR.BLUE);
        Player different = new Player("Other", COLOR.RED);
        assertEquals(player, same);
        assertNotEquals(player, different);
    }

    @Test
    void connectionStatus_setAndGet_transitionsCorrectly() {
        player.setConnection(CONNECTION_STATUS.CONNECTED);
        assertEquals(CONNECTION_STATUS.CONNECTED, player.getConnection());
        player.setConnection(CONNECTION_STATUS.DISCONNECTED);
        assertEquals(CONNECTION_STATUS.DISCONNECTED, player.getConnection());
    }

    @Test
    void tryBuyBuilding_withBuilderDiscount_clampedToZeroCost() {
        // discount(15) > cost(10) → clamped to 0, no food spent
        player.addCardToTribe(new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 15, 10, 0));
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 10, 10, EVENT_TYPE.END_ROUND);
        player.manageFoodAndPP(3);
        assertDoesNotThrow(() -> player.tryBuyBuilding(building));
        assertEquals(3, player.getFood());
    }

    @Test
    void triggerEndRoundBuilding_multipleHunters_awardsPPPerHunter() {
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        building.setBuildingEffect(new PPPerCharType(3, CARD_TYPE.HUNTER));
        player.addBuilding(building);
        player.addCardToTribe(new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true));
        player.addCardToTribe(new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false));

        player.triggerEndRoundBuilding();

        assertEquals(6, player.getPrestigePoint());
    }

    @Test
    void triggerEndGameBuilding_endGameEffect_awardsFixedPP() {
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_GAME);
        building.setBuildingEffect(new TwentyFivePPEndGame());
        player.addBuilding(building);

        player.triggerEndGameBuilding();

        assertEquals(25, player.getPrestigePoint());
    }

    @Test
    void checkpoints_multipleCardTypes_sumsAllContributions() {
        // 4 artists → 2 pairs → 20 PP
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        // 1 builder with finalPrestigePoint=8
        player.addCardToTribe(new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 2, 8, 0));
        // 2 inventors with 2 distinct icons → 2 * 2 = 4 PP
        player.addCardToTribe(new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.BREAD));
        player.addCardToTribe(new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.ARROW));

        assertEquals(32, player.checkpoints()); // 20 + 8 + 4
    }

    // ─────────────────────────── Player(PlayerDTO) ───────────────────────────

    @Test
    void constructorFromDTO_mapsNicknameAndColor_ignoresDTOFoodAndPP() {
        // Il costruttore da DTO comincia sempre con food=0, PP=0, tribù vuota
        PlayerDTO dto = new PlayerDTO("Pippo", 99, 999, COLOR.YELLOW);
        Player fromDTO = new Player(dto);

        assertEquals("Pippo",       fromDTO.getNickname());
        assertEquals(COLOR.YELLOW,  fromDTO.getTotem().color());
        assertEquals(0,             fromDTO.getFood());
        assertEquals(0,             fromDTO.getPrestigePoint());
        assertTrue(fromDTO.getTribe().isEmpty());
        assertTrue(fromDTO.getBuildingCards().isEmpty());
    }

    // ─────────────────────────── removeObserver ───────────────────────────

    @Test
    void removeObserver_removedObserverReceivesNoMoreNotifications() {
        AtomicInteger calls = new AtomicInteger(0);
        PlayerObserver obs = noopObserverWithFoodCounter(calls);

        player.addObserver(obs);
        player.manageFoodAndPP(1); // notifica → calls = 1
        assertEquals(1, calls.get());

        player.removeObserver(obs);
        player.manageFoodAndPP(1); // nessuna notifica dopo rimozione
        assertEquals(1, calls.get());
    }

    // ─────────────────────────── notifyCurrentTribe ───────────────────────────

    @Test
    void notifyCurrentTribe_firesOneNotificationPerCardInTribeAndBuildings() {
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new GathererCard(ERA.ERA_II, CARD_TYPE.GATHERER));
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 0, 0, EVENT_TYPE.END_ROUND);
        player.manageFoodAndPP(10);
        assertDoesNotThrow(() -> player.tryBuyBuilding(building));

        AtomicInteger cardNotifications = new AtomicInteger(0);
        PlayerObserver counter = cardCountingObserver(cardNotifications);
        player.addObserver(counter);

        player.notifyCurrentTribe();

        // 2 carte tribù + 1 edificio = 3 notifiche
        assertEquals(3, cardNotifications.get());
    }

    // ─────────────────────────── requestExtraDraw ───────────────────────────

    @Test
    void requestExtraDraw_notifiesAllRegisteredObservers() {
        AtomicInteger draws = new AtomicInteger(0);
        PlayerObserver obs = extraDrawObserver(draws);
        player.addObserver(obs);

        player.requestExtraDraw();

        assertEquals(1, draws.get());
    }

    // ─────────────────────────── restoreMemento ───────────────────────────

    @Test
    void restoreMemento_restoresFoodPPAndTribe() {
        // Costruisco uno stato, serializzo, ripristino su un player vuoto
        player.manageFoodAndPP(8);
        player.managePP(15);
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false));
        PlayerMemento memento = player.createMemento();

        Player restored = new Player("Lorem Ipsum", COLOR.BLUE);
        restored.restoreMemento(memento);

        assertEquals(8,  restored.getFood());
        assertEquals(15, restored.getPrestigePoint());
        assertEquals(2,  restored.getTribe().size());
        assertEquals(CARD_TYPE.ARTIST, restored.getTribe().get(0).getCardType());
        assertEquals(CARD_TYPE.HUNTER, restored.getTribe().get(1).getCardType());
    }

    // ─────────────────────────── restoreBuildings ───────────────────────────

    @Test
    void restoreBuildings_replacesExistingBuildingList() {
        BuildingCard old = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 0, 5, EVENT_TYPE.END_ROUND);
        player.addBuilding(old);
        assertEquals(1, player.getBuildingCards().size());

        BuildingCard newB1 = new BuildingCard(ERA.ERA_II, CARD_TYPE.BUILDING, 2, 0, 10, EVENT_TYPE.END_GAME);
        BuildingCard newB2 = new BuildingCard(ERA.ERA_III, CARD_TYPE.BUILDING, 3, 0, 15, EVENT_TYPE.END_ROUND);
        player.restoreBuildings(List.of(newB1, newB2));

        assertEquals(2, player.getBuildingCards().size());
        assertFalse(player.getBuildingCards().contains(old));
        assertTrue(player.getBuildingCards().contains(newB1));
        assertTrue(player.getBuildingCards().contains(newB2));
    }

    // ─────────────────────────── stub helpers ───────────────────────────

    private static PlayerObserver noopObserverWithFoodCounter(AtomicInteger foodCalls) {
        return new PlayerObserver() {
            @Override public void onPlayerChanged(String n, Totem t, int f, int pp, List<Card> tr, List<BuildingCard> b) {}
            @Override public void notifyFoodChanged(String n, int f) { foodCalls.incrementAndGet(); }
            @Override public void notifyPPChanged(String n, int pp) {}
            @Override public void notifyCardAddedToTribe(String n, Card c) {}
            @Override public void requestExtraDraw(String n) {}
        };
    }

    private static PlayerObserver cardCountingObserver(AtomicInteger cardCalls) {
        return new PlayerObserver() {
            @Override public void onPlayerChanged(String n, Totem t, int f, int pp, List<Card> tr, List<BuildingCard> b) {}
            @Override public void notifyFoodChanged(String n, int f) {}
            @Override public void notifyPPChanged(String n, int pp) {}
            @Override public void notifyCardAddedToTribe(String n, Card c) { cardCalls.incrementAndGet(); }
            @Override public void requestExtraDraw(String n) {}
        };
    }

    private static PlayerObserver extraDrawObserver(AtomicInteger drawCalls) {
        return new PlayerObserver() {
            @Override public void onPlayerChanged(String n, Totem t, int f, int pp, List<Card> tr, List<BuildingCard> b) {}
            @Override public void notifyFoodChanged(String n, int f) {}
            @Override public void notifyPPChanged(String n, int pp) {}
            @Override public void notifyCardAddedToTribe(String n, Card c) {}
            @Override public void requestExtraDraw(String n) { drawCalls.incrementAndGet(); }
        };
    }
}