package dungeonsAkimbo;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.heuristics.ManhattanHeuristic;

import dungeonsAkimbo.entities.DaAssault;
import dungeonsAkimbo.entities.DaBoi;
import dungeonsAkimbo.entities.DaMiniBoi;
import dungeonsAkimbo.entities.DaMob;
import dungeonsAkimbo.entities.DaSpawner;
import dungeonsAkimbo.entities.Player;
import dungeonsAkimbo.entities.Projectile;
import dungeonsAkimbo.entities.Ranged;
import dungeonsAkimbo.map.DaMap;
import jig.Entity;

public class DaLogic {

	DaMap map;
	DaCollisions bonkHandler;
	AStarPathFinder DaWey;
	DungeonsAkimboGame dag;
	
	
	

	public DaLogic(DaMap gameMap) {
		this.map = gameMap;
		DaWey = new AStarPathFinder(map, 500, true, new ManhattanHeuristic(0));
		startCollisions();
	}
	
	
	//logic update to be used by client until we can remove the need for playerID
	//currently just updating entities and checking collisions
	//this should get get called in the playstate's update()
	//and used by the server to update it's map.
	
	public void clientUpdate(int playerID, int delta) {
		clientUpdateEntities(playerID, delta);
		collideCheck(delta);
	}
	
	
	//we use this update function to update local games not using the netowrk.
	public void localUpdate(int delta) {
        for(Map.Entry<Integer, Player> uniquePlayer : map.getPlayerList().entrySet()) {
            int playerId = uniquePlayer.getKey();
            playerUpdate(playerId, delta);
            mobUpdate(playerId, delta);
        }
        projectileUpdate(delta);
        collideCheck(delta);
    }
	
	
	public Path getPathToTarget(Mover mover, Entity target) {
		int moverX = Math.round(((Entity) mover).getX()/32);
		int moverY = Math.round(((Entity) mover).getY()/32);
		int targetX = Math.round(target.getX()/32);
		int targetY = Math.round(target.getY()/32);
		
		Path path = DaWey.findPath(mover, moverX, moverY, targetX, targetY);
		
		return path;
		
	}
	
	
	public Path getPathToTile(Mover mover, int tX, int tY) {
		int moverX = Math.round(((Entity) mover).getX()/32);
		int moverY = Math.round(((Entity) mover).getY()/32);
		
		Path path = DaWey.findPath(mover, moverX, moverY, tX, tY);
		
		return path;
	}
	
	
	public void resetPath(int playerID) {		/* Mob attacking the player, and updating pathing if certain type
		 * of mob stays still
		 */
		// Get current player as an Entity (change to player later if needed)
		Player currentPlayer = map.getPlayerList().get(playerID);
		
		ArrayList<DaMob> mobs = map.getMobList();
		for (DaMob mob : mobs) {
			if(mob.getType() == 2 && (!((Player)currentPlayer).isRest() || mob.getPath() == null)) {
				// Get pathing to the player using Slick2D
				ArrayDeque<Path.Step> newPath =  new ArrayDeque<Path.Step>();
				Path tempPath = this.getPathToTarget(mob, currentPlayer);
				if(tempPath.getLength() > 0) {
					// Path received, push every new step to the newPath
					for(int i = 0; i < tempPath.getLength(); i++) {
						newPath.push(tempPath.getStep(i));
					}
				} else {
					newPath = null;
				}
				mob.setPath(newPath);
			}
		}
		
	}
	
	//things that happend during update() for the player go here
	private void playerUpdate(int playerID, int delta) {
		map.getPlayerList().get(playerID).update(delta);
		map.getPlayerList().get(playerID).getPrimaryWeapon().update(delta);
	}
	
	//things that happened during update() for mobs go here
	private void mobUpdate(int playerID, int delta) {
		
		// Get current player as an Entity (change to player later if needed)
		Player currentPlayer = map.getPlayerList().get(playerID);
		
		// Generates spawn
		for(DaSpawner spawn:  map.getSpawnList()) {
			int type = spawn.generateMob();
			if(type >= 0) {
				map.getMobList().add(new DaMob(spawn.getX(), spawn.getY(), type, true));
			}
		}
		
		// Handle mob attack
		ArrayList<DaMob> mobs = map.getMobList();
		for (DaMob mob : mobs) {
			// Handle Attack
			Projectile hit = mob.attack(currentPlayer);
			if (hit != null) {
				map.getEnemyAttacks().add(hit);
			}
		}
		
		// Handle miniboss attack
		for(Iterator<DaMiniBoi> currentMini = map.getMiniBoss().iterator(); currentMini.hasNext();) {
			DaMiniBoi miniBoss = currentMini.next();
			Projectile miniBossAttack = miniBoss.attack(currentPlayer);
			ArrayList<Projectile> miniBossMultiAttack = miniBoss.multiAttack();
			if(miniBossAttack != null) {
				// Single attack returned, add to list of enemy attacks
				map.getEnemyAttacks().add(miniBossAttack);
			}
			if(miniBossMultiAttack != null) {
				// Iterate through the list of multi attacks
				miniBossMultiAttack.forEach((hit) -> map.getEnemyAttacks().add(hit));
			}
		}
		
		// Handle boss attack
		for(DaBoi boss: map.getBoss()) {
			Projectile bossAttack = boss.attack(currentPlayer);
			ArrayList<Projectile> bossMultiAttack = boss.multiAttack(currentPlayer);
			if(bossAttack != null) {
				map.getEnemyAttacks().add(bossAttack);
			}
			if(bossMultiAttack != null) {
				bossMultiAttack.forEach((hit)-> map.getEnemyAttacks().add(hit));
			}
		}
		
		// Update enemies based on their updates
		mobs.forEach((mob) -> mob.update(delta));
		map.getBoss().forEach((theBoss) -> theBoss.update(delta));
	}
	
