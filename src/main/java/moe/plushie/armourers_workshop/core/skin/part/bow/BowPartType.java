package moe.plushie.armourers_workshop.core.skin.part.bow;

import com.google.common.collect.Range;
import moe.plushie.armourers_workshop.api.action.ICanHeld;
import moe.plushie.armourers_workshop.api.action.ICanUse;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartType;
import moe.plushie.armourers_workshop.utils.Rectangle3i;
import net.minecraft.util.math.vector.Vector3i;

public class BowPartType extends SkinPartType implements ICanHeld, ICanUse {

    private final Range<Integer> useRange;

    public BowPartType(int frame) {
        super();
        this.buildingSpace = new Rectangle3i(-12, -32, -46, 24, 64, 64);
        this.guideSpace = new Rectangle3i(-2, -2, 2, 4, 4, 8);
        this.offset = getFrameOffset(frame);
        this.useRange = getFrameUseRange(frame);
    }

    public static Range<Integer> getFrameUseRange(int frame) {
        // (float)(p_239429_0_.getUseDuration() - p_239429_2_.getUseItemRemainingTicks()) / 20.0F;
        // pulling: 1, 0
        // pulling: 1, 0.65
        // pulling: 1, 0.9
        switch (frame) {
            case 0:
                return Range.closed(0, 12);
            case 1:
                return Range.closed(13, 17);
            default:
                return Range.closed(18, 30);
        }
    }

    public static Vector3i getFrameOffset(int frame) {
        switch (frame) {
            case 0:
                return new Vector3i(-25, 0, 0);
            case 1:
                return new Vector3i(0, 0, 0);
            case 2:
                return new Vector3i(25, 0, 0);
            default:
                return null;
        }
    }

    @Override
    public Range<Integer> getUseRange() {
        return useRange;
    }

    @Override
    public int getMinimumMarkersNeeded() {
        return 1;
    }

    @Override
    public int getMaximumMarkersNeeded() {
        return 1;
    }

    @Override
    public boolean isPartRequired() {
        return true;
    }
}
