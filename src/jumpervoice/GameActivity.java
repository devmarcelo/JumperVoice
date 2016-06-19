package jumpervoice;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.widget.Toast;
import manager.ResourcesManager;
import manager.SceneManager;
import manager.SceneManager.SceneType;
import service.VoiceService;

public class GameActivity extends BaseGameActivity {

	private Camera camera;
	private ResourcesManager resources;
	private LocalBroadcastManager localBroadcastManager;

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		//a utilização desta classe faz com que o jogo tenha velocidade
		//semelhante em diferentes dispositivos
		return new LimitedFPSEngine(pEngineOptions, 60);
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		final int CAMERA_WIDTH  = ResourcesManager.getInstance().getWidthDisplay();
		final int CAMERA_HEIGHT = ResourcesManager.getInstance().getHeightDisplay();

		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		EngineOptions engine = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engine.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
		engine.setWakeLockOptions(WakeLockOptions.SCREEN_ON);

		return engine;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException {
		startService(new Intent(this, VoiceService.class));
		ResourcesManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
		resources = ResourcesManager.getInstance();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException {
		SceneManager.getInstance().createSceneSplash(pOnCreateSceneCallback);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException {
		mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				mEngine.unregisterUpdateHandler(pTimerHandler);
				SceneManager.getInstance().createMenuScene();
				
			}
		}));

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {

		super.onCreate(pSavedInstanceState);

		localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("jogar");

		localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, VoiceService.class));
		localBroadcastManager.unregisterReceiver(broadcastReceiver);
		System.exit(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == event.KEYCODE_BACK) {
			SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
		} 
		return false;
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("jogar")) {
				SceneManager.getInstance().loadGameScene(mEngine);
			}
		}
	};

}
