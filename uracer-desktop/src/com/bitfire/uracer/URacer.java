package com.bitfire.uracer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.bitfire.uracer.screen.CarTestScreen;
import com.bitfire.uracer.screen.Screen;

public class URacer implements ApplicationListener
{
	private Screen screen;
	private Input input = new Input();
	private boolean running = false;

	private float temporalAliasing = 0;
	private float timeAccumSecs = 0;
	private float oneOnOneBillion = 0;

	// stats
	private static float graphicsTime = 0;
	private static float physicsTime = 0;

	@Override
	public void create()
	{
		Art.load();
		input.releaseAllKeys();

		Gdx.input.setInputProcessor( input );
		Gdx.graphics.setVSync( true );

		running = true;
		oneOnOneBillion = 1.0f / 1000000000.0f;
		temporalAliasing = 0;

		setScreen( new CarTestScreen() );
	}

	@Override
	public void render()
	{
		float deltaTime = Gdx.graphics.getDeltaTime();

		// avoid spiral of death
		if( deltaTime > 0.25f )
			deltaTime = 0.25f;

		long startTime = System.nanoTime();
		{
			timeAccumSecs += deltaTime * Config.PhysicsTimeMultiplier;
			while( timeAccumSecs > Physics.dt )
			{
				input.tick();
				screen.tick( /* input */);

				timeAccumSecs -= Physics.dt;
			}
		}
		physicsTime = (System.nanoTime() - startTime) * oneOnOneBillion;

		// compute the temporal aliasing factor, entities will render
		// themselves accordingly to this to avoid flickering and
		// permitting slow-motion effects without artifacts.
		// (this imply accepting a one-frame-behind behavior)
		temporalAliasing = timeAccumSecs * Config.PhysicsTimestepHz;

		screen.beforeRender( temporalAliasing );

		startTime = System.nanoTime();
		{
			screen.render( temporalAliasing );
		}
		graphicsTime = (System.nanoTime() - startTime) * oneOnOneBillion;
	}

	@Override
	public void resize( int width, int height )
	{
	}

	@Override
	public void pause()
	{
		running = false;
	}

	@Override
	public void resume()
	{
		running = true;
	}

	@Override
	public void dispose()
	{
		Physics.dispose();
	}

	public void setScreen( Screen newScreen )
	{
		if( screen != null )
		{
			screen.removed();
		}

		screen = newScreen;

		if( screen != null )
		{
			screen.init( this );
		}
	}

	public boolean isRunning()
	{
		return running;
	}

	public static float getRenderTime()
	{
		return graphicsTime;
	}

	public static float getPhysicsTime()
	{
		return physicsTime;
	}
}
