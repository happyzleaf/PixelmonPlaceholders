package com.github.happyzleaf.pixelmonplaceholders.utility;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class RayTracingHelper {
	/**
	 * @param source   The source where to start the ray trace.
	 * @param distance The maximum distance where the entity can be found.
	 * @see net.minecraft.client.renderer.EntityRenderer#getMouseOver(float)
	 */
	public static Optional<Entity> getLookedEntity(Entity source, double distance) {
		Entity pointedEntity = null;

		Vec3d start = source.getPositionEyes(1f);
		Vec3d look = source.getLook(1f);
		Vec3d end = start.addVector(look.x * distance, look.y * distance, look.z * distance);

		RayTraceResult closestBlock = source.world.rayTraceBlocks(start, end, false, false, true);
		if (closestBlock != null) {
			distance = closestBlock.hitVec.distanceTo(start);
		}

		for (Entity entity : source.world.getEntitiesInAABBexcluding(source, source.getEntityBoundingBox().expand(end.x, end.y, end.y).grow(1d, 1d, 1d), Predicates.and(EntitySelectors.NOT_SPECTATING, e -> e != null && e.canBeCollidedWith()))) {
			AxisAlignedBB boundingBox = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
			RayTraceResult traceResult = boundingBox.calculateIntercept(start, end);

			if (boundingBox.contains(start)) {
				if (distance >= 0d) {
					pointedEntity = entity;
					distance = 0d;
				}
			} else if (traceResult != null) {
				double newDistance = start.distanceTo(traceResult.hitVec);

				if (newDistance < distance || distance == 0d) {
					if (entity.getLowestRidingEntity() == source.getLowestRidingEntity() && !entity.canRiderInteract()) {
						if (distance == 0d) {
							pointedEntity = entity;
						}
					} else {
						pointedEntity = entity;
						distance = newDistance;
					}
				}
			}
		}

		return Optional.ofNullable(pointedEntity);
	}
}
