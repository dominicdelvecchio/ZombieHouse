package cs351.entities;

/**
 * This class handles the speed and direction of a
 * Random Walk Zombie
 *
 * @author Scott Cooper
 */

import cs351.core.Engine;
import cs351.core.GlobalConstants;
import cs351.core.Vector3;
import cs351.project1.ZombieHouseEngine;
import javafx.geometry.Point2D;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class RandomWalkZombie extends Zombie
{
  // initialize to something we set
  private double elapsedSeconds = 0;
  private Random rand = new Random();
  private double xDirection = 0;
  private double yDirection = 0;
  private Vector3 directionXY = new Vector3(0.0);
  private LinkedList<Double> zombieMapX = new LinkedList<Double>();
  private LinkedList<Double> zombieMapY = new LinkedList<Double>();
  private LinkedList<Double> zombieLife = new LinkedList<Double>();
  private double zombieTime = 0.0;
  private double currentTime = 0.0;;
  private boolean playerMet = false;
  private boolean playerHasDied = false;
  private int movement = 0;
  private int move;

  public RandomWalkZombie(String textureFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, x, y, width, height, depth);
  }

  public RandomWalkZombie(String textureFile, String modelFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, modelFile, x, y, width, height, depth);
  }

  /**
   * Parameters are given from the Engine so that the appropriate
   * updates can be made
   *
   * @param engine
   * @param deltaSeconds
   */
  public UpdateResult update(Engine engine, double deltaSeconds)
  {
    if(shouldUpdate)
    {
      // totalSpeed represents the movement speed offset in tiles per second
      elapsedSeconds += deltaSeconds;
      double zombieSpeed = Double.parseDouble(engine.getSettings().getValue("zombie_speed"));
      // every zombieDecisionRate seconds, switch direction
      if(elapsedSeconds > GlobalConstants.zombieDecisionRate)
      {
        elapsedSeconds = 0.0;
        if(!canSmellPlayer())
        {
          // If the x/y directions are positive and set new direction is true,
          // it means that with the current heading the zombie collided with something,
          // so make the new direction be negative to get away from whatever we collided with
          boolean shouldChooseNegativeX = xDirection > 0.0 && setNewDirection;
          boolean shouldChooseNegativeY = yDirection > 0.0 && setNewDirection;
          xDirection = rand.nextDouble();
          yDirection = 1.0 - xDirection;
          // If shouldChooseNegativeX/Y are true, the zombie is forced to make the heading
          // choice negative - otherwise it makes it a random choice
          if(shouldChooseNegativeX)
          {
            xDirection = -xDirection;
          }
          else if(rand.nextInt(100) >= 50)
          {
            xDirection = -xDirection;
          }
          if(shouldChooseNegativeY)
          {
            yDirection = -yDirection;
          }
          else if(rand.nextInt(100) >= 50)
          {
            yDirection = -yDirection;
          }
          directionXY.set(xDirection, yDirection, 0.0);
        }
        else
        {
          Point2D pt = super.PathfindToThePlayer(engine);
          xDirection = pt.getX();
          yDirection = pt.getY();
          if(yDirection == 0.0 && xDirection != 0.0)
          {
            xDirection = xDirection < 0.0 ? -1.0 : 1.0;
          }
          else if(xDirection != 0.0)
          {
            xDirection = xDirection < 0.0 ? -0.5 : 0.5;
          }
          if(xDirection == 0.0 && yDirection != 0.0)
          {
            yDirection = yDirection < 0.0 ? -1.0 : 1.0;
          }
          else if(yDirection != 0.0)
          {
            yDirection = yDirection < 0.0 ? -0.5 : 0.5;
          }
          directionXY.set(xDirection, yDirection, 0.0);
          ((MasterZombie) engine.getWorld().getMasterZombie()).detectPlayer();
        }
          /*
          bifurcate here using:
          ((ZombieHouseEngine) engine).bifurcate(this);
          */
      }

      playerDistance(engine);

      if(canSmellPlayer())
      {
        playerMet = true;
        lookAt(engine.getWorld().getPlayer().getLocation().getX(), engine.getWorld().getPlayer().getLocation().getY());
      }

      if(startingHealth < 0.0)
      {
        startingHealth = zombieHealth;
        currentHealth = startingHealth;
      }

      if(((Player) engine.getWorld().getPlayer()).attacking() && isAttackable())
      {
        currentHealth -= 75.0 * deltaSeconds;
      }

      if(currentHealth <= 0)
      {
        ((ZombieHouseEngine) engine).killZombie(this);
      }
      
      if(zombieTime > currentTime) {moveZombiePast();}
      
      else
      {
        double totalSpeed = zombieSpeed * deltaSeconds;
        setLocation(getLocation().getX() + directionXY.getX() * totalSpeed,
                getLocation().getY() + directionXY.getY() * totalSpeed);
      }
      
      if(playerMet && !playerHasDied) {recordZombie();}
        
      if(!playerMet){zombieTime = (zombieTime+deltaSeconds);}
      currentTime = (currentTime + deltaSeconds);
      checkPlaySound(engine, deltaSeconds);
      
      if(((Player) engine.getWorld().getPlayer()).getCurrentHealth() <= 0.0)
      {
        playerHasDied = true;
        currentTime = 0.0;
        move = 0;
        
      }
      
    }
    return UpdateResult.UPDATE_COMPLETED;
  }
  private void recordZombie()
  {
    movement++;
    zombieMapX.add(getLocation().getX());
    zombieMapY.add(getLocation().getY());
  }
  
  private void moveZombiePast()
  {
    if(move < movement)
    {
      setLocation(zombieMapX.get(move), zombieMapY.get(move));
      move++;
    }
    
  }
  
}
