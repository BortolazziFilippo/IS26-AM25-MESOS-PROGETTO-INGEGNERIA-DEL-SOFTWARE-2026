/**
 * Module descriptor for the Mesos board-game application.
 */
module it.polimi.ingsw.am25 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    requires java.rmi;
    requires java.sql;
    //requires it.polimi.ingsw.am25; mi da errore --> ciclica dipendenza
    exports it.polimi.ingsw.am25.client.webLayer.RMI to java.rmi;
    exports it.polimi.ingsw.am25.server.webLayer.RMI to java.rmi;
    exports it.polimi.ingsw.am25.server.webLayer.DTOs to java.rmi;
    opens it.polimi.ingsw.am25.server.webLayer.DTOs to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Enums to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Board to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Player to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Card to com.google.gson;
    opens it.polimi.ingsw.am25 to javafx.fxml;
    exports it.polimi.ingsw.am25.client.TUI;
    exports it.polimi.ingsw.am25.client to java.rmi;
    exports it.polimi.ingsw.am25.server.model.Controller;
    exports it.polimi.ingsw.am25.server.model.Game;
    exports it.polimi.ingsw.am25.server.model.Player;
    exports it.polimi.ingsw.am25.server.model.Board;
    exports it.polimi.ingsw.am25.server.model.Card;
    exports it.polimi.ingsw.am25.server.model.Enums;
    exports it.polimi.ingsw.am25.server.model.Observers;
    exports it.polimi.ingsw.am25.server.model.Utilities;
    exports it.polimi.ingsw.am25.server.model.Utilities.Exception;
    exports it.polimi.ingsw.am25.server.model.Factory.Building;
    exports it.polimi.ingsw.am25.server.model.Factory.DefaultTile;
    exports it.polimi.ingsw.am25.server.model.Factory.Deck;
    exports it.polimi.ingsw.am25.server.model.Factory.OfferTile;
    exports it.polimi.ingsw.am25.server.model.Factory.Event;
    exports it.polimi.ingsw.am25.server.webLayer;
}