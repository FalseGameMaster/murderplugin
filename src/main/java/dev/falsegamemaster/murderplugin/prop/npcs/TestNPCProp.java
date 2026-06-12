package dev.falsegamemaster.murderplugin.prop.npcs;

import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.prop.ai.PropAI;
import dev.falsegamemaster.propengine.prop.part.EntityPropPart;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestNPCProp extends Prop {

    public TestNPCProp(Registrar registrar, String uniqueName) {
        super(registrar, uniqueName);
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return TestNPCProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.NPC;
    }

    @Override
    public String getLiteral() {
        return "test_npc";
    }

    @Override
    public String getDisplayName() {
        return "Test NPC";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(SpawnRequest request) {
        return List.of(BasePart::new);
    }

    @Override
    public void load() {
        super.load();
        PropAI.PathGraph graph = new PropAI.PathGraph("test_graph", Color.ORANGE);
        graph.addNode(new PropAI.PathNode("node0", spawnRequest.location().clone(), new HashSet<>()));
        graph.addNode(new PropAI.PathNode("node1", spawnRequest.location().clone().add(6, 0, 0), new HashSet<>()));
        graph.addNode(new PropAI.PathNode("node2", spawnRequest.location().clone().add(6, 0, 6), new HashSet<>()));
        graph.addNode(new PropAI.PathNode("node3", spawnRequest.location().clone().add(0, 0, 6), new HashSet<>()));
        graph.addEdge("node0", "node1", 6);
        graph.addEdge("node1", "node2", 6);
        graph.addEdge("node2", "node3", 6);
        graph.addEdge("node3", "node0", 6);
        Set<String> tags = new HashSet<>();
        tags.add("test_route");

        // TODO (5/13/2026): Remove this once I have path creation commands in place
        PropAI.PatrolRouteDefinition routeDefinition = new PropAI.PatrolRouteDefinition("test_route", graph, PropAI.PatrolMode.LOOP, tags, Color.AQUA, "node0", "node1", "node2", "node3");
        setController(new PropAI.Controller(this, graph, new PropAI.PatrolRouteInstance(routeDefinition), 2.0));
        PropEnginePlugin.getInstance().pathRegistrar.register(graph);
        PropEnginePlugin.getInstance().pathRegistrar.register(routeDefinition);

//        // TODO (5/13/2026): Remove this once I have path display commands in place
//        PropEnginePlugin.getInstance().getRouteDebugRenderer().showRoute(routeDefinition);
    }

    public static class BasePart extends EntityPropPart<TestNPCProp, ArmorStand> {
        public BasePart(Prop prop, int sequentialID) {
            super(TestNPCProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "base";
        }

        @Override
        public String getLiteral() {
            return "base";
        }

        @Override
        public Class<ArmorStand> getInternalClass() {
            return ArmorStand.class;
        }

        @Override
        public void prepareInternal(ArmorStand entity, SpawnRequest request) {
            entity.teleport(request.location().clone().add(-0.5, -1.0, -0.5));
            entity.setRotation(request.location().getYaw(), request.location().getPitch());
            entity.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
        }
    }

}
