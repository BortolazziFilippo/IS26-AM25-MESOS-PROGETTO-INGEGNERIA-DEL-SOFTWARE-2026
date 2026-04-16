package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Player.Player;

public class PlayerDTO {
    private String nickName;
    private int food;
    private int prestigePoint;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getPrestigePoint() {
        return prestigePoint;
    }

    public void setPrestigePoint(int prestigePoint) {
        this.prestigePoint = prestigePoint;
    }

    /**
     * constructor that convert player to playerDTO
     * @param player player to convert
     */
    public PlayerDTO(Player player){
        this.nickName=player.getNickname();
        this.food=player.getFood();
        this.prestigePoint=player.getPrestigePoint();
    }
}
