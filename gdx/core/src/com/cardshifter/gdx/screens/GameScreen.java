package com.cardshifter.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.gdx.*;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.PlayerView;
import com.cardshifter.gdx.ui.cards.CardView;
import com.cardshifter.gdx.ui.zones.CompactHiddenZoneView;
import com.cardshifter.gdx.ui.zones.DefaultZoneView;
import com.cardshifter.gdx.ui.zones.ZoneView;

import java.util.*;

/**
 * Created by Simon on 1/31/2015.
 */
public class GameScreen implements Screen {

    private final CardshifterGame game;
    private final CardshifterClient client;
    private final int playerIndex;
    private final int gameId;

    private final Table table;
    private final Map<Integer, ZoneView> zoneViews = new HashMap<Integer, ZoneView>();
    private final Map<Integer, EntityView> entityViews = new HashMap<Integer, EntityView>();
    private final Map<String, Container<Actor>> holders = new HashMap<String, Container<Actor>>();
    private final List<EntityView> targetsSelected = new ArrayList<EntityView>();
    private AvailableTargetsMessage targetsAvailable;
    private final TargetableCallback onTarget = new TargetableCallback() {
        @Override
        public boolean addEntity(EntityView view) {
            if (targetsSelected.contains(view)) {
                targetsSelected.remove(view);
                view.setTargetable(TargetStatus.TARGETABLE, this);
                return false;
            }

            if (targetsAvailable != null && targetsAvailable.getMax() == 1 && targetsAvailable.getMin() == 1) {
                client.send(new UseAbilityMessage(gameId, targetsAvailable.getEntity(), targetsAvailable.getAction(), new int[]{ view.getId() }));
                return false;
            }

            view.setTargetable(TargetStatus.TARGETED, this);
            return targetsSelected.add(view);
        }
    };
    private final CardshifterClientContext context;

