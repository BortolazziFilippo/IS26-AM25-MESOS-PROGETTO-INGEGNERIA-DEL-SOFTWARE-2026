package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ServerSocketProxy;
import it.polimi.ingsw.am25.client.webLayer.Socket.ServerListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;


public class GUIapp extends Application {

    private ClientVirtualView clientHandler;
    private ServerRemoteInterface serverStub;

    /**
     * JavaFX entry point. Reads the server IP and connection method from the application
     * parameters, displays the splash screen, and on user click establishes
     * the server connection and opens the lobby on the same stage.
     *
     * @param primaryStage the primary JavaFX stage provided by the runtime.
     */
    @Override
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();
        String serverIP = args.size() >= 1 ? args.get(0) : "127.0.0.1";
        String method   = args.size() >= 2 ? args.get(1) : "RMI";

        primaryStage.setOnCloseRequest(e -> System.exit(0));

        // 1. Mostra lo splash screen (immagine, respiro, particelle, prompt).
        // 2. Quando l'utente clicca, fa fade out ed esegue il Runnable qui sotto:
        //    connessione al server + apertura della lobby.
        MesosSplashScreen.show(primaryStage, () -> {
            // Mentre la connessione è in corso, mostriamo una finestra di stato
            // semplice. Se va bene la lobby la sostituirà subito.
            Label statusLabel = new Label("Connessione a " + serverIP + " via " + method + "...");
            VBox statusRoot = new VBox(10, statusLabel);
            statusRoot.setPadding(new Insets(20));
            primaryStage.setScene(new Scene(statusRoot, 500, 200));
            primaryStage.setTitle("IS26-AM25");
            primaryStage.setResizable(true);
            primaryStage.show();

            try {
                connectionToServer(serverIP, method);
                statusLabel.setText("✅ Connesso. Apro la lobby...");
                // Apri la Lobby: riusa lo stesso Stage e sostituisce la scena.
                new LobbyController(serverStub, clientHandler, primaryStage).showing();
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("❌ Connessione fallita ["
                        + e.getClass().getSimpleName() + "]: " + e.getMessage());
            }
        });
    }

    private void connectionToServer(String serverIP, String method) throws Exception {
        clientHandler = new ClientVirtualView();
        if (method.equals("RMI")) {
            String IP = it.polimi.ingsw.am25.client.ClientApp.getLocalIPv4();
            System.setProperty("java.rmi.server.hostname", IP);
            Registry registry = LocateRegistry.getRegistry(serverIP, 1099);
            serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
        } else if (method.equals("SOCKET")) {
            Socket socket = new Socket(serverIP, 6969);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            serverStub = new ServerSocketProxy(out, clientHandler);
            ServerListener listener = new ServerListener(in, clientHandler);
            listener.start();
        } else {
            throw new IllegalArgumentException("Metodo non valido: " + method + " (usa RMI o SOCKET)");
        }


    }

    static void main(String[] args) {
        launch(args);
    }
}
