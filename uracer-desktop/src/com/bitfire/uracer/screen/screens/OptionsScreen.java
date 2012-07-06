package com.bitfire.uracer.screen.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bitfire.postprocessing.filters.RadialBlur;
import com.bitfire.uracer.Input;
import com.bitfire.uracer.ScalingStrategy;
import com.bitfire.uracer.URacer;
import com.bitfire.uracer.configuration.Gameplay;
import com.bitfire.uracer.configuration.Gameplay.TimeDilateInputMode;
import com.bitfire.uracer.configuration.UserPreferences;
import com.bitfire.uracer.configuration.UserPreferences.Preference;
import com.bitfire.uracer.resources.Art;
import com.bitfire.uracer.screen.Screen;
import com.bitfire.uracer.screen.ScreenFactory.ScreenType;
import com.bitfire.uracer.utils.UIUtils;

public class OptionsScreen extends Screen {

	private Stage ui;
	private Input input;
	private Table container;
	private CheckBox ppVignetting, ppBloom, ppZoom, ppZoomBlur;
	private SelectBox timeInputModeSel, ppZoomBlurQ;

	@Override
	public void init( ScalingStrategy scalingStrategy ) {
		input = URacer.Game.getInputSystem();
		setupUI();
	}

	@Override
	public void enable() {
		Gdx.input.setInputProcessor( ui );
	}

	private void setupUI() {
		ui = new Stage();

		// background
		Image bg = new Image( Art.scrBackground );
		bg.setFillParent( true );
		ui.addActor( bg );

		container = new Table();
		// container.debug();
		container.setFillParent( true );
		container.defaults().pad( 2 );
		ui.addActor( container );

		// time dilation input mode
		{
			TimeDilateInputMode im = TimeDilateInputMode.valueOf( UserPreferences.string( Preference.TimeDilateInputMode ) );
			Label timeInputModeLabel = new Label( "Time dilation input mode", Art.scrSkin );
			timeInputModeSel = new SelectBox( new String[] { "Touch to toggle", "Touch and release" }, Art.scrSkin );
			timeInputModeSel.setSelection( im.ordinal() );
			timeInputModeSel.addListener( new ChangeListener() {
				@Override
				public void changed( ChangeEvent event, Actor actor ) {
					int index = timeInputModeSel.getSelectionIndex();
					UserPreferences.string( Preference.TimeDilateInputMode, Gameplay.TimeDilateInputMode.values()[index].toString() );
					UserPreferences.save();
				}
			} );

			container.add( timeInputModeLabel ).width( 200 ).pad( 5 );
			container.add( timeInputModeSel );
		}

		// post-processing
		{
			ppVignetting = UIUtils.newCheckBox( "Vignetting and gradient mapping", UserPreferences.bool( Preference.Vignetting ) );
			ppBloom = UIUtils.newCheckBox( "Bloom", UserPreferences.bool( Preference.Bloom ) );
			ppZoom = UIUtils.newCheckBox( "Zoom", UserPreferences.bool( Preference.Zoom ) );
			ppZoomBlur = UIUtils.newCheckBox( "Zoom blur", UserPreferences.bool( Preference.ZoomRadialBlur ) );

			ppVignetting.addListener( new ClickListener() {
				@Override
				public void clicked( ActorEvent event, float x, float y ) {
					UserPreferences.bool( Preference.Vignetting, ppVignetting.isChecked() );
					UserPreferences.save();
				}
			} );

			ppBloom.addListener( new ClickListener() {
				@Override
				public void clicked( ActorEvent event, float x, float y ) {
					UserPreferences.bool( Preference.Bloom, ppBloom.isChecked() );
					UserPreferences.save();
				}
			} );

			ppZoom.addListener( new ClickListener() {
				@Override
				public void clicked( ActorEvent event, float x, float y ) {
					UserPreferences.bool( Preference.Zoom, ppZoom.isChecked() );
					UserPreferences.save();
				}
			} );

			ppZoomBlur.addListener( new ClickListener() {
				@Override
				public void clicked( ActorEvent event, float x, float y ) {
					UserPreferences.bool( Preference.ZoomRadialBlur, ppZoomBlur.isChecked() );
					UserPreferences.save();
				}
			} );

			RadialBlur.Quality rbq = RadialBlur.Quality.valueOf( UserPreferences.string( Preference.ZoomRadialBlurQuality ) );
			ppZoomBlurQ = new SelectBox( new String[] { "Very high", "High", "Normal", "Medium", "Low" }, Art.scrSkin );
			ppZoomBlurQ.setSelection( rbq.ordinal() );
			ppZoomBlurQ.addListener( new ChangeListener() {
				@Override
				public void changed( ChangeEvent event, Actor actor ) {
					int index = ppZoomBlurQ.getSelectionIndex();
					UserPreferences.string( Preference.ZoomRadialBlurQuality, RadialBlur.Quality.values()[index].toString() );
					UserPreferences.save();
				}
			} );

			final CheckBox postProcessingCb = UIUtils.newCheckBox( "Enable post-processing effects", UserPreferences.bool( Preference.PostProcessing ) );
			postProcessingCb.addListener( new ClickListener() {
				@Override
				public void clicked( ActorEvent event, float x, float y ) {
					if( !postProcessingCb.isChecked() ) {
						// disable all post-processing
						ppVignetting.setChecked( false );
						ppBloom.setChecked( false );
						ppZoom.setChecked( false );
						ppZoomBlur.setChecked( false );
					}

					UserPreferences.bool( Preference.PostProcessing, postProcessingCb.isChecked() );
					UserPreferences.bool( Preference.Vignetting, ppVignetting.isChecked() );
					UserPreferences.bool( Preference.Bloom, ppBloom.isChecked() );
					UserPreferences.bool( Preference.Zoom, ppZoom.isChecked() );
					UserPreferences.bool( Preference.ZoomRadialBlur, ppZoomBlur.isChecked() );
					UserPreferences.save();
				}
			} );

			// lay out things
			container.row().colspan( 2 );
			container.add( postProcessingCb );

			container.row().colspan( 2 );
			container.add( ppVignetting );

			container.row().colspan( 2 );
			container.add( ppBloom );

			container.row().colspan( 2 );
			container.add( ppZoom );
			{
				container.row().colspan( 2 );
				container.add( ppZoomBlur );

				container.row();
				container.add( new Label( "Zoom blur quality", Art.scrSkin ) );
				container.add( ppZoomBlurQ );
			}
		}
	}

	@Override
	public void dispose() {
		ui.dispose();
		disable();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void tick() {
		if( input.isPressed( Keys.Q ) || input.isPressed( Keys.BACK ) || input.isPressed( Keys.ESCAPE ) ) {
			URacer.Game.show( ScreenType.MainScreen );
			// URacer.Game.quit();
		}
	}

	@Override
	public void tickCompleted() {
	}

	@Override
	public void render( FrameBuffer dest ) {
		boolean hasDest = (dest != null);
		if( hasDest ) {
			dest.begin();
		}

		Gdx.gl.glClearColor( 0, 0, 0, 0 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		ui.draw();
		// Table.drawDebug( ui );

		if( hasDest ) {
			dest.end();
		}
	}
}
