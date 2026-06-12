package dev.falsegamemaster.murderplugin.prop.doors;

import dev.falsegamemaster.murderplugin.MurderPlugin;
import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.prop.animation.IPropAnimationFrame;
import dev.falsegamemaster.propengine.prop.animation.PropAnimation;
import dev.falsegamemaster.propengine.prop.part.EntityPropPart;
import dev.falsegamemaster.propengine.prop.part.PropPart;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.Transform;
import dev.falsegamemaster.propengine.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

public class SWUtilityDoorProp extends Prop {

    private boolean isOpened = false;
    private long nextInteractionTimeMillis = 0L, interactionCooldownMillis = 0L;

    public SWUtilityDoorProp(Prop.Registrar registrar, String uniqueFriendlyName) {
        super(registrar, uniqueFriendlyName);
        registerAnimation(new SWUtilityDoorProp.OpenAnimation(this, false));
        registerAnimation(new SWUtilityDoorProp.CloseAnimation(this, false));
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return SWUtilityDoorProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.DOOR;
    }

    @Override
    public String getLiteral() {
        return "sw_utility_door";
    }

    @Override
    public String getDisplayName() {
        return "Star Wars - Utility Door";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(SpawnRequest request) {
        return List.of(FramePart::new, DoorPart::new, FrontButtonPart::new, RearButtonPart::new);
    }

    @Override
    public void load() {
        super.load();
        loadData();
    }

    private void loadData() {
        if (data == null) return;
        interactionCooldownMillis = data.has("cooldown") ? data.get("cooldown").getAsLong() : 0L;
    }

    public boolean canInteract() {
        return System.currentTimeMillis() >= nextInteractionTimeMillis;
    }

    public void toggle() {
        if (!canInteract()) return;
        isOpened = !isOpened;
        if (isOpened) playAnimation("open");
        else playAnimation("close");
        nextInteractionTimeMillis = System.currentTimeMillis() + interactionCooldownMillis;
    }

    public static class FramePart extends EntityPropPart<SWUtilityDoorProp, ItemDisplay> {
        public FramePart(Prop prop, int sequentialID) {
            super(SWUtilityDoorProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "base";
        }

        @Override
        public String getLiteral() {
            return "frame";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, SpawnRequest request) {
            entity.setItemStack(Util.Item.getItemWithCustomModelData(Material.PINK_SHULKER_BOX, 7));
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class DoorPart extends EntityPropPart<SWUtilityDoorProp, ItemDisplay> {
        public DoorPart(Prop prop, int sequentialID) {
            super(SWUtilityDoorProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "door";
        }

        @Override
        public String getLiteral() {
            return "main";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, SpawnRequest request) {
            entity.setItemStack(Util.Item.getItemWithCustomModelData(Material.PINK_SHULKER_BOX, 77));
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static abstract class ButtonPart extends EntityPropPart<SWUtilityDoorProp, Interaction> {
        private final Vector3f offset;

        public ButtonPart(Prop prop, int sequentialID, Vector3f offset) {
            super(SWUtilityDoorProp.class, prop, sequentialID);
            this.offset = offset;
        }

        @Override
        public String getCategory() {
            return "button";
        }

        @Override
        public String getLiteral() {
            return "main";
        }

        @Override
        public Class<Interaction> getInternalClass() {
            return Interaction.class;
        }

        @Override
        public void prepareInternal(Interaction entity, SpawnRequest request) {
            entity.setPersistent(true);
            entity.setResponsive(true);
            entity.setInteractionWidth(0.4f);
            entity.setInteractionHeight(1.0f);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 0.0f, -0.5f)).build();
            Vector3f localButtonPos = propTransform.transformPoint(offset);
            entity.teleport(location.clone().add(localButtonPos.x, localButtonPos.y, localButtonPos.z));
        }

        protected void onInteract(PlayerInteractEntityEvent event) {
            LifecycleState state = prop.getLifecycleState();
            if (state != LifecycleState.LOADED || !(event.getRightClicked() instanceof Interaction interaction)) return;
            Set<String> tags = interaction.getScoreboardTags();
            if (!tags.contains(prop.uniqueName) || !tags.contains(getQualifiedName())) return;
            prop.toggle();
            event.setCancelled(true);
        }

//        protected void onInteract(PlayerInteractEntityEvent event) {
//            if (!(event.getRightClicked() instanceof Interaction interaction)) return;
//            Set<String> tags = interaction.getScoreboardTags();
//            if (!tags.contains(prop.uniqueName) || !tags.contains(getQualifiedName())) return;
//            PropSpawnRequest dummyRequest = PropPersistentData.read(interaction);
//            if (dummyRequest == null) return;
//            Prop prop = Prop.getProps().get(dummyRequest.key());
//            LifecycleState state = prop.getLifecycleState();
//            if (state != LifecycleState.LOADED || !(prop instanceof SWUtilityDoorProp doorProp)) return;
//            doorProp.toggle();
//            event.setCancelled(true);
//        }
    }

    public static class FrontButtonPart extends ButtonPart implements Listener {
        public FrontButtonPart(Prop prop, int sequentialID) {
            super(prop, sequentialID, new Vector3f(0.8f, 0.0f, -0.2f));
            Bukkit.getPluginManager().registerEvents(this, MurderPlugin.getInstance());
        }

        @Override
        public String getLiteral() {
            return "front";
        }

        @EventHandler
        @Override
        protected void onInteract(PlayerInteractEntityEvent event) {
            super.onInteract(event);
        }
    }

    public static class RearButtonPart extends ButtonPart implements Listener {
        public RearButtonPart(Prop prop, int sequentialID) {
            super(prop, sequentialID, new Vector3f(-1.8f, 0.0f, -0.8f));
            Bukkit.getPluginManager().registerEvents(this, MurderPlugin.getInstance());
        }

        @Override
        public String getLiteral() {
            return "rear";
        }

        @EventHandler
        @Override
        protected void onInteract(PlayerInteractEntityEvent event) {
            super.onInteract(event);
        }
    }

    public static class OpenAnimation extends PropAnimation<SWUtilityDoorProp> {
        public OpenAnimation(Prop prop, boolean isLooping) {
            super("open", SWUtilityDoorProp.class, prop, isLooping);
        }

        @Override
        public void createFrames(List<IPropAnimationFrame<SWUtilityDoorProp>> frames) {
            for (int i = 0; i < 2; i ++) {
                frames.add(IPropAnimationFrame.create(prop -> {
                    PropPart<SWUtilityDoorProp, ItemDisplay> doorPart = prop.getPart("door.main");
                    if (doorPart == null) return;
                    AdvancedLocation location = prop.getLocation();
                    float yOffset = 2.75f;
                    Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
                    Transform doorPartTransform = new Transform.Builder().translation(new Vector3f(-0.5f, 0.5f + yOffset, -0.5f)).build();
                    doorPart.getInternal().setInterpolationDelay(0);
                    doorPart.getInternal().setInterpolationDuration(20);
                    doorPart.getInternal().setTransformation(propTransform.compose(doorPartTransform).bake());
                }, 20));
            }
        }
    }

    public static class CloseAnimation extends PropAnimation<SWUtilityDoorProp> {
        public CloseAnimation(Prop prop, boolean isLooping) {
            super("close", SWUtilityDoorProp.class, prop, isLooping);
        }

        @Override
        public void createFrames(List<IPropAnimationFrame<SWUtilityDoorProp>> frames) {
            for (int i = 0; i < 2; i ++) {
                frames.add(IPropAnimationFrame.create(prop -> {
                    PropPart<SWUtilityDoorProp, ItemDisplay> doorPart = prop.getPart("door.main");
                    if (doorPart == null) return;
                    AdvancedLocation location = prop.getLocation();
                    float yOffset = 0.0f;
                    Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
                    Transform doorPartTransform = new Transform.Builder().translation(new Vector3f(-0.5f, 0.5f + yOffset, -0.5f)).build();
                    doorPart.getInternal().setInterpolationDelay(0);
                    doorPart.getInternal().setInterpolationDuration(20);
                    doorPart.getInternal().setTransformation(propTransform.compose(doorPartTransform).bake());
                }, 20));
            }
        }
    }

}
