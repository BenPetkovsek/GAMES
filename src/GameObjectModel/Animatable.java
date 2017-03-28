package GameObjectModel;

import AnimationModel.Animation;

/**
 * Models any gameobject that is animatable
 * Meaning it can be drawn with a sprite
 * @author Michael
 *
 */
public abstract class Animatable extends GameObject{

	private Animation currentAnim;
	private Direction direction = Direction.RIGHT;
	private float scale=1;
	
	
	public Animatable(float x, float y) {
		super(x,y);
	}
	
	@Override
	public void update(){
		currentAnim.update();
	}

	//GETTERS
	
	public Animation getAnim(){ return currentAnim; }
	
	public Direction getDirection(){ return direction; }
	
	public float getScale(){ return scale; }
	
	public float getWidth(){
		if(currentAnim !=null){
			return ((float) currentAnim.getCurrFrame().getWidth())*scale;
		}
		return 0;
	}
	
	public float getHeight(){
		if(currentAnim !=null){
			return ((float) currentAnim.getCurrFrame().getHeight())*scale;
		}
		return 0;
	}

	//SETTERS
	public void setAnim(Animation anim){ this.currentAnim = anim; }
	
	public void setDirection(Direction newDir){ this.direction = newDir; }
	public void setScale(float newScale){ this.scale=  newScale; }
}
