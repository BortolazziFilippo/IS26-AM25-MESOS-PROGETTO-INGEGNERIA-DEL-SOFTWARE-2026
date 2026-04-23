package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Player.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    private String nickName;
    private int food;
    private int prestigePoint;
    private COLOR colorTotem;
    private List<CardDTO> cardDtoList=new ArrayList<>();

    /**
     * Returns nick name.
     * @return the result of the operation.
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Sets nick name.
     * @param nickName parameter nickName.
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * Returns food.
     * @return the result of the operation.
     */
    public int getFood() {
        return food;
    }

    /**
     * Sets food.
     * @param food parameter food.
     */
    public void setFood(int food) {
        this.food = food;
    }

    /**
     * Returns prestige point.
     * @return the result of the operation.
     */
    public int getPrestigePoint() {
        return prestigePoint;
    }

    /**
     * Sets prestige point.
     * @param prestigePoint parameter prestigePoint.
     */
    public void setPrestigePoint(int prestigePoint) {
        this.prestigePoint = prestigePoint;
    }

    /**
     * Executes add card to tribe.
     * @param cardDTO parameter cardDTO.
     */
    public void addCardToTribe(CardDTO cardDTO){
        if (this.cardDtoList == null) {
            this.cardDtoList = new ArrayList<>();
        }
        this.cardDtoList.add(cardDTO);
    }

    /**
     * Builds a DTO snapshot from a player instance.
     *
     * @param player player to convert
     */
    public PlayerDTO(Player player){
        this.nickName=player.getNickname();
        this.food=player.getFood();
        this.prestigePoint=player.getPrestigePoint();
        this.colorTotem=player.getTotem().color();
    }

    /**
     * Returns color totem.
     * @return the result of the operation.
     */
    public COLOR getColorTotem() {
        return colorTotem;
    }

    /**
     * Sets color totem.
     * @param colorTotem parameter colorTotem.
     */
    public void setColorTotem(COLOR colorTotem) {
        this.colorTotem = colorTotem;
    }

    /**
     * Creates a new player dto instance.
     * @param nickName parameter nickName.
     * @param food parameter food.
     * @param prestigePoint parameter prestigePoint.
     * @param colorTotem parameter colorTotem.
     */
    public PlayerDTO(String nickName, int food, int prestigePoint, COLOR colorTotem) {
        this.nickName = nickName;
        this.food = food;
        this.prestigePoint = prestigePoint;
        this.colorTotem = colorTotem;
    }
}
