package com.cardshifter.server.model;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;

public class Handlers {

	private static final Logger logger = LogManager.getLogger(Handlers.class);
	private final Server server;
	
	public Handlers(Server server) {
		this.server = server;
	}

	public void loginMessage(LoginMessage message, ClientIO client) {
		logger.info("Login request: " + message.getUsername() + " for client " + client);
		if (message.getUsername().startsWith("x")) {
			client.sendToClient(new WelcomeMessage(0, false));
			return;
		}
		logger.info("Client is welcome!");
		client.setName(message.getUsername());
		client.sendToClient(new WelcomeMessage(client.getId(), true));
	}

	public void play(StartGameRequest message, ClientIO client) {
		if (message.getOpponent() < 0) {
			this.playAny(message, client);
		}
		else {
			ClientIO target = server.getClients().get(message.getOpponent());
			if (target == null) {
				logger.warn("Invite sent to unknown user: " + message);
				client.sendToClient(new InviteResponse(0, false));
				return;
			}
			
			ServerGame game = server.createGame(message.getGameType());
			ServerHandler<GameInvite> invites = server.getInvites();
			GameInvite invite = new GameInvite(server, invites.newId(), client, game);
			invites.add(invite);
			client.sendToClient(new WaitMessage());
			
			invite.sendInvite(target);
		}
	}
	
	public void inviteResponse(InviteResponse message, ClientIO client) {
		GameInvite invite = server.getInvites().get(message.getInviteId());
		if (invite != null) {
			invite.handleResponse(client, message.isAccepted());
		}
		else {
			logger.warn("No such invite: " + message.getInviteId());
		}
	}

	private void playAny(StartGameRequest message, ClientIO client) {
		AtomicReference<ClientIO> playAny = server.getPlayAny();
		if (playAny.compareAndSet(null, client)) {
			client.sendToClient(new WaitMessage());
		}
		else {
			ClientIO opponent = playAny.getAndSet(null);
			
			ServerGame game = server.createGame(message.getGameType());
			ServerHandler<GameInvite> invites = server.getInvites();
			GameInvite invite = new GameInvite(server, invites.newId(), client, game);
			invites.add(invite);
			invite.addPlayer(opponent);
			invite.start();
		}
	}

	public void useAbility(UseAbilityMessage message, ClientIO client) {
		TCGGame game = (TCGGame) server.getGames().get(message.getGameId());
		game.handleMove(message, client);
	}

	public void requestTargets(RequestTargetsMessage message, ClientIO client) {
		TCGGame game = (TCGGame) server.getGames().get(message.getGameId());
		game.informAboutTargets(message, client);
	}

}
