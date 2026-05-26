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
    /** The player's unique display name. */
    private String nickName;
    /** The player's current food supply. */
    private int food;
    /** The player's accumulated prestige points. */
    private int prestigePoint;
    /** The color of the player's totem. */
    private COLOR colorTotem;
    /** The list of tribe-member card DTOs in the player's hand. */
    private List<CardDTO> cardDtoList = new ArrayList<>();

    /**
     * Builds a DTO snapshot from a player instance.
     *
     * @param player the player to convert.
     */
    public PlayerDTO(Player player) {
        this.nickName = player.getNickname();
        this.food = player.getFood();
        this.prestigePoint = player.getPrestigePoint();
        this.colorTotem = player.getTotem().color();
    }

    /**
     * Creates a PlayerDTO with explicit field values.
     *
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

    /**
     * Returns the player's display name.
     *
     * @return the player's nickname.
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Sets the player's display name.
     *
     * @param nickName the player's display name.
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * Returns the player's current food total.
     *
     * @return the food total.
     */
    public int getFood() {
        return food;
    }

    /**
     * Sets the player's current food total.
     *
     * @param food the player's current food total.
     */
    public void setFood(int food) {
        this.food = food;
    }

    /**
     * Returns the player's current prestige-point total.
     *
     * @return the prestige-point total.
     */
    public int getPrestigePoint() {
        return prestigePoint;
    }

    /**
     * Sets the player's current prestige-point total.
     *
     * @param prestigePoint the player's current prestige-point total.
     */
    public void setPrestigePoint(int prestigePoint) {
        this.prestigePoint = prestigePoint;
    }

    /**
     * Returns the color of the player's totem.
     *
     * @return the totem color.
     */
    public COLOR getColorTotem() {
        return colorTotem;
    }

    /**
     * Sets the color of the player's totem.
     *
     * @param colorTotem the color of the player's totem.
     */
    public void setColorTotem(COLOR colorTotem) {
        this.colorTotem = colorTotem;
    }

    /**
     * Returns the list of tribe cards belonging to this player.
     *
     * @return the tribe card list.
     */
    public List<CardDTO> getCardDtoList() {
        return cardDtoList;
    }

    /**
     * Appends a card DTO to this player's tribe list.
     *
     * @param cardDTO the card to append to the player's tribe list.
     */
    public void addCardToTribe(CardDTO cardDTO) {
        if (this.cardDtoList == null) {
            this.cardDtoList = new ArrayList<>();
        }
        this.cardDtoList.add(cardDTO);
    }
}
