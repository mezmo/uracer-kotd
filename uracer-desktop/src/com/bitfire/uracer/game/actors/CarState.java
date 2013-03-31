
package com.bitfire.uracer.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.bitfire.uracer.game.player.PlayerCar;
import com.bitfire.uracer.game.world.GameWorld;
import com.bitfire.uracer.utils.AMath;
import com.bitfire.uracer.utils.Convert;

public final class CarState {
	/* event */
	public CarStateEvent event = null;

	/* observed car */
	public final Car car;
	public boolean isPlayer;

	/* position */
	public int currTileX = -1, currTileY = -1;
	public Vector2 tilePosition = new Vector2();

	/* speed/force factors */
	public float currVelocityLenSquared = 0;
	public float currThrottle = 0;
	public float currSpeedFactor = 0;
	public float currForceFactor = 0;

	/* lateral forces */
	// public Vector2 lateralForceFront = new Vector2(), lateralForceRear = new Vector2();

	// temporaries
	private float carMaxSpeedSquared = 0;
	private float carMaxForce = 0;
	private int lastTileX = 0, lastTileY = 0;

	private GameWorld world;

	public CarState (GameWorld world, Car car) {
		this.event = new CarStateEvent(this);
		this.world = world;
		this.car = car;
		this.isPlayer = (car instanceof PlayerCar);

		// precompute factors
		if (car != null) {
			carMaxSpeedSquared = car.getCarModel().max_speed * car.getCarModel().max_speed;
			carMaxForce = car.getCarModel().max_force;
		}
	}

	public void dispose () {
		event.removeAllListeners();
		event = null;
	}

	public void reset () {
		// causes an onTileChanged event to be raised the next update step
		lastTileX = -1;
		lastTileY = -1;
		currTileX = -1;
		currTileY = -1;
	}

	public void update (CarDescriptor carDescriptor) {
		if (carDescriptor != null) {
			updateFactors(carDescriptor);
		}

		updateTilePosition();
	}

	private void updateFactors (CarDescriptor carDescriptor) {
		// speed/force normalized factors
		currVelocityLenSquared = carDescriptor.velocity_wc.len2();
		currThrottle = carDescriptor.throttle;
		currSpeedFactor = AMath.clamp(currVelocityLenSquared / carMaxSpeedSquared, 0f, 1f);
		currForceFactor = AMath.clamp(currThrottle / carMaxForce, 0f, 1f);
	}

	/*
	 * Keeps track of the car's tile position and trigger a TileChanged event whenever the car's world position translates to a
	 * tile index that is different than the previous one
	 */
	private void updateTilePosition () {
		lastTileX = currTileX;
		lastTileY = currTileY;

		// compute car's tile position
		tilePosition
			.set(world.pxToTile(Convert.mt2px(car.getBody().getPosition().x), Convert.mt2px(car.getBody().getPosition().y)));

		currTileX = (int)tilePosition.x;
		currTileY = (int)tilePosition.y;

		if ((lastTileX != currTileX) || (lastTileY != currTileY)) {
			event.trigger(this, CarStateEvent.Type.onTileChanged);
		}
	}
}
