
package com.bitfire.uracer.game;

import com.bitfire.uracer.game.actors.GhostCar;
import com.bitfire.uracer.game.logic.types.helpers.TimeModulator;

public interface GameLogic {
	void dispose ();

	void addPlayer ();

	void removePlayer ();

	void restartGame ();

	void resetGame ();

	void quitGame ();

	boolean isQuitPending ();

	float getCollisionFactor ();

	GhostCar getNextTarget ();

	void startTimeDilation ();

	void endTimeDilation ();

	boolean isTimeDilationAvailable ();

	void tick ();

	void tickCompleted ();

	Time getOutOfTrackTimer ();

	Time getTimeDilationTimer ();

	TimeModulator getTimeModulator ();
}
