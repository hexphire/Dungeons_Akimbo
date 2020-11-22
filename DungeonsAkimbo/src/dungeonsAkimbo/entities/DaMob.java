package dungeonsAkimbo.entities;

import java.util.stream.IntStream;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

import dungeonsAkimbo.DungeonsAkimboGame;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

public class DaMob extends Entity implements DaEnemy {

	private int health;
	private int type;
	private Vector velocity;
	private float initX;
	private float initY;
	private float bounceCooldown;
	private int direction;
	
	private SpriteSheet spritesheet;
	private Animation sprite;
	
	public DaMob(float x, float y, int type, boolean debug) {
		super(x,y);
		super.setDebug(debug);
		setInitX(x);
		setInitY(y);
		this.setBounceCooldown(0);
		this.setType(type);
		this.direction = 0;
		this.setSprite(new Animation(false));
		this.spritesheet = new SpriteSheet(ResourceManager.getImage(DungeonsAkimboGame.MOB_ZERO), 32, 32, 0, 0);
		// Begin creating and handling sprites
		Image tempSprite = spritesheet.getSprite(1, this.direction).getScaledCopy(.5f);
		this.addImageWithBoundingBox(tempSprite);
		this.removeImage(tempSprite);
		if(type == 0) {
			// Mob Zero: Spoopy Sprite
			this.setHealth(35);
			IntStream.range(0, 4).forEachOrdered(n -> {
				this.getSprite().addFrame(this.spritesheet.getSprite(1, n).getScaledCopy(.5f), 1);
			});
		} else {
			// Mob One: Mommy Sprite
			this.spritesheet = new SpriteSheet(ResourceManager.getImage(DungeonsAkimboGame.MOB_ONE), 32, 32, 0, 0);
			setHealth(20);
			IntStream.range(0, 4).forEachOrdered(n -> {
				this.getSprite().addFrame(this.spritesheet.getSprite(1, n).getScaledCopy(.5f), 1);
			});
		}
		this.addAnimation(getSprite());
		this.velocity = new Vector(0, 0);
	}

	@Override
	public void collisionAction(boolean isHit, boolean isPlayer) {
		if(isHit) {
			// Lost health and gain invincibility for a bit
			this.setHealth(this.getHealth() - 1);
			this.setBounceCooldown(20);
			// If collide to player, stop and pause
			if(isPlayer) {
				this.velocity = new Vector(0, 0);
			}
		}
	}

	@Override
	public void attack(Entity player) {
		Vector distance = this.getPosition().subtract(player.getPosition());
		if(this.getBounceCooldown() > 0 && type == 0) {
			// Basic mob collided, stopped and recover
			this.setBounceCooldown(this.getBounceCooldown() - 1);
		} else if(type == 0) {
			// Just track the player and collide with them
			this.velocity = distance.unit().negate();
			this.velocity = this.velocity.unit().scale(.05f);
		}

		// Get angle, determine which direction sprite goes
		double currentDirection = distance.negate().getRotation();
		if(this.direction != 0 && (currentDirection >= 45 && currentDirection < 135)) {
			// Rotate down
			this.direction = 0;
			this.getSprite().setCurrentFrame(this.direction);
		} else if (this.direction != 1 && (currentDirection < -135 && currentDirection < 135)) {
			// Rotate right
			this.direction = 1;
			this.getSprite().setCurrentFrame(this.direction);
		} else if (this.direction != 2 && ( -45 <= currentDirection && currentDirection < 45)) {
			// Rotate left
			this.direction = 2;
			this.getSprite().setCurrentFrame(this.direction);
		} else if (this.direction != 3 && (currentDirection >= -135 && currentDirection < -45))  {
			// Rotate up
			this.direction = 3;
			this.getSprite().setCurrentFrame(this.direction);
		}
	}
	
	public void update(final int delta) {
		// Move the sprite
		translate(this.velocity.scale(delta));
	}
	
	public boolean isDead() {
		return this.health <= 0;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

	public float getInitX() {
		return initX;
	}

	public void setInitX(float initX) {
		this.initX = initX;
	}

	public float getInitY() {
		return initY;
	}

	public void setInitY(float initY) {
		this.initY = initY;
	}

	public float getBounceCooldown() {
		return bounceCooldown;
	}

	public void setBounceCooldown(float bounceCooldown) {
		this.bounceCooldown = bounceCooldown;
	}

	public Animation getSprite() {
		return sprite;
	}

	public void setSprite(Animation sprite) {
		this.sprite = sprite;
	}
	
}
