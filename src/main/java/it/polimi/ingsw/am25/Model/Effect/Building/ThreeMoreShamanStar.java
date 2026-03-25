package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.ShamanCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.Model.Player.Player;

public class ThreeMoreShamanStar extends BuildingEffect{
    private boolean flag = false;
    public ThreeMoreShamanStar() {
    }

    @Override
    public void applyEffect(Player player) {
        if(!flag) {
            player.addCardToTribe(new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE));
            flag = true;
        }else{
            player.getTribe().removeLast();
            flag = false;
        }
    }
}
