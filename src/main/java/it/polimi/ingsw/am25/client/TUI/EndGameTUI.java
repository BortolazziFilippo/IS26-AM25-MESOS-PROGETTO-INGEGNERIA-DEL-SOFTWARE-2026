package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.util.List;

public class EndGameTUI {
        private ClientVirtualView clientVirtualView;
        private TUIUtils tuiUtils;

        public EndGameTUI(ClientVirtualView clientVirtualView, TUIUtils tuiUtils) {
            this.clientVirtualView = clientVirtualView;
            this.tuiUtils = tuiUtils;
        }

        public void finished(List<PlayerDTO> winners) {

            tuiUtils.clearScreen();
            System.out.println("--- FINE PARTITA ---");
            System.out.println();

            if(winners.size() == 1){
                System.out.println("Il vincitore è : " + winners.get(0).getNickName());
            }
            else{
                System.out.println("I vincitori sono: ");
                winners.forEach(p -> System.out.println(p.getNickName()));
            }
            tuiUtils.pauseAndClear();
        }


}
