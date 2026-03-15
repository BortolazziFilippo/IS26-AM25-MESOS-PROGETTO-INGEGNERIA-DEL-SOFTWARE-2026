module it.polimi.ingsw.am25 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    opens it.polimi.ingsw.am25.Model.Factory.DTO to com.google.gson;
    opens it.polimi.ingsw.am25.Model.Enums to com.google.gson;


    opens it.polimi.ingsw.am25 to javafx.fxml;
    exports it.polimi.ingsw.am25;
}