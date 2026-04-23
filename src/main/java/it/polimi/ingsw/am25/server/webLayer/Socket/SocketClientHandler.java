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
            out=new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in=new ObjectInputStream(socket.getInputStream());
            ClientSocketProxy clientSocketProxy= new ClientSocketProxy(out);
            while (true){
                ClientToServerMessage message=(ClientToServerMessage) in.readObject();
                message.execute(serverLogic,clientSocketProxy);
            }
        }catch (IOException | ClassNotFoundException e) {
            System.out.println("Un client si è disconnesso.");
            //TODO:gestire disconnessione
        } catch (Exception e) {
            UtilitiesFunction.logError(PREFIX+"Errore Socket "+e);
        } finally {
            try { socket.close();
            } catch(IOException e) {
                UtilitiesFunction.logError(PREFIX+"Errore Socket "+e);
            }
        }

    }
}
