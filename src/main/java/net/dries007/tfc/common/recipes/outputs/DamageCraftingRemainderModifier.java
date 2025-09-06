/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes.outputs;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.util.Helpers;

public enum DamageCraftingRemainderModifier implements ItemStackModifier
{
    INSTANCE;


    @Override
    @SuppressWarnings("deprecation") // For damageItem(), but we don't have access to a level here
    public ItemStack apply(ItemStack stack, ItemStack input, Context context)
    {
        for (ItemStack craftingStack : RecipeHelpers.getCraftingInput())
        {
            if (craftingStack.isDamageableItem())
            {
                final @Nullable Player player = RecipeHelpers.getCraftingPlayer();
                if (player != null)
                {
                    Helpers.damageItem(craftingStack, player.level());
                }
                else
                {
                    Helpers.damageItem(craftingStack);
                }
            }
        }
        if (input.isDamageableItem() || input.has(DataComponents.UNBREAKABLE) || input.hasCraftingRemainingItem())
        {
            return input.copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStackModifierType<?> type()
    {
        return ItemStackModifiers.DAMAGE_CRAFTING_REMAINDER.get();
    }
}
