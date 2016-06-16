package manager;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

import android.graphics.Color;
import jumpervoice.GameActivity;

public class ResourcesManager {

	private static final ResourcesManager INSTANCE = new ResourcesManager();

	public Engine engine;
	public GameActivity activity;
	public Camera camera;
	public VertexBufferObjectManager vbom;
	public Font font;

	public ITextureRegion splashRegion;
	public ITextureRegion menuBackgroundRegion;
	public ITextureRegion jogarRegion;

	public ITiledTextureRegion playerRegion;
	public ITiledTextureRegion starRegion;

	private BitmapTextureAtlas splashTextureAtlas;
	private BuildableBitmapTextureAtlas menuTextureAtlas;
	public BuildableBitmapTextureAtlas gameTextureAtlas;

	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	public ITextureRegion mParallaxLayerBack;
	public ITextureRegion mParallaxLayerFront;

	private static int widthDisplay  = 1500;
	private static int heightDisplay = 900;

	public void loadMenuResources() {
		loadMenuGraphics();
		loadMenuFonts();
	}

	public void loadGameResources() {
		loadGameGraphics();
	}

	private void loadMenuGraphics() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		menuTextureAtlas     = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
		menuBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "menubackground.png");

		jogarRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "jogar.png");

		try {
			menuTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
			menuTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
	}

	private void loadGameGraphics() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(activity.getTextureManager(), 2176, 1024, TextureOptions.REPEATING_NEAREST_PREMULTIPLYALPHA);
		mParallaxLayerFront            = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, activity, "backgroundFront.png", 0, 0);
		mParallaxLayerBack             = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, activity, "backgroundSky.png", 0, 450);
		mAutoParallaxBackgroundTexture.load();

		gameTextureAtlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 4096, 2048, TextureOptions.BILINEAR);
		playerRegion     = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "spritesheetvolt_run.png", 5, 2);
		starRegion       = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "estrelaNinja.png", 3, 4);

		try {
			gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(1, 0, 0));
			gameTextureAtlas.load();
		} catch (final TextureAtlasBuilderException e) {
			Debug.e(e);
		}
	}

	public void loadSplashScreen() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		splashTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
		splashRegion       = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, activity, "splashscreen.png", 0, 0);

		splashTextureAtlas.load();
	}

	public void unloadSplashScreen() {
		splashTextureAtlas.unload();
		splashRegion = null;
	}

	public static void prepareManager(Engine engine, GameActivity activity, Camera camera, VertexBufferObjectManager vbom) {
		getInstance().engine   = engine;
		getInstance().activity = activity;
		getInstance().camera   = camera;
		getInstance().vbom     = vbom;
	}

	public static ResourcesManager getInstance() {
		return INSTANCE;
	}

	private void loadMenuFonts() {
		FontFactory.setAssetBasePath("font/");

		final ITexture mainFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		font = FontFactory.createStrokeFromAsset(activity.getFontManager(), mainFontTexture, activity.getAssets(), "ChocolateBarDemo.otf", 50, true, Color.RED, 2, Color.BLACK);
		font.load();
	}

	public void unloadMenuTextures() {
		menuTextureAtlas.unload();
	}

	public void loadMenuTextures() {
		menuTextureAtlas.load();
	}

	public int getWidthDisplay() {
		return widthDisplay;
	}

	public int getHeightDisplay() {
		return heightDisplay;
	}

	public void unloadGameTextures() {
		
	}

}
