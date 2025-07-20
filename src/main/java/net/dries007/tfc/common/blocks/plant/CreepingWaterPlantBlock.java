package net.dries007.tfc.common.blocks.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;

public abstract class CreepingWaterPlantBlock extends CreepingPlantBlock implements IFluidLoggable
{
    public static CreepingWaterPlantBlock create(RegistryPlant plant, FluidProperty fluid, ExtendedProperties properties)
    {
        return new CreepingWaterPlantBlock(properties)
        {
            @Override
            public RegistryPlant getPlant()
            {
                return plant;
            }

            @Override
            public FluidProperty getFluidProperty()
            {
                return fluid;
            }

            @Override
            public boolean canCreepOn(LevelReader level, BlockPos pos, BlockState state, Direction direction)
            {
                return Helpers.isBlock(state, TFCTags.Blocks.ANEMONE_PLANTABLE_ON) && super.canCreepOn(level, pos, state, direction);
            }

        };
    }

    public static CreepingWaterPlantBlock createRock(RegistryPlant plant, FluidProperty fluid, ExtendedProperties properties)
    {
        return new CreepingWaterPlantBlock(properties)
        {
            @Override
            public RegistryPlant getPlant()
            {
                return plant;
            }

            @Override
            public FluidProperty getFluidProperty()
            {
                return fluid;
            }

            @Override
            public boolean canCreepOn(LevelReader level, BlockPos pos, BlockState state, Direction direction)
            {
                return Helpers.isBlock(state, TFCTags.Blocks.CREEPING_STONE_PLANTABLE_ON) && super.canCreepOn(level, pos, state, direction);
            }

        };
    }

    protected CreepingWaterPlantBlock(ExtendedProperties properties)
    {
        super(properties);

        registerDefaultState(getStateDefinition().any().setValue(getFluidProperty(), getFluidProperty().keyFor(Fluids.EMPTY)));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        BlockState state = defaultBlockState();
        if (getFluidProperty().canContain(fluidState.getType()))
        {
            state = state.setValue(getFluidProperty(), getFluidProperty().keyFor(fluidState.getType()));
        }
        return updateStateFromSides(context.getLevel(), context.getClickedPos(), state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        FluidHelpers.tickFluid(level, currentPos, state);
        state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), canCreepOn(level, facingPos, facingState, direction));
        return isEmptyContents(state) ? Blocks.AIR.defaultBlockState() : state;
    }

    private static boolean isEmptyContents(BlockState state)
    {
        for (BooleanProperty property : SHAPES.keySet())
        {
            if (state.getValue(property))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(getFluidProperty());
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return IFluidLoggable.super.getFluidState(state);
    }
}
