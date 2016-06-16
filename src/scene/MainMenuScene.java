package scene;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;

import base.BaseScene;
import manager.SceneManager;
import manager.SceneManager.SceneType;

public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener {

	private MenuScene menuChildScene;

	private final int MENU_JOGAR = 0;

	private void createMenuChildScene() {
		menuChildScene = new MenuScene(camera);
		menuChildScene.setPosition(resourceManager.getWidthDisplay(), resourceManager.getHeightDisplay());

		final IMenuItem jogarMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_JOGAR, resourceManager.jogarRegion, vbom), 1.2f, 1);

		menuChildScene.addMenuItem(jogarMenuItem);

		menuChildScene.buildAnimations();
		menuChildScene.setBackgroundEnabled(false);

		jogarMenuItem.setPosition(jogarMenuItem.getX() - resourceManager.getWidthDisplay(), jogarMenuItem.getY() - resourceManager.getHeightDisplay());

		menuChildScene.setOnMenuItemClickListener(this);

		setChildScene(menuChildScene);
	}

	@Override
	public void createScene() {
		createBackground();
		createMenuChildScene();
	}

	@Override
	public void onBackKeyPressed() {
		System.exit(0);
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_MENU;
	}

	@Override
	public void disposeScene() {
		
	}

	private void createBackground() {
		attachChild(new Sprite(resourceManager.getWidthDisplay() / 2, resourceManager.getHeightDisplay() / 2, resourceManager.menuBackgroundRegion, vbom) {
			@Override
			protected void preDraw(GLState pGLState, Camera pCamera) {
				super.preDraw(pGLState, pCamera);
				pGLState.enableDither();
			}
		});
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
		switch (pMenuItem.getID()) {
		case MENU_JOGAR:
			SceneManager.getInstance().loadGameScene(engine);
			return true;
		default:
			return false;
		}
	}

}
