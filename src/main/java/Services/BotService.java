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

            // var foodList = gameState.getGameObjects()
            //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
            //         .sorted(Comparator
            //                 .comparing(item -> getDistanceBetween(bot, item)))
            //         .collect(Collectors.toList());

            // playerAction.heading = getHeadingBetween(foodList.get(0));
            playerAction = eatfood(playerAction, gameState, bot);

        }
        // Bot has been consumed
        else
        {
            System.out.println("I am ded.");


            
            // playerAction = eatfood(playerAction, gameState, bot);
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

    public int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
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
            System.out.println("sf");
            

        } else if (getDistanceBetween(superfoodList.get(0), bot) > 100 && getDistanceBetween(bot, obstacle.get(0)) > 30) {
            playerAction.heading = getHeadingBetween(foodlist.get(0));
            System.out.println("f");
        }
        return playerAction;
    }

}
