package dev.falsegamemaster.murderplugin.prop.statics;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.prop.part.EntityPropPart;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.Transform;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerCorpseProp extends Prop {

    public PlayerCorpseProp(Prop.Registrar registrar, String uniqueFriendlyName) {
        super(registrar, uniqueFriendlyName);
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return PlayerCorpseProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.STATIC;
    }

    @Override
    public String getLiteral() {
        return "player_corpse";
    }

    @Override
    public String getDisplayName() {
        return "Player Corpse";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(PropSpawnRequest request) {
        List<IPropPartFactory<?>> factories = new ArrayList<>();
        factories.add(BodyPart::new);
        JsonObject data = request.data();
        if (data == null) return factories;
        if (data.has("player")) factories.add(HeadPart::new);
        if (data.has("hat")) factories.add(HatPart::new);
        return factories;
    }

    public static class BodyPart extends EntityPropPart<PlayerCorpseProp, ItemDisplay> {
        public BodyPart(Prop prop, int sequentialID) {
            super(PlayerCorpseProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "base";
        }

        @Override
        public String getLiteral() {
            return "body";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
            entity.setItemStack(new ItemStack(Material.BEEF));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform partTransform = new Transform(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 0.125f, -0.5f)).build();
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class HeadPart extends EntityPropPart<PlayerCorpseProp, ItemDisplay> {
        public HeadPart(Prop prop, int sequentialID) {
            super(PlayerCorpseProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "head";
        }

        @Override
        public String getLiteral() {
            return "head";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        private ItemStack createPlayerHead(@Nullable JsonObject data) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (data == null) return head;
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) return head;
            OfflinePlayer player = Bukkit.getOfflinePlayer(data.get("player").getAsString());
            PlayerProfile profile = player.getPlayerProfile();
            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
            return head;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
            entity.setItemStack(createPlayerHead(request.data()));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Vector3f pivot = new Vector3f(0.0f, -0.5f, 0.0f);
            Vector3f translation = new Vector3f(-0.5f, -0.375f, 0.0f);
            Quaternionf rotation = new Quaternionf().rotationX((float)(0.25*Math.PI));
            Transform partTransform = new Transform(translation, pivot, rotation, new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 0.125f, -0.5f)).build();
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class HatPart extends EntityPropPart<PlayerCorpseProp, ItemDisplay> {
        public HatPart(Prop prop, int sequentialID) {
            super(PlayerCorpseProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "hat";
        }

        @Override
        public String getLiteral() {
            return "hat";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        private ItemStack createHat(@Nullable JsonObject data) {
            ItemStack hat = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            if (data == null) return hat;
            LeatherArmorMeta meta = (LeatherArmorMeta) hat.getItemMeta();
            if (meta == null) return hat;
            meta.setCustomModelData(data.get("hat").getAsInt());
            if (data.has("color")) meta.setColor(Color.fromRGB(data.get("color").getAsInt()));
            hat.setItemMeta(meta);
            return hat;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
            entity.setItemStack(createHat(request.data()));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Vector3f pivot = new Vector3f(0.0f, -0.5f, 0.0f);
            Vector3f translation = new Vector3f(-0.5f, -0.375f, 0.0f);
            Quaternionf rotationY = new Quaternionf().rotationY((float)(Math.PI));
            Quaternionf rotationX = new Quaternionf().rotationX((float)(0.25*Math.PI));
            Quaternionf rotation = rotationX.mul(rotationY);
            Transform partTransform = new Transform(translation, pivot, rotation, new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 0.125f, -0.5f)).build();
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

}
