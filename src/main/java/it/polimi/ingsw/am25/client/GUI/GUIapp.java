package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ServerListener;
import it.polimi.ingsw.am25.client.webLayer.Socket.ServerSocketProxy;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> args = getParameters().getRaw();
        String serverIP = args.size() >= 1 ? args.get(0) : "127.0.0.1";
        String method = args.size() >= 2 ? args.get(1) : "RMI";

        Label connection = new Label("Connessione a " + serverIP + " via " + method + "...");
        VBox root = new VBox(10, connection);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 500, 200);
        primaryStage.setTitle("IS26-AM25");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));

        //tento la connessione
        try {
            connectionToServer(serverIP, method);
            connection.setText("✅ Connesso. Apro la lobby...");
            // Apri la Lobby. Il LobbyController riusa lo stesso Stage e ne sostituisce la scena.
            new LobbyController(serverStub, clientHandler, primaryStage).showing();
        } catch (Exception e) {
            e.printStackTrace();
            connection.setText("❌ Connessione fallita ["
                    + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }


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
