package it.polimi.ingsw.am25.Model.Observers;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Player.Totem;

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


}
