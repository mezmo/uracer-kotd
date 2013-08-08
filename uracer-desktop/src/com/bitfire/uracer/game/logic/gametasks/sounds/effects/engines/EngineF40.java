
package com.bitfire.uracer.game.logic.gametasks.sounds.effects.engines;

import com.badlogic.gdx.math.MathUtils;
import com.bitfire.uracer.game.logic.gametasks.sounds.effects.PlayerTensiveMusic;
import com.bitfire.uracer.game.logic.helpers.TrackProgressData;
import com.bitfire.uracer.resources.Sounds;
import com.bitfire.uracer.utils.AMath;
import com.bitfire.uracer.utils.InterpolatedFloat;

public class EngineF40 extends EngineSoundSet {
	private static final boolean UseGears = true;
	private static final int MinGear = 1;
	private static final int MaxGear = 4;
	private InterpolatedFloat ifactor = new InterpolatedFloat(), ispeed = new InterpolatedFloat(),
		ivolume = new InterpolatedFloat();
	private TrackProgressData progressData;

	public EngineF40 (TrackProgressData progressData) {
		super();
		this.progressData = progressData;
		engine = Sounds.carEngine_f40;
		rpm = 1000;
		gear = MinGear;
	}

	@Override
	public float getGlobalVolume () {
		float target_vol = 0;
		if (hasPlayer && progressData.hasTarget && !progressData.isWarmUp) {
			float d = progressData.playerDistance.get() - progressData.targetDistance.get();
			d = MathUtils.clamp(d, -PlayerTensiveMusic.ScaleMt, PlayerTensiveMusic.ScaleMt);
			d *= PlayerTensiveMusic.InvScaleMt; // normalized range
			float to_target = AMath.fixup(d);

			if (to_target < 0) {
				// player behind
				// fade out on distance from target (use tensive music meters scaling)
				float v = MathUtils.clamp(to_target + 1, 0, 1);
				target_vol = v;
			} else {
				// player heading the race
				target_vol = 1;
			}
		}

		// return .025f + 0.025f * volmul;
		ivolume.set(0.05f + 0.1f * target_vol * player.carState.currSpeedFactor, 0.05f);

		// Gdx.app.log("", "tv=" + target_vol);

		return ivolume.get();
	}

	@Override
	public boolean hasGears () {
		return UseGears;
	}

	@Override
	public float getGearRatio () {
		if (!UseGears) {
			return 3;
		}

		// {3, 1, 0.7f, 0.5f, 0.3f, 0.2f, 0.1f}

		float res = 1;

		//@off
		switch(gear) {
		case 0: res =  1f;
		//
		case 1: res = 2f; 	break;
		case 2: res = 1f;		break;
		case 3: res = 0.7f;	break;
		case 4: res = 0.5f;	break;
		}
		//@on

		return res;// * 0.15f;
	}

	@Override
	public void updatePitches () {
		float lowLimit = 6000f;
		float hiLimit = 10000f;

		// rpm = 1000;
		float rpmLow = rpm / lowLimit;
		float rpmDef = rpm / hiLimit;

		// dbg
		// setVolume(1, 1 * SoundManager.SfxVolumeMul);
		// setVolume(4, 1 * SoundManager.SfxVolumeMul);
		// dbg

		// sample specific
		if (rpm < lowLimit) {
			setXnaPitch(0, rpmLow);
			setXnaPitch(1, rpmDef + 0.2f);
		}

		setXnaPitch(2, rpmDef - 0.4f);
		setXnaPitch(3, rpmDef - 0.8f);
		setXnaPitch(4, rpmDef - 0.2f);
		setXnaPitch(5, rpmDef - 0.8f);
		setXnaPitch(6, rpmDef - 0.8f);
	}

	private float prevSpeed = 0;

	@Override
	public float updateRpm (float load) {
		if (UseGears) {
			if (!hasPlayer) {
				rpm = 1000;
				return 1000;
			}

			updateGear();
			ispeed.set(player.carState.currSpeedFactor, 0.85f);
			float sf = ispeed.get();

			float q = 15000;// 12858;
			float factor = q * sf * getGearRatio();

			// updateGearFIS();
			if (sf < prevSpeed) {
				factor = q * sf * getGearRatioOff();
			}

			// Gdx.app.log("EngineSoundSet", "gear=" + gear + ", rpm=" + rpm + ", throttle=" + player.getCarDescriptor().throttle
			// + ", throttling=" + player.isThrottling);// + ", speed="+ sf);

			// more accurate representation
			// float inc = 0;
			// if (load < 0) {
			// inc = load;
			// } else {
			// inc = load * getGearRatio() * sf;
			// }

			if (sf < prevSpeed) {
				ifactor.set(factor, 0.6f);
			} else {
				ifactor.set(factor, 0.85f);
			}

			rpm = 1000 + ifactor.get() + (load < 0 ? load * 1f : load * 15);

			// Gdx.app.log("EngineSoundSet", "gear=" + gear + ", rpm=" + rpm + ", throttle=" + player.getCarDescriptor().throttle
			// + ", throttling=" + player.isThrottling);// + ", speed="+ sf);

			rpm = MathUtils.clamp(rpm, 1000, 10000);

			prevSpeed = sf;
			return rpm;
		}

		return super.updateRpm(load);
	}

	private int updateGear () {
		float sf = player.carState.currSpeedFactor;

		if (sf > prevSpeed && gear < MaxGear) {
			switch (gear) {
			default:
				if (rpm > 9500) {
					shiftUp();
					return 1;
				}
				break;
			}

		} else if (sf < prevSpeed && gear > MinGear) {

			switch (gear) {
			default:
				if (rpm < 7000) {
					shiftDown();
					return -1;
				}
				break;
			case 2:
				if (rpm < 2000) {
					shiftDown();
					return -1;
				}
				break;
			}
		}

		return 0;
	}

	@Override
	public void shiftUp () {
		if (UseGears) {
			if (gear > 0 && gear < MaxGear) {
				gear++;

				float dist = 2000;
				if (rpm >= dist + 1000) {
					// rpm -= dist;
				} else {
					// rpm = 1000;
				}
			}

			if (gear == 0) {
				gear++;
				// rpm = 1000;
			}

			rpm = MathUtils.clamp(rpm, 1000, 10000);
		}

		gear = MathUtils.clamp(gear, MinGear, MaxGear);
	}

	@Override
	public void shiftDown () {
		if (UseGears) {
			// gear = 1;

			// if (gear == 1) {
			// gear--;
			// }

			if (gear > MinGear && gear <= MaxGear) {
				gear--;
				if (rpm != 1000) {
					// rpm += MathUtils.random(-500, 1100);
					// rpm += 3000;
				}

				rpm = MathUtils.clamp(rpm, 1000, 10000);
				// Gdx.app.log("EngineF40", "shift down");
			}
		}

		gear = MathUtils.clamp(gear, MinGear, MaxGear);
	}

	@Override
	public void reset () {
		super.reset();

		rpm = 1000;
		gear = 1;
		ifactor.reset(true);
		ispeed.reset(true);
		ivolume.reset(true);
	}
}
