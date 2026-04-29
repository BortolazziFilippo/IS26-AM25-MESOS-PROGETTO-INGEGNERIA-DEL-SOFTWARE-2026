package it.polimi.ingsw.am25.server.webLayer.Socket.messages.drawOneMoreCard;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Socket message that asks the client to pick an extra card from the market,
 * triggered by the draw-one-more building effect.
 */
public class AskExtraDrawMessage implements ServerToClientMessage {
    /** Creates an ask-extra-draw notification message. */
    public AskExtraDrawMessage() {
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#askExtraDraw}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.askExtraDraw();
    }
}
