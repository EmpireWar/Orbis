/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.empirewar.orbis.modded.mixin;

import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockEventMixin {
    //    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    //    private void onBlockGrow(BlockState state, Level level, BlockPos pos, BlockState oldState,
    // boolean isMoving, CallbackInfoReturnable<Boolean> cir) {
    //        if (level.isClientSide() || !(level instanceof ServerLevel)) return;
    //
    //        BlockActionListener listener = ((OrbisFabric)
    // OrbisFabric.getInstance()).getBlockActionListener();
    //        if (!listener.onBlockGrow(level, pos, state)) {
    //            cir.setReturnValue(false);
    //        }
    //    }
}
