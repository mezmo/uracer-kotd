
package com.bitfire.uracer.game.logic.replaying;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.uracer.configuration.UserProfile;
import com.bitfire.uracer.game.GameplaySettings;
import com.bitfire.utils.ItemsManager;

/** Maintains an updated list of the best <n> Replay objects for the specified track level */
public final class ReplayManager implements Disposable {

	public static final int MaxReplays = 2;
	private final String trackId;
	private final ItemsManager<Replay> replays = new ItemsManager<Replay>();
	private final Array<Replay> replayItems = replays.items;

	private Replay best, worst;
	private int ridx;

	public ReplayManager (UserProfile userProfile, String trackId) {
		this.trackId = trackId;
		for (int i = 0; i < MaxReplays; i++) {
			replays.add(new Replay(userProfile.userId));
		}

		best = null;
		worst = null;
		ridx = 0;
	}

	@Override
	public void dispose () {
		replays.dispose();
	}

	public Replay addReplay (Replay replay) {
		if (replay == null) {
			Gdx.app.log("ReplayManager", "Discarding null replay");
			return null;
		}

		if (replay.trackTimeSeconds < GameplaySettings.ReplayMinDurationSecs) {
			Gdx.app.log("ReplayManager", "Invalid lap detected, (" + replay.trackTimeSeconds + "sec < "
				+ GameplaySettings.ReplayMinDurationSecs + ")");
			return null;
		}

		if (!replay.isValid) {
			Gdx.app.log("ReplayManager", "The specified replay is not valid.");
			return null;
		}

		if (!replay.trackId.equals(trackId)) {
			Gdx.app.log("ReplayManager", "The specified replay belongs to another game track.");
			return null;
		}

		Replay added = null;

		// empty?
		if (ridx == 0) {
			added = replays.items.get(ridx++);
			added.copyData(replay);

			// update state
			best = added;
			worst = added;
		} else {

			if (replay == worst) {
				Gdx.app.log("!!!!", "!!");
			}

			if (replay.trackTimeSeconds >= worst.trackTimeSeconds) {
				Gdx.app.log("ReplayManager", "Discarded, worse than the worst! (" + replay.trackTimeSeconds + " >= "
					+ worst.trackTimeSeconds + ")");
				return null;
			}

			if (ridx == MaxReplays) {
				// full, overwrite worst
				worst.copyData(replay);
				added = worst;

				// recompute best/worst
				worst = replays.items.get(0);
				best = replays.items.get(0);
				for (int i = 1; i < MaxReplays; i++) {
					Replay r = replays.items.get(i);

					if (worst.trackTimeSeconds < r.trackTimeSeconds) {
						worst = r;
					}

					if (best.trackTimeSeconds > r.trackTimeSeconds) {
						best = r;
					}
				}
			} else {
				// add new
				added = replays.items.get(ridx++);
				added.copyData(replay);

				// compute best
				if (best.trackTimeSeconds > added.trackTimeSeconds) {
					best = added;
				}
			}
		}

		// dump replays
		// for (int i = 0; i < MaxReplays; i++) {
		// Replay r = replays.items.get(i);
		// if (r.isValid) {
		// Gdx.app.log("ReplayManager", "#" + i + ", seconds=" + r.trackTimeSeconds);
		// }
		// }

		if (added != null) {
			Gdx.app.log("ReplayManager", "added!");
		}
		return added;
	}

	public void reset () {
		ridx = 0;
		for (int i = 0; i < MaxReplays; i++) {
			replays.items.get(i).reset();
		}
	}

	public boolean hasReplays () {
		return replays.items.size > 0;
	}

	public boolean canClassify () {
		return (best != worst && best != null && worst != null && best.isValid && worst.isValid);
	}

	public Replay getBestReplay () {
		return best;
	}

	public Replay getWorstReplay () {
		return worst;
	}

	public Array<Replay> getReplays () {
		return replayItems;
	}
}
