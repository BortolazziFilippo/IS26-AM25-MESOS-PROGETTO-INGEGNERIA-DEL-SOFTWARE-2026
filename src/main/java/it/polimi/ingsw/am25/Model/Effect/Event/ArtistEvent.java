package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class ArtistEvent extends EventEffect{
    private final int artistNeeded;
    private final int PPLost;
    private final int PPtoMultiply;

    public ArtistEvent(int artistNeeded, int PPLost, int PPtoMultiply){
        this.artistNeeded = artistNeeded;
        this.PPLost = PPLost;
        this.PPtoMultiply = PPtoMultiply;
    }

    @Override
    public void solveEvent(List<Player> playersList) {
        playersList.stream().filter(player -> player.getArtistNumber()>=artistNeeded).forEach(player -> player.managePP(PPtoMultiply*player.getArtistNumber()));
        playersList.stream().filter(player -> player.getArtistNumber()<artistNeeded).forEach(player -> player.managePP(-PPLost));
        for(Player pl:playersList){
            pl.getBuildingCards().stream().filter(buildingCard-> buildingCard.getApplyOn()== EVENT_TYPE.PAINTINGS).forEach(buildingCard->buildingCard.applyBuildingEffect(pl));
        }
    }
}
