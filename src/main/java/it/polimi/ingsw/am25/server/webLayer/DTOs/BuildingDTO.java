package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.io.Serial;
import java.io.Serializable;

public class BuildingDTO extends CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    private  int buildingID;
    private  int foodCost;
    private  int endGamePP;
    private  EVENT_TYPE applyOn;

    public BuildingDTO(BuildingCard buildingCard) {
        super(buildingCard.getEra(), CARD_TYPE.BUILDING);
        this.foodCost=buildingCard.getFoodCost();
        this.endGamePP=buildingCard.getEndgamePP();
        this.buildingID = buildingCard.getBuildingID();
    }


    public int getFoodCost() {
        return foodCost;
    }

    public int getEndGamePP() {
        return endGamePP;
    }

    public EVENT_TYPE getApplyOn() {
        return applyOn;
    }

    public int getBuildingID() {
        return buildingID;
    }

    @Override
    public String toString() {
        String effectDescription = "";

        switch (this.buildingID) {
            case 1:
                effectDescription = "Ottieni 6 Cibo per ogni set completo di icone Invenzione.";
                break;
            case 2:
                effectDescription = "Sconto di 1 Cibo sul sostentamento dei Raccoglitori.";
                break;
            case 3:
                effectDescription = "Sconto di 1 Cibo sul sostentamento degli Artisti.";
                break;
            case 4:
                effectDescription = "Immunità: Non perdi PP durante il calcolo degli Sciamani.";
                break;
            case 5:
                effectDescription = "Ottieni +1 Cibo quando ritorni sulla casella di partenza.";
                break;
            case 6:
                effectDescription = "Ottieni Cibo quando giochi una nuova coppia di Inventori.";
                break;
            case 7:
                effectDescription = "Ottieni il doppio dei PP durante la risoluzione dell'evento Sciamano.";
                break;
            case 8:
                effectDescription = "+3 Stelle Sciamano totali per la risoluzione della maggioranza.";
                break;
            case 9:
                effectDescription = "Sconto di 1 Cibo sul sostentamento degli Inventori.";
                break;
            case 10:
                effectDescription = "Evento Caccia: Ottieni +1 Cibo e +1 PP per ogni tuo Cacciatore.";
                break;
            case 11:
                effectDescription = "Raddoppia i PP forniti dai tuoi Costruttori.";
                break;
            case 12:
                effectDescription = "Evento Pittura: Ottieni +1 Cibo per ogni tuo Artista.";
                break;
            case 13:
                effectDescription = "Abilita bonus extra per la collezione di un set da 6 Invenzioni.";
                break;
            case 14:
                effectDescription = "Fine Partita: +3 PP per ogni tuo Cacciatore.";
                break;
            case 15:
                effectDescription = "Fine Partita: +4 PP per ogni tuo Raccoglitore.";
                break;
            case 16:
                effectDescription = "Fine Partita: +4 PP per ogni tuo Sciamano.";
                break;
            case 17:
                effectDescription = "Fine Partita: +4 PP per ogni tuo Costruttore.";
                break;
            case 18:
                effectDescription = "Fine Partita: +4 PP per ogni tuo Artista.";
                break;
            case 19:
                effectDescription = "Fine Partita: +2 PP per ogni tuo Inventore.";
                break;
            case 20:
                effectDescription = "Fine Round: Puoi pescare 1 carta extra dal mazzo.";
                break;
            case 21:
                effectDescription = "Fine Partita: +25 PP (Punti Prestigio) extra fissi.";
                break;
            default:
                effectDescription = "Effetto speciale sconosciuto (ID: " + this.buildingID + ").";
                break;
        }
        //format of the string
        return String.format("Costo: %d Cibo | PP Fissi: %-2d | Effetto: %s",
                this.foodCost, this.endGamePP, effectDescription);
    }
}
