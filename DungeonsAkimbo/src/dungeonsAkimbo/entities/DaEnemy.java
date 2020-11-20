package dungeonsAkimbo.entities;

import jig.Entity;

public interface DaEnemy {
	
	// Called when a collision interaction is confirmed
	public boolean collisionAction(Entity object, boolean isPlayer);
	
	// An implementation line to handle an enemy attack
	public void attack(Entity player);
	
}
