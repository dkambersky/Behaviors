/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.minion.work.systems;

import com.google.common.collect.Lists;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.minion.work.Work;
import org.terasology.minion.work.WorkFactory;
import org.terasology.minion.work.WorkTargetComponent;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.registry.In;

import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;



import java.util.List;

/**
 * @author synopia
 */
@RegisterSystem
public class RemoveBlock extends BaseComponentSystem implements Work, ComponentSystem {
    private static final int[][] NEIGHBORS = new int[][]{
            {-1, 0, 0}, {1, 0, 0}, {0, 0, -1}, {0, 0, 1},
//            {-1, 1, 0}, {1, 1, 0}, {0, 1, -1}, {0, 1, 1},
            {-1, -1, 0}, {1, -1, 0}, {0, -1, -1}, {0, -1, 1},
            {-1, -2, 0}, {1, -2, 0}, {0, -2, -1}, {0, -2, 1},
    };

    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private WorldProvider worldProvider;
    @In
    private WorkFactory workFactory;

    private final SimpleUri uri;




    public RemoveBlock() {
        uri = new SimpleUri("Pathfinding:removeBlock");
    }

    @Override
    public void initialise() {
        workFactory.register(this);

    }

    @Override
    public void shutdown() {
    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    @Override
    public List<WalkableBlock> getTargetPositions(EntityRef block) {
        List<WalkableBlock> result = Lists.newArrayList();

        Vector3i worldPos = block.getComponent(BlockComponent.class).getPosition();
        WalkableBlock walkableBlock;
        Vector3i pos = new Vector3i();
        for (int[] neighbor : NEIGHBORS) {
            pos.set(worldPos.x + neighbor[0], worldPos.y + neighbor[1], worldPos.z + neighbor[2]);
            walkableBlock = pathfinderSystem.getBlock(pos);
            if (walkableBlock != null) {
                result.add(walkableBlock);
            }
        }
        return result;
    }

    @Override
    public boolean canMinionWork(EntityRef block, EntityRef minion) {
        Vector3f pos = new Vector3f();
        pos.sub(block.getComponent(LocationComponent.class).getWorldPosition(), minion.getComponent(LocationComponent.class).getWorldPosition());
        pos.y /= 4;
        float length = pos.length();
        return length < 2;
    }

    @Override
    public void letMinionWork(EntityRef block, EntityRef minion) {
        block.removeComponent(WorkTargetComponent.class);
        worldProvider.setBlock(block.getComponent(BlockComponent.class).getPosition(), workFactory.getAir());
    }

    @Override
    public boolean isAssignable(EntityRef block) {
        Vector3i position = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        Block type = worldProvider.getBlock(position);
        return !type.isPenetrable();
    }

    @Override
    public boolean isRequestable(EntityRef block) {
        return true;
    }

    @Override
    public float cooldownTime() {
        return 0;
    }

    @Override
    public String toString() {
        return "Remove Block";
    }
}



