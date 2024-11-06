/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.surface.builder;


import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;

public class ActiveShieldVolcanoSurfaceBuilder implements SurfaceBuilder
{
    public static final SurfaceBuilderFactory ACTIVE = seed -> new ActiveShieldVolcanoSurfaceBuilder(seed, BiomeNoise.activeShieldVolcano(seed, 0, BiomeNoise.activeHotSpots(seed)));

    private final Noise2D heightNoise;

    ActiveShieldVolcanoSurfaceBuilder(long seed, Noise2D heightNoise)
    {
        this.heightNoise = heightNoise;
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY)
    {
        final long seed = context.getSeed();
        final Noise2D noise = new OpenSimplex2D(seed).octaves(2).spread(0.25);
        final Noise2D lavaFlows = BiomeNoise.lavaFlow(seed);
        final int x = context.pos().getX();
        final int z = context.pos().getZ();
        final double flowValue = lavaFlows.noise(x, z);
        final double height = this.heightNoise.noise(x, z);


        final int depth = height > 50 ? (int) (height - 50) : 0;
        endY = startY - depth;

        final NormalSurfaceBuilder surfaceBuilder = NormalSurfaceBuilder.ROCKY;


        if (flowValue < 0.40)
        {
            surfaceBuilder.buildSurface(context, startY, endY);
        }
        else
        {
            final double noiseValue = noise.noise(x, z);

            if (flowValue < 0.50)
            {
                if (noiseValue > 0)
                {
                    surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.BASALT_GRAVEL, SurfaceStates.BASALT_GRAVEL, SurfaceStates.RAW, SurfaceStates.BASALT_GRAVEL, SurfaceStates.BASALT_GRAVEL);
                }
                else
                {
                    surfaceBuilder.buildSurface(context, startY, endY);
                }
            }
            else if (flowValue < 0.75)
            {
                if (noiseValue > 0)
                {
                    surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.BASALT_GRAVEL, SurfaceStates.BASALT_GRAVEL, SurfaceStates.RAW, SurfaceStates.BASALT_GRAVEL, SurfaceStates.BASALT_GRAVEL);
                }
                else
                {
                    surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.BASALT_COBBLE, SurfaceStates.BASALT_COBBLE, SurfaceStates.RAW, SurfaceStates.BASALT_COBBLE, SurfaceStates.BASALT_COBBLE);
                }
            }
            else
            {
                if (noiseValue > -0.6)
                {
                    surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.BASALT, SurfaceStates.BASALT, SurfaceStates.RAW, SurfaceStates.BASALT_COBBLE, SurfaceStates.BASALT_COBBLE);
                }
                else
                {
                    surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.BASALT_COBBLE, SurfaceStates.BASALT_COBBLE, SurfaceStates.RAW, SurfaceStates.BASALT_COBBLE, SurfaceStates.BASALT_COBBLE);
                }
            }

        }
    }
}
