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
  public Ghost(String textureFile, String modelFile, LinkedList<Vector3> ghostMap, double x, double y)
  {
    super(textureFile, modelFile);
    setLocation(x, y);
  }
  
  @Override
  public UpdateResult update(Engine engine, double deltaSeconds)
  {
    return null;
  }
  
  @Override
  public void collided(Engine engine, Actor actor)
  {
    
  }
  
  
}