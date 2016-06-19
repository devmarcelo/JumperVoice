package scene;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.adt.align.HorizontalAlign;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import base.BaseScene;
import manager.ResourcesManager;
import manager.SceneManager;
import manager.SceneManager.SceneType;

public class GameScene extends BaseScene implements IAccelerationListener, IOnSceneTouchListener {

	private HUD gameHUD;
	private Text scoreText;
	private PhysicsWorld physicsWorld;
	private AnimatedSprite playerSprite;
	private AnimatedSprite starSprite;
	private Body body;
	private Body bodyStar;
	
	private Text gameOverText;
	private boolean gameOverDisplayed = false;

	private int score = 0;

	private AutoParallaxBackground mAutoParallaxBackground;
	private LocalBroadcastManager localBroadcastManager;

	private float gravityX;
	private float gravityY;

	private void createHUD() {
		gameHUD = new HUD();

		scoreText = new Text(20, resourceManager.getHeightDisplay() - 60, resourceManager.font, "Pontuação: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		scoreText.setAnchorCenter(0, 0);
		scoreText.setText("Pontuação: 0");
		gameHUD.attachChild(scoreText);

		camera.setHUD(gameHUD);
	}

	@Override
	public void createScene() {
		createBackground();
		createHUD();
		createPhysics();
		loadGame();
		createLocalBroadcastManager();
		createGameOverText();
		setOnSceneTouchListener(this);
	}

	@Override
	public void onBackKeyPressed() {
		SceneManager.getInstance().loadMenuScene(engine);
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene() {
		camera.setHUD(null);
		camera.setCenter(resourceManager.getWidthDisplay()  / 2, resourceManager.getHeightDisplay() / 2);
		camera.setChaseEntity(null);
	}

	private void createBackground() {
		mAutoParallaxBackground = new AutoParallaxBackground(0.1f, 0.8f, 1.5f, 25);

		Sprite layerBack  = new Sprite(resourceManager.getWidthDisplay() - 420, resourceManager.getHeightDisplay() - 240, resourceManager.mParallaxLayerBack, vbom);
		Sprite layerFront = new Sprite(resourceManager.getWidthDisplay() - 420, resourceManager.getHeightDisplay() / 2 - 240, resourceManager.mParallaxLayerFront, vbom);

		mAutoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, layerBack));
		mAutoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, layerFront));

		setBackground(mAutoParallaxBackground);
	}

	private void addToScore(int points) {
		score += points;
		scoreText.setText("Pontuação: "+score);
	}

	private void createPhysics() {
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -SensorManager.GRAVITY_EARTH * 1.8f), false, 8, 3);
		registerUpdateHandler(physicsWorld);
	}

	public void loadGame() {
		final float playerX = (resourceManager.getWidthDisplay() - resourceManager.playerRegion.getWidth()) / 2 - 300;
		final float playerY = resourceManager.getHeightDisplay() - resourceManager.playerRegion.getHeight() - 200;

		final IShape bottom = new Rectangle(playerX, playerY - 200, resourceManager.getWidthDisplay(), 50, vbom);
		bottom.setVisible(false);
		PhysicsFactory.createBoxBody(physicsWorld, bottom, BodyType.StaticBody, PhysicsFactory.createFixtureDef(0, 0, 0.1f));

		attachChild(bottom);

		playerSprite = new AnimatedSprite(playerX, playerY, resourceManager.playerRegion, vbom);
		
		body = PhysicsFactory.createBoxBody(physicsWorld, playerSprite, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f));
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(playerSprite, body, true, true));

		playerSprite.setScale(0.7f);
		setPlayerRunning();
		playerSprite.setUserData(body);
		registerTouchArea(playerSprite);
		attachChild(playerSprite);

		starSprite = new AnimatedSprite(resourceManager.getWidthDisplay(), resourceManager.getHeightDisplay() / 2 - 150, resourceManager.starRegion, vbom) {

			@Override
			protected void onManagedUpdate(float pSecondsElapsed) {
				super.onManagedUpdate(pSecondsElapsed);

				if (this.mX < 0) {
					addToScore(5);
					setIgnoreUpdate(true);
				}

				if (collidesWith(playerSprite)) {
					if (!gameOverDisplayed) {
						displayGameOverText();
						engine.stop();
					}
				}
			}
			
		};

		bodyStar = PhysicsFactory.createBoxBody(physicsWorld, starSprite, BodyType.KinematicBody, PhysicsFactory.createFixtureDef(0, 0, 0));
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(starSprite, bodyStar, true, false));

		starSprite.setScale(2f);
		animateStar();
		starSprite.setUserData(bodyStar);
		attachChild(starSprite);
		setMovingStar();
	}

	private void setPlayerRunning() {
		final long[] PLAYER_ANIMATE = new long[] {100, 100, 100, 100, 100};

		playerSprite.animate(PLAYER_ANIMATE, 5, 9, true);

		body.setLinearVelocity(new Vector2(0, body.getLinearVelocity().y));
	}

	private void setPlayerJumping() {
		final long[] PLAYER_ANIMATE = new long[] {100, 100, 100, 100, 100};

		playerSprite.animate(PLAYER_ANIMATE, 0, 4, false);
		
		final Body playerBody = (Body)playerSprite.getUserData();

		final Vector2 velocity = Vector2Pool.obtain(0, 30.4f * -50);
		
		playerBody.setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);
		setPlayerRunning();
	}

	private void animateStar() {
		final long[] STAR_ANIMATE = new long[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100};

		starSprite.animate(STAR_ANIMATE, 0, 11, true);

		bodyStar.setLinearVelocity(bodyStar.getLinearVelocity().x * 3, 0);
	}

	private void setMovingStar() {
		final Body starBody = (Body)starSprite.getUserData();
		final Vector2 velocity = Vector2Pool.obtain(0.5f * -15, 0);

		starBody.setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown()) {
			setPlayerJumping();
			return true;
		}

		return false;
	}

	private void createLocalBroadcastManager() {
		localBroadcastManager = LocalBroadcastManager.getInstance(activity.getApplicationContext());

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("pula");

		localBroadcastManager.registerReceiver(getBroadcastReceiver(), intentFilter);
	}

	private BroadcastReceiver getBroadcastReceiver() {
		if (broadcastReceiver == null) {
			broadcastReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals("pula")) {
						setPlayerJumping();
					}
				}
			};
		}
		return broadcastReceiver;
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("pula")) {
				setPlayerJumping();
			}
		}
	};

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		gravityX = pAccelerationData.getX();
		gravityY = pAccelerationData.getY() + 4.0f;

		final Vector2 gravity = Vector2Pool.obtain(gravityX, gravityY);
		physicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	private void createGameOverText() {
		gameOverText = new Text(0, 0, resourceManager.font, "Fim do Jogo!", vbom);
	}

	private void displayGameOverText() {
		camera.setChaseEntity(null);
		gameOverText.setPosition(camera.getCenterX(), camera.getCenterY());
		attachChild(gameOverText);
		gameOverDisplayed = true;
	}

}
