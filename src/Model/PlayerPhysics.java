package Model;

import java.util.ArrayList;

import View.GameRender;

/**
 * Models all movement, collision and general physics for the player class
 * seperated from player class as it was getting too large
 * seperation of concerns honestly was the largest factor in creating this class
 * @author Michael
 *
 */
public class PlayerPhysics {
	/*
	 *	MOVEMENT VARIABLES
	 */
	private float moveSpeedX=3f;
	private float moveSpeedY=3f;
	//max speed variables
	private float maxXSpeed= 3f; 
	private float maxYSpeed =3f;

	

	///windows scrolling variables
	private float bgX;
	private float bgY;
	static int bgWidth = GameRender.width;
	static int bgHeight = GameRender.height;
	static int windowWidth = MainLoop.getWindowWidth();
	static int windowHeight = MainLoop.getWindowHeight();
	
	static float deadzoneXOffset = 100;
	static float deadzoneYOffset = 50;
	
	static float deadzoneMaxX = windowWidth - deadzoneXOffset-40;
	static float deadzoneMinX = deadzoneXOffset;
	static float deadzoneMaxY = windowHeight - deadzoneYOffset - 150;
	static float deadzoneMinY = deadzoneYOffset;
	
	boolean maxXHit = false;
	boolean minXHit = false;
	boolean maxYHit = false;
	boolean minYHit = false;
	
	private Player player;
	
	/**
	 * Creates a physics engine "controller" that handles all calculations
	 * @param player the player the physics engine should handle
	 */
	public PlayerPhysics(Player player){
		this.player= player;
		bgX = player.getX();
		bgY = player.getY();
		updateWindowVars();
		
	}
	
	/**
	 * Does all updates necessary needed 
	 * -Map Scrolling
	 * -Movement
	 * -Collision
	 * @param objs The list of collidable objects to check for collision with the player
	 */
	public void update(ArrayList<Collidable> objs){
		checkDeadzoneX();
		checkDeadzoneY();
		//LARGE UPDATE OF DELTA MOVEMENT 
		updateDeltaMovement();
		
		boolean movedX=false;
		boolean movedY=false;
		
		boolean movedBgX=false;
		boolean movedBgY= false;
		//movement updates
		//if we hit any X-edges of background
		if(minXHit ||maxXHit ){	
			//move the character, not background
			player.addX(player.getDx());
			movedX=true;
			//if we are not in contact with an X-edge
		}else{
			//move background
			bgX += player.getDx();
			movedBgX=true;	
		}
		//if we hit any Y-EDGES
		if(minYHit ||maxYHit){
			//move player
			player.addY(player.getDy());
			movedY=true;
			//if we are not in contact with an edge
		}else{
			//move background
			bgY += player.getDy();
			movedBgY=true;
		
		}

		
		
		//collision updates
		//idk if this is good practise but i just reverse the changes if it collides
		//then i test each direction (x,y) collisions then give the player back its dx or dy if its not colliding
		//this just translate to the player  being allowed to hitting a wall from the right but still being able to move up and down
		for (Collidable obj: objs){
			obj.setXOffset(-bgX);
			obj.setYOffset(-bgY);
			if (player.checkCollision(obj) && !obj.isTrigger()){
				if(movedX) player.addX(-player.getDx());
				if(movedY) player.addY(-player.getDy());
				if(movedBgX) bgX-=player.getDx();
				if(movedBgY) bgY-=player.getDy();
				
				/*checks if the player is hitting the object from the top or bottom
				 * This means the player can still move in left or right direction
				 */
				obj.setXOffset(-bgX);
				obj.setYOffset(-bgY);
				if(!player.checkTBCollision(obj)){
					if(movedX) player.addX(player.getDx());
					if(movedBgX) bgX+=player.getDx();

				}
				/*checks if the player is hitting the object from the right or left
				 * This means the player can still move in up or down direction
				 */
				obj.setXOffset(-bgX);
				obj.setYOffset(-bgY);
				if(!player.checkLRCollision(obj)){
					if(movedY) player.addY(player.getDy());
					if(movedBgY) bgY+=player.getDy();
	

				}
				
			
			}
			GameRender.setBackgroundOffset(-Math.round(bgX), -Math.round(bgY));
		}

	}
	/**
	 * movement update based on user input
	 * Now runs on main update loop as opposed to own button listener thread or some shit
	 */
	private void updateDeltaMovement(){
		boolean movingUp =player.getMovementDir()[0];
		boolean movingDown = player.getMovementDir()[1];
		boolean movingLeft = player.getMovementDir()[2];
		boolean movingRight =player.getMovementDir()[3];
	
		//LEFT press
		if(movingLeft){
			if(player.getDx() > -maxXSpeed && !player.isFrozen()){
				if(player.getDx()>0){
					player.setDx(0);
				}
				//change direction
				if(player.facingRight()){
					flip();
				}
				player.addDx(-moveSpeedX);
			}
		}
		
		//RIGHT press
		if(movingRight){
			if(player.getDx()< maxXSpeed && !player.isFrozen()){
				if(player.getDx()<0){
					player.setDx(0);
				}
				//change direction
				if(!player.facingRight()){
					flip();
				}
				player.addDx(moveSpeedX);
			}
		}
		//stop dx if no buttons are pressed and movement is allowed
		if(!movingRight && !movingLeft && !player.isFrozen()){
			player.setDx(0);
		}
		//UP press
		if(movingUp){
			if(player.getDy()> -maxYSpeed && !player.isFrozen()){
				player.addDy(-moveSpeedY);
			}	
		}
		
		//DOWN press
		if(movingDown){
			if(player.getDy()< maxYSpeed && !player.isFrozen()){
				player.addDy(moveSpeedY);
			}	
		}
		
		//stop dy if no buttons are pressed and movement is allowed
		if(!movingDown && !movingUp && !player.isFrozen()){
			player.setDy(0);
		}
		
	}
	
