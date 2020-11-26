package dungeonsAkimbo.netcode;

import java.io.Serializable;

import jig.Vector;

public class UpdatePacket implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1824778510789925441L;
	
	private int playerID, frame;
	private Vector playerPos, playerVel;
	
	public UpdatePacket(int playerID, Vector playerPos, Vector playerVel, int frame) {
		this.frame = frame;
		this.playerID = playerID;
		this.playerPos = playerPos;
		this.playerVel = playerVel;
	}

	public int getPlayerID() {
		return playerID;
	}

	public Vector getPlayerPos() {
		return playerPos;
	}

	public Vector getPlayerVel() {
		return playerVel;
	}

	public int getFrame() {
		return frame;
	}
	

}
