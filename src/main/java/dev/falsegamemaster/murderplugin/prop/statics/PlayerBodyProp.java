package dev.falsegamemaster.murderplugin.prop.statics;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.prop.Prop;
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

public class PlayerBodyProp extends Prop {

    private static float getScale(@Nullable JsonObject data) {
        return data == null || !data.has("scale") || !data.get("scale").isJsonPrimitive() ? 1.0f : Math.max(0.01f, data.get("scale").getAsFloat());
    }

    private static Transform createPropTransform(SpawnRequest request) {
        AdvancedLocation location = request.location();
        float scale = getScale(request.data());
        return new Transform.Builder(location).translation(new Vector3f(0.0f, 1.0f, 0.0f)).scale(new Vector3f(scale, scale, scale)).build(); // never forget... IT WAS 1
    }

    private static Integer getHatId(@Nullable JsonObject data) {
        if (data == null) return null;
        JsonObject hatJson = data.get("hat").getAsJsonObject();
        String hasHelmet = String.valueOf(hatJson.get("has_helmet").getAsInt());
        String hatCategory = String.valueOf(hatJson.get("hat_cat").getAsInt());
        String hatSubcategory = String.format("%02d", hatJson.get("hat_sub_cat").getAsInt());
        String hat = String.format("%04d", hatJson.get("hat").getAsInt());
        String hatIdString = hasHelmet + hatCategory + hatSubcategory + hat;
        return Integer.parseInt(hatIdString);
    }

    public PlayerBodyProp(Prop.Registrar registrar, String uniqueFriendlyName) {
        super(registrar, uniqueFriendlyName);
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return PlayerBodyProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.STATIC;
    }

    @Override
    public String getLiteral() {
        return "player_body";
    }

    @Override
    public String getDisplayName() {
        return "Player Body";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(SpawnRequest request) {
        List<IPropPartFactory<?>> factories = new ArrayList<>();
        factories.add(BodyPart::new);
        JsonObject data = request.data();
        if (data == null || !data.has("hat") || !data.get("hat").isJsonObject()) {
            factories.add(HeadPart::new);
            return factories;
        }
        JsonObject hat = data.get("hat").getAsJsonObject();
        if (hat.has("has_helmet") && hat.has("hat_cat") && hat.has("hat_sub_cat") && hat.has("hat")) {
            if (hat.get("has_helmet").getAsInt() == 0) factories.add(HeadPart::new);
            Integer hatId = getHatId(data);
            if (hatId != null && hatId != 0) factories.add(HatPart::new);
            return factories;
        }
        factories.add(HeadPart::new);
        return factories;
    }

    public static class BodyPart extends EntityPropPart<PlayerBodyProp, ItemDisplay> {
        public BodyPart(Prop prop, int sequentialID) {
            super(PlayerBodyProp.class, prop, sequentialID);
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

        private ItemStack createBody(@Nullable JsonObject data) {
            ItemStack bodyItem = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            LeatherArmorMeta meta = (LeatherArmorMeta) bodyItem.getItemMeta();
            if (meta == null) return bodyItem;
            if (data != null && data.has("color") && data.get("color").isJsonPrimitive()) meta.setColor(Color.fromRGB(data.get("color").getAsInt()));
            meta.setCustomModelData(10000001);
            bodyItem.setItemMeta(meta);
            return bodyItem;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, SpawnRequest request) {
            entity.setItemStack(createBody(request.data()));
            Transform partTransform = new Transform(new Vector3f(0.0f, -0.5f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            entity.setTransformation(createPropTransform(request).compose(partTransform).bake());
        }
    }

    public static class HeadPart extends EntityPropPart<PlayerBodyProp, ItemDisplay> {
        public HeadPart(Prop prop, int sequentialID) {
            super(PlayerBodyProp.class, prop, sequentialID);
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
            if (data == null || !data.has("name") || !data.get("name").isJsonPrimitive()) return head;
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) return head;
            OfflinePlayer player = Bukkit.getOfflinePlayer(data.get("name").getAsString());
            PlayerProfile profile = player.getPlayerProfile();
            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
            return head;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, SpawnRequest request) {
            entity.addScoreboardTag("target");
            entity.setItemStack(createPlayerHead(request.data()));
            Vector3f pivot = new Vector3f(0.0f, -0.875f, 0.5f);
            Vector3f translation = new Vector3f(0.0f, -0.375f, 0.5f);
            Quaternionf rotation = new Quaternionf().rotationX((float)(0.25*Math.PI));
            Transform partTransform = new Transform(translation, pivot, rotation, new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            entity.setTransformation(createPropTransform(request).compose(partTransform).bake());
        }
    }

    public static class HatPart extends EntityPropPart<PlayerBodyProp, ItemDisplay> {
        public HatPart(Prop prop, int sequentialID) {
            super(PlayerBodyProp.class, prop, sequentialID);
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
            ItemStack hatItem = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            if (data == null || !data.has("hat") || !data.get("hat").isJsonObject()) return hatItem;
            LeatherArmorMeta meta = (LeatherArmorMeta) hatItem.getItemMeta();
            if (meta == null) return hatItem;
            meta.setCustomModelData(getHatId(data));
            if (data.has("color") && data.get("color").isJsonPrimitive()) meta.setColor(Color.fromRGB(data.get("color").getAsInt()));
            hatItem.setItemMeta(meta);
            return hatItem;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, SpawnRequest request) {
            entity.addScoreboardTag("target");
            entity.setItemStack(createHat(request.data()));
            Vector3f pivot = new Vector3f(0.0f, -0.875f, 0.5f);
            Vector3f translation = new Vector3f(0.0f, -0.476f, 0.5f);
            Quaternionf rotationY = new Quaternionf().rotationY((float)(Math.PI));
            Quaternionf rotationX = new Quaternionf().rotationX((float)(0.25*Math.PI));
            Quaternionf rotation = rotationX.mul(rotationY);
            Transform partTransform = new Transform(translation, pivot, rotation, new Vector3f(0.625f, 0.625f, 0.625f), new Quaternionf());
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
            entity.setTransformation(createPropTransform(request).compose(partTransform).bake());
        }
    }

}
