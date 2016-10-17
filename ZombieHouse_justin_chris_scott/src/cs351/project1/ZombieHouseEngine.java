package cs351.project1;

import cs351.core.*;
import cs351.entities.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.util.*;

/**
 * This is the Engine that ties all of the game's 
 * components together
 * 
 * @author Justin
 *
 */
public class ZombieHouseEngine implements Engine
{
  private World world;
  private SoundEngine soundEngine;
  private Renderer renderer;
  private CollisionDetection collision;
  private KeyboardInput keyInput;
  private Settings settings;
  private int worldWidth, worldHeight;         // measured in tiles
  private final HashSet<Actor> ALL_ACTORS;
  private final HashSet<Actor> UPDATE_ACTORS;  // only the actors that want to be updated each frame
  private final HashSet<Actor> KILLED_ACTORS;
  private boolean isInitialized = false;
  private boolean isPendingShutdown = false;
  private LinkedList<Double> ghostMap1X = new LinkedList<Double>();
  private LinkedList<Double> ghostMap1Y = new LinkedList<Double>();
  private LinkedList<Double> ghostMap2X = new LinkedList<Double>();
  private LinkedList<Double> ghostMap2Y = new LinkedList<Double>();
  private LinkedList<Double> ghostMap3X = new LinkedList<Double>();
  private LinkedList<Double> ghostMap3Y = new LinkedList<Double>();
  int ghostCount = 0;
  int ghostMovement1 = 0;
  int ghostMovement2 = 0;
  int ghostMovement3 = 0;
  private int frameCount = 0;
  private String deathSound = "sound/death.wav";

  
  // pendingLevelRestart and pendingNextLevel let the engine know if it needs to do something
  // after the current frame is finished
  private boolean pendingLevelRestart;
  private boolean pendingNextLevel;
  private long millisecondsSinceLastFrame;
  private long millisecondTimeStamp;
  private boolean[][] pathingData;

  /**
   * The following variables are initialized by accessing the settings class.
   */
  private final String ENGINE_PAUSED = "engine_paused";
  private final String PLAYER_NO_CLIP = "player_no_clip";
  private final String COLLISION = "collision";
  private final String SOUND_ENGINE = "sound_engine";
  private final String ADVANCED_LIGHTING = "advanced_lighting";
  private final String SPECULAR_LIGHTING = "specular_lighting";
  private final String LIGHT_INTENSITY = "light_intensity";
  private final String PLAYER_VISION = "player_vision";
  private final String PLAYER_HEARING = "player_hearing";

  private boolean isPaused;
  private boolean useCollisionDetection;
  private boolean playerNoClip;
  private boolean updateSoundEngine;

  // initialize the settings class with default settings
  {
    settings = new Settings();
    settings.registerSetting(ENGINE_PAUSED, "false");
    settings.registerSetting(PLAYER_NO_CLIP, "off");
    settings.registerSetting(COLLISION, "on");
    settings.registerSetting(ADVANCED_LIGHTING, "on");
    settings.registerSetting(SPECULAR_LIGHTING, "on");
    settings.registerSetting(LIGHT_INTENSITY, "0.7");
    settings.registerSetting(PLAYER_VISION, "7"); // measured in tiles
    settings.registerSetting(SOUND_ENGINE, "on");
    settings.registerSetting(PLAYER_HEARING, "20.0");

    setEngineVariablesFromSettings();
  }

  public ZombieHouseEngine()
  {
    ALL_ACTORS = new HashSet<>(500);
    UPDATE_ACTORS = new HashSet<>(500);
    KILLED_ACTORS = new HashSet<>(500);
    keyInput = new KeyboardInput();
  }

  /**
   * Gets the current World object and returns it
   */
  @Override
  public World getWorld()
  {
    validateEngineState();
    return world;
  }
 
  @Override
  public KeyboardInput getKeyInputSystem()
  {
    return keyInput;
  }

  @Override
  public SoundEngine getSoundEngine()
  {
    validateEngineState();
    return soundEngine;
  }