	//things that happened during update() for projectiles go here
	private void projectileUpdate(int delta) {
		/* Check for collision with mobs, and also update projectiles */
		for (Iterator<Projectile> bIter = map.getPlayer_bullets().iterator(); bIter.hasNext();) {
			Projectile b = bIter.next();
			
			b.update(delta);
			b.decreaseTimer(delta);
			
			if (b.getTimer() <= 0) {
				bIter.remove();
			}
		}
		for(Iterator<Projectile> attacks = map.getEnemyAttacks().iterator(); attacks.hasNext();) {
			Projectile attack = attacks.next();
			
			// update attack (move if it has speed, decrease timer duration)
			if(attack.getSpriteType() >= 0) {
				if(!attack.isActive()) {
					attack.setTimer(0);
				}
			} else {
				attack.decreaseTimer(delta);
			}
			
			// Check if duration of attack is over
			if(attack.getTimer() <= 0) {
				attacks.remove();
			}
		}
		map.getEnemyAttacks().forEach((hitbox) -> hitbox.update(delta));
	}
	
	//update entities(for network testing/use) 
	private void clientUpdateEntities(int playerID, int delta) {
		/* Mob attacking the player, and updating pathing if certain type
		 * of mob stays still
		 */
		// Get current player as an Entity (change to player later if needed)
		Player currentPlayer = map.getPlayerList().get(playerID);

		// Generates spawn
		for(DaSpawner spawn:  map.getSpawnList()) {
			int type = spawn.generateMob();
			if(type >= 0) {
				map.getMobList().add(new DaMob(spawn.getX(), spawn.getY(), type, true));
			}
		}
		
		// Handle mob attack
		ArrayList<DaMob> mobs = map.getMobList();
		for (DaMob mob : mobs) {
			// Handle Attack
			Projectile hit = mob.attack(currentPlayer);
			if (hit != null) {
				map.getEnemyAttacks().add(hit);
			}
		}
		// Handle miniboss attack
		for(Iterator<DaMiniBoi> currentMini = map.getMiniBoss().iterator(); currentMini.hasNext();) {
			DaMiniBoi miniBoss = currentMini.next();
			Projectile miniBossAttack = miniBoss.attack(currentPlayer);
			ArrayList<Projectile> miniBossMultiAttack = miniBoss.multiAttack();
			if(miniBossAttack != null) {
				// Single attack returned, add to list of enemy attacks
				map.getEnemyAttacks().add(miniBossAttack);
			}
			if(miniBossMultiAttack != null) {
				// Iterate through the list of multi attacks
				miniBossMultiAttack.forEach((hit) -> map.getEnemyAttacks().add(hit));
			}
		}
		
		// Handle boss attack
		for(DaBoi boss: map.getBoss()) {
			Projectile bossAttack = boss.attack(currentPlayer);
			ArrayList<Projectile> bossMultiAttack = boss.multiAttack(currentPlayer);
			if(bossAttack != null) {
				map.getEnemyAttacks().add(bossAttack);
			}
			if(bossMultiAttack != null) {
				bossMultiAttack.forEach((hit)-> map.getEnemyAttacks().add(hit));
			}
		}
		
		/* Check for collision with mobs, and also update projectiles */
		for (Iterator<Projectile> bIter = map.getPlayer_bullets().iterator(); bIter.hasNext();) {
			Projectile b = bIter.next();
			
			b.update(delta);
			b.decreaseTimer(delta);
			
			if (b.getTimer() <= 0) {
				bIter.remove();
			}
		}
		for(Iterator<Projectile> attacks = map.getEnemyAttacks().iterator(); attacks.hasNext();) {
			Projectile attack = attacks.next();
			
			// update attack (move if it has speed, decrease timer duration)
			if(attack.getSpriteType() >= 0) {
				if(!attack.isActive()) {
					attack.setTimer(0);
				}
			} else {
				attack.decreaseTimer(delta);
			}
			
			// Check if duration of attack is over
			if(attack.getTimer() <= 0) {
				attacks.remove();
			}
		}

		// Update entities
		map.getPlayerList().get(playerID).update(delta);
		map.getPlayerList().get(playerID).getPrimaryWeapon().update(delta);
		map.getEnemyAttacks().forEach((hitbox) -> hitbox.update(delta));
		mobs.forEach((mob) -> mob.update(delta));
		map.getBoss().forEach((theBoss) -> theBoss.update(delta));

	}
	//start up a collision handler for the map
	public void startCollisions() {
		bonkHandler = new DaCollisions(map);
	}
	//check for collisions in this update
	public void collideCheck(int delta) {	
		bonkHandler.playerCollsionsTest(delta);
		bonkHandler.enemyCollisionsTest(delta);
	}
	
	public void assaultBurst(StateBasedGame game, int playerID, double inAngle) {
		dag = (DungeonsAkimboGame) game;
		Player player = dag.getCurrentMap().getPlayerList().get(playerID);
		DaAssault wep = ((DaAssault) dag.getCurrentMap().getPlayerList().get(playerID).getPrimaryWeapon());
		
		if (wep.isCanFireBullet()) {
			Projectile bullet = new Projectile(player.getX(), player.getY(), wep.getDamage(), wep.getRange());
			bullet.rotate(inAngle);
			bullet.Set_Velocity(inAngle);
			dag.getCurrentMap().getPlayer_bullets().add((Projectile) bullet);
			
			wep.setIsBursting(false);
			wep.setCanFireBullet(false);
			
			wep.decrementBurstAmount();
		}	
	}
}