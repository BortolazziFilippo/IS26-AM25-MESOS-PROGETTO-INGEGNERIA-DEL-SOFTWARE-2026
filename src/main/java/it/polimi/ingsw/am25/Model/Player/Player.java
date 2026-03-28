package it.polimi.ingsw.am25.Model.Player;

import it.polimi.ingsw.am25.Model.Card.*;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.CONNECTION_STATUS;
import it.polimi.ingsw.am25.Model.Utilities.Exception.NotEnoughFoodException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {
    private final String nickname;
    private final Totem totem;
    private int food;
    private int prestigePoint;
    private final List<Card> tribe;
    private final List<BuildingCard> buildingCards;
    private CONNECTION_STATUS connectionStatus;

    public String getNickname() {
        return nickname;
    }

    /**
     * default constructor of player
     * @param nickname name of the player
     * @param color color of the totem
     */
    public Player(String nickname, COLOR color) {
            this.nickname = nickname;
            this.tribe = new ArrayList<>();
            this.buildingCards = new ArrayList<>();
            this.totem=new Totem(color);
    }

    /**
     * method to manage the player's food amount. Mainly to be used when returning to the default tile
     * since if the player doesn't have enough food it automatically removes two PP per food below zero and then set food to 0.
     * This second behavior sometimes could not be wanted, if so before calling you should check the amount of food available.
     * @param foodAmount food to be removed
     */
    public void manageFoodAndPP(int foodAmount){
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
     * this method tries to buy the card, if the player cannot afford it, it throws not enough food exception
     * @param selectedBuildingCard building to be bought
     */
    public void tryBuyBuilding( BuildingCard selectedBuildingCard) throws NotEnoughFoodException{
        int cost;
        cost=selectedBuildingCard.getFoodCost();
        cost=cost-this.getBuilderDiscount();
        if(cost<0){
            cost=0;
        }
        if(this.food-cost<0){
            throw new NotEnoughFoodException();
        }else{
            this.food-=cost;
            selectedBuildingCard.addCardToPlayer(this);
        }

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

    public int getNumberOfDifferentInventorIcon(){
        return (int) tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.INVENTOR)
                .map(InventorCard.class::cast)    // 1. Trasforma la Card in InventorCard
                .map(InventorCard::getInvIcon)    // 2. Estraggo l'icona
                .distinct()                       // 3. Tengo solo le icone diverse
                .count();                         // 4. Conto quante ne sono rimaste
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
        return countStar;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Player player)) return false;
        return Objects.equals(nickname, player.nickname) && totem.equals(player.totem);
    }

}