	//flips image
	private void flip(){
		if(!player.isAttacking()){
			player.setFacingRight(!player.facingRight());
		}
	}
	//Checks dead zone for X direction
	private void checkDeadzoneX(){
		//if window hits the right side of background
		if(bgX >= bgWidth - windowWidth){
			maxXHit = true;
			//if window hits left side of background
			}else if(bgX <=0){
			minXHit = true;
		}
		//if we hit the left side, but are now walking right and get back to the middle
		if(minXHit & player.getX() >= windowWidth/2 & player.facingRight()){
			minXHit = false;
		//if we hit the right side but are now walking left back to the middle
		}else if(maxXHit & player.getX() <= windowWidth/2 & !player.facingRight()){
			maxXHit = false;
		}
		
	}
	
	//checks dead zone for y direction
	private void checkDeadzoneY(){
		//if the window hits the bottom of the background
		if(bgY >= bgHeight - windowHeight){
			maxYHit = true;
			//if the window hits the top of the background
			}else if(bgY <= 0){
			minYHit = true;
			}
		
		//if we hit the top of the background but are now walking down to the middle
		if(minYHit & player.getY()>= windowWidth/2 & player.getDy() >0){
			minYHit = false;
			//if we hit the bottom of the background but are now walking up towards the middle
		}else if(maxYHit & player.getY() <= windowWidth/2 & player.getDy() < 0){
			maxYHit = false;
		}
			
	}
	/**
	 * Updates window variables for scrolling
	 * TODO Ben comment this because I dont know what this does either
	 */
	public static void updateWindowVars(){
		bgWidth = GameRender.width;
		bgHeight = GameRender.height;
		windowWidth = MainLoop.getWindowWidth();
		windowHeight = MainLoop.getWindowHeight();
		
		deadzoneMaxX = windowWidth - deadzoneXOffset - 40;
		deadzoneMinX = deadzoneXOffset;
		deadzoneMaxY = windowHeight - deadzoneYOffset - 150;
		deadzoneMinY = deadzoneYOffset;
		
	}


}