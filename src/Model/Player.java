package Model;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import View.*;

public class Player extends GameObject {
	int HP, totalHP, str, def, intel;
	String name;
	
	/*
	 *	MOVEMENT VARIABLES
	 */
	private float moveSpeedX=3f;
	private float moveSpeedY=3f;
	//max speed variables
	private float maxXSpeed= 3f; 
	private float maxYSpeed =3f;
	//delta variables
	//NOTE: I have no idea what im doing
/*	private float dx;
	private float dy;*/
	
	//movement booleans
	private boolean movingLeft=false;
	private boolean movingRight=false;
	private boolean movingUp=false;
	private boolean movingDown=false;
	
	private boolean noMovement=false;	//if the player can't move
	
	//Animation Variables
	private BufferedImage[] walkRight= {ImageStyler.loadImg("heroWalk1.png"),ImageStyler.loadImg("heroWalk2.png"),ImageStyler.loadImg("heroWalk3.png")};
	private BufferedImage[] walkLeft = ImageStyler.flipImgs(walkRight);
	
	private Animation walkRightAnim;
	private Animation walkLeftAnim;
	
	private BufferedImage[] idleRight = {ImageStyler.loadImg("heroIdle.png")};
	private BufferedImage[] idleLeft = ImageStyler.flipImgs(idleRight);
	
	private Animation idleRightAnim;
	private Animation idleLeftAnim;
	
	private BufferedImage[] attackRight = {ImageStyler.loadImg("heroAttack.png")};
	private BufferedImage[] attackLeft =ImageStyler.flipImgs(attackRight);
	
	private Animation attackRAnim;
	private Animation attackLAnim;
	
	private BufferedImage[] hurtRight = {ImageStyler.loadImg("heroHurt.png")};
	private BufferedImage[] hurtLeft = ImageStyler.flipImgs(hurtRight);
	
	private Animation hurtRightAnim;
	private Animation hurtLeftAnim;
	
	private Animation oldAnim;
	
	//effects variables
	private Invulnerability grace;	//this is when the player gets hit, it allows them to be invincible for a second to prevent insta death
	
	private KnockBack knockback;
	
	//attacking variables
	private boolean attacking=false;
	private Attack currentAttack;	//TODO make a list of attacks , right now there will be one
	
	//Scrolling/deadzone variables
	float bgX =  x;
	float bgY =  y;
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
	
	
/*	//complicated constructor for future releases with stats
	public Player(int x, int y,String initName, int initHP, int initStr, int initDef, int initIntel ){
		this(x,y);
		HP = initHP;
		totalHP = initHP;
		str = initStr;
		def = initDef;
		intel = initIntel;
		name = initName;
		
	}*/
	
	//constructor for simple people like me
	public Player(float x,float y){
		super(x,y);
		HP =100;
		isCollidable = true;
		scale= 5f;
		drawBorders=true;
		animInit();
		attackInit();
		offsetInit();
		
		//sets the grace period for the player
		grace= new Invulnerability(70, 10);
		updateWindowVars();
	}
	//init for all anims
	private void animInit(){
		//initialize images for all anims
		walkRightAnim= new Animation(true,0);
		walkRightAnim.addFrame(walkRight[0]).addFrame(walkRight[1]).addFrame(walkRight[2]);
		walkLeftAnim= new Animation(true,0);
		walkLeftAnim.addFrame(walkLeft[0]).addFrame(walkLeft[1]).addFrame(walkLeft[2]);
		idleRightAnim = new Animation(true,0);
		idleRightAnim.addFrame(idleRight[0]);
		idleLeftAnim = new Animation(true,0);
		idleLeftAnim.addFrame(idleLeft[0]);
		attackRAnim = new Animation(false,1).addFrame(attackRight[0]);
		attackLAnim = new Animation(false,1).addFrame(attackLeft[0]);
		hurtRightAnim = new Animation(false,2).addFrameWithLength(hurtRight[0],8);
		hurtLeftAnim = new Animation(false,2).addFrameWithLength(hurtLeft[0],8);
		
		
		//init first anim
		if (facingRight){
			currentAnim = idleRightAnim;
		}
		else{
			currentAnim = idleLeftAnim;
		}
		oldAnim = currentAnim;
		
	}
	
