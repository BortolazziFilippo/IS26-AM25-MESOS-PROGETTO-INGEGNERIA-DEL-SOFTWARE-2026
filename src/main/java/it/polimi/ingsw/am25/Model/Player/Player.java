package it.polimi.ingsw.am25.Model.Player;

import it.polimi.ingsw.am25.Model.Card.*;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.CONNECTION_STATUS;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String nickname;
    /* private Totem totem;  manca la classe Totem */
    private int food;
    private int prestigePoint;
    private List<Card> tribe;
    private List<BuildingCard> buildingCards;
    private CONNECTION_STATUS connectionStatus;

    public Player(String nickname, COLOR color) {
            this.nickname = nickname;
            this.tribe = new ArrayList<Card>();
            this.buildingCards = new ArrayList<>();
            /* bisogna firnirlo dopo la classe totem !! */

    }

    public void manageFood(int foodAmount){
        if(foodAmount < 0){
            if( (this.food + foodAmount) < 0){
                this.food += foodAmount;
                int temp = this.food*2;
                managePP(temp);
                this.food = 0;
            }else{
                this.food+=foodAmount;
            }
        }
        else{
            this.food += foodAmount;
        }
    }

    public void managePP(int PPamount){
        this.prestigePoint += PPamount;
    }

    public CONNECTION_STATUS getConnection() {
        return CONNECTION_STATUS.CONNECTED;
    }

    public void setConnection(CONNECTION_STATUS connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public void addCardToTribe(Card card){
        this.tribe.add(card);
    }

    public void addBuilding(BuildingCard card){
        this.buildingCards.add(card);
    }

    public int getFood() {
        return food;
    }

    public int getPrestigePoint() {
        return prestigePoint;
    }

    public int getShamanStarTotal(){
        int countStar = 0;
        for(Card card : this.tribe){
            if(card.getCardType()== CARD_TYPE.SHAMAN){
                countStar= countStar + ((ShamanCard) card).getStarNumber();
            }
        }
        return countStar;
    }

    public int getBuilderDiscount(){

        return this.tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.BUILDER).map(card -> (BuilderCard)card).mapToInt(BuilderCard::getFoodDiscount).sum();
    }

    public int getGatherDiscount(){

        return  (int) this.tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.GATHERER).count();
    }

    public int getHunterNumber(){
        return (int) tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.HUNTER).count();
    }

    public int getArtistNumber(){
        return (int) this.tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.ARTIST).count();
    }

    public int getNumberOfCard(){
        return this.tribe.size();
    }

    public List<Card> getTribe() {
        return tribe;
    }

    public List<BuildingCard> getBuildingCards() {
        return buildingCards;
    }
}
