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
    private int temporaryShamanBonus = 0;

    /**
     * default constructor of player
     * @param nickname name of the player
     * @param color color of the totem
     */
    public Player(String nickname, COLOR color) {
            this.nickname = nickname;
            this.tribe = new ArrayList<>();
            this.buildingCards = new ArrayList<>();
            /* bisogna firnirlo dopo la classe totem !! */

    }

    /**
     * method to manage the player's food amount. Mainly to be used when returning to the default tile
     * since if the player doesn't have enough food it automatically removes two PP per food below zero and then set food to 0.
     * This second behavior sometimes could not be wanted, if so before calling you should check the amount of food available.
     * @param foodAmount food to be removed
     */
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

    /**
     * method used from buildingEffect three more shaman,
     * @param bonus bonus
     */
    public void addTemporaryShamanBonus(int bonus){
        this.temporaryShamanBonus += bonus;
    }
    /**
     * method used from buildingEffect three more shaman,
     */
    public void resetTemporaryShamanBonus(){
        this.temporaryShamanBonus = 0;
    }

    /**
     * Method used to manage player's PP. It adds or subtracts the amount
     * @param PPamount amount to be managed
     */
    public void managePP(int PPamount){
        this.prestigePoint += PPamount;
    }

    public CONNECTION_STATUS getConnection() {
        return CONNECTION_STATUS.CONNECTED;
    }

    public void setConnection(CONNECTION_STATUS connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    /**
     * method used to add a villager card to the tribe
     * @param card card to be added
     */
    public void addCardToTribe(Card card){
        this.tribe.add(card);
    }

    /**
     * Method to add a building card to the list of buildings
     * @param buildingCard building to be added
     */
    public void addBuilding(BuildingCard buildingCard){
        this.buildingCards.add(buildingCard);
    }

    public int getFood() {
        return food;
    }

    public int getPrestigePoint() {
        return prestigePoint;
    }

    /**
     *
     * @return the number of total shaman star a player has
     */
    public int getShamanStarTotal(){
        int countStar = 0;
        for(Card card : this.tribe){
            if(card.getCardType()== CARD_TYPE.SHAMAN){
                countStar= countStar + ((ShamanCard) card).getStarNumber();
            }
        }
        return countStar + temporaryShamanBonus;
    }

    /**
     *
     * @return the total discount of the builder a player has
     */
    public int getBuilderDiscount(){

        return this.tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.BUILDER).map(card -> (BuilderCard)card).mapToInt(BuilderCard::getFoodDiscount).sum();
    }

    /**
     *
     * @return return the discount given by the gatherer during sustenance event
     */
    public int getGatherDiscount(){

        return  (int) this.tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.GATHERER).count()*3;
    }

    /**
     *
     * @return return the number of Hunter in tribe
     */
    public int getHunterNumber(){
        return (int) tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.HUNTER).count();
    }

    /**
     *
     * @return return the number of artists in the tribe
     */
    public int getArtistNumber(){
        return (int) this.tribe.stream().filter(card -> card.getCardType()==CARD_TYPE.ARTIST).count();
    }

    /**
     *
     * @return the total size of the tribe
     */
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
