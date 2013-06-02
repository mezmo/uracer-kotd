
package com.bitfire.uracer.game;

import com.bitfire.uracer.events.CarEvent;
import com.bitfire.uracer.events.GameRendererEvent;
import com.bitfire.uracer.events.GhostCarEvent;
import com.bitfire.uracer.events.PhysicsStepEvent;
import com.bitfire.uracer.events.PlayerDriftStateEvent;
import com.bitfire.uracer.events.TaskManagerEvent;

public final class GameEvents {

	public static final GameRendererEvent gameRenderer = new GameRendererEvent();
	public static final PhysicsStepEvent physicsStep = new PhysicsStepEvent();
	public static final PlayerDriftStateEvent driftState = new PlayerDriftStateEvent();
	public static final CarEvent playerCar = new CarEvent();
	public static final GhostCarEvent ghostCars = new GhostCarEvent();
	public static final TaskManagerEvent taskManager = new TaskManagerEvent();

	// public static final CarStateEvent playerCarState = new CarStateEvent();

	private GameEvents () {
	}
}
