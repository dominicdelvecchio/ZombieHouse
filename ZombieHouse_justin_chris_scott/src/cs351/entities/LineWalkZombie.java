package cs351.entities;
/**
 * This class handles the speed and direction of a
 * "line walk" zombie
 *
 * @author Scott Cooper
 */

import java.util.LinkedList;
import java.util.Random;
import cs351.core.Engine;
import cs351.core.GlobalConstants;
import javafx.geometry.Point2D;
import cs351.project1.ZombieHouseEngine;


public class LineWalkZombie extends Zombie {
  //initialize to something we set
  private double elapsedSeconds=0;
  private Random rand = new Random();
  private double xDirection = 0.5;
  private double yDirection = 0.5;
  private double startingHealth = -1.0;
  private double currentHealth;
  private boolean metPlayer = false;
  private boolean zombieMemory = false;
  private boolean died = false;
  private LinkedList zombieMapX;
  private LinkedList zombieMapY;

  public LineWalkZombie(String textureFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, x, y, width, height, depth);
  }

  public LineWalkZombie(String textureFile, String modelFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, modelFile, x, y, width, height, depth);
  }

  /**
   * Parameters are given from the Engine so that the appropriate
   * updates can be made
   * @param engine
   * @param deltaSeconds
   */
  public UpdateResult update(Engine engine, double deltaSeconds)
  { 

    double zombieSpeed = Double.parseDouble(engine.getSettings().getValue("zombie_speed"));

    // totalSpeed represents the movement speed offset in tiles per second
    elapsedSeconds += deltaSeconds;

    // every zombieDecisionRate seconds, switch direction
    if (elapsedSeconds > GlobalConstants.zombieDecisionRate)
    {


      elapsedSeconds = 0.0;
      if (!canSmellPlayer(engine)  && setNewDirection)
      {

        setNewDirection = false;
        // left or right random
        xDirection = 0.5-rand.nextInt(1000)/1000.0;
        yDirection = 0.5-rand.nextInt(1000)/1000.0;
   
      }
      else if (canSmellPlayer(engine))
      {
        metPlayer = true;
        setNewDirection = false;
        Point2D pt = super.PathfindToThePlayer(engine);
        xDirection = pt.getX();
        yDirection = pt.getY();
        if (yDirection == 0.0 && xDirection != 0.0) xDirection = xDirection < 0.0 ? -1.0 : 1.0;
        else if (xDirection != 0.0) xDirection = xDirection < 0.0 ? -0.5 : 0.5;
        if (xDirection == 0.0 && yDirection != 0.0) yDirection = yDirection < 0.0 ? -1.0 : 1.0;
        else if (yDirection != 0.0) yDirection = yDirection < 0.0 ? -0.5 : 0.5;
        // alert the master zombie
        ((MasterZombie)engine.getWorld().getMasterZombie()).detectPlayer();
      }
    }

    if(canSmellPlayer(engine))
    {
      lookAt(engine.getWorld().getPlayer().getLocation().getX(), engine.getWorld().getPlayer().getLocation().getY());
      rotation.setAngle(rotation.getAngle()+180);
    }

    if(startingHealth < 0.0)
    {
      startingHealth = 20;
      currentHealth = startingHealth;
    }

    if(((Player) engine.getWorld().getPlayer()).attacking() && isAttackable(engine))
    {
      currentHealth -= 10.0 * deltaSeconds;
    }

    if(currentHealth <= 0)
    {
      ((ZombieHouseEngine) engine).killZombie(this);
    }

    double totalSpeed = zombieSpeed * deltaSeconds;
    setLocation(getLocation().getX() + xDirection * totalSpeed,
                getLocation().getY() + yDirection * totalSpeed);

    checkPlaySound(engine, deltaSeconds);
    return UpdateResult.UPDATE_COMPLETED;

  }
}

