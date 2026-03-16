package net.filipes.rituals.client.render.state;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.text.OrderedText;
import java.util.ArrayList;
import java.util.List;

public class PedestalRenderState extends BlockEntityRenderState {
    public final List<OrderedText> lines = new ArrayList<>();
}