package PlayerModel;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import AnimationModel.Animation;
import EffectModel.EffectManager;
import EffectModel.Invulnerability;
import EffectModel.KnockBack;
import EnemyModel.Enemy;
import GameObjectModel.Collidable;
import MiscModel.Attack;
import View.*;

/**
 * Models the main player of the game
 * Does not deal with physics, animation
 * Only state variables (attacking, freezing etc)
 * @author Michael
 *
 */
// TODO remove all traces of old attack methods and shit
public class Player extends Collidable {
	int HP, totalHP, str, def, intel;
	String name;
	
	float mouseX,mouseY;
	
	private boolean movingLeft=false;
	private boolean movingRight=false;
	private boolean movingUp=false;
	private boolean movingDown=false;
	
	private boolean[] movementDirections;	//puts the above vars in an array
	
	//Animator Variables
	
	//Physics Engine
	private PlayerPhysics playerPhysics;
	
	//attacking variables
	private boolean attacking=false;
	private Attack currentAttack;	//TODO make a list of attacks , right now there will be one
	
	private boolean noMovement=false;	//if the player can't move
	
	private Weapon weapon;
	private int isDodging = 0;// 0 for not dodging, 1 for dodge ramp up, 2 for dodge ramp down
	private int maxDodgeLength = 10;
	private int currentDodgeLength = 0;
	private int dodgeCooldown = 20;
	private int currentDodgeCooldown = dodgeCooldown;
	
	/**
	 * Creates a player at (x,y)
	 * @param x x coordinate to place player at
	 * @param y y coordinate to place player at
	 */
	public Player(float x,float y){
		super(x,y);
		animator = new PlayerAnimator<Player>(this);
		playerPhysics=  new PlayerPhysics(this);
		HP =100;
		setScale(5f);
		drawBorders=true;
		attackInit();
		offsetInit();
		weapon = new Weapon(this,0);
	}
	//inits attack
	private void attackInit(){
		BufferedImage attackWidth;
		BufferedImage idle;
		//current punch attack
		//xOffset is the difference in the sprite compared to idle animation
		//float offset = getScale()*(animator.getImage("attackRight", 0).getWidth() - animator.getImage("idleRight", 0).getWidth());
		//currentAttack = new Attack(this,getWidth(),0f,30,offset,getHeight(), (int) animator.getAnimationCollection("attackLeft").getDuration(),offset);
	}
	
	//offset inits
	private void offsetInit(){
		collisionBox = new Rectangle2D.Float(x+colXOffset, y+colYOffset, getWidth(), getHeight());
	}
	
	/**
	 * Updates the player
	 * @param objs list of collidable objects to check for collision
	 */
	public void update(ArrayList<Collidable> objs){
		super.update();
		playerPhysics.update(objs);
		animator.update();
		weapon.update(calcWeaponAngle());
	}
	
