package net.mostlyoriginal.game.system;

import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Angle;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.camera.Camera;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.map.MapSolid;
import net.mostlyoriginal.api.component.map.MapWallSensor;
import net.mostlyoriginal.api.component.physics.*;
import net.mostlyoriginal.api.component.script.Schedule;
import net.mostlyoriginal.api.utils.SafeEntityReference;
import net.mostlyoriginal.api.utils.TagEntityReference;
import net.mostlyoriginal.game.MainScreen;
import net.mostlyoriginal.game.component.Slumberer;
import net.mostlyoriginal.game.component.control.PlayerControlled;
import net.mostlyoriginal.game.component.interact.Pluckable;

/**
 * Game specific entity factory.
 *
 * @author Daan van Yperen
 */
@Wire
public class EntityFactorySystem extends VoidEntitySystem {

    private TagManager tagManager;
    private AssetSystem assetSystem;

    public Entity createEntity(String entity, int cx, int cy, MapProperties properties) {
        switch (entity) {
            case "player":
                return createPlayer(cx, cy);
            case "slumberer":
                return createSlumberer(cx, cy);
            case "turnip":
                return defaultEntity(cx, cy, "turnip-stuck")
                        .addComponent(new Pluckable("turnip-idle"))
                        .addComponent(new Frozen());
            case "chicklet":
                return defaultEntity(cx, cy, "chicklet-stuck")
                        .addComponent(new Pluckable("chicklet-idle"))
                        .addComponent(new Frozen());
            /** @todo Add your entities here */
        }
        return null;
    }

    private Entity createSlumberer(int cx, int cy) {
        Entity slumberer =
                defaultEntity(cx, cy, "slumberer-idle").addComponent(new Slumberer());
        slumberer.getComponent(Anim.class).layer = -2;

        Anim eyeAnim     = new Anim("slumberer-eye", -3);
        eyeAnim.loop     = false;
        Inbetween inbetween = new Inbetween(new SafeEntityReference(slumberer), new TagEntityReference(tagManager, "player"), 0.05f);
        inbetween.ax = 10;
        inbetween.ay = 26;
        inbetween.bx = 10;
        inbetween.by = 10;
        inbetween.maxDistance = 2f;
        Entity eye = world.createEntity()
                .addComponent(new Pos())
                .addComponent(eyeAnim)
                .addComponent(inbetween);
        eye.addToWorld();
        tagManager.register("slumberer-eye", eye);

        Anim eyelidAnim = new Anim("slumberer-eyelid", -1);
        eyelidAnim.loop = false;
        Entity eyelid = world.createEntity()
                .addComponent(new Pos())
                .addComponent(eyelidAnim)
                .addComponent(new Attached(new SafeEntityReference(slumberer), 12, 28));
        eyelid.addToWorld();
        tagManager.register("slumberer-eyelid", eyelid);

        return slumberer;
    }

    public Entity createSweat(int x, int y, String animId) {

        final Physics physics = new Physics();
        physics.vx = MathUtils.random(-90, 90)*1.5f;
        physics.vy = MathUtils.random(50, 110)*1.5f;
        physics.friction = 0.1f;

        final TextureRegion frame = assetSystem.get(animId).getKeyFrame(0);

        return basicCenteredParticle(x, y, animId, 1, 1)
                .addComponent(new Schedule().wait(1f).deleteFromWorld())
                .addComponent(physics)
                .addComponent(new Bounds(frame))
                .addComponent(new Gravity());
    }

    /**
     * Spawns a particle, animation centered on x,y.
     *
     * @param x
     * @param y
     * @param animId
     * @return
     */
    private Entity basicCenteredParticle(int x, int y, String animId, float scale, float speed) {
        Anim anim = new Anim(animId);
        anim.scale=scale;
        anim.speed=speed;
        anim.color.a= 0.9f;

        TextureRegion frame = assetSystem.get(animId).getKeyFrame(0);

        return world.createEntity()
                .addComponent(new Pos(x - ((frame.getRegionWidth() * anim.scale) / 2), y - (frame.getRegionHeight() * anim.scale) / 2))
                .addComponent(anim);
    }


    private Entity createPlayer(int cx, int cy) {
        Entity player =
                defaultEntity(cx, cy, "player-idle")
                        .addComponent(new PlayerControlled())
                        .addComponent(new MapWallSensor());

        tagManager.register("player", player);

        // now create a drone that will swerve towards the player which contains the camera. this will create a smooth moving camera.
        world.createEntity()
                .addComponent(new Pos(0, 0))
                .addComponent(createCameraBounds())
                .addComponent(new Physics())
                .addComponent(new Homing(new SafeEntityReference(player)))
                .addComponent(new Camera())
                .addComponent(new Clamped(0, 0, 20 * 16, 15 * 16))
                .addToWorld();

        return player;
    }

    private Bounds createCameraBounds() {
        // convert viewport into bounds.
        return new Bounds(
                (-Gdx.graphics.getWidth() / 2) / MainScreen.CAMERA_ZOOM_FACTOR,
                (-Gdx.graphics.getHeight() / 2) / MainScreen.CAMERA_ZOOM_FACTOR,
                (Gdx.graphics.getWidth() / 2) / MainScreen.CAMERA_ZOOM_FACTOR,
                (Gdx.graphics.getHeight() / 2) / MainScreen.CAMERA_ZOOM_FACTOR
        );
    }

    private Entity defaultEntity(int cx, int cy, String startingAnim) {
        return world.createEntity()
                .addComponent(new Pos(cx, cy))
                .addComponent(new Angle())
                .addComponent(new Bounds(0, 0, 25, 16))
                .addComponent(new Anim(startingAnim))
                .addComponent(new MapSolid())
                .addComponent(new Physics())
                .addComponent(new Gravity());
    }

    @Override
    protected void processSystem() {
    }
}