/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.recipes.AdvancedShapedRecipe;
import net.dries007.tfc.common.recipes.AdvancedShapelessRecipe;
import net.dries007.tfc.common.recipes.outputs.DamageCraftingRemainderModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.test.TestSetup;
import net.dries007.tfc.util.Helpers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.Tags;

public class CraftingRecipesTest implements TestSetup
{
    @SuppressWarnings("unchecked")
    @Test
    public void testCraftingRecipesWithToolsDamageInputs()
    {
        final Set<String> expectedDoNotDamageInputs = Stream.of(
            Stream.of(
                "minecraft:fire_charge",
                "tfc:crafting/add_large_bait",
                "tfc:crafting/add_small_bait"
            ),
            Arrays.stream(Wood.VALUES).map(wood -> "tfc:crafting/wood/sewing_table/" + wood.getSerializedName())
        ).flatMap(s -> s).collect(Collectors.toSet());

        final RecipeManager manager = Helpers.getUnsafeRecipeManager();

        final List<String> recipes = manager
            .getAllRecipesFor(RecipeType.CRAFTING)
            .stream()
            .filter(holder -> {
                if (expectedDoNotDamageInputs.contains(holder.id().toString())) return false;
                final CraftingRecipe recipe = holder.value();
                if (recipe instanceof AdvancedShapedRecipe || recipe instanceof AdvancedShapelessRecipe)
                {
                    Class<?> recipeType = recipe instanceof AdvancedShapedRecipe ? AdvancedShapedRecipe.class : AdvancedShapelessRecipe.class;
                    @Nullable Field remainder = null;
                    try
                    {
                        remainder = recipeType.getDeclaredField("remainder");
                    }
                    catch (Exception e)
                    {
                        fail("Could not get field 'remainder' of recipeType: " + e);
                    }

                    remainder.setAccessible(true);
                    @Nullable Optional<ItemStackProvider> remainderProvider = null;
                    try {
                        remainderProvider = (Optional<ItemStackProvider>) remainder.get(recipe);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        fail("Could not get value of field 'remainder' of recipeType: " + e);
                    }
                    
                    if (remainderProvider.isPresent() && remainderProvider.get().modifiers().stream().anyMatch(modifier ->  modifier instanceof DamageCraftingRemainderModifier))
                    {
                        return false;
                    }
                }

                final Stream<ItemStack> stacks = recipe
                    .getIngredients()
                    .stream()
                    .flatMap(ingredient -> Arrays.stream(ingredient.getItems()));

                return stacks.anyMatch(stack -> stack.isDamageableItem() && stack.is(Tags.Items.TOOLS));
            })
            .map(holder -> holder.id().toString())
            .toList();

        assertTrue(recipes.isEmpty(), "Recipes with tools do not damage inputs: " + String.join("\n", recipes));
    }
}