	private Double calcWeaponAngle(){
		Double diffX= this.getCollisionBox().getCenterX() - mouseX;
		Double diffY = this.getCollisionBox().getCenterY() - mouseY;
		Double angle = Math.atan2(diffY, diffX);
		angle = Math.toDegrees(angle);
		angle -=180;
		angle= Math.abs(angle);
		return angle;
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
	
	/**
	 * Player attempts to take damage
	 * @param dmg how much the attack does
	 * @param enemy what enemy attacked
	 */
	//TODO Make it more modular so it takes in an attack or something
	public void takeDamage(int dmg, Enemy enemy){
		if(!isInvulnerable()){
			HP -= dmg;
			checkDeath();
			EffectManager.addEffect(new Invulnerability(70, 10,this));
			EffectManager.addEffect(new KnockBack(enemy,this,150,5));

		}
	}
	/**
	 * Sets the player to attack
	 */
	// TODO remove this temp method
	public void attack(){
		//cant attack if already attacking or hurt
		//might change this idk
		attacking=true;
	}
	
	// TODO remove this temp method
	public void stopAttacking(){
		attacking=false;
	}
	
	//if the player presses space and the dodge isnt on cooldown
	public void setDodge(){
		if(isDodging==0 && currentDodgeCooldown >= dodgeCooldown){
		isDodging = 1;
		currentDodgeCooldown = 0;	//start dodge and start cooldown
		}
	}
	//is the player currently dodging?
	public int getDodge(){
		return isDodging;
	}
	//controls how long the dodge lasts for + updates cooldown
	public void updateDodge(){
		//System.out.println("isDodging = " + isDodging + "Cooldown Remaining: " + (dodgeCooldown -  currentDodgeCooldown));
		//System.out.println("Current dodge length: "+ currentDodgeLength +" - max Dodge Length: " + maxDodgeLength);
		if(isDodging != 0){	//if currently dodging, regardless of phase
			currentDodgeLength++;	
			if(currentDodgeLength >= maxDodgeLength/2 && currentDodgeLength < maxDodgeLength){//check if the dodge is halfway done
				isDodging = 2;		//set the dodge to start ramping down
			}else if(currentDodgeLength >= maxDodgeLength){	//if the dodge is completely finished
				isDodging = 0;
				currentDodgeLength = 0;
			}
		}else if(isDodging == 0 && currentDodgeCooldown < dodgeCooldown){	//if the dodge has ended and the cooldown has started
			currentDodgeCooldown++;
		}
	}
	private void checkDeath(){
		if(HP <= 0){
			//System.out.println("Hero has died!");
		}
	}
	
	/**** Overriding Methods ****/
	
	/**
	 * Gets collision box of player
	 */
	@Override
	public Rectangle2D.Float getCollisionBox(){
		
/*		collisionBox.x = x +colXOffset + colXOverride;
		collisionBox.y = y + colYOffset + colYOverride;
		return collisionBox;*/
		Rectangle2D.Float colBox;
		if(currColBoxAnim != null){
			colBox = currColBoxAnim.getCurrFrame();
			colBox.x = x + currColBoxAnim.getCurrFrameOffsetX() + colXOverride;
			colBox.y = y + currColBoxAnim.getCurrFrameOffsetY() + colYOverride;
		}
		else{
			colBox =collisionBox;
			colBox.x =x + colXOffset + colXOverride;
			colBox.y =y + colYOffset + colYOverride;
		}
		
		return colBox;
	}
	
	/**
	 * Gets animation from Animator
	 */
	@Override
	public Animation getAnim(){
		return animator.getAnim();
	}
	
	/**
	 * gets width of player
	 */
	@Override
	public float getWidth(){
		return getAnim().getCurrFrame().getWidth() * getScale();
	}
	
	/**
	 * gets height of player
	 */
	@Override
	public float getHeight(){
		return getAnim().getCurrFrame().getHeight() * getScale();
	}
	
	/**** External Class Getters ****/
	
	/**
	 * Gets the physics Engine
	 */
	public PlayerPhysics getPhysicsEngine(){ return playerPhysics; }
	/**
	 * Gets the animator
	 */
//	public PlayerAnimator getAnimator(){ return (PlayerAnimator) animator; }
	
	/****State Methods ****/
	
	/**
	 * Gets if the player is currently attacking
	 */
	public boolean isAttacking(){ return attacking; }
	/**
	 * Gets the current attack the player is attacking with
	 */
	public Attack getAttack(){ return currentAttack; }
	
	/**
	 * Returns the players weapon
	 */
	public Weapon getWeapon(){ return weapon; }
	/**
	 * Gets if the player is not allowed to moved (frozen)
	 */
	public boolean isFrozen(){ return noMovement; }
	/**
	 * Sets if the player is allowed to moved
	 */
	public void setFreeze(boolean state){ noMovement = state; }
	

	/*****MOVEMENT/ CONTROLS******/
	
	/**
	 * Player is set to move left now
	 */
	public void moveLeft(){ movingLeft=true; }
	/**
	 * Player is set to stop moving left 
	 */
	public void stopMovingLeft(){ movingLeft=false; }
	/**
	 * Player is set to move right now
	 */
	public void moveRight(){ movingRight=true; }
	/**
	 * Player is set to stop moving right
	 */
	public void stopMovingRight(){ movingRight=false; }
	/**
	 * Player is set to move up now
	 */
	public void moveUp(){ movingUp=true; }
	/**
	 * Player is set to stop moving up
	 */
	public void stopMovingUp(){ movingUp= false; }
	/**
	 * Player is set to move down now
	 */
	public void moveDown(){ movingDown=true; }
	/**
	 * Player is set to stop moving down
	 */
	public void stopMovingDown(){ movingDown =false; }
	/**
	 * Gets all the state variables of the players movement directions
	 * @return Array list containing : {Up state, down state, left state, right state}
	 */
	public boolean[] getMovementDir(){
		boolean[] dir = {movingUp,movingDown,movingLeft,movingRight};
		return dir;
	}
	
	/**
	 * Get mouse position from mouselistener
	 * @param x x pos of mouse
	 * @param y y pos of mouse 
	 */
	public void recieveMousePos(float x, float y){
		mouseX = x;
		mouseY = y;
	}
}
