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

    public double getDistanceBetweenEdge(GameObject object1, GameObject object2) {
        return getDistanceBetween(object1, object2) - (object1.getSize()/2) - (object2.getSize()/2);
    }

    public int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public int getSpecifiedHeadingBetween(GameObject otherObject, int degrees) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + degrees) % 360;
    }

    public int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    // ====================== COMPUTE NEXT PLAYER ACTION ====================== //

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        // Bot is still in the game
        // if (!gameState.getGameObjects().isEmpty()) {
        if (!gameState.getGameObjects().isEmpty()) {

            // Get Nearest Food
            var nearestFood = gameState.getGameObjects() 
                .stream().filter(items -> items.getGameObjectType() == ObjectTypes.FOOD) 
                .sorted(Comparator 
                        .comparing(items -> getDistanceBetween(bot, items))) 
                .collect(Collectors.toList()).get(0);
        
            // Get Nearest Super Food
            var nearestSuperFood = gameState.getGameObjects() 
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD) 
                .sorted(Comparator 
                        .comparing(item -> getDistanceBetween(bot, item))) 
                .collect(Collectors.toList()).get(0);

            // Get Nearest Asteroid Field
            var nearestAsteroidField = gameState.getGameObjects() 
                .stream().filter(obj -> obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                .sorted(Comparator 
                        .comparing(obj -> getDistanceBetween(bot, obj))) 
                .collect(Collectors.toList()).get(0);

            // Get Nearest Gas Cloud
            var nearestGasCloud = gameState.getGameObjects() 
                .stream().filter(obj -> obj.getGameObjectType() == ObjectTypes.GAS_CLOUD)
                .sorted(Comparator 
                        .comparing(obj -> getDistanceBetween(bot, obj))) 
                .collect(Collectors.toList()).get(0);

            playerAction.action = PlayerActions.FORWARD;

            // Avoid Gas Cloud
            if (getDistanceBetweenEdge(nearestGasCloud, bot) < 100) {
                playerAction.heading = getSpecifiedHeadingBetween(nearestGasCloud, 90);
                System.out.println("AVOIDING GAS CLOUD.");

            // Avoid Asteroid Field
            } else if (getDistanceBetweenEdge(nearestAsteroidField, bot) < 100) {
                playerAction.heading = getSpecifiedHeadingBetween(nearestAsteroidField, 90);
                System.out.println("AVOIDING ASTEROID FIELD.");

            // Going For Food
            } else {

                // Prioritizing Super Food
                if (getDistanceBetweenEdge(nearestSuperFood, bot) < getDistanceBetweenEdge(nearestFood, bot)) {
                    playerAction.heading = getHeadingBetween(nearestSuperFood);
                    System.out.println("GOING FOR SUPERFOOD.");
                } else {
                    playerAction.heading = getHeadingBetween(nearestFood);
                    System.out.println("GOING FOR FOOD.");
                }
            }
        }

        this.playerAction = playerAction;
    }
}
