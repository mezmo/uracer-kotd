package com.bitfire.uracer.game.logic;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.uracer.Config;
import com.bitfire.uracer.Director;
import com.bitfire.uracer.Input;
import com.bitfire.uracer.Physics;
import com.bitfire.uracer.effects.RadialBlur;
import com.bitfire.uracer.effects.postprocessing.PostProcessor;
import com.bitfire.uracer.entities.EntityManager;
import com.bitfire.uracer.entities.vehicles.Car;
import com.bitfire.uracer.game.Game;
import com.bitfire.uracer.tiled.Level;
import com.bitfire.uracer.utils.Convert;

public class GameLogic
{
	private Game game;
	private IGameLogicListener listener;
	private static TweenManager tweener;

	// events - onTileChanged
	private Vector2 carTileAt = new Vector2();
	private Vector2 lastCarTileAt = new Vector2();

	// lap and entities
	private Level level;
	private Car player = null;

	// effects
	private RadialBlur rb;

	public GameLogic( Game game )
	{
		Tween.setPoolEnabled( true );
		GameLogic.tweener = new TweenManager();

		this.game = game;
		this.level = game.getLevel();
		this.player = level.getPlayer();
		this.listener = new GameLogicListener( this );

		// effects
		if( Config.EnablePostProcessingFx )
		{
			rb = new RadialBlur();
			rb.setEnabled( true );
			PostProcessor.init( Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
			// PostProcessor.init( 512, 512 );
			PostProcessor.setEffect( rb );
		}

		DriftInfo.init(this);
		reset();
	}

	public void dispose()
	{
	}

	public void tick()
	{
		EntityManager.raiseOnTick();

		if( player != null )
		{
			if( Input.isOn( Keys.R ) )
			{
				game.restart();
			} else if( Input.isOn( Keys.T ) )
			{
				game.reset();
			}

			// onTileChanged
			lastCarTileAt.set( carTileAt );
			carTileAt.set( Convert.pxToTile( player.pos().x, player.pos().y ) );
			if( (lastCarTileAt.x != carTileAt.x) || (lastCarTileAt.y != carTileAt.y) )
			{
				listener.onTileChanged( carTileAt );
			}

			if( Config.EnablePostProcessingFx )
			{
				rb.dampStrength( 0.8f, Physics.dt );
				rb.setOrigin( Director.screenPosFor( player.getBody() ) );
			}

			// update DriftInfo, raise onBeginDrift / onEndDrift
			DriftInfo drift = DriftInfo.get();
			drift.update( player );
		}
	}

	public void reset()
	{
		restart();
		listener.onReset();
	}

	public void restart()
	{
		// causes an onTileChanged event to be raised
		lastCarTileAt.set( -1, -1 );
		carTileAt.set( lastCarTileAt );

		listener.onRestart();
	}

	public void render()
	{
		tweener.update();
	}

	public LapInfo getLapInfo()
	{
		return listener.onGetLapInfo();
	}

	public static TweenManager getTweener()
	{
		return tweener;
	}

	public Game getGame()
	{
		return game;
	}

	public IGameLogicListener getListener()
	{
		return listener;
	}
}
