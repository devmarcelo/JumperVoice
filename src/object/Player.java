package object;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import manager.ResourcesManager;

public abstract class Player extends AnimatedSprite {

	private Body body;
	private boolean canRun = false;
	private int footContacts = 0;

	final long[] PLAYER_ANIMATE = new long[] {100, 100, 100, 100, 100};

	public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld) {
		super(pX, pY, ResourcesManager.getInstance().playerRegion, vbo);

		//setPosition(pX, pY);

		createPhysics(camera, physicsWorld);
		camera.setChaseEntity(this);
	}

	private void createPhysics(final Camera camera, PhysicsWorld physicsWorld) {
		body = PhysicsFactory.createBoxBody(physicsWorld, Player.this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));

		body.setUserData(this.getUserData());
		body.setFixedRotation(true);

		physicsWorld.registerPhysicsConnector(new PhysicsConnector(Player.this, body, true, false) {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				super.onUpdate(pSecondsElapsed);
				camera.onUpdate(0.1f);

				if (getY() <= 0) {
					onDie();
				}

				if (canRun) {
					body.setLinearVelocity(new Vector2(5, body.getLinearVelocity().y));
				}
			}
		});
	}

	public void setRunning() {
		canRun = true;

		animate(PLAYER_ANIMATE, 5, 9, true);
	}

	public void jump() {
		/*if (footContacts < 1) {
			return;
		}*/

		animate(PLAYER_ANIMATE, 0, 4, true);

		//body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, 12));
		body.setAngularVelocity((float) (-Math.PI));
		//body.setTransform(0, 200f, (float) (-Math.PI));
		animate(PLAYER_ANIMATE, 5, 9, true);
	}

	public void increaseFootContacts() {
		footContacts++;
	}

	public void decreaseFootContacts() {
		footContacts--;
	}

	public Body getBody() {
		return body;
	}
	
	public abstract void onDie();

}
