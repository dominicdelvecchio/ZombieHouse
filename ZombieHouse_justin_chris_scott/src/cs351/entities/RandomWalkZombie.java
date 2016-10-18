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
import java.util.Random;

public class RandomWalkZombie extends Zombie
{
  // initialize to something we set
  private double elapsedSeconds = 0;
  private Random rand = new Random();
  private double xDirection = 0;
  private double yDirection = 0;
  private Vector3 directionXY = new Vector3(0.0);


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
      if(!zombieMemory)
      {

      elapsedSeconds += deltaSeconds;
      double zombieSpeed = Double.parseDouble(engine.getSettings().getValue("zombie_speed"));
      // every zombieDecisionRate seconds, switch direction
      if (elapsedSeconds > GlobalConstants.zombieDecisionRate)
      {
        elapsedSeconds = 0.0;
        if (!canSmellPlayer())
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
          if (shouldChooseNegativeX)
          {
            xDirection = -xDirection;
          } else if (rand.nextInt(100) >= 50)
          {
            xDirection = -xDirection;
          }
          if (shouldChooseNegativeY)
          {
            yDirection = -yDirection;
          } else if (rand.nextInt(100) >= 50)
          {
            yDirection = -yDirection;
          }
          directionXY.set(xDirection, yDirection, 0.0);
        } else
        {
          Point2D pt = super.PathfindToThePlayer(engine);
          xDirection = pt.getX();
          yDirection = pt.getY();
          if (yDirection == 0.0 && xDirection != 0.0)
          {
            xDirection = xDirection < 0.0 ? -1.0 : 1.0;
          } else if (xDirection != 0.0)
          {
            xDirection = xDirection < 0.0 ? -0.5 : 0.5;
          }
          if (xDirection == 0.0 && yDirection != 0.0)
          {
            yDirection = yDirection < 0.0 ? -1.0 : 1.0;
          } else if (yDirection != 0.0)
          {
            yDirection = yDirection < 0.0 ? -0.5 : 0.5;
          }
          directionXY.set(xDirection, yDirection, 0.0);
          ((MasterZombie) engine.getWorld().getMasterZombie()).detectPlayer();
        }
      }

      playerDistance(engine);

      if (canSmellPlayer())
      {
        lookAt(engine.getWorld().getPlayer().getLocation().getX(), engine.getWorld().getPlayer().getLocation().getY());
      }

      double totalSpeed = zombieSpeed * deltaSeconds;
      setLocation(getLocation().getX() + directionXY.getX() * totalSpeed,
              getLocation().getY() + directionXY.getY() * totalSpeed);

      recordZombie();
    }
    else if(move < movement) {moveZombiePast();}
      else if(zombieHasDied) {((ZombieHouseEngine) engine).killZombie(this);}
      else{zombieMemory=false;}

      if (startingHealth < 0.0)
      {
        startingHealth = zombieHealth;
        currentHealth = startingHealth;
      }

      if (((Player) engine.getWorld().getPlayer()).attacking() && isAttackable())
      {
        currentHealth -= 150.0 * deltaSeconds;
        System.out.println("ATTACK SUCCESSFUL");
        engine.getSoundEngine().queueSoundAtLocation("sound/attack.wav", getLocation().getX()-directionXY.getX(),
                getLocation().getY()-directionXY.getY(),4.0,1);
        setLocation((getLocation().getX()-directionXY.getX()),getLocation().getY()-directionXY.getY());
        playerMet = true;
        if(zombieMemory)
        {
          shouldBifurcate = true;
       }
        //zombieMemory = true;
      }
      if(shouldBifurcate && !hasBifurcated)
      {
        ((ZombieHouseEngine) engine).bifurcate(this);
        //System.out.println("Bifurcate");
        shouldBifurcate = false;
        hasBifurcated = true;
      }

      if (currentHealth <= 0)
      {
        ((ZombieHouseEngine) engine).killZombie(this);
        zombieHasDied = true;
      }

      checkPlaySound(engine, deltaSeconds);
    }
    return UpdateResult.UPDATE_COMPLETED;
  }
  }
  

