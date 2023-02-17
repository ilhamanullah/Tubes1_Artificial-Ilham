package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private GameObject currentTeleporter;
    private int currentTeleporterHeading = -999;
    private boolean isFiredTeleporter = false;

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
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
                .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    public int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    // ==================== GET DISTANCE ==================== //

    public double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public double getDistanceBetweenEdge(GameObject object1, GameObject object2) {
        return getDistanceBetween(object1, object2) - (object1.getSize() / 2) - (object2.getSize() / 2);
    }

    public double getDistanceBetweenWorldCenter(GameObject object1) {
        var triangleX = Math.abs(object1.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public double maxDistance(List<GameObject> objList) {

        double max = 0;

        for (int i = 0; i < objList.size(); i++) {
            if (getDistanceBetween(objList.get(i), bot) > max) {
                max = getDistanceBetween(objList.get(i), bot);
            }
        }

        return max;
    }

    // ==================== GET HEADING ==================== //

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

    public int getHeadingBetweenWorldCenter(int degrees) {
        var direction = toDegrees(Math.atan2(-bot.getPosition().y, -bot.getPosition().x));
        return (direction + degrees) % 360;
    }

    public int getHeadingKuadran(GameObject object) {
        var currentDirection = object.getHeading();
        if (currentDirection > 0 && currentDirection <= 90) {
            return 1;
        } else if (currentDirection > 90 && currentDirection <= 180) {
            return 2;
        } else if (currentDirection > 180 && currentDirection <= 270) {
            return 3;
        } else {
            return 4;
        }
    }

    // ==================== DISPLAY STATE ==================== //

    public void displayBotDetail() {

        var currTick = gameState.getWorld().getCurrentTick(); // Get current tick
        var x = bot.getPosition().getX(); // Get bot x position
        var y = bot.getPosition().getY(); // Get bot y position
        var r = bot.getSize(); // Get bot resolution
        var s = bot.getSpeed(); // Get bot speed
        var h = bot.getHeading(); // Get bot heading

        System.out.print(currTick);

        System.out.print(". SC(");
        System.out.print(bot.shieldCount);
        System.out.print(") : TC(");
        System.out.print(bot.teleporterCount);
        System.out.print(") : TORC(");
        System.out.print(bot.torpedoSalvoCount);

        System.out.print(") : P(");
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

    // ==================== GET LISTS OF GAME OBJECTS ==================== //

    public boolean isObjectClear(GameObject object, List<GameObject> enemyList, List<GameObject> gasCloudList,
            int threatDistanceEnemy, int threatDistanceGasCloud) {

        if (enemyList.isEmpty() && gasCloudList.isEmpty()) {
            return (gameState.getWorld().getRadius() - getDistanceBetweenWorldCenter(object)
                    - (bot.getSize() / 2) > 100);
        } else if (!enemyList.isEmpty() && gasCloudList.isEmpty()) {
            return (getDistanceBetween(object, enemyList.get(0)) > threatDistanceEnemy &&
                    gameState.getWorld().getRadius() - getDistanceBetweenWorldCenter(object)
                            - (bot.getSize() / 2) > 100);
        } else if (enemyList.isEmpty() && !gasCloudList.isEmpty()) {
            return (getDistanceBetween(object, gasCloudList.get(0)) > threatDistanceGasCloud &&
                    gameState.getWorld().getRadius() - getDistanceBetweenWorldCenter(object)
                            - (bot.getSize() / 2) > 100);
        } else {
            return (getDistanceBetween(object, gasCloudList.get(0)) > threatDistanceGasCloud &&
                    getDistanceBetween(object, enemyList.get(0)) > threatDistanceEnemy &&
                    gameState.getWorld().getRadius() - getDistanceBetweenWorldCenter(object)
                            - (bot.getSize() / 2) > 100);
        }

    }

    public List<GameObject> getEnemyList(GameObject referenceObject) {
        return gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(referenceObject, player)))
                .collect(Collectors.toList());
    }

    public List<GameObject> getObjectList(ObjectTypes objectType, GameObject referObject) {
        return gameState.getGameObjects()
                .stream().filter(obj -> obj.getGameObjectType() == objectType)
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(referObject, obj)))
                .collect(Collectors.toList());
    }

    public List<GameObject> getFoodList(ObjectTypes objectType, GameObject referObject, List<GameObject> enemyList,
            List<GameObject> gasCloudList, int threatDistanceEnemy,
            int threatDistanceGasCloud) {
        return gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == objectType
                        && isObjectClear(obj, enemyList, gasCloudList, threatDistanceEnemy, threatDistanceGasCloud))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(referObject, obj)))
                .collect(Collectors.toList());
    }

    // ==================== TELEPORTER FLAG ==================== //

    public void setIsFiredTeleporter(boolean isFire) {
        isFiredTeleporter = isFire;
    }

    public void setTeleporterHeading(int heading) {
        currentTeleporterHeading = heading;
    }

    public void setCurrentTeleporter() {
        var teleporterList = getObjectList(ObjectTypes.TELEPORTER, bot);

        currentTeleporter = null;
        for (GameObject tp : teleporterList) {
            if (tp.getHeading() + 5 >= currentTeleporterHeading && tp.getHeading() - 5 <= currentTeleporterHeading) {
                currentTeleporter = tp;
                break;
            }
            setTeleporterHeading(-999);
        }
    }

    // ==================== SCORING ==================== //

    public double scoringKuadran1(GameState gameState) {

        var smallerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() < bot.getSize()
                        && player.getPosition().getX() >= bot.getPosition().getX()
                        && player.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var biggerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() >= bot.getSize()
                        && player.getPosition().getX() >= bot.getPosition().getX()
                        && player.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var foodList = gameState.getGameObjects()
                .stream()
                .filter(food -> food.getGameObjectType() == ObjectTypes.FOOD
                        && food.getPosition().getX() >= bot.getPosition().getX()
                        && food.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(food -> getDistanceBetween(bot, food)))
                .collect(Collectors.toList());

        var superFoodList = gameState.getGameObjects()
                .stream()
                .filter(superFood -> superFood.getGameObjectType() == ObjectTypes.SUPERFOOD
                        && superFood.getPosition().getX() >= bot.getPosition().getX()
                        && superFood.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(superFood -> getDistanceBetween(bot, superFood)))
                .collect(Collectors.toList());

        var obstacleList = gameState.getGameObjects()
                .stream()
                .filter(obj -> (obj.getGameObjectType() == ObjectTypes.GAS_CLOUD
                        || obj.getGameObjectType() == ObjectTypes.WORMHOLE)
                        && (obj.getPosition().getX() >= bot.getPosition().getX()
                                && obj.getPosition().getY() >= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var asteroidFieldList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD
                        && (obj.getPosition().getX() >= bot.getPosition().getX()
                                && obj.getPosition().getY() >= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var torpedoList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
                        && (obj.getPosition().getX() >= bot.getPosition().getX()
                                && obj.getPosition().getY() >= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        double score = 0;

        // Smaller Enemy
        for (int i = 0; i < smallerEnemyList.size(); i++) {
            score += (5 * ((bot.getSize() - smallerEnemyList.get(i).getSize()) / bot.getSize())
                    * ((maxDistance(smallerEnemyList) - smallerEnemyList.get(i).getSize())
                            / maxDistance(smallerEnemyList)))
                    + 1;
        }

        // Food
        for (int i = 0; i < foodList.size(); i++) {
            score += ((maxDistance(foodList) - getDistanceBetweenEdge(foodList.get(i), bot)) / maxDistance(foodList))
                    + 1;
        }

        // Super Food
        for (int i = 0; i < superFoodList.size(); i++) {
            score += (5
                    * ((maxDistance(superFoodList) - getDistanceBetweenEdge(superFoodList.get(i), bot))
                            / maxDistance(superFoodList)))
                    + 1;
        }

        // Bigger Enemy
        for (int i = 0; i < biggerEnemyList.size(); i++) {
            score += 50 * ((biggerEnemyList.get(i).getSize() - bot.getSize()) / biggerEnemyList.get(i).getSize())
                    * (getDistanceBetweenEdge(biggerEnemyList.get(i), bot) / maxDistance(biggerEnemyList));
        }

        // Obstacle
        for (int i = 0; i < obstacleList.size(); i++) {
            score += 15 * (getDistanceBetweenEdge(obstacleList.get(i), bot) / maxDistance(obstacleList));
        }

        // Asteroid
        for (int i = 0; i < asteroidFieldList.size(); i++) {
            score += 10 * (getDistanceBetween(bot, asteroidFieldList.get(i))
                    / maxDistance(asteroidFieldList));
        }

        // Torpedo
        for (int i = 0; i < torpedoList.size(); i++) {
            score += 15 * (getDistanceBetween(bot, torpedoList.get(i))
                    / maxDistance(torpedoList));
        }

        return score;
    }

    public double scoringKuadran2(GameState gameState) {

        var smallerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() < bot.getSize()
                        && player.getPosition().getX() <= bot.getPosition().getX()
                        && player.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var biggerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() >= bot.getSize()
                        && player.getPosition().getX() <= bot.getPosition().getX()
                        && player.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var foodList = gameState.getGameObjects()
                .stream()
                .filter(food -> food.getGameObjectType() == ObjectTypes.FOOD
                        && food.getPosition().getX() <= bot.getPosition().getX()
                        && food.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(food -> getDistanceBetween(bot, food)))
                .collect(Collectors.toList());

        var superFoodList = gameState.getGameObjects()
                .stream()
                .filter(superFood -> superFood.getGameObjectType() == ObjectTypes.SUPERFOOD
                        && superFood.getPosition().getX() <= bot.getPosition().getX()
                        && superFood.getPosition().getY() >= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(superFood -> getDistanceBetween(bot, superFood)))
                .collect(Collectors.toList());

        var obstacleList = gameState.getGameObjects()
                .stream()
                .filter(obj -> (obj.getGameObjectType() == ObjectTypes.GAS_CLOUD
                        || obj.getGameObjectType() == ObjectTypes.WORMHOLE)
                        && (obj.getPosition().getX() <= bot.getPosition().getX()
                                && obj.getPosition().getY() >= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var asteroidFieldList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD
                        && (obj.getPosition().getX() <= bot.getPosition().getX()
                                && obj.getPosition().getY() >= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var torpedoList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
                        && (obj.getPosition().getX() <= bot.getPosition().getX()
                                && obj.getPosition().getY() >= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        double score = 0;

        // Smaller Enemy
        for (int i = 0; i < smallerEnemyList.size(); i++) {
            score += (5 * ((bot.getSize() - smallerEnemyList.get(i).getSize()) / bot.getSize())
                    * ((maxDistance(smallerEnemyList) - biggerEnemyList.get(i).getSize())
                            / maxDistance(smallerEnemyList)))
                    + 1;
        }

        // Food
        for (int i = 0; i < foodList.size(); i++) {
            score += ((maxDistance(foodList) - getDistanceBetweenEdge(foodList.get(i), bot)) / maxDistance(foodList))
                    + 1;
        }

        // Super Food
        for (int i = 0; i < superFoodList.size(); i++) {
            score += (5
                    * ((maxDistance(superFoodList) - getDistanceBetweenEdge(superFoodList.get(i), bot))
                            / maxDistance(superFoodList)))
                    + 1;
        }

        // Bigger Enemy
        for (int i = 0; i < biggerEnemyList.size(); i++) {
            score += 50 * ((biggerEnemyList.get(i).getSize() - bot.getSize()) / biggerEnemyList.get(i).getSize())
                    * (getDistanceBetweenEdge(biggerEnemyList.get(i), bot) / maxDistance(biggerEnemyList));
        }

        // Obstacle
        for (int i = 0; i < obstacleList.size(); i++) {
            score += 15 * (getDistanceBetweenEdge(obstacleList.get(i), bot) / maxDistance(obstacleList));
        }

        // Asteroid
        for (int i = 0; i < asteroidFieldList.size(); i++) {
            score += 10 * (getDistanceBetween(bot, asteroidFieldList.get(i))
                    / maxDistance(asteroidFieldList));
        }

        // Torpedo
        for (int i = 0; i < torpedoList.size(); i++) {
            score += 15 * (getDistanceBetween(bot, torpedoList.get(i))
                    / maxDistance(torpedoList));
        }

        return score;
    }

    public double scoringKuadran3(GameState gameState) {

        var smallerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() < bot.getSize()
                        && player.getPosition().getX() <= bot.getPosition().getX()
                        && player.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var biggerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() >= bot.getSize()
                        && player.getPosition().getX() <= bot.getPosition().getX()
                        && player.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var foodList = gameState.getGameObjects()
                .stream()
                .filter(food -> food.getGameObjectType() == ObjectTypes.FOOD
                        && food.getPosition().getX() <= bot.getPosition().getX()
                        && food.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(food -> getDistanceBetween(bot, food)))
                .collect(Collectors.toList());

        var superFoodList = gameState.getGameObjects()
                .stream()
                .filter(superFood -> superFood.getGameObjectType() == ObjectTypes.SUPERFOOD
                        && superFood.getPosition().getX() <= bot.getPosition().getX()
                        && superFood.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(superFood -> getDistanceBetween(bot, superFood)))
                .collect(Collectors.toList());

        var obstacleList = gameState.getGameObjects()
                .stream()
                .filter(obj -> (obj.getGameObjectType() == ObjectTypes.GAS_CLOUD
                        || obj.getGameObjectType() == ObjectTypes.WORMHOLE)
                        && (obj.getPosition().getX() <= bot.getPosition().getX()
                                && obj.getPosition().getY() <= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var asteroidFieldList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD
                        && (obj.getPosition().getX() <= bot.getPosition().getX()
                                && obj.getPosition().getY() <= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var torpedoList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
                        && (obj.getPosition().getX() <= bot.getPosition().getX()
                                && obj.getPosition().getY() <= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        double score = 0;

        // Smaller Enemy
        for (int i = 0; i < smallerEnemyList.size(); i++) {
            score += (5 * ((bot.getSize() - smallerEnemyList.get(i).getSize()) / bot.getSize())
                    * ((maxDistance(smallerEnemyList) - smallerEnemyList.get(i).getSize())
                            / maxDistance(smallerEnemyList)))
                    + 1;
        }

        // Food
        for (int i = 0; i < foodList.size(); i++) {
            score += ((maxDistance(foodList) - getDistanceBetweenEdge(foodList.get(i), bot)) / maxDistance(foodList))
                    + 1;
        }

        // Super Food
        for (int i = 0; i < superFoodList.size(); i++) {
            score += (5
                    * ((maxDistance(superFoodList) - getDistanceBetweenEdge(superFoodList.get(i), bot))
                            / maxDistance(superFoodList)))
                    + 1;
        }

        // Bigger Enemy
        for (int i = 0; i < biggerEnemyList.size(); i++) {
            score += 50 * ((biggerEnemyList.get(i).getSize() - bot.getSize()) / biggerEnemyList.get(i).getSize())
                    * (getDistanceBetweenEdge(biggerEnemyList.get(i), bot) / maxDistance(biggerEnemyList));
        }

        // Obstacle
        for (int i = 0; i < obstacleList.size(); i++) {
            score += 15 * (getDistanceBetweenEdge(obstacleList.get(i), bot) / maxDistance(obstacleList));
        }

        // Asteroid
        for (int i = 0; i < asteroidFieldList.size(); i++) {
            score += 10 * (getDistanceBetween(bot, asteroidFieldList.get(i))
                    / maxDistance(asteroidFieldList));
        }

        // Torpedo
        for (int i = 0; i < torpedoList.size(); i++) {
            score += 15 * (getDistanceBetween(bot, torpedoList.get(i))
                    / maxDistance(torpedoList));
        }

        return score;
    }

    public double scoringKuadran4(GameState gameState) {

        var smallerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() < bot.getSize()
                        && player.getPosition().getX() >= bot.getPosition().getX()
                        && player.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var biggerEnemyList = gameState.playerGameObjects
                .stream()
                .filter(player -> player.getGameObjectType() == ObjectTypes.PLAYER
                        && player.getId() != bot.getId()
                        && player.getSize() >= bot.getSize()
                        && player.getPosition().getX() >= bot.getPosition().getX()
                        && player.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());

        var foodList = gameState.getGameObjects()
                .stream()
                .filter(food -> food.getGameObjectType() == ObjectTypes.FOOD
                        && food.getPosition().getX() >= bot.getPosition().getX()
                        && food.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(food -> getDistanceBetween(bot, food)))
                .collect(Collectors.toList());

        var superFoodList = gameState.getGameObjects()
                .stream()
                .filter(superFood -> superFood.getGameObjectType() == ObjectTypes.SUPERFOOD
                        && superFood.getPosition().getX() >= bot.getPosition().getX()
                        && superFood.getPosition().getY() <= bot.getPosition().getY())
                .sorted(Comparator
                        .comparing(superFood -> getDistanceBetween(bot, superFood)))
                .collect(Collectors.toList());

        var obstacleList = gameState.getGameObjects()
                .stream()
                .filter(obj -> (obj.getGameObjectType() == ObjectTypes.GAS_CLOUD
                        || obj.getGameObjectType() == ObjectTypes.WORMHOLE)
                        && (obj.getPosition().getX() >= bot.getPosition().getX()
                                && obj.getPosition().getY() <= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var asteroidFieldList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD
                        && (obj.getPosition().getX() >= bot.getPosition().getX()
                                && obj.getPosition().getY() <= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        var torpedoList = gameState.getGameObjects()
                .stream()
                .filter(obj -> obj.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
                        && (obj.getPosition().getX() >= bot.getPosition().getX()
                                && obj.getPosition().getY() <= bot.getPosition()
                                        .getY()))
                .sorted(Comparator
                        .comparing(obj -> getDistanceBetween(bot, obj)))
                .collect(Collectors.toList());

        double score = 0;

        // Smaller Enemy
        for (int i = 0; i < smallerEnemyList.size(); i++) {
            score += (5 * ((bot.getSize() - smallerEnemyList.get(i).getSize()) / bot.getSize())
                    * ((maxDistance(smallerEnemyList) - smallerEnemyList.get(i).getSize())
                            / maxDistance(smallerEnemyList)))
                    + 1;
        }

        // Food
        for (int i = 0; i < foodList.size(); i++) {
            score += ((maxDistance(foodList) - getDistanceBetweenEdge(foodList.get(i), bot)) / maxDistance(foodList))
                    + 1;
        }

        // Super Food
        for (int i = 0; i < superFoodList.size(); i++) {
            score += (5
                    * ((maxDistance(superFoodList) - getDistanceBetweenEdge(superFoodList.get(i), bot))
                            / maxDistance(superFoodList)))
                    + 1;
        }

        // Bigger Enemy
        for (int i = 0; i < biggerEnemyList.size(); i++) {
            score += 50 * ((biggerEnemyList.get(i).getSize() - bot.getSize()) / biggerEnemyList.get(i).getSize())
                    * (getDistanceBetweenEdge(biggerEnemyList.get(i), bot) / maxDistance(biggerEnemyList));
        }

        // Obstacle
        for (int i = 0; i < obstacleList.size(); i++) {
            score += 15 * (getDistanceBetweenEdge(obstacleList.get(i), bot) / maxDistance(obstacleList));
        }

        // Asteroid
        for (int i = 0; i < asteroidFieldList.size(); i++) {
            score += 10 * (getDistanceBetween(bot, asteroidFieldList.get(i))
                    / maxDistance(asteroidFieldList));
        }

        // Torpedo
        for (int i = 0; i < torpedoList.size(); i++) {
            score += 15 * (getDistanceBetween(bot, torpedoList.get(i))
                    / maxDistance(torpedoList));
        }

        return score;
    }

    public int bestScore(double score1, double score2, double score3, double score4) {
        if (score1 >= score2 && score1 >= score3 && score1 >= score4) {
            return 1;
        } else if (score2 >= score1 && score2 >= score3 && score2 >= score4) {
            return 2;
        } else if (score3 >= score1 && score3 >= score2 && score3 >= score4) {
            return 3;
        } else {
            return 4;
        }
    }

    // ==================== COMPUTE NEXT PLAYER ACTION ==================== //

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        // Bot is still in the game
        if (!gameState.getGameObjects().isEmpty()) {

            // Threat Distances
            var threatDistanceWorldBorder = 100;
            var threatDistanceTorpedo = 70;
            var threatDistanceGasCloud = 50;
            var threatDistanceEnemy = 250;
            var threatDistanceTeleporter = 300;

            // Get Enemy List
            var enemyList = getEnemyList(bot);

            // Get Gas Cloud List
            var gasCloudList = getObjectList(ObjectTypes.GAS_CLOUD, bot);

            // Get Torpedo List
            var torpedoList = getObjectList(ObjectTypes.TORPEDO_SALVO, bot);

            // Get Teleporter List
            var teleporterList = getObjectList(ObjectTypes.TELEPORTER, bot);

            // Get Food List
            var foodList = getFoodList(ObjectTypes.FOOD, bot, enemyList, gasCloudList, threatDistanceEnemy,
                    threatDistanceGasCloud);

            // Get Super Food List
            var superFoodList = getFoodList(ObjectTypes.SUPERFOOD, bot, enemyList, gasCloudList, threatDistanceEnemy,
                    threatDistanceGasCloud);

            playerAction.action = PlayerActions.FORWARD;
            displayBotDetail();
            setCurrentTeleporter();

            var botFacingKuadran = getHeadingKuadran(bot);

            // Teleport
            if (currentTeleporter != null) {

                var nearestEnemy2Teleporter = getEnemyList(currentTeleporter).get(0);

                if (getDistanceBetweenEdge(currentTeleporter, nearestEnemy2Teleporter) < bot.getSize() / 2
                        && nearestEnemy2Teleporter.getSize() < bot.getSize() - 6) {
                    playerAction.action = PlayerActions.TELEPORT;
                    setTeleporterHeading(-999);
                    setIsFiredTeleporter(false);
                    System.out.println("TELEPORT");
                }
            }

            // Stay in World Zone
            if (gameState.getWorld().getRadius() - getDistanceBetweenWorldCenter(bot)
                    - (bot.getSize() / 2) < threatDistanceWorldBorder) {

                if (botFacingKuadran == 1) {
                    if (bestScore(0, 2, 3, 4) == 2) {
                        playerAction.heading = getHeadingBetweenWorldCenter(-45);
                    } else if (bestScore(0, 2, 3, 4) == 3) {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    } else if (bestScore(0, 2, 3, 4) == 4) {
                        playerAction.heading = getHeadingBetweenWorldCenter(45);
                    } else {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    }
                } else if (botFacingKuadran == 2) {
                    if (bestScore(1, 0, 3, 4) == 1) {
                        playerAction.heading = getHeadingBetweenWorldCenter(45);
                    } else if (bestScore(1, 0, 3, 4) == 3) {
                        playerAction.heading = getHeadingBetweenWorldCenter(-45);
                    } else if (bestScore(1, 0, 3, 4) == 4) {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    } else {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    }
                } else if (botFacingKuadran == 3) {
                    if (bestScore(1, 2, 0, 4) == 1) {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    } else if (bestScore(1, 2, 0, 4) == 2) {
                        playerAction.heading = getHeadingBetweenWorldCenter(45);
                    } else if (bestScore(1, 2, 0, 4) == 4) {
                        playerAction.heading = getHeadingBetweenWorldCenter(-45);
                    } else {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    }
                } else if (botFacingKuadran == 4) {
                    if (bestScore(1, 2, 3, 0) == 1) {
                        playerAction.heading = getHeadingBetweenWorldCenter(-45);
                    } else if (bestScore(1, 2, 3, 0) == 2) {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    } else if (bestScore(1, 2, 3, 0) == 3) {
                        playerAction.heading = getHeadingBetweenWorldCenter(45);
                    } else {
                        playerAction.heading = getHeadingBetweenWorldCenter(0);
                    }
                }

                System.out.println("AVOIDING WORLD EDGE.");

                // Avoid Teleporter
            } else if (!teleporterList.isEmpty() && !isFiredTeleporter && currentTeleporter == null
                    && currentTeleporterHeading == -999
                    && getDistanceBetweenEdge(teleporterList.get(0), bot) < threatDistanceTeleporter) {

                var score1 = scoringKuadran1(gameState);
                var score2 = scoringKuadran2(gameState);
                var score3 = scoringKuadran3(gameState);
                var score4 = scoringKuadran4(gameState);

                if (getHeadingKuadran(teleporterList.get(0)) == 1) {
                    if (bestScore(0, score2, 0, score4) == score2) {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), -90);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), 90);
                    }
                } else if (getHeadingKuadran(teleporterList.get(0)) == 2) {
                    if (bestScore(score1, 0, score3, 0) == score1) {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), -90);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), 90);
                    }
                } else if (getHeadingKuadran(teleporterList.get(0)) == 3) {
                    if (bestScore(0, score2, 0, score4) == score2) {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), -90);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), 90);
                    }
                } else if (getHeadingKuadran(teleporterList.get(0)) == 4) {
                    if (bestScore(score1, 0, score3, 0) == score1) {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), -90);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(teleporterList.get(0), 90);
                    }
                }

                System.out.println("AVOIDING TELEPORTER");

                // Shoot Teleporter
            } else if (!isFiredTeleporter && currentTeleporterHeading == -999 && currentTeleporter == null
                    && bot.teleporterCount > 0
                    && getDistanceBetweenEdge(bot, enemyList.get(0)) < 500
                    && getDistanceBetweenEdge(bot, enemyList.get(0)) > 75
                    && enemyList.get(0).getSize() < bot.getSize() - 27
                    && bot.getSize() > 40) {

                playerAction.action = PlayerActions.FIRETELEPORT;
                playerAction.heading = getHeadingBetween(enemyList.get(0));
                setTeleporterHeading(getHeadingBetween(enemyList.get(0)));
                setIsFiredTeleporter(true);
                System.out.println("SHOOT TELEPORTER");

                // Activate Shield
            } else if (bot.shieldCount > 0 && !torpedoList.isEmpty()
                    && getDistanceBetweenEdge(bot, torpedoList.get(0)) <= threatDistanceTorpedo
                    && bot.getSize() >= 30) {
                playerAction.action = PlayerActions.ACTIVATESHIELD;
                System.out.println("ACTIVATE SHIELD.");

                // Chasing a Way Smaller Enemy
            } else if (!enemyList.isEmpty() && enemyList.get(0).getSize() < 0.5 * bot.getSize()
                    && getDistanceBetweenEdge(enemyList.get(0), bot) <= 2 * bot.getSize()) {
                playerAction.heading = getHeadingBetween(enemyList.get(0));
                System.out.println("CHASING A WAY SMALLER ENEMY.");

                // Shoot Torpedo
            } else if (!enemyList.isEmpty() && bot.torpedoSalvoCount > 0 && bot.getSize() >= 60
                    && ((enemyList.get(0).getSize() >= bot.getSize()
                            && getDistanceBetweenEdge(bot, enemyList.get(0)) <= threatDistanceEnemy)
                            || (enemyList.get(0).getSize() < bot.getSize()
                                    && getDistanceBetweenEdge(bot, enemyList.get(0)) < 70))
                    && bot.torpedoSalvoCount != 0) {
                playerAction.heading = getHeadingBetween(enemyList.get(0));
                playerAction.action = PlayerActions.FIRETORPEDOES;
                System.out.println("SHOOTING TORPEDOS AT INCOMING ENEMY.");

                // Avoid Gas Cloud
            } else if (!gasCloudList.isEmpty()
                    && getDistanceBetweenEdge(gasCloudList.get(0), bot) < threatDistanceGasCloud) {

                if (botFacingKuadran == 1) {
                    if (bestScore(0, 2, 3, 4) == 2) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                100);
                    } else if (bestScore(0, 2, 3, 4) == 3) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    } else if (bestScore(0, 2, 3, 4) == 4) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                260);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    }
                } else if (botFacingKuadran == 2) {
                    if (bestScore(1, 0, 3, 4) == 1) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                260);
                    } else if (bestScore(1, 0, 3, 4) == 3) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                100);
                    } else if (bestScore(1, 0, 3, 4) == 4) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    }
                } else if (botFacingKuadran == 3) {
                    if (bestScore(1, 2, 0, 4) == 1) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    } else if (bestScore(1, 2, 0, 4) == 2) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                260);
                    } else if (bestScore(1, 2, 0, 4) == 4) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                100);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    }
                } else if (botFacingKuadran == 4) {
                    if (bestScore(1, 2, 3, 0) == 1) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                100);
                    } else if (bestScore(1, 2, 3, 0) == 2) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    } else if (bestScore(1, 2, 3, 0) == 3) {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                260);
                    } else {
                        playerAction.heading = getSpecifiedHeadingBetween(gasCloudList.get(0),
                                180);
                    }
                }

                System.out.println("AVOIDING GAS CLOUD.");

                // Chasing A Smaller Enemy
            } else if (enemyList.get(0).getSize() + 50 < bot.getSize()
                    && getDistanceBetweenEdge(enemyList.get(0), bot) <= 2 * bot.getSize()) {
                playerAction.heading = getHeadingBetween(enemyList.get(0));
                System.out.println("CHASING AN ENEMY.");

                // Running Away from An Enemy
            } else if (!enemyList.isEmpty() && enemyList.get(0).getSize() >= bot.getSize()
                    && getDistanceBetweenEdge(enemyList.get(0), bot) <= threatDistanceEnemy) {

                playerAction.heading = getSpecifiedHeadingBetween(enemyList.get(0), 180);
                setIsFiredTeleporter(false);
                System.out.println("RUNNING AWAY FROM AN ENEMY.");

                // Going For Food
            } else if (!foodList.isEmpty() && !superFoodList.isEmpty()) {

                // Prioritizing Super Food
                if (getDistanceBetweenEdge(superFoodList.get(0), bot) < getDistanceBetweenEdge(foodList.get(0), bot)
                        + 75) {
                    playerAction.heading = getHeadingBetween(superFoodList.get(0));
                    System.out.println("GOING FOR SUPERFOOD.");
                } else {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                    System.out.println("GOING FOR FOOD.");
                }
            } else {
                playerAction.heading = getHeadingBetweenWorldCenter(0);
                System.out.println("GOING TO CENTER.");
            }
        }

        this.playerAction = playerAction;
    }
}
