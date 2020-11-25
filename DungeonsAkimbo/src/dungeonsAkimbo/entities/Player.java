package dungeonsAkimbo.entities;



import java.util.ArrayList;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

import dungeonsAkimbo.DungeonsAkimboGame;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

public class Player extends Entity {
	
	private ArrayList<Weapon> gunBackpack;
	
	private Ranged primaryWeapon;
	
	

	private float speed;
	private int currentHealth;
	private int max_health;
	private int dodgeTimer = 500;
	
	private boolean dodging;
	
	private Vector velocity;
	
	private Animation sprite, walkUp, walkDown, walkLeft, walkRight, current;
	private SpriteSheet sprites;
	
	public Player(final float x, final float y, int type) {
		super(x, y);
		// Max health can either be set from constructor, or a be statically 
		// constant, deal with later
		this.sprite = new Animation(false);
		this.sprites = new SpriteSheet(ResourceManager.getImage(DungeonsAkimboGame.DA_PLAYER_RSC), 32, 32, 0, 0);
		setMax_health(100);
		setCurrent_health(getMax_health());
		speed = 0.5f;		
		primaryWeapon = new DaSniper();		
		gunBackpack = new ArrayList<Weapon>();		
		gunBackpack.add(primaryWeapon);
		this.addImageWithBoundingBox(this.sprites.getSprite(1, 0));
		this.removeImage(this.sprites.getSprite(1, 0));
		
		this.sprite.addFrame(this.sprites.getSprite(1, 0), 1);		//Player face down
		this.sprite.addFrame(this.sprites.getSprite(1, 1), 1);		//Player face left
		this.sprite.addFrame(this.sprites.getSprite(1, 2), 1);		//Player face right
		this.sprite.addFrame(this.sprites.getSprite(1, 3), 1);		//Player face up
		
		this.walkDown = new Animation(sprites, 0,0, 2,0, true, 2, true);		//Player walk down
		this.walkLeft = new Animation(sprites, 0,1, 2,1, true, 2, true);	//Player walk left	
		this.walkRight = new Animation(sprites, 0,2, 2,2, true, 2, true);	//Player walk right
		this.walkUp = new Animation(sprites, 0,3, 2,3, true, 2, true);	//Player walk up
		
		this.current = walkDown;
		this.addAnimation(current);
	}
	
	public void gunSelect(int i) {
		this.primaryWeapon = (Ranged) gunBackpack.get(i);
	}
	
	public Ranged getPrimaryWeapon() {
		return primaryWeapon;
	}
	
	public Projectile Shoot(double inAngle) {
		return (Projectile) this.primaryWeapon.primaryAtk(inAngle);	
	}
	
	public Weapon swapWep(Weapon s) {
		Weapon temp = this.primaryWeapon;
		this.primaryWeapon = (Ranged) s;
		return temp;
	}
	
	public void doDodge(final int delta, int scaler) {
		this.speed = scaler*this.speed;
		this.dodging = true;
	}
	
	public void setVelocity(final Vector v) {
		velocity = v;
	}
	
	public Vector getVelocity() {
		return velocity;
	}
	
	public void setSpeed(float s) {
		this.speed = s;
	}
	
	public float getSpeed() {
		return this.speed;
	}
	
	public int getHash() {
		int hash = 17*(int)this.getX()^19 * (int)(this.getY());
		return hash;
	}
	
	public int getMax_health() {
		return max_health;
	}

	public void setMax_health(int max_health) {
		this.max_health = max_health;
	}

	public int getCurrent_health() {
		return currentHealth;
	}

	public void setCurrent_health(int current_health) {
		this.currentHealth = current_health;
	}
	
public void update(final int delta) {
		
		if (this.dodging == true) {
			dodgeTimer -= delta;
			//System.out.println("Player is dodge? " + this.dodging);
		}
		
		if (dodgeTimer <= 0) {
			this.speed = 1f;
			this.dodging = false;
			dodgeTimer = 500;	
		}
		
		if (Math.abs(((Entity) this.primaryWeapon).getRotation()) < 45) { 		//Player facing right
			if(current.equals(walkRight) &&  velocity.length() != 0) {
				current.start();				
			}else if( current.equals(walkRight) && velocity.length() == 0) {
				current.stop();
				current.setCurrentFrame(1);
			}else if(!current.equals(walkRight)) {
				current.stop();
				this.removeAnimation(current);
				this.addAnimation(walkRight);
				current = walkRight;
			}		
			
		} else if (Math.abs(((Entity) this.primaryWeapon).getRotation()) > 135) {		//Player facing left
			if(current.equals(walkLeft) &&  velocity.length() != 0) {
				current.start();				
			}else if( current.equals(walkLeft) && velocity.length() == 0) {
				current.stop();
				current.setCurrentFrame(1);
			}else if(!current.equals(walkLeft)) {
				current.stop();
				this.removeAnimation(current);
				this.addAnimation(walkLeft);
				current = walkLeft;
			}
		} else if (((Entity) this.primaryWeapon).getRotation() > 45 && ((Entity) this.primaryWeapon).getRotation() < 135) {		//Player facing down
			if(current.equals(walkDown) &&  velocity.length() != 0) {
				current.start();				
			}else if( current.equals(walkDown) && velocity.length() == 0) {
				current.stop();
				current.setCurrentFrame(1);
			}else if(!current.equals(walkDown)) {
				current.stop();
				this.removeAnimation(current);
				this.addAnimation(walkDown);
				current = walkDown;
			}
		}else if (((Entity) this.primaryWeapon).getRotation() > -135 && ((Entity) this.primaryWeapon).getRotation() < -45 ) {		//Player facing up
			if(current.equals(walkUp) &&  velocity.length() != 0) {
				current.start();				
			}else if( current.equals(walkUp) && velocity.length() == 0) {
				current.stop();
				current.setCurrentFrame(1);
			}else if(!current.equals(walkUp)) {
				current.stop();
				this.removeAnimation(current);
				this.addAnimation(walkUp);
				current = walkUp;
			}
		}
	}
}
