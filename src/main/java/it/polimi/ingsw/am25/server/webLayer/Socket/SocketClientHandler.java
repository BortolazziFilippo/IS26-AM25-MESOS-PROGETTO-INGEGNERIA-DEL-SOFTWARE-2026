package it.polimi.ingsw.am25.server.webLayer.Socket;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClientHandler extends Thread{
    private final String PREFIX="[SERVER][SOCKET]";
    private final Socket socket;
    private final ServerRemoteInterface serverLogic;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketClientHandler(Socket socket,ServerRemoteInterface serverLogic){
        this.socket=socket;
        this.serverLogic=serverLogic;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            ClientSocketProxy clientSocketProxy = new ClientSocketProxy(out);

            while (true) {
                ClientToServerMessage message;
                try {
                    // Lettura del messaggio: se cade la rete lancia eccezione e chiude la socket
                    message = (ClientToServerMessage) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw e; // Rilancia per farlo catturare dal catch esterno e disconnettere
                }

                try {
                    // Esecuzione logica: se c'è un errore di gioco, NON chiudiamo la connessione
                    message.execute(serverLogic, clientSocketProxy);
                } catch (Exception e) {
                    UtilitiesFunction.logError(PREFIX + " Errore logica partita: " + e.getMessage());
                    // Notifichiamo l'errore al client tramite il proxy
                    clientSocketProxy.showErrorMessage(e.getMessage());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Un client si è disconnesso.");
            //TODO: gestire disconnessione
        } finally {
            try {
                socket.close();
            } catch(IOException e) {
                UtilitiesFunction.logError(PREFIX + "Errore Socket " + e);
            }
        }
    }
}