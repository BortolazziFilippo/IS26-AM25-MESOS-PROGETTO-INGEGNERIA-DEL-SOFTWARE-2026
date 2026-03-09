module it.polimi.ingsw.am25 {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.polimi.ingsw.am25 to javafx.fxml;
    exports it.polimi.ingsw.am25;
}