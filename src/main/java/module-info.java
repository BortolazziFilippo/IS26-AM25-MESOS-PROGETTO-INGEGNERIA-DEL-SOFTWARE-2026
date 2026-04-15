module it.polimi.ingsw.am25 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Factory.DTO to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Enums to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Board to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Player to com.google.gson;
    opens it.polimi.ingsw.am25.server.model.Card to com.google.gson;
    opens it.polimi.ingsw.am25 to javafx.fxml;
}