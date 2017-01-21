package Model;

import java.awt.image.BufferedImage;

import View.ImageStyler;

public class Rock extends GameObject{

	private Animation idleAnim;
	private BufferedImage idle = ImageStyler.loadImg("rock.png");
	
	public Rock(float x, float y){
		super(x,y);
		idleAnim = new Animation(false);
		idleAnim.addFrame(idle);
		currentAnim  = idleAnim;
		scale=0.5f;
		drawBorders=true;
		
	}
	
	@Override
	public void update(){
		currentAnim.update();
	}
	
}
