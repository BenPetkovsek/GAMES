package GameObjectModel;

import java.awt.geom.Rectangle2D;	

import MiscModel.Level;
import MiscModel.LevelManager;
import MiscModel.MainLoop;
import PlayerModel.Player;

public class LoadTrigger extends Collidable {
	
	String destination;
	static int currentCooldownTime = 0;
	static boolean canTeleport;
	
	public LoadTrigger(float x, float y, String initDestination) {
		super(x, y);
		this.destination = initDestination;
		drawBorders = true;
		setTrigger(true);
		collisionBox = new Rectangle2D.Float(x,y,50,50);
	}
	
	public void update(Player hero){
		Level desLevel = LevelManager.getLevel(destination);
		if(checkCollision(hero) && canTeleport){
			canTeleport = false;
			MainLoop.changeCurrentLevel(destination);
			System.out.println("Changed level to: " + destination);
			//if background for level is bigger than window, spawn player at an x position rather than moving bg
			desLevel.mapDebug();
			if(desLevel.getSmallMapX()){
				hero.setX(desLevel.getSpawnX());
			}else {hero.getPhysicsEngine().setBgX(desLevel.getSpawnX());}
			//if background for level is bigger than window, spawn player at a y position rather than moving bg
			if(desLevel.getSmallMapY()){
				hero.setY(desLevel.getSpawnY());
			}else {hero.getPhysicsEngine().setBgY(desLevel.getSpawnY());}
			
			
		}
	}
	
	// creates a GLOBAL cooldown for ALL TELEPORTERS which stops
	//the player from teleporting again before the new level loads
	public static void teleportCooldown(){

		int cooldownTime = 50;
		//System.out.println("TeleCooldownTest - canTeleport = "+ canTeleport + "Teleport Cooldown remaining: " + (cooldownTime - currentCooldownTime));
		if(!canTeleport){
		currentCooldownTime++;
		}
		if(!canTeleport && currentCooldownTime >= cooldownTime){ //only count cooldown if the teleporter has been used
			currentCooldownTime = 0;
			canTeleport = true;
		}
	}
	
	@Override
	public float getWidth(){
		return 50;
	}
	@Override
	public float getHeight(){
		return 50;
	}
	
	
	

}
