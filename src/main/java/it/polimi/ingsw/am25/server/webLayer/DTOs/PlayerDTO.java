package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Player.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-transfer object for a Mesos player, carrying the nickname, food total,
 * prestige-point total, totem color, and the list of tribe cards.
 */
public class PlayerDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    private String nickName;
    private int food;
    private int prestigePoint;
    private COLOR colorTotem;
    private List<CardDTO> cardDtoList=new ArrayList<>();

    /**
     * Builds a DTO snapshot from a player instance.
     *
     * @param player the player to convert.
     */
    public PlayerDTO(Player player){
        this.nickName=player.getNickname();
        this.food=player.getFood();
        this.prestigePoint=player.getPrestigePoint();
        this.colorTotem=player.getTotem().color();
    }

    /**
     * @param nickName      the player's display name.
     * @param food          the player's current food total.
     * @param prestigePoint the player's current prestige-point total.
     * @param colorTotem    the color of the player's totem.
     */
    public PlayerDTO(String nickName, int food, int prestigePoint, COLOR colorTotem) {
        this.nickName = nickName;
        this.food = food;
        this.prestigePoint = prestigePoint;
        this.colorTotem = colorTotem;
    }

    /** @return the player's display name. */
    public String getNickName() {
        return nickName;
    }

    /** @param nickName the player's display name. */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /** @return the player's current food total. */
    public int getFood() {
        return food;
    }

    /** @param food the player's current food total. */
    public void setFood(int food) {
        this.food = food;
    }

    /** @return the player's current prestige-point total. */
    public int getPrestigePoint() {
        return prestigePoint;
    }

    /** @param prestigePoint the player's current prestige-point total. */
    public void setPrestigePoint(int prestigePoint) {
        this.prestigePoint = prestigePoint;
    }

    /** @return the color of the player's totem. */
    public COLOR getColorTotem() {
        return colorTotem;
    }

    /** @param colorTotem the color of the player's totem. */
    public void setColorTotem(COLOR colorTotem) {
        this.colorTotem = colorTotem;
    }

    /** @return the list of tribe cards belonging to this player. */
    public List<CardDTO> getCardDtoList() {
        return cardDtoList;
    }

    /** @param cardDTO the card to append to the player's tribe list. */
    public void addCardToTribe(CardDTO cardDTO){
        if (this.cardDtoList == null) {
            this.cardDtoList = new ArrayList<>();
        }
        this.cardDtoList.add(cardDTO);
    }
}
