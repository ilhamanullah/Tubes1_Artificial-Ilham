package Services;
import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;


public class Eatfood {
    public BotService bots;

    public PlayerAction eatfood(PlayerAction playerAction, GameState gameState, GameObject bot) {
        var foodlist = gameState.getGameObjects() 
            .stream().filter(items -> items.getGameObjectType() == ObjectTypes.FOOD) 
            .sorted(Comparator 
                    .comparing(items -> bots.getDistanceBetween(bot, items))) 
            .collect(Collectors.toList());  
                
        var superfoodList = gameState.getGameObjects() 
            .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD) 
            .sorted(Comparator 
                    .comparing(item -> bots.getDistanceBetween(bot, item))) 
            .collect(Collectors.toList()); 
        
        var obstacle = gameState.getGameObjects() 
                .stream().filter(obj -> obj.getGameObjectType() == ObjectTypes.GAS_CLOUD || obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD) // this filters the list to only food
                .sorted(Comparator 
                        .comparing(obj -> bots.getDistanceBetween(bot, obj))) 
                .collect(Collectors.toList());

        playerAction.action = PlayerActions.FORWARD;
        if (bots.getDistanceBetween(superfoodList.get(0), bot) < 50 && bots.getDistanceBetween(bot, obstacle.get(0)) > 30) {
            playerAction.heading = bots.getHeadingBetween(superfoodList.get(0));
            System.out.println("sf");
            

        } else if (bots.getDistanceBetween(superfoodList.get(0), bot) > 50 && bots.getDistanceBetween(bot, obstacle.get(0)) > 30) {
            playerAction.heading = bots.getHeadingBetween(foodlist.get(0));
            System.out.println("f");
        }
        return playerAction;
    }
    
}
