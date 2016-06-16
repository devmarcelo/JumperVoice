package base;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.app.Activity;
import manager.ResourcesManager;
import manager.SceneManager.SceneType;

public abstract class BaseScene extends Scene {

	protected Engine engine;
	protected Activity activity;
	protected ResourcesManager resourceManager;
	protected VertexBufferObjectManager vbom;
	protected Camera camera;

	public BaseScene() {
		resourceManager = ResourcesManager.getInstance();
		engine          = resourceManager.engine;
		activity        = resourceManager.activity;
		vbom            = resourceManager.vbom;
		camera          = resourceManager.camera;

		createScene();
	}

	public abstract void createScene();

	public abstract void onBackKeyPressed();

	public abstract SceneType getSceneType();

	public abstract void disposeScene();

}
