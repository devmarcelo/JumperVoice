package scene;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;

import base.BaseScene;
import manager.SceneManager.SceneType;

public class SplashScene extends BaseScene {

	private Sprite splashScreen;

	@Override
	public void createScene() {
		splashScreen = new Sprite(0, 0, resourceManager.splashRegion, vbom) {
			@Override
			protected void preDraw(GLState pGLState, Camera pCamera) {
				super.preDraw(pGLState, pCamera);
				pGLState.enableDither();
			}
		};

		splashScreen.setScale(1.5f);
		splashScreen.setPosition(resourceManager.getWidthDisplay() / 2, resourceManager.getHeightDisplay() / 2);

		attachChild(splashScreen);
	}

	@Override
	public void onBackKeyPressed() {
		
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_SPLASH;
	}

	@Override
	public void disposeScene() {
		splashScreen.detachSelf();
		splashScreen.dispose();

		this.detachSelf();
		this.dispose();
	}

}