	private void attackInit(){
		//current punch attack
		//xOffset is the difference in the sprite compared to idle animation
		float offset = scale*(attackRight[0].getWidth() - idleRight[0].getWidth());
		currentAttack = new Attack(this,getWidth(),0f,30,offset,getHeight(), (int) attackLAnim.getDuration(),offset);
	}
	private void offsetInit(){
		//offSetDir is whether the offset was applied to the sprite when it was facing right or not
		//this is important because the offset should reflect when the player also reflects
		offsetDir = facingRight;
		/*setOffsets(getWidth() *0.3f,-getWidth() *0.1f,getHeight() *0.3f,0);*/
		
		collisionBox = new Rectangle2D.Float(x, y, getWidth(), getHeight());
	}
	//main update for the object, is called every loop

	public void update(ArrayList<GameObject> objs){

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
			x +=dx;
			movedX=true;
			//System.out.println("Moving char - x");
			//if we are not in contact with an X-edge
		}else{
			//move background
			bgX += dx;
			movedBgX=true;	
			//System.out.println("Moving BG - x");
		}
		//if we hit any Y-EDGES
		if(minYHit ||maxYHit){
			//move player
			y +=dy;
			movedY=true;
			//System.out.println("Moving char - y");
			//if we are not in contact with an edge
		}else{
			//move background
			bgY += dy;
			movedBgY=true;
			//System.out.println("Moving BG - y");
		
		}

		
		
		//collision updates
		//idk if this is good practise but i just reverse the changes if it collides
		//then i test each direction (x,y) collisions then give the player back its dx or dy if its not colliding
		//this just translate to the player  being allowed to hitting a wall from the right but still being able to move up and down
		for (GameObject obj: objs){
			obj.setXOffset(-bgX);
			obj.setYOffset(-bgY);
			if (this.checkCollision(obj) && obj.isCollidable()){
				if(movedX) x-=dx;
				if(movedY) y-=dy;
				if(movedBgX) bgX-=dx;
				if(movedBgY) bgY-=dy;
				/*if(checkDeadzoneX() && checkDeadzoneY()){
					x -=dx;
					y -=dy;
					getCollisionBox().x -=dx;
					getCollisionBox().y -=dy;
				}else{
					if((maxXHit && facingRight) || (minXHit && !facingRight)){
						x -= dx;
						//getCollisionBox().x -=dx;
					}else{
						System.out.println("Back ground x move after collision");
						bgX -= dx;
					}
					
					if((maxYHit && dy >0) || (minYHit && dy < 0)){
						y -= dy;
						//getCollisionBox().y -=dy;
					}else{
						System.out.println("Back ground y move after collision");
						bgY -= dy;
					}
				}*/
				
				/*checks if the player is hitting the object from the top or bottom
				 * This means the player can still move in left or right direction
				 */
				obj.setXOffset(-bgX);
				obj.setYOffset(-bgY);
				if(!this.checkTBCollision(obj)){
					if(movedX) x+=dx;
					if(movedBgX) bgX+=dx;
					/*if(checkDeadzoneX() && checkDeadzoneY()){
						x +=dx;
						//getCollisionBox().x +=dx;
					}else{
						if((maxXHit && facingRight) || (minXHit && !facingRight)){
							x += dx;
							//getCollisionBox().x +=dx;
						}else{
							System.out.println("Back ground x move after collision trying to move left/right");
							bgX += dx;
						}
					}*/
				}
				/*checks if the player is hitting the object from the right or left
				 * This means the player can still move in up or down direction
				 */
				obj.setXOffset(-bgX);
				obj.setYOffset(-bgY);
				if(!this.checkLRCollision(obj)){
					if(movedY) y+=dy;
					if(movedBgY) bgY+=dy;
					/*if(checkDeadzoneX() && checkDeadzoneY()){
						y +=dy;
						//getCollisionBox().y +=dy;
					}else{
						
						if((maxYHit && dy >0) || (minYHit && dy < 0)){
							y += dy;
							//getCollisionBox().y +=dy;
						}else{
							System.out.println("Back ground y move after collision trying to move up/down");
							bgY += dy;
						}
					}*/

				}
				
			
			}
			GameRender.setBackgroundOffset(-Math.round(bgX), -Math.round(bgY));
		}

