package it.polimi.ingsw.am25.Model.Factory.Building;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Effect.Building.*;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Factory.DTO.BuildingDTO;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class BuildingFactory {

    public BuildingFactory() {
    }

    public List<BuildingCard> createBuildingDeck (int playerNumber){
        int randomCardId;
        Random random=new Random();
        List<BuildingCard> tempList =new ArrayList<>();
        List<BuildingCard> listToReturn=new ArrayList<>();
        List<Integer> randomNumber=new ArrayList<>();
        InputStream inputStream = BuildingFactory.class.getResourceAsStream("/CardResources/json/building.json");
        if(inputStream==null) {
            throw new RuntimeException("Errore apertura file building.json");
        }
        Reader reader= new InputStreamReader(inputStream);
        Gson gson= new Gson();
        BuildingDTO[] tempCatalogue= gson.fromJson(reader, BuildingDTO[].class);
        for (BuildingDTO dto : tempCatalogue){
            tempList.add(new BuildingCard(dto.getEra(),dto.getBuildingID(),dto.getFoodCost(),dto.getEndGamePP(),dto.getApplyOn()));
        }

        switch (playerNumber){
            case 2:
                //ERA 1
                randomNumber=randomNumerList(1,6);
                listToReturn.add(tempList.get(randomNumber.getFirst()-1));
                //ERA 2
                randomNumber=randomNumerList(7,13);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                //ERA 3
                randomNumber=randomNumerList(14,21);
                for (int i = 0; i < 3; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                break;
            case 3:
                //ERA 1
                randomNumber=randomNumerList(1,6);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                //ERA 2
                randomNumber=randomNumerList(7,13);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                //ERA 3
                randomNumber=randomNumerList(14,21);
                for (int i = 0; i < 4; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                break;
            case 4:
                //ERA 1
                randomNumber=randomNumerList(1,6);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                //ERA 2
                randomNumber=randomNumerList(7,13);
                for (int i = 0; i < 3; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                //ERA 3
                randomNumber=randomNumerList(14,21);
                for (int i = 0; i < 4; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                break;
            case 5:
                //ERA 1
                randomNumber=randomNumerList(1,6);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                //ERA 2
                randomNumber=randomNumerList(7,13);
                for (int i = 0; i < 3; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                //ERA 3
                randomNumber=randomNumerList(14,21);
                for (int i = 0; i < 5; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)-1));
                }
                break;
            default:
                System.err.println("Parametro PlayerNumber non valido" );
        }
        for (BuildingCard n:listToReturn){
            n.setBuildingEffect(returnCorrectBuildingEffect(n));
        }
        return listToReturn;
    }
    private BuildingEffect returnCorrectBuildingEffect(BuildingCard buildingToSetEffect){
        BuildingEffect effectToReturn= null;
        switch (buildingToSetEffect.getBuildingID()) {
            case 1:
                effectToReturn = new FiveFoodCompletedSet();
                break;
            case 2:
                effectToReturn = new DiscountFoodOnSustenance(CARD_TYPE.GATHERER);
                break;
            case 3:
                effectToReturn = new DiscountFoodOnSustenance(CARD_TYPE.ARTIST);
                break;
            case 4:
                effectToReturn = new NoPPLostOnShaman();
                break;
            case 5:
                effectToReturn = new PlusOneFoodOnReturnDefaultTile();
                break;
            case 6:
                effectToReturn = new FoodOnNewCoupleInventors();
                break;
            case 7:
                effectToReturn = new DoublePPOnShamanEvent();
                break;
            case 8:
                effectToReturn = new ThreeMoreShamanStar();
                break;
            case 9:
                effectToReturn = new DiscountFoodOnSustenance(CARD_TYPE.INVENTOR);
                break;
            case 10:
                effectToReturn = new OnEventHuntOneFoodAndOnePPPerHunter();
                break;
            case 11:
                effectToReturn = new BuilderDoublePP();
                break;
            case 12:
                effectToReturn = new OnPaintingOneFoodPerArtist();
                break;
            case 13:
                effectToReturn = new SetSixCard();
                break;
            case 14:
                effectToReturn = new PPPerCharType(3,CARD_TYPE.HUNTER);
                break;
            case 15:
                effectToReturn = new PPPerCharType(4,CARD_TYPE.GATHERER);
                break;
            case 16:
                effectToReturn = new PPPerCharType(4,CARD_TYPE.SHAMAN);
                break;
            case 17:
                effectToReturn = new PPPerCharType(4,CARD_TYPE.BUILDER);
                break;
            case 18:
                effectToReturn = new PPPerCharType(4,CARD_TYPE.ARTIST);
                break;
            case 19:
                effectToReturn = new PPPerCharType(2,CARD_TYPE.INVENTOR);
                break;
            case 20:
                effectToReturn = new DrawOneMOreCard();
                break;
            case 21:
                effectToReturn = new TwentyFivePPEndGame();
                break;
            default:
                // Gestione per ID non previsti
                System.out.println("ID Edificio non riconosciuto: " + buildingToSetEffect.getBuildingID());
                break;
        }
        return effectToReturn;
    }
    private List<Integer> randomNumerList(int lowerBound, int upperBound){
        List<Integer> number = new ArrayList<>();
        for (int i = lowerBound; i <= upperBound ; i++) {
            number.add(i);
        }
        Collections.shuffle(number);
        return  number;
    }
}
