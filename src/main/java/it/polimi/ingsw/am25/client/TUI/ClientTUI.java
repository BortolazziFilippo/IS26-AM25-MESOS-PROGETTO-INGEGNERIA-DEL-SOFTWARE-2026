package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class ClientTUI {

    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final Scanner scanner;
    private PlayerDTO myPlayer = null;
    private final String RESET="\u001b[0m";
    private final String RED = "\033[31;49;1m";
    private final String GREEN = "\033[32;49;1m";
    private final String YELLOW = "\033[33;49;1m";
    private final String BLUE = "\033[34;49;1m";
    private final String PURPLE = "\033[35;49;1m";

    /**
     * Creates a new client tui instance.
     * @param serverStub parameter serverStub.
     * @param clientHandler parameter clientHandler.
     */
    public ClientTUI(ServerRemoteInterface serverStub, ClientVirtualView clientHandler) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Executes start.
     */
    public void start() {
        boolean inGame = false;
        // ==========================================================
        // 1. LOBBY LOOP
        // ==========================================================
        while (!inGame) {
            clearScreen();
            System.out.println("--- MENU PRINCIPALE ---");
            System.out.println("1 - Crea gioco");
            System.out.println("2 - Entra in una partita");
            System.out.print("Scelta: ");

            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    if (createGame()) {
                        waitForGameStart();
                        inGame = true;
                    }
                    break;
                case "2":
                    if (addPlayer()) {
                        waitForGameStart();
                        inGame = true;
                    }
                    break;
                default:
                    System.err.println("❌ Scelta non valida.");
                    pauseAndClear();
                    break;
            }
        }

        // ==========================================================
        // 2. GAME LOOP
        // ==========================================================
        while (true) {
            waitForMyTurn();

            if (clientHandler.needsExtraDraw) {
                handleExtraDraw();
                continue;
            }

            clearScreen();
            System.out.println("--- GIOCO (" + myPlayer.getNickName() + ") ---");
            System.out.println("Fase attuale: " + clientHandler.getGamePhase());
            System.out.println("\nScegli un'azione:");

            switch (clientHandler.getGamePhase()) {
                case PLACING_PHASE, LAST_ROUND_PLACING_PHASE:
                    System.out.println("1 - Piazza Totem (Fase Piazzamento)");
                    break;
                case RESOLVE_ACTION, LAST_ROUND_RESOLVE_ACTION:
                    System.out.println("Azioni disponibili:");
                    System.out.println("Pesca da sopra: "+clientHandler.getDrawTop());
                    System.out.println("Pesca da sotto: "+clientHandler.getDrawBot());
                    System.out.println("2 - Pesca carta da sopra (Fase Azioni)");
                    System.out.println("3 - Pesca carta da sotto (Fase Azioni)");
                    System.out.println("4 - Passa il turno");
                    printMarket();
                    break;
                default:
                    System.out.println("In attesa del caricamento...");
                    break;
            }

            System.out.print("\nScelta: ");
            String mossa = scanner.nextLine();

            switch (mossa) {
                case "1":
                    if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                        placePlayer();
                    } else {
                        System.err.println("\n❌ Azione non permessa: non siamo nella fase di piazzamento.");
                        pauseAndClear();
                    }
                    break;
                case "2":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        drawTopCard();
                    } else {
                        System.err.println("\n❌ Azione non permessa in questa fase del gioco.");
                        pauseAndClear();
                    }
                    break;
                case "3":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        drawBottomCard();
                    } else {
                        System.err.println("\n❌ Azione non permessa in questa fase del gioco.");
                        pauseAndClear();
                    }
                    break;
                case "4":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        passTurn();
                    } else {
                        System.err.println("\n❌ Azione non permessa in questa fase del gioco.");
                        pauseAndClear();
                    }
                    break;
                default:
                    System.err.println("\n❌ Scelta non valida. Inserisci un numero tra quelli proposti.");
                    pauseAndClear();
                    break;
            }
        }
    }

    // ==========================================================
    // --- LOBBY METHODS ---
    // ==========================================================

    /**
     * Executes create game.
     * @return the result of the operation.
     */
    private boolean createGame() {
        clearScreen();
        System.out.println("--- CREAZIONE PARTITA ---");
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();
        COLOR colorTotem = bindTotemColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);
        System.out.println();
        int playerNumber = numberOfPlayer();

        clientHandler.connectionError = false;
        clientHandler.isGameStarted = false;

        try {
            serverStub.createGame(player, playerNumber, clientHandler);
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore: comunicazione con il Server fallita.");
            pauseAndClear();
            return false;
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Errore: lobby già presente.");
            pauseAndClear();
            return false;
        }

        clearScreen();
        System.out.println("\nRichiesta inviata. In attesa che si connettano gli altri giocatori...");

        synchronized (clientHandler.gameStartLock) {
            while (!clientHandler.isGameStarted && !clientHandler.connectionError) {
                try {
                    clientHandler.gameStartLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        if(clientHandler.connectionError){
            clientHandler.connectionError = false;
            System.err.println("❌ Gioco già creato, unisciti tramite 'Aggiungi Giocatore'.");
            pauseAndClear();
            return false;
        }
        System.out.println("\n✅ Tutti i giocatori sono connessi! La partita inizia!");
        // 3. SET PLAYER E RETURN TRUE!
        this.myPlayer = player;
        pauseAndClear();
        return true;
    }

    /**
     * Executes add player.
     * @return the result of the operation.
     */
    private boolean addPlayer() {
        clearScreen();
        System.out.println("--- AGGIUNTA GIOCATORE ---");
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();
        COLOR colorTotem = bindTotemColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);
        clientHandler.connectionError = false;
        clientHandler.isGameStarted = false;
        try {
            serverStub.addPlayer(player, clientHandler);
        } catch (Exception e) {
            //FOR RMI THE ERROR IS CATCHED HERE
            System.err.println("\n❌ Impossibile unirsi: " + extractCleanError(e));
            pauseAndClear();
            return false;
        }
        clearScreen();
        System.out.println("\nRichiesta inviata. In attesa che si connettano gli altri giocatori...");
        synchronized (clientHandler.gameStartLock) {
            while (!clientHandler.isGameStarted && !clientHandler.connectionError) {
                try {
                    clientHandler.gameStartLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        //woke up, checking the reason
        if (clientHandler.connectionError) {
            //if got inside an error occured and already printed by clientVirtualView
            clientHandler.connectionError=false;
            System.err.println("❌ Unione alla partita annullata.");
            pauseAndClear();
            return false;
        }

        System.out.println("\n✅ Tutti i giocatori sono connessi! La partita inizia!");
        this.myPlayer = player;
        pauseAndClear();
        return true;
    }

    // ==========================================================
    // --- AZIONI DI GIOCO ---
    // ==========================================================

    /**
     * Executes place player.
     */
    private void placePlayer() {
        clearScreen();
        System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
        boolean isPlaced = false;

        while (!isPlaced) {
            //resetting
            clientHandler.connectionError = false;
            int position = getPlacingIndex();
            if (position == -1) return; //user pressed q
            try {

                serverStub.placingPlayer(myPlayer, position);
            } catch (Exception e) {
                // catched by RMI
                System.err.println("\n❌ Errore RMI: " + extractCleanError(e));
                pauseAndClear();
                clearScreen();
                System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
                continue;
            }
            System.out.println("\nIn attesa di conferma dal server...");
            synchronized (clientHandler.turnLock) {

                while (!clientHandler.connectionError &&
                        (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) &&
                        clientHandler.getPlayerToPlace() != null &&
                        clientHandler.getPlayerToPlace().equals(myPlayer.getNickName())) {
                    try {
                        clientHandler.turnLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            // woke up
            if (clientHandler.connectionError) {
                // socket:
                System.err.println("❌ Casella gia occupata");
                pauseAndClear();
                clearScreen();
                System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
            } else {
                // SUCCESS: server accepted move and went to next player
                System.out.println("\n✅ Totem posizionato con successo nella casella " + (position + 1) + "!");
                pauseAndClear();
                isPlaced = true; //ending while loop
            }
        }
    }

    // --- DRAW FROM TOP ---
    /**
     * Executes draw top card.
     */
    private void drawTopCard() {
        clearScreen();
        System.out.println("--- PESCA CARTA (SOPRA) ---");

        boolean isDrawn = false;
        while (!isDrawn) {
            System.out.println("\nCosa vuoi pescare?");
            System.out.println("1 - Carta Tribù");
            System.out.println("2 - Carta Edificio");
            System.out.println("q - Annulla e torna al menu principale");
            printCardList("CARTE TRIBÙ DISPONIBILI (SOPRA)", clientHandler.getTopCards());
            printCardList("CARTE EDIFICIO DISPONIBILI (SOPRA)", clientHandler.getTopBuildings());
            System.out.print("\nScelta: ");
            System.out.print("Scelta: ");

            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("q")) {
                System.out.println("\nAzione annullata.");
                pauseAndClear();
                return;
            } else if (input.equals("1")) {
                isDrawn = drawTopTribeCard();
            } else if (input.equals("2")) {
                isDrawn = drawTopBuildingCard();
            } else {
                System.err.println("\n❌ Errore: Inserisci 1 o 2.");
                pauseAndClear();
                clearScreen();
                System.out.println("--- PESCA CARTA (SOPRA) ---");
            }
        }
    }

    /**
     * Executes draw top tribe card.
     * @return the result of the operation.
     */
    private boolean drawTopTribeCard() {
        boolean isDrawn = false;
        while (!isDrawn) {
            clientHandler.connectionError = false;
            clearScreen();
            System.out.println("--- PESCA CARTA TRIBÙ (SOPRA) ---");

            printCardList("CARTE TRIBÙ DISPONIBILI (SOPRA)", clientHandler.getTopCards());

            System.out.print("\nInserisci la posizione della carta (1 a " + clientHandler.getTopCardSize() + ") oppure 'q' per tornare indietro: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("q")) {
                clearScreen();
                System.out.println("--- PESCA CARTA (SOPRA) ---");
                return false;
            }
            int azioniSopraPrecedenti = clientHandler.getDrawTop();
            int azioniSottoPrecedenti = clientHandler.getDrawBot();
            try {
                int position = Integer.parseInt(input) - 1;
                if (position < 0 || position >= clientHandler.getTopCardSize()) {
                    System.err.println("\n❌ Errore: Posizione fuori limite.");
                    pauseAndClear();
                    continue;
                }
                serverStub.selectCardFromTopList(myPlayer, CARD_TYPE.ARTIST, position); // Artist = placeholder for Tribe

            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Inserisci un NUMERO valido.");
                pauseAndClear();
                continue;
            } catch (Exception e) {
                // instant rmi method error
                System.err.println("\n❌ Impossibile pescare: " + extractCleanError(e));
                pauseAndClear();
                continue;
            }
            System.out.println("\nIn attesa di conferma dal server...");

            synchronized (clientHandler.turnLock) {
                //  leave if there is an error, the phase changes or if the round is ended or the action point are decreased
                while (!clientHandler.connectionError &&
                        clientHandler.getDrawTop() == azioniSopraPrecedenti &&
                        clientHandler.getDrawBot() == azioniSottoPrecedenti &&
                        (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) &&
                        clientHandler.getPlayerToPlay() != null &&
                        clientHandler.getPlayerToPlay().equals(myPlayer.getNickName())) {
                    try {
                        clientHandler.turnLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
            if (clientHandler.connectionError) {
                // error sent from server
                System.err.println("\n❌ Impossibile pescare.");
                pauseAndClear();
                continue;
            }

            System.out.println("\n✅ Carta pescata con successo o turno concluso!");
            pauseAndClear();
            isDrawn = true;
            return true;
        }
        return true;
    }

    /**
     * Executes draw top building card.
     * @return the result of the operation.
     */
    private boolean drawTopBuildingCard() {
        boolean isDrawn = false;
        while (!isDrawn) {

            clientHandler.connectionError = false;

            clearScreen();
            System.out.println("--- PESCA CARTA EDIFICIO (SOPRA) ---");


            printCardList("CARTE EDIFICIO DISPONIBILI (SOPRA)", clientHandler.getTopBuildings());
            System.out.print("\nInserisci la posizione della carta (1 a " + clientHandler.getTopBuildingSize() + ") oppure 'q' per tornare indietro: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("q")) {
                clearScreen();
                System.out.println("--- PESCA CARTA (SOPRA) ---");
                return false;
            }
            int azioniSopraPrecedenti = clientHandler.getDrawTop();
            int azioniSottoPrecedenti = clientHandler.getDrawBot();
            try {
                int position = Integer.parseInt(input) - 1;
                if (position < 0 || position >= clientHandler.getTopBuildingSize()) {
                    System.err.println("\n❌ Errore: Posizione fuori limite.");
                    pauseAndClear();
                    continue;
                }
                serverStub.selectCardFromTopList(myPlayer, CARD_TYPE.BUILDING, position);

            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Inserisci un NUMERO valido.");
                pauseAndClear();
                continue;
            } catch (Exception e) {
                System.err.println("\n❌ Impossibile pescare: " + extractCleanError(e));
                pauseAndClear();
                continue;
            }

            System.out.println("\nIn attesa di conferma dal server...");


            synchronized (clientHandler.turnLock) {
                //  leave if there is an error, the phase changes or if the round is ended or the action point are decreased
                while (!clientHandler.connectionError &&
                        clientHandler.getDrawTop() == azioniSopraPrecedenti &&
                        clientHandler.getDrawBot() == azioniSottoPrecedenti &&
                        (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) &&
                        clientHandler.getPlayerToPlay() != null &&
                        clientHandler.getPlayerToPlay().equals(myPlayer.getNickName())) {
                    try {
                        clientHandler.turnLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }


            if (clientHandler.connectionError) {
                System.err.println("\n❌ Impossibile pescare.");
                pauseAndClear();
                continue;
            }


            System.out.println("\n✅ Carta Edificio pescata con successo o turno concluso!");
            pauseAndClear();
            isDrawn = true;
            return true;
        }
        return true;
    }

    // --- DRAW FROM BOTTOM ---
    /**
     * Executes draw bottom card.
     */
    private boolean drawBottomCard() {
        boolean isDrawn = false;
        while (!isDrawn) {
            clientHandler.connectionError = false;
            clearScreen();
            System.out.println("--- PESCA CARTA TRIBÙ (SOTTO) ---");

            printCardList("CARTE TRIBÙ DISPONIBILI (SOTTO)", clientHandler.getBottomCards());
            System.out.print("\nInserisci la posizione della carta (1 a " + clientHandler.getBottomCardSize() + ") oppure 'q' per tornare indietro: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("q")) {
                clearScreen();
                System.out.println("--- PESCA CARTA (SOTTO) ---");
                return false;
            }
            int azioniSopraPrecedenti = clientHandler.getDrawTop();
            int azioniSottoPrecedenti = clientHandler.getDrawBot();
            try {
                int position = Integer.parseInt(input) - 1;
                if (position < 0 || position >= clientHandler.getBottomCardSize()) {
                    System.err.println("\n❌ Errore: Posizione fuori limite.");
                    pauseAndClear();
                    continue;
                }
                serverStub.selectCardFromBottomList(myPlayer, CARD_TYPE.ARTIST, position);
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Inserisci un NUMERO valido.");
                pauseAndClear();
                continue;
            } catch (Exception e) {
                System.err.println("\n❌ Impossibile pescare: " + extractCleanError(e));
                pauseAndClear();
                continue;
            }

            System.out.println("\nIn attesa di conferma dal server...");

            synchronized (clientHandler.turnLock) {
                //  leave if there is an error, the phase changes or if the round is ended or the action point are decreased
                while (!clientHandler.connectionError &&
                        clientHandler.getDrawTop() == azioniSopraPrecedenti &&
                        clientHandler.getDrawBot() == azioniSottoPrecedenti &&
                        (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) &&
                        clientHandler.getPlayerToPlay() != null &&
                        clientHandler.getPlayerToPlay().equals(myPlayer.getNickName())) {
                    try {
                        clientHandler.turnLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }

            if (clientHandler.connectionError) {
                System.err.println("\n❌ Impossibile pescare.");
                pauseAndClear();
                continue;
            }

            System.out.println("\n✅ Carta Tribù pescata con successo o turno concluso!");
            pauseAndClear();
            isDrawn = true;
            return true;
        }
        return true;
    }

    private boolean drawBottomBuildingCard() {
        boolean isDrawn = false;
        while (!isDrawn) {
            clientHandler.connectionError = false;
            clearScreen();
            System.out.println("--- PESCA CARTA EDIFICIO (SOTTO) ---");

            printCardList("CARTE EDIFICIO DISPONIBILI (SOTTO)", clientHandler.getBottomBuildings());

            System.out.print("\nInserisci la posizione della carta (1 a " + clientHandler.getBottomBuildingSize() + ") oppure 'q' per tornare indietro: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("q")) {
                clearScreen();
                System.out.println("--- PESCA CARTA (SOTTO) ---");
                return false;
            }
            int azioniSopraPrecedenti = clientHandler.getDrawTop();
            int azioniSottoPrecedenti = clientHandler.getDrawBot();

            try {
                int position = Integer.parseInt(input) - 1;
                if (position < 0 || position >= clientHandler.getBottomBuildingSize()) {
                    System.err.println("\n❌ Errore: Posizione fuori limite.");
                    pauseAndClear();
                    continue;
                }
                serverStub.selectCardFromBottomList(myPlayer, CARD_TYPE.BUILDING, position);
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Inserisci un NUMERO valido.");
                pauseAndClear();
                continue;
            } catch (Exception e) {
                System.err.println("\n❌ Impossibile pescare: " + extractCleanError(e));
                pauseAndClear();
                continue;
            }

            System.out.println("\nIn attesa di conferma dal server...");

            synchronized (clientHandler.turnLock) {
                //  leave if there is an error, the phase changes or if the round is ended or the action point are decreased
                while (!clientHandler.connectionError &&
                        clientHandler.getDrawTop() == azioniSopraPrecedenti &&
                        clientHandler.getDrawBot() == azioniSottoPrecedenti &&
                        (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) &&
                        clientHandler.getPlayerToPlay() != null &&
                        clientHandler.getPlayerToPlay().equals(myPlayer.getNickName())) {
                    try {
                        clientHandler.turnLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }

            if (clientHandler.connectionError) {
                System.err.println("\n❌ Impossibile pescare.");
                pauseAndClear();
                continue;
            }

            System.out.println("\n✅ Carta Edificio pescata con successo o turno concluso!");
            pauseAndClear();
            isDrawn = true;
            return true;
        }
        return true;
    }

    private void passTurn() {
        clientHandler.connectionError = false;
        clearScreen();
        System.out.println("--- PASSA TURNO ---");
        try {
            serverStub.playerDoNothing(myPlayer);
        } catch (Exception e) {
            System.err.println("\n❌ Impossibile passare il turno: " + extractCleanError(e));
            pauseAndClear();
            return;
        }
        System.out.println("\nIn attesa di conferma dal server...");
        //i want to be blocked until:
        //there is a connection error, the phase changes th player changes
        synchronized (clientHandler.turnLock) {
            while (!clientHandler.connectionError &&
                    (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) &&
                    clientHandler.getPlayerToPlay() != null &&
                    clientHandler.getPlayerToPlay().equals(myPlayer.getNickName())) {
                try {
                    clientHandler.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        if (clientHandler.connectionError) {
            System.err.println("\n❌ Impossibile passare il turno.");
            pauseAndClear();
        } else {
            System.out.println("\n✅ Turno terminato.");
            pauseAndClear();
        }
    }

    /**
     * Executes handle extra draw.
     */
    private void handleExtraDraw() {
        clearScreen();
        System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");

        boolean isDrawn = false;
        while (!isDrawn) {
            System.out.println("\nScegli da quale mazzo in CIMA vuoi pescare la carta extra:");
            System.out.println("1 - Carta Tribù");
            System.out.println("2 - Carta Edificio");
            System.out.print("Scelta: ");
            String scelta = scanner.nextLine();

            int maxLimit = 0;

            // PRINT THE LIST BASED ON THE CHOICE
            if (scelta.equals("1")) {
                printCardList("CARTE TRIBÙ DISPONIBILI", clientHandler.getTopCards());
                maxLimit = clientHandler.getTopCardSize();
            } else if (scelta.equals("2")) {
                printCardList("CARTE EDIFICIO DISPONIBILI", clientHandler.getTopBuildings());
                maxLimit = clientHandler.getTopBuildingSize();
            } else {
                System.err.println("\n❌ Scelta mazzo non valida. Digita 1 o 2.");
                pauseAndClear();
                clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
                continue;
            }

            System.out.print("\nInserisci la posizione della carta (1 a " + maxLimit + ") oppure 'q' per cambiare mazzo: ");
            String inputPos = scanner.nextLine();

            if (inputPos.equalsIgnoreCase("q")) {
                clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
                continue;
            }

            try {
                int position = Integer.parseInt(inputPos) - 1;
                if (position < 0 || position >= maxLimit) {
                    System.err.println("\n❌ Errore: Posizione fuori limite.");
                    pauseAndClear();
                    clearScreen();
                    System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
                    continue;
                }

                if (scelta.equals("1")) {
                    serverStub.selectExtraCard(myPlayer, CARD_TYPE.ARTIST, position);
                    isDrawn = true;
                } else {
                    serverStub.selectExtraCard(myPlayer, CARD_TYPE.BUILDING, position);
                    isDrawn = true;
                }

                System.out.println("\n✅ Carta extra pescata con successo!");
                pauseAndClear();

            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Inserisci un NUMERO valido.");
                pauseAndClear();
                clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
            } catch (Exception e) {
                System.err.println("\n❌ Impossibile pescare: " + extractCleanError(e));
                pauseAndClear();
                clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
            }
        }
        clientHandler.needsExtraDraw = false;
    }


    // ==========================================================
    // --- HELPER METHODS ---
    // ==========================================================

    /**
     * Executes pause and clear.
     */
    private void pauseAndClear() {
        System.out.println("\n(Premi INVIO per continuare...)");
        scanner.nextLine();
        clearScreen();
    }

    /**
     * Executes clear screen.
     */
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Executes extract clean error.
     * @param e parameter e.
     * @return the result of the operation.
     */
    private String extractCleanError(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains(":")) {
            String[] parts = e.getMessage().split(":");
            return parts[parts.length - 1].trim();
        }
        return e.getMessage() != null ? e.getMessage() : "Errore sconosciuto";
    }

    /**
     * Executes wait for game start.
     */
    private void waitForGameStart() {
        System.out.println("\n⏳ In attesa che si connettano gli altri giocatori...");
        synchronized (clientHandler.gameStartLock) {
            while (!clientHandler.isGameStarted) {
                try {
                    clientHandler.gameStartLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        clearScreen();
        System.out.println("🎉 GIOCO INIZIATO! 🎉");
        System.out.println("Tutti i giocatori sono connessi.");
        pauseAndClear();
    }

    /**
     * Executes wait for my turn.
     */
    private void waitForMyTurn() {
        boolean wasWaiting = false;

        synchronized (clientHandler.turnLock) {
            while (!isMyTurn()) {
                wasWaiting = true;
                clearScreen();
                System.out.println("⏳ In attesa del tuo turno...");

                String currentPlayer = "";
                if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                    currentPlayer = clientHandler.getPlayerToPlace();
                } else if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                    currentPlayer = clientHandler.getPlayerToPlay();
                }

                if (currentPlayer != null && !currentPlayer.isEmpty()) {
                    System.out.println("Attualmente sta giocando: " + currentPlayer);
                }

                try {
                    clientHandler.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (wasWaiting) {
            clearScreen();
            System.out.println("🔔 È IL TUO TURNO!");
            pauseAndClear();
        }
    }

    /**
     * Checks whether my turn.
     * @return the result of the operation.
     */
    private boolean isMyTurn() {
        if (myPlayer == null) return false;
        if (clientHandler.needsExtraDraw) return true;

        if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            return myPlayer.getNickName().equals(clientHandler.getPlayerToPlace());
        } else if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            return myPlayer.getNickName().equals(clientHandler.getPlayerToPlay());
        }
        return false;
    }

    /**
     * Executes number of player.
     * @return the result of the operation.
     */
    private int numberOfPlayer() {
        int numOfPlayers = 0;
        while (true) {
            System.out.print("Quanti giocatori (2-5)? ");
            try {
                numOfPlayers = Integer.parseInt(scanner.nextLine());
                if (numOfPlayers >= 2 && numOfPlayers <= 5) break;
                System.err.println("\n❌ Errore: Il numero deve essere compreso tra 2 e 5.");
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Devi inserire un NUMERO.");
            }
        }
        return numOfPlayers;
    }

    /**
     * Executes bind totem color.
     * @return the result of the operation.
     */
    private COLOR bindTotemColor() {
        while (true) {
            System.out.println("\nScegli colore totem:");
            System.out.println(RED+"1-ROSSO"+BLUE+"\n2-BLU"+YELLOW+"\n3-GIALLO"+GREEN+"\n4-GREEN"+PURPLE+"\n5-PURPLE"+RESET);
            System.out.print("Scelta: ");
            switch (scanner.nextLine()) {
                case "1": return COLOR.RED;
                case "2": return COLOR.BLUE;
                case "3": return COLOR.YELLOW;
                case "4": return COLOR.GREEN;
                case "5": return COLOR.PURPLE;
                default: System.err.println("\n❌ Input non valido");
            }
        }
    }

    /**
     * Returns placing index.
     * @return the result of the operation.
     */
    private int getPlacingIndex() {
        int index = -1;
        while (true) {
            System.out.print("\n📍 Inserisci la casella in cui piazzare il Totem (1 a " + clientHandler.getOfferTileSize() + ") oppure 'q' per annullare: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("q")) {
                return -1;
            }
            try {
                index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < clientHandler.getOfferTileSize()) {
                    break;
                } else {
                    System.err.println("\n❌ Errore: L'indice deve essere tra 1 e " + clientHandler.getOfferTileSize() + ".");
                    pauseAndClear();
                }
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Devi inserire un NUMERO intero valido.");
                pauseAndClear();
            }
        }
        return index;
    }
    // ==========================================================
    // MARKET PRINT METHODS
    // ==========================================================

    /**
     * Prints the whole market (Top, Bottom, Buildings)
     */
    public void printMarket() {
        System.out.println("\n=============================================================");
        System.out.println("                       🏪 IL MERCATO 🏪                      ");
        System.out.println("=============================================================");

        printCardList("CARTE NORMALI (SOPRA)", clientHandler.getTopCards());
        printCardList("CARTE EDIFICIO (SOPRA)", clientHandler.getTopBuildings());

        System.out.println("-------------------------------------------------------------");

        printCardList("CARTE NORMALI (SOTTO)", clientHandler.getBottomCards());
        printCardList("CARTE EDIFICIO (SOTTO)", clientHandler.getBottomBuildings());

        System.out.println("=============================================================\n");
    }


    //List<? ...> means an unknown type except it extends CardDTO
    /**
     * Executes print card list.
     * @param title parameter title.
     * @param cards parameter cards.
     */
    private void printCardList(String title, List<? extends CardDTO> cards) {
        System.out.println("\n▶ " + title + ":");

        if (cards == null || cards.isEmpty()) {
            System.out.println("   [Nessuna carta disponibile in questa fila]");
            return;
        }

        // Table header
        System.out.printf("   %-4s | %-15s | %s\n", "Pos", "Tipo Carta", "Descrizione");

        // Widen the line to 90 dashes because the toString description can be long
        System.out.println("   " + "-".repeat(90));

        for (int i = 0; i < cards.size(); i++) {
            CardDTO card = cards.get(i);
            String type = card.getCardType().toString();

            System.out.printf("   [%d]  | %-15s | %s\n", (i + 1), type, card);
        }
    }

}