		System.out.println("__________________");
		
		//grace updates
		if(grace.going()){
			grace.update();
		}
		
		//knockback updates
		//TODO have it so the knockback class is completely hidden from player
		//possible use an external knockback manager?
		if(knockback !=null){
			if(knockback.getStatus()){
				knockback.update();
			}
			else{	
				knockback=null;	//reset
				noMovement=false;	//reset
			}
		}
		
		//attacking updates
		//TODO Do attack interruption better
		if(currentAttack != null){
			//if currently attacking, update else dont
			if(attacking){
				if(!currentAttack.isActive() || noMovement){
					if(!facingRight){
						x+=currentAttack.getOffset();
					}
					currentAttack.stop();
					attacking =false;
				}
				currentAttack.update();
			}
			
		}
		
		animationUpdate();
		
	}
	
	private void checkDeadzoneX(){
		//System.out.println("PLAYER X POS: " + x);
		//System.out.println("PLAYER Y POS: " + y);
		//System.out.println("WINWIDTH/2: " + windowWidth/2);
		//if window hits the right side of background
		if(bgX >= bgWidth - windowWidth){
			maxXHit = true;
			//System.out.println("MAX X = TRUE");
			//if window hits left side of background
			}else if(bgX <=0){
			minXHit = true;
			//System.out.println("MIN X = TRUE");	
		}
		//if we hit the left side, but are now walking right and get back to the middle
		if(minXHit & x >= windowWidth/2 & facingRight){
			minXHit = false;
		//if we hit the right side but are now walking left back to the middle
		}else if(maxXHit & x <= windowWidth/2 & !facingRight){
			maxXHit = false;
		}
		
	}
	

	private void checkDeadzoneY(){
		//if the window hits the bottom of the background
		if(bgY >= bgHeight - windowHeight){
			maxYHit = true;
			//System.out.println("MAX Y = TRUE");
			//if the window hits the top of the background
			}else if(bgY <= 0){
			minYHit = true;
			//System.out.println("MIN Y = TRUE");
			}
		
		//if we hit the top of the background but are now walking down to the middle
		if(minYHit & y >= windowWidth/2 & dy >0){
			minYHit = false;
			//if we hit the bottom of the background but are now walking up towards the middle
		}else if(maxYHit & y <= windowWidth/2 & dy < 0){
			maxYHit = false;
		}
			
	}
	
	/**
	 * movement update based on user input
	 * Now runs on main update loop as opposed to own button listener thread or some shit
	 */
	private void updateDeltaMovement(){
		//LEFT press
		if(movingLeft){
			if(dx> -maxXSpeed && !noMovement){
				if(dx>0){
					dx=0;
				}
				//change direction
				if(facingRight){
					flip();
				}
				this.dx-=moveSpeedX;
			}
		}
		
		//RIGHT press
		if(movingRight){
			if(dx< maxXSpeed && !noMovement){
				if(dx<0){
					dx=0;
				}
				//change direction
				if(!facingRight){
					flip();
				}
				this.dx+=moveSpeedX;
			}
		}
		//stop dx if no buttons are pressed and movement is allowed
		if(!movingRight && !movingLeft && !noMovement){
			dx =0;
		}
		//UP press
		if(movingUp){
			if(dy> -maxYSpeed && !noMovement){
				this.dy-=moveSpeedY;
			}	
		}
		
		//DOWN press
		if(movingDown){
			if(dy< maxYSpeed && !noMovement){
				this.dy+=moveSpeedY;
			}	
		}
		
		//stop dy if no buttons are pressed and movement is allowed
		if(!movingDown && !movingUp && !noMovement){
			dy=0;
		}
		
		//movement updates
		/*x +=dx;
		y +=dy;
		getCollisionBox().x +=dx;
		getCollisionBox().y +=dy;*/
	}
	
	//flips image
	private void flip(){
		if(!attacking){
			facingRight = !facingRight;
		}
	}
	
	/*
	 * updates animation
	 * TODO do a better job at possibleNewAnim
	 */
	private void animationUpdate(){
		Animation possibleNewAnim = currentAnim;
		
		//these should be in the same order of the priority
		if(dx <0){
			possibleNewAnim = walkLeftAnim; 
		}
		else if(dx >0){
			possibleNewAnim = walkRightAnim;
		}
		else if(dx==0){
			possibleNewAnim = (facingRight) ? idleRightAnim : idleLeftAnim;
		}
		//getting attacked, do better
		if(noMovement){
			possibleNewAnim = (facingRight) ? hurtRightAnim : hurtLeftAnim;
		}
		//attacking overrides movement animation
		if(attacking){
			possibleNewAnim = (facingRight) ? attackRAnim: attackLAnim;
		}
		
		
		//priority checking, animation change
		if(possibleNewAnim.getPriority() >= currentAnim.getPriority() || currentAnim.isFinished()){
			currentAnim = possibleNewAnim;
			if(oldAnim != currentAnim){
				//System.out.println("reset");
				currentAnim.reset();
			}
		}
		oldAnim = currentAnim;
		
		currentAnim.update();
	
		
		
	}
	/**
	 * Calculates the offset in x when changing direction
	 * based on difference of offset
	 * @param right - if the player is switching to left and right direction (right == true)
	 * TODO - account for difference cases of offset: both negative, both positive, one positive one negative etc.
	 *//*
	private void offsetXFix(boolean right){
		float diff = Math.abs(xAOffset) - Math.abs(xBOffset);
		if(right){	//switch direction to right
			x-=diff;
		}
		else{	//switch direction to left

			x+=diff;
		}
	}*/
	
	
	//player takes dmg if they arent in grace mode
	//TODO Make it more modular so it takes in an attack or something
	public void takeDamage(int dmg, Enemy enemy){
		if(!grace.going()){
			grace.start();
			HP -= dmg;
			checkDeath();
			knockback = new KnockBack(enemy,this,150,8);
			noMovement =true;
		}
	}
	
	private void checkDeath(){
		if(HP <= 0){
			//System.out.println("Hero has died!");
		}
	}

	
	
	
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
	//GETTERS