    public GameScreen(final CardshifterGame game, final CardshifterClient client, NewGameMessage message) {
        this.game = game;
        this.client = client;
        this.playerIndex = message.getPlayerIndex();
        this.gameId = message.getGameId();
        this.context = new CardshifterClientContext(game.skin, message.getGameId(), client);

        this.table = new Table(game.skin);

        Table leftTable = new Table(game.skin);
        Table topTable = new Table(game.skin);
        Table rightTable = new Table(game.skin);
        Table centerTable = new Table(game.skin);

        addZoneHolder(leftTable, 1 - this.playerIndex, "");
        addZoneHolder(leftTable, this.playerIndex, "");
        rightTable.add("controls").row();
        TextButton actionDone = new TextButton("Done", game.skin);
        actionDone.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (targetsAvailable != null) {
                    int selected = targetsSelected.size();
                    if (selected >= targetsAvailable.getMin() && selected <= targetsAvailable.getMax()) {
                        int[] targets = new int[targetsSelected.size()];
                        for (int i = 0; i < targets.length; i++) {
                            targets[i] = targetsSelected.get(i).getId();
                        }
                        UseAbilityMessage message = new UseAbilityMessage(gameId, targetsAvailable.getEntity(), targetsAvailable.getAction(), targets);
                        client.send(message);
                    }
                }
            }
        });
        rightTable.add(actionDone);
        topTable.add(leftTable).left().width(150).expandY().fillY();
        topTable.add(centerTable).center().expandX().expandY().fill();
        topTable.add(rightTable).right().width(150).expandY().fillY();

        addZoneHolder(centerTable, 1 - this.playerIndex, "Hand").top();
        addZoneHolder(centerTable, 1 - this.playerIndex, "Battlefield");
        addZoneHolder(centerTable, this.playerIndex, "Battlefield").bottom();

        this.table.add(topTable).expandY().fill().row();
        addZoneHolder(this.table, this.playerIndex, "Hand");

        this.table.setFillParent(true);
        this.table.setDebug(true, true);
    }

    private Cell<Container<Actor>> addZoneHolder(Table table, int i, String name) {
        Container<Actor> container = new Container<Actor>();
        Cell<Container<Actor>> cell = table.add(container).expandX().fillX();
        table.row();
        holders.put(i + name, container);
        return cell;
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {
        game.stage.addActor(table);
    }

    @Override
    public void hide() {
        table.remove();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    public Map<Class<? extends Message>, SpecificHandler<?>> getHandlers() {
        Map<Class<? extends Message>, SpecificHandler<?>> handlers =
                new HashMap<Class<? extends Message>, SpecificHandler<?>>();

        handlers.put(AvailableTargetsMessage.class, new SpecificHandler<AvailableTargetsMessage>() {
            @Override
            public void handle(AvailableTargetsMessage message) {
                targetsAvailable = message;
                targetsSelected.clear();
                for (EntityView view : entityViews.values()) {
                    view.setTargetable(TargetStatus.NOT_TARGETABLE, onTarget);
                }
                for (int id : message.getTargets()) {
                    EntityView view = entityViews.get(id);
                    if (view != null) {
                        view.setTargetable(TargetStatus.TARGETABLE, onTarget);
                    }
                }
            }
        });
        handlers.put(UsableActionMessage.class, new SpecificHandler<UsableActionMessage>() {
            @Override
            public void handle(UsableActionMessage message) {
                int id = message.getId();
                EntityView view = entityViews.get(id);
                if (view != null) {
                    view.usableAction(message);
                }
            }
        });
        handlers.put(CardInfoMessage.class, new SpecificHandler<CardInfoMessage>() {
            @Override
            public void handle(CardInfoMessage message) {
                ZoneView zone = getZoneView(message.getZone());
                removeCard(zone, message.getId());
                if (zone != null) {
                    entityViews.put(message.getId(), zone.addCard(message));
                }
            }
        });
        handlers.put(EntityRemoveMessage.class, new SpecificHandler<EntityRemoveMessage>() {
            @Override
            public void handle(EntityRemoveMessage message) {
                EntityView view = entityViews.get(message.getEntity());
                if (view != null) {
                    view.remove();
                    entityViews.remove(message.getEntity());
                }
            }
        });
        handlers.put(GameOverMessage.class, null);
        handlers.put(PlayerMessage.class, new SpecificHandler<PlayerMessage>() {
            @Override
            public void handle(PlayerMessage message) {
                PlayerView playerView = new PlayerView(context, message);
                entityViews.put(message.getId(), playerView);

                Container<Actor> holder = holders.get(String.valueOf(message.getIndex()));
                if (holder != null) {
                    holder.setActor(playerView.getActor());
                }
            }
        });
        handlers.put(ResetAvailableActionsMessage.class, new SpecificHandler<ResetAvailableActionsMessage>() {
            @Override
            public void handle(ResetAvailableActionsMessage message) {
                for (EntityView view : entityViews.values()) {
                    view.clearUsableActions();
                }
            }
        });
        handlers.put(UpdateMessage.class, new SpecificHandler<UpdateMessage>() {
            @Override
            public void handle(UpdateMessage message) {
                EntityView entityView = entityViews.get(message.getId());
                if (entityView != null) {
                    entityView.set(message.getKey(), message.getValue());
                }
            }
        });
        handlers.put(ZoneChangeMessage.class, new SpecificHandler<ZoneChangeMessage>() {
            @Override
            public void handle(ZoneChangeMessage message) {
                ZoneView oldZone = getZoneView(message.getSourceZone()); // can be null
                ZoneView destinationZone = getZoneView(message.getDestinationZone());
                int id = message.getEntity();
                CardView cardView = (CardView) entityViews.get(id); // can be null
                removeCard(oldZone, id);
                if (destinationZone != null) {
                    EntityView newView = destinationZone.addCard(new CardInfoMessage(message.getDestinationZone(), id, cardView == null ? null : cardView.getInfo()));
                    entityViews.put(id, newView);
                }
            }
        });
        handlers.put(ZoneMessage.class, new SpecificHandler<ZoneMessage>() {
            @Override
            public void handle(ZoneMessage message) {
                Gdx.app.log("GameScreen", "Zone " + message);
                ZoneView zoneView = createZoneView(message);
                if (zoneView != null) {
                    PlayerView view = (PlayerView) entityViews.get(message.getOwner());
                    if (view == null) {
                        Gdx.app.log("GameScreen", "no playerView for " + message.getOwner());
                        return;
                    }
                    String key = view.getIndex() + message.getName();
                    Container<Actor> container = holders.get(key);
                    if (container == null) {
                        Gdx.app.log("GameScreen", "no container for " + key);
                        return;
                    }
                    Gdx.app.log("GameScreen", "putting zoneview for " + key);
                    container.setActor(zoneView.getActor());
                    zoneViews.put(message.getId(), zoneView);
                    zoneView.apply(message);
                    table.setDebug(true, true);
                }
            }
        });

        return handlers;
    }

    private void removeCard(ZoneView zone, int id) {
        if (zone != null) {
            zone.removeCard(id);
        }
        EntityView entityView = entityViews.remove(id);
        if (entityView != null) {
            entityView.remove();
        }
    }

    private ZoneView createZoneView(ZoneMessage message) {
        String type = message.getName();
        if (type.equals("Battlefield")) {
            return new DefaultZoneView(context, message, this.entityViews);
        }
        if (type.equals("Hand")) {
            return new DefaultZoneView(context, message, this.entityViews);
        }
        if (type.equals("Deck")) {
            return new CompactHiddenZoneView(game, message);
        }
        if (type.equals("Cards")) {
            return null; // Card models only
        }
        throw new RuntimeException("Unknown ZoneView type: " + message.getName());
    }

    private ZoneView getZoneView(int id) {
        return this.zoneViews.get(id);
    }
}
