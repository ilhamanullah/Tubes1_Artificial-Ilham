package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private Eatfood eatfoods;
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }


    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        // Bot is still in the game
        if (!gameState.getGameObjects().isEmpty()) {

            if(!playerIsDead(gameState, bot)) {
                playerAction = avoidAsteroidField(playerAction, gameState, bot);
                playerAction = avoidGasCloud(playerAction, gameState, bot);
                playerAction = eatfood(playerAction, gameState, bot);
            } else {
                System.out.println("I DED.");
            }
        }

        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    public void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    public double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public int getOppositeHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 180) % 360;
    }

    public int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    // Avoid Asteroid Field
    public PlayerAction avoidAsteroidField(PlayerAction playerAction, GameState gameState, GameObject bot) {
        var asteroidFieldList = gameState.getGameObjects() 
                .stream().filter(obj -> obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                .sorted(Comparator 
                        .comparing(obj -> getDistanceBetween(bot, obj))) 
                .collect(Collectors.toList());

        if(getDistanceBetween(bot, asteroidFieldList.get(0)) - (bot.getSize()/2) < 100)
        {
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = getOppositeHeadingBetween(asteroidFieldList.get(0));
            System.out.println("AVOIDING ASTEROID FIELD.");
        }
        
        return playerAction;
    }

    // Avoid Gas Cloud
    public PlayerAction avoidGasCloud(PlayerAction playerAction, GameState gameState, GameObject bot) {
        var gasCloudList = gameState.getGameObjects() 
                .stream().filter(obj -> obj.getGameObjectType() == ObjectTypes.GAS_CLOUD)
                .sorted(Comparator 
                        .comparing(obj -> getDistanceBetween(bot, obj))) 
                .collect(Collectors.toList());

        if(getDistanceBetween(bot, gasCloudList.get(0)) - (bot.getSize()/2) < 100)
        {
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = getOppositeHeadingBetween(gasCloudList.get(0));
            System.out.println("AVOIDING GAS CLOUD.");
        }
        
        return playerAction;
    }

    // Ded (Minor Bug)
    public boolean playerIsDead(GameState gameState, GameObject bot) {
        var playerList = gameState.getPlayerGameObjects()
            .stream().filter(bots -> bots.getGameObjectType() == ObjectTypes.PLAYER && bots.getId() == bot.getId())
            .collect(Collectors.toList());

        return (playerList.isEmpty());
    }

    // Eat Food (Prioritize Super Food)
    public PlayerAction eatfood(PlayerAction playerAction, GameState gameState, GameObject bot) {
        var foodlist = gameState.getGameObjects() 
            .stream().filter(items -> items.getGameObjectType() == ObjectTypes.FOOD) 
            .sorted(Comparator 
                    .comparing(items -> getDistanceBetween(bot, items))) 
            .collect(Collectors.toList());  
                
        var superfoodList = gameState.getGameObjects() 
            .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD) 
            .sorted(Comparator 
                    .comparing(item -> getDistanceBetween(bot, item))) 
            .collect(Collectors.toList()); 
        
        var obstacle = gameState.getGameObjects() 
                .stream().filter(obj -> obj.getGameObjectType() == ObjectTypes.GAS_CLOUD || obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD) // this filters the list to only food
                .sorted(Comparator 
                        .comparing(obj -> getDistanceBetween(bot, obj))) 
                .collect(Collectors.toList());

        playerAction.action = PlayerActions.FORWARD;
        if (getDistanceBetween(superfoodList.get(0), bot) < 100 && getDistanceBetween(bot, obstacle.get(0)) > 30) {
            playerAction.heading = getHeadingBetween(superfoodList.get(0));
            System.out.println("GOING FOR SUPERFOOD.");
            

        } else if (getDistanceBetween(superfoodList.get(0), bot) > 100 && getDistanceBetween(bot, obstacle.get(0)) > 30) {
            playerAction.heading = getHeadingBetween(foodlist.get(0));
            System.out.println("GOING FOR FOOD.");
        }

        return playerAction;
    }

}