/*	public float getDx(){ return dx; }
	
	public float getDy(){ return dy; }*/
	
	@Override
	public Rectangle2D.Float getCollisionBox(){
		
		//mother of all fucking god do this better LMAO
		if(attacking && !facingRight){
			collisionBox.x = x+ currentAttack.getOffset();
		}
		else{
			collisionBox.x = x;
		}
		
		collisionBox.y = y;
		return collisionBox;
	}
	
	public boolean isAttacking(){ return attacking; }

	public Attack getAttack(){ return currentAttack; }
	
	public boolean isBlinked(){ return grace.getBlink(); }
	
	//SETTERS
/*	public void setDx(float dx){ this.dx = dx;}
	public void setDy(float dy){ this.dy = dy;}*/
	

	//MOVEMENT/ CONTROLS
	public void moveLeft(){
		movingLeft=true;
	}
	//stops the player from moving left
	public void stopMovingLeft(){
		movingLeft=false;

	}
	
	//stops the player from moving right
	public void moveRight(){
		movingRight=true;
	}
	
	public void stopMovingRight(){
		movingRight=false;
	}
	
	//WIP
	public void moveUp(){
		movingUp=true;
	}
	//WIP
	public void stopMovingUp(){
		movingUp= false;
	}
	//WIP
	public void moveDown(){
		movingDown=true;
	}
	//WIP
	public void stopMovingDown(){
		movingDown =false;
	}

	
	public void attack(){
		//cant attack if already attacking or hurt
		//might change this idk
		if(!attacking && !noMovement){
			attacking=true;
			if(!facingRight){
				//move the character the offset designated to the attack
				x-=currentAttack.getOffset();
			}
			
			currentAttack.activate();
		}
		
	}
	@Override
	public void spawn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void die() {
		// TODO Auto-generated method stub
		
	}
}//end class
