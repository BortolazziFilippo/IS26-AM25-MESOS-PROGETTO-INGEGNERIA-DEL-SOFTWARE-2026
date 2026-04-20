package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Player.Totem;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.util.List;

public interface PlayerObserver {

    void onPlayerChanged(
            String nickname,
            Totem totem,
            int food,
            int prestigePoint,
            List<Card> tribe,
            List<BuildingCard> buildingCards
    );
    void notifyFoodChanged(
            String playerNickName,
            int newFood
    );
    void notifyPPChanged(
            String playerNickName,
            int newPP
    );

    void notifyCardAddedToTribe(
            String playername,
            Card cardAdded
    );
}
