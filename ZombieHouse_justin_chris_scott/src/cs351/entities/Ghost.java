package cs351.entities;

import cs351.core.Actor;
import cs351.core.Engine;
import cs351.core.Vector3;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Dominic on 9/21/2016.
 */

//Class used to create a ghost of the players previous actions. Takes linked list of the
//Players previous location vectors to guid movement.
public class Ghost extends Actor
{
  private LinkedList ghostMapX;
  private LinkedList ghostMapY;
  private int ghostMovement;
  private int move = 0;
  
  public Ghost(String textureFile)
  {
    super(textureFile);
  }
  public Ghost(String textureFile, String modelFile, LinkedList<Double> ghostMapX, LinkedList<Double> ghostMapY, double x, double y, int width, int height, int depth, int ghostMovement)
  {
    super(textureFile, modelFile);
    this.ghostMapX = ghostMapX;
    this.ghostMapY = ghostMapY;
    this.ghostMovement = ghostMovement;
    setLocation(x, y);
    setWidthHeightDepth(width, height, depth);
  }
  
  @Override
  public UpdateResult update(Engine engine, double deltaSeconds)
  {
    double x;
    double y;
    
    if(move < ghostMovement)
    {
      x = (double) ghostMapX.get(move);
      y = (double) ghostMapY.get(move);
      //System.out.println("x = " +x+ " and y = " + y);
      //System.out.println("move = " +move);
      setLocation(x,y);
      move++;
    }
    else
    {
      System.out.println("done");
    }
    return UpdateResult.UPDATE_COMPLETED;
  }
  
  @Override
  public void collided(Engine engine, Actor actor)
  {
    
  }
   public void setMove(int move){this.move = move;}
  
}