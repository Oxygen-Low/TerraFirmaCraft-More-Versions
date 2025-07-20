/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.surface.builder;

import net.minecraft.core.BlockPos;

import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;

public class ShoreSurfaceBuilder implements SurfaceBuilder
{
    public static final SurfaceBuilderFactory NORMAL = seed -> new ShoreSurfaceBuilder(seed, SurfaceStates.SHORE_SURFACE, SurfaceStates.SHORE_UNDERLAYER, false, 6, false);
    public static final SurfaceBuilderFactory SANDY = seed -> new ShoreSurfaceBuilder(seed, SurfaceStates.SHORE_SAND, SurfaceStates.SHORE_SANDSTONE, false, 6, true);
    public static final SurfaceBuilderFactory FORCE_RARE_SAND = seed -> new ShoreSurfaceBuilder(seed, SurfaceStates.RARE_SHORE_SAND, SurfaceStates.RARE_SHORE_SANDSTONE, false, 6, true);
    public static final SurfaceBuilderFactory GRAVELLY = seed -> new ShoreSurfaceBuilder(seed, SurfaceStates.SECOND_GRAVEL, SurfaceStates.RAW, false, 6, false);
    public static final SurfaceBuilderFactory OCEAN = seed -> new ShoreSurfaceBuilder(seed, SurfaceStates.SHORE_SURFACE, SurfaceStates.SHORE_UNDERLAYER, true, 6, false);
    public static final SurfaceBuilderFactory SEA_CLIFFS = seed -> new ShoreSurfaceBuilder(seed, SurfaceStates.SHORE_SURFACE, SurfaceStates.SHORE_UNDERLAYER, false, 2, false);

    final Seed seed;
    final SurfaceState surface;
    final SurfaceState subsurface;
    final boolean isOcean;
    final boolean sandyLand;
    final int sandHeight;

    protected ShoreSurfaceBuilder(Seed seed, SurfaceState surface, SurfaceState subsurface, boolean isOcean, int sandHeight, boolean sandyLand)
    {
        this.seed = seed;
        this.surface = surface;
        this.subsurface = subsurface;
        this.isOcean = isOcean;
        this.sandHeight = sandHeight;
        this.sandyLand = sandyLand;
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY)
    {
        final BlockPos pos = context.pos();
        final int x = pos.getX();
        final int z = pos.getZ();
        final int tideLevel = (int) BiomeNoise.shoreTideLevelNoise(seed).noise(x, z);
        final int sandHeightAbsolute = tideLevel + sandHeight;

        if (isOcean && startY < tideLevel - 6)
        {
            // Always use gravel in deeper ocean water
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, surface, surface, subsurface, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL);
        }
        else if (startY <= sandHeightAbsolute)
        {
            // Shore surface at shore heights, above/below water
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, surface, surface, subsurface, surface, surface);
        }
        else if (sandyLand)
        {
            // If, in dry biomes, sand should be used instead of gravel on land
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, SurfaceStates.TOP_GRASS_TO_SHORE_SAND, SurfaceStates.MID_DIRT_TO_SHORE_SAND, SurfaceStates.UNDER_GRAVEL, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL, surface, surface, subsurface, sandHeightAbsolute);
        }
        {
            // Normal land surface, with shore material at beach level in caves/below overhangs
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, surface, surface, subsurface, sandHeightAbsolute);
        }
    }

}