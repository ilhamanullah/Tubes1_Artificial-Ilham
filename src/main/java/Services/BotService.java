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
    
    public void displayBotDetail() {
        var currTick = gameState.getWorld().getCurrentTick(); // Get current tick
        var x = bot.getPosition().getX(); // Get bot x position
        var y = bot.getPosition().getY(); // Get bot y position
        var r = bot.getSize(); // Get bot resolution
        var s = bot.getSpeed(); // Get bot speed
        var h = bot.getHeading(); // Get bot heading

        System.out.print(currTick);
        System.out.print(". P(");
        System.out.print(x);
        System.out.print(",");
        System.out.print(y);
        System.out.print(") : R(");
        System.out.print(r);
        System.out.print(") : S(");
        System.out.print(s);
        System.out.print(") : H(");
        System.out.print(h);
        System.out.print(") : ");
    }

    public double getDistanceBetweenWorldCenter(GameObject object1) {
        var triangleX = Math.abs(object1.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public int getHeadingBetweenWorldCenter(int degrees) {
        var direction = toDegrees(Math.atan2(-bot.getPosition().y, -bot.getPosition().x));
        return (direction + degrees) % 360;
    }

    // ====================== COMPUTE NEXT PLAYER ACTION ====================== //

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        // Bot is still in the game
        if (!gameState.getGameObjects().isEmpty()) {
    
            // Get Enemy List
            var enemyList = gameState.playerGameObjects
                .stream().filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER && player.getId() != bot.getId()) 
                .sorted(Comparator 
                        .comparing(player -> getDistanceBetween(bot, player))) 
                .collect(Collectors.toList());

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

            // Display Bot Action Status
            displayBotDetail();

            // Stays in World Zone
            if (gameState.getWorld().getRadius() - getDistanceBetweenWorldCenter(bot) - (bot.getSize()/2) < 300) {
                playerAction.heading = getHeadingBetweenWorldCenter(70);
                System.out.println("AVOIDING WORLD EDGE.");

            // Avoid Gas Cloud
            } else if (getDistanceBetweenEdge(nearestGasCloud, bot) < 100) {
                playerAction.heading = getSpecifiedHeadingBetween(nearestGasCloud, 100);
                System.out.println("AVOIDING GAS CLOUD.");

            // Avoid Asteroid Field
            } else if (getDistanceBetweenEdge(nearestAsteroidField, bot) < 100) {
                playerAction.heading = getSpecifiedHeadingBetween(nearestAsteroidField, 100);
                System.out.println("AVOIDING ASTEROID FIELD.");

            // Target A Smaller Enemy
            } else if (enemyList.get(0).getSize() < bot.getSize() && getDistanceBetweenEdge(enemyList.get(0), bot) <= 600) {
                playerAction.heading = getHeadingBetween(enemyList.get(0));
                System.out.println("CHASING AN ENEMY.");
            
            // Running Away from An Enemy
            } else if (enemyList.get(0).getSize() >= bot.getSize() && getDistanceBetweenEdge(enemyList.get(0), bot) <= 150) {
                playerAction.heading = getSpecifiedHeadingBetween(enemyList.get(0), 180);
                System.out.println("RUNNING AWAY FROM AN ENEMY.");

            // Going For Food
            } else {

                // Prioritizing Super Food
                if (getDistanceBetweenEdge(nearestSuperFood, bot) + 100 == getDistanceBetweenEdge(nearestFood, bot)) {
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
