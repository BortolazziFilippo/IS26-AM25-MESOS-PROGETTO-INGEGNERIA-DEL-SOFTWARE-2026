package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EndGameController {

    @FXML private Label winnersLabel;
    @FXML private TableView<PlayerRankEntry> rankingTable;
    @FXML private TableColumn<PlayerRankEntry, Integer> rankColumn;
    @FXML private TableColumn<PlayerRankEntry, String>  nameColumn;
    @FXML private TableColumn<PlayerRankEntry, String>  colorColumn;
    @FXML private TableColumn<PlayerRankEntry, Integer> ppColumn;
    @FXML private TableColumn<PlayerRankEntry, Integer> foodColumn;
    @FXML private ListView<String> globalLeaderboardList;

    /**
     * Called by JavaFX after FXML node injection.
     * Configures the column factories for the ranking table and
     * sets the custom renderer for the global leaderboard list.
     */
    @FXML
    public void initialize() {
        rankColumn .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getRank()).asObject());
        nameColumn .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colorColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColor()));
        ppColumn   .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getPp()).asObject());
        foodColumn .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getFood()).asObject());
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        if (globalLeaderboardList != null) {
            globalLeaderboardList.setItems(FXCollections.observableArrayList("Caricamento classifica globale..."));
            globalLeaderboardList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("cell-gold", "cell-silver", "cell-bronze");
                    } else {
                        setText(item);
                        setAlignment(javafx.geometry.Pos.CENTER);
                        getStyleClass().removeAll("cell-gold", "cell-silver", "cell-bronze");
                        int idx = getIndex();
                        if      (idx == 0) getStyleClass().add("cell-gold");
                        else if (idx == 1) getStyleClass().add("cell-silver");
                        else if (idx == 2) getStyleClass().add("cell-bronze");
                    }
                }
            });
        }
    }

    /**
     * Populates the end-game screen with the winners and the ranking of all players.
     * Players are sorted by descending Prestige Points.
     *
     * @param winners    the list of winning players.
     * @param allPlayers the complete list of all players used to build the ranking.
     */
    public void setData(List<PlayerDTO> winners, List<PlayerDTO> allPlayers) {
        winnersLabel.setText(buildWinnersText(winners));

        List<PlayerDTO> sorted = new ArrayList<>(allPlayers);
        sorted.sort(Comparator.comparingInt(PlayerDTO::getPrestigePoint).reversed());

        ObservableList<PlayerRankEntry> entries = FXCollections.observableArrayList();
        for (int i = 0; i < sorted.size(); i++) {
            PlayerDTO p = sorted.get(i);
            String colorName = p.getColorTotem() != null ? p.getColorTotem().toString() : "-";
            entries.add(new PlayerRankEntry(i + 1, p.getNickName(), colorName,
                    p.getPrestigePoint(), p.getFood()));
        }
        rankingTable.setItems(entries);
    }

    private String buildWinnersText(List<PlayerDTO> winners) {
        if (winners == null || winners.isEmpty()) return "Nessun vincitore.";
        if (winners.size() == 1)
            return "Vincitore: " + winners.getFirst().getNickName()
                    + "  (" + winners.getFirst().getPrestigePoint() + " PP)";
        StringBuilder sb = new StringBuilder("Vincitori: ");
        for (PlayerDTO p : winners)
            sb.append(p.getNickName()).append(" (").append(p.getPrestigePoint()).append(" PP)  ");
        return sb.toString().trim();
    }

    /**
     * Updates the global leaderboard list with the rows received from the server.
     * May be called from the network thread; UI update is executed via {@code Platform.runLater}.
     *
     * @param entries the global leaderboard rows for the current player count,
     *                or {@code null}/empty list if unavailable.
     */
    public void setGlobalLeaderboard(List<String> entries) {
        Platform.runLater(() -> {
            if (globalLeaderboardList == null) return;
            if (entries == null || entries.isEmpty()) {
                globalLeaderboardList.setItems(FXCollections.observableArrayList("Nessun dato disponibile."));
            } else {
                globalLeaderboardList.setItems(FXCollections.observableArrayList(entries));
            }
        });
    }

    @FXML
    private void handleClose() {
        System.exit(0);
    }

    public static class PlayerRankEntry {
        private final int rank;
        private final String name;
        private final String color;
        private final int pp;
        private final int food;

        /**
         * Creates a new ranking entry for the end-game table.
         *
         * @param rank  the ranking position (1-based).
         * @param name  the player's nickname.
         * @param color the name of the player's totem color.
         * @param pp    the Prestige Points earned.
         * @param food  the final food reserve.
         */
        public PlayerRankEntry(int rank, String name, String color, int pp, int food) {
            this.rank  = rank;
            this.name  = name;
            this.color = color;
            this.pp    = pp;
            this.food  = food;
        }

        /** @return the ranking position (1-based). */
        public int    getRank()  { return rank;  }
        /** @return the player's nickname. */
        public String getName()  { return name;  }
        /** @return the name of the player's totem color. */
        public String getColor() { return color; }
        /** @return the Prestige Points earned by the player. */
        public int    getPp()    { return pp;    }
        /** @return the player's final food reserve. */
        public int    getFood()  { return food;  }
    }
}