  @Override
  public Renderer getRenderer()
  {
    validateEngineState();
    return renderer;
  }

  @Override
  public Settings getSettings()
  {
    validateEngineState();
    return settings;
  }

  @Override
  public int getWorldWidth()
  {
    validateEngineState();
    return worldWidth;
  }

  @Override
  public int getWorldHeight()
  {
    validateEngineState();
    return worldHeight;
  }

  @Override
  public boolean isEnginePendingShutdown()
  {
    return isPendingShutdown;
  }

  @Override
  public Collection<Actor> getNeighboringActors(Actor actor, int tileDistance)
  {
    return null;
  }

  @Override
  public boolean[][] getPathingData()
  {
    return pathingData;
  }
  
  /*private void ghostBehvior(int ghostCount)
  {
    if(ghostCount == 0)
    {
      ghostMap1.add(getWorld().getPlayer().getLocation());
      System.out.println("Player XLocation = " + getWorld().getPlayer().getLocation().getX());
      System.out.println("Player YLocation = " + getWorld().getPlayer().getLocation().getY());
      ghostMovement++;
    }
  }*/
  void monitorGhost(int ghostCount)
  {
    if(ghostCount == 0)
    {
      ghostMap1X.add(getWorld().getPlayer().getLocation().getX());
      ghostMap1Y.add(getWorld().getPlayer().getLocation().getY());
      ghostMovement1++;
    }
    if(ghostCount == 1)
    {
      ghostMap2X.add(getWorld().getPlayer().getLocation().getX());
      ghostMap2Y.add(getWorld().getPlayer().getLocation().getY());
      ghostMovement2++;
    }
    if(ghostCount == 2)
    {
      ghostMap3X.add(getWorld().getPlayer().getLocation().getX());
      ghostMap3Y.add(getWorld().getPlayer().getLocation().getY());
      ghostMovement3++;
    }
  }
  private void initializeGhost (int ghostCount)
  {
    if(ghostCount >=0)
    {
      Ghost ghost1 = new Ghost("textures/ice_texture.jpg", "resources/Zombie2_Animated.txt",ghostMap1X,ghostMap1Y,
              getWorld().getCurrentLevel().getRandomLevelGenerator().getXSpawnPoint(), getWorld().getCurrentLevel().getRandomLevelGenerator().getYSpawnPoint(),1,1,1, ghostMovement1);
      getWorld().setGhost(ghostCount, ghost1);
      ALL_ACTORS.add(ghost1);
      UPDATE_ACTORS.add(ghost1);
      getRenderer().registerActor(ghost1,
              ghost1.getRenderEntity(),
              Color.BEIGE, // diffuse
              Color.BEIGE, // specular
              Color.WHITE); // ambient
    }
    if(ghostCount >= 1)
    {
      getWorld().getGhost(1).setMove(0);
      Ghost ghost2 = new Ghost("textures/ice_texture.jpg", "resources/Zombie2_Animated.txt",ghostMap2X,ghostMap2Y,
              getWorld().getCurrentLevel().getRandomLevelGenerator().getXSpawnPoint(), getWorld().getCurrentLevel().getRandomLevelGenerator().getYSpawnPoint(),1,1,1, ghostMovement2);
      getWorld().setGhost(ghostCount, ghost2);
      ALL_ACTORS.add(ghost2);
      UPDATE_ACTORS.add(ghost2);
      getRenderer().registerActor(ghost2,
              ghost2.getRenderEntity(),
              Color.BEIGE, // diffuse
              Color.BEIGE, // specular
              Color.WHITE); // ambient
    }
    if(ghostCount >= 2)
    {
      getWorld().getGhost(1).setMove(0);
      getWorld().getGhost(2).setMove(0);
      getWorld().getGhost(1).setMove(0);
      Ghost ghost3 = new Ghost("textures/ice_texture.jpg", "resources/Zombie2_Animated.txt",ghostMap3X,ghostMap3Y,
              getWorld().getCurrentLevel().getRandomLevelGenerator().getXSpawnPoint(), getWorld().getCurrentLevel().getRandomLevelGenerator().getYSpawnPoint(),1,1,1, ghostMovement3);
      getWorld().setGhost(ghostCount, ghost3);
      ALL_ACTORS.add(ghost3);
      UPDATE_ACTORS.add(ghost3);
      getRenderer().registerActor(ghost3,
              ghost3.getRenderEntity(),
              Color.BEIGE, // diffuse
              Color.BEIGE, // specular
              Color.WHITE); // ambient
    }
    if(ghostCount > 2)
    {
      getWorld().getGhost(1).setMove(0);
      getWorld().getGhost(2).setMove(0);
      getWorld().getGhost(3).setMove(0);
    }
  }

  @Override
  public void init(Stage stage, World world, SoundEngine soundEngine, Renderer renderer)
  {
    if (isInitialized) throw new RuntimeException("Engine was not shutdown before a new call to init");
    System.out.println("-> Initializing engine");
    this.world = world;
    this.soundEngine = soundEngine;
    this.renderer = renderer;
    isInitialized = true;
    isPendingShutdown = false;
    pendingLevelRestart = false;
    pendingNextLevel = true;
    millisecondTimeStamp = System.currentTimeMillis();
    millisecondsSinceLastFrame = 0;
    togglePause(false);
    stage.setOnCloseRequest(this::windowClosed);
    collision = new CollisionDetection(this); // init the collision detection system
    getSoundEngine().init(this);
    pathingData = new boolean[world.getWorldPixelWidth() / world.getTilePixelWidth()]
                             [world.getWorldPixelHeight() / world.getTilePixelHeight()];
    initEngineState(); // init the initial engine state from the world
    keyInput.init(stage);
    for (int x = 0; x < pathingData.length; x++)
    {
      // start off assuming each location can be visited
      Arrays.fill(pathingData[x], false);
    }
  }

  @Override
  public void init(String settingsFile, Stage stage, World world, SoundEngine soundEngine, Renderer renderer)
  {
    settings.importSettings(settingsFile);
    init(stage, world, soundEngine, renderer);
    setEngineVariablesFromSettings();
    getSoundEngine().init(this); // call this again so it can check the *new* values of the engine.settings
  }

  @Override
  public void shutdown()
  {
    if (!isInitialized) throw new RuntimeException("Engine was not initialized before the call to shutdown");
    System.out.println("-> Shutting down engine");
    invalidateEngineData();
    isPendingShutdown = true;
    isInitialized = false;
  }

  @Override
  public void togglePause(boolean value)
  {
    isPaused = value;
  }

  @Override
  public void frame()
  {
    
    //System.out.println("called");
    if (!isInitialized || isPaused || isPendingShutdown) return;
    // start the collision detection system's new frame
    collision.initFrame();
    millisecondsSinceLastFrame = System.currentTimeMillis() - millisecondTimeStamp;
    millisecondTimeStamp = System.currentTimeMillis();         // mark the time when this frame started
    double deltaSeconds = millisecondsSinceLastFrame / 1000.0; // used for the actors
    // update all actors and process their return statements
    getWorld().getPlayer().setNoClip(playerNoClip);
    //ghostBehvior(ghostCount);
    //ghostMapX.add(getWorld().getPlayer().getLocation().getX());
    //ghostMapY.add(getWorld().getPlayer().getLocation().getY());
    monitorGhost(ghostCount);
    //ghostMovement++;
    for (Actor actor : UPDATE_ACTORS)
    {
      processActorReturnStatement(actor.update(this, deltaSeconds));
      // insert the actor into the collision detection system - don't add/re-add
      // any static, floor or ceiling actors
      if (!actor.isStatic() && !actor.isPartOfFloor() && !actor.isPartOfCeiling()) collision.insert(actor);
    }
    if (updateSoundEngine)
    {
      getSoundEngine().update(this);
      // set the center point for the sound engine
      getSoundEngine().setCentralPoint((int) getWorld().getPlayer().getLocation().getX(),
                                       (int) getWorld().getPlayer().getLocation().getY());
    }
    // now that the frame is nearly complete, see if anything collided during
    // the actor update phase
    if (useCollisionDetection)
    {
      HashMap<Actor, LinkedList<Actor>> collisionEvents = collision.detectCollisions();
      // push all collision events to the appropriate actors
      for (Map.Entry<Actor, LinkedList<Actor>> entry : collisionEvents.entrySet())
      {
        Actor entryActor = entry.getKey();
        for (Actor actor : entry.getValue())
        {
          entryActor.collided(this, actor);
          actor.collided(this, entryActor);
        }
      }
    }
    if(KILLED_ACTORS.size()>0){
      removeActors();
    }
    // render the world
    getRenderer().render(this, DrawMode.FILL, deltaSeconds);
    // if during the frame an actor(s) were added to the world, pull them now
    pullLatestActorsFromWorld();
    // with the frame complete, call initEngineState to see if anything needs to change
    initEngineState();
  }

  public void killZombie(Actor actor)
  {
    KILLED_ACTORS.add(actor);
  }

  private void windowClosed(WindowEvent event)
  {
    if (isInitialized) shutdown();
  }

  private void validateEngineState()
  {
    if (!isInitialized || isPaused) throw new IllegalStateException("Engine was not initialized/un-paused before use");
  }

  private void invalidateEngineData()
  {
    //for (Actor actor : ALL_ACTORS) actor.destroy();
    ALL_ACTORS.clear();
    UPDATE_ACTORS.clear();
    soundEngine.shutdown();
    renderer.shutdown();
    collision.destroy();
  }

  private void removeActors()
  {
    for(Actor actor : KILLED_ACTORS)
    {
      actor.setNoClip(true);
      actor.setShouldUpdate(false);
      actor.setLocation(500, 500);
    }
    KILLED_ACTORS.clear();
  }

  private void processActorReturnStatement(Actor.UpdateResult result)
  {
    if (pendingLevelRestart || pendingNextLevel) return; // if it is already set to do something at the end of the frame, do nothing
    if (result == Actor.UpdateResult.PLAYER_DEFEAT) pendingLevelRestart = true;
    else if (result == Actor.UpdateResult.PLAYER_VICTORY) pendingNextLevel = true;
  }

  private void initEngineState()
  {
    if (pendingNextLevel && getWorld().hasNextLevel())
    {
      initEngineFromWorld(true);
      pendingNextLevel = false;
    }
    else if (pendingLevelRestart)
    {
      //Sound for player's death
      getSoundEngine().queueSoundAtLocation(deathSound,
              getWorld().getPlayer().getLocation().getY(), getWorld().getPlayer().getLocation().getY());
      restoreZombies();
      
      for (Actor actor : ALL_ACTORS)
      {
        if(actor instanceof Zombie)
        {
          ((Zombie) actor).resetZombie();
        }
      }
      initEngineFromWorld(false);
      pendingLevelRestart = false;
    }
    else if (pendingNextLevel && !getWorld().hasNextLevel()) isPendingShutdown = true;
  }

  private void initEngineFromWorld(boolean shouldGetNextLevel)
  {
    final double DEFAULT_FIELD_OF_VIEW = 90.0; // measured in degrees
   
    invalidateEngineData();
    getRenderer().init(this);
    soundEngine.init(this);
    collision = new CollisionDetection(this);
    // queue up the next level/restart the current level
    if (shouldGetNextLevel) getWorld().nextLevel(this);
    else
      {
        initializeGhost(ghostCount);
        ghostCount++;

        /*getWorld().add(ghost);
        Vector3 location = new Vector3
                (getWorld().getCurrentLevel().getRandomLevelGenerator().getXSpawnPoint(), getWorld().getCurrentLevel().getRandomLevelGenerator().getYSpawnPoint(), 0);
        getWorld().getCurrentLevel().getDynamicActorLocations().put(location, new HashSet<>());
        getWorld().getCurrentLevel().getDynamicActorLocations().get(location).add(ghost);
        */
        //renderer.registerActor(ghost,);
        //if(getWorld().contains(ghost)) System.out.println("true ye true");
        getWorld().restartLevel(this);
    }

    pullLatestActorsFromWorld();
    // the +1 accounts for it truncating values after the decimal when the
    // division isn't clean
    worldWidth = world.getWorldPixelWidth() / world.getTilePixelWidth() + 1;
    worldHeight = world.getWorldPixelHeight() / world.getTilePixelHeight() + 1;
    renderer.registerPlayer(getWorld().getPlayer(), DEFAULT_FIELD_OF_VIEW);
  }

  private void pullLatestActorsFromWorld()
  {
    if (getWorld().getChangeList(false).size() == 0) return;
    Collection<Actor> changeList = getWorld().getChangeList(true);
    ALL_ACTORS.addAll(changeList); // this should contain all actors since nextLevel was called
    getWorld().getPlayer().setNoClip(playerNoClip);
    for (Actor actor : changeList)
    {
      if (actor.shouldUpdate()) UPDATE_ACTORS.add(actor);

      if (actor.isStatic() || (actor.isPartOfFloor() && actor.shouldUpdate())) collision.insert(actor);

      if (actor.isStatic() || actor.isPartOfFloor())
      {
        // get the integer coordinates for this actor
        int x = (int)(actor.getLocation().getX() / getWorld().getTilePixelWidth());
        int y = (int)(actor.getLocation().getY() / getWorld().getTilePixelHeight());
        if (x >= 0 && x < pathingData.length && y >= 0 && y < pathingData[0].length)
        {
          // if the current actor is not static (which is for walls) and it is part of
          // the floor and the pathing data at the current location is still true (otherwise
          // it was already covered by a wall), then leave the current location as valid
          
          // true if wall or static actor
          pathingData[x][y] = actor.isStatic() || !actor.isPartOfFloor();
        }
      }
    }
  }

  private void setEngineVariablesFromSettings()
  {
    isPaused = settings.getValue(ENGINE_PAUSED).equals("true");
    useCollisionDetection = settings.getValue(COLLISION).equals("on");
    playerNoClip = settings.getValue(PLAYER_NO_CLIP).equals("on");
    updateSoundEngine = settings.getValue(SOUND_ENGINE).equals("on");
  }

  private void restoreZombies(){
    for(Actor actor : ALL_ACTORS){
      if(actor instanceof Zombie){
        actor.setNoClip(false);
        actor.setShouldUpdate(true);
        ((Zombie) actor).restoreHealth();
      }
    }
  }

  public void bifurcate(Actor actor){
    Zombie zombie = null;
    if(actor instanceof LineWalkZombie){
      zombie = new LineWalkZombie("textures/rock_texture.jpg",
              "resources/Zombie1_Animated.txt",
              actor.getLocation().getX()+0.1,
              actor.getLocation().getY()+0.1,
              actor.getWidth(), actor.getHeight(), actor.getDepth());
    }
    else if(actor instanceof RandomWalkZombie){
      zombie = new RandomWalkZombie("textures/metal_texture.jpg",
              "resources/Zombie2_Animated.txt",
              actor.getLocation().getX()+0.1,
              actor.getLocation().getY()+0.1,
              actor.getWidth(), actor.getHeight(), actor.getDepth());
    }
    renderer.registerActor(zombie,
            zombie.getRenderEntity(),
            Color.BEIGE, // diffuse
            Color.BEIGE, // specular
            Color.WHITE); // ambient
    world.add(zombie);
    if(zombie instanceof LineWalkZombie){
      renderer.mapTextureToActor("textures/rock_texture.jpg", zombie);
    }
    else if(zombie instanceof RandomWalkZombie){
      renderer.mapTextureToActor("textures/metal_texture.jpg", zombie);
    }
  }
  public int getFrameCount()
  {
    return frameCount;
  }
}