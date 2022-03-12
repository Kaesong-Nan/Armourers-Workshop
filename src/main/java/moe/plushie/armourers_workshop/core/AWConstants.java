package moe.plushie.armourers_workshop.core;

import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.storage.FolderName;

public final class AWConstants {

    public final static Vector3f ZERO = new Vector3f();
    public final static Vector3f ONE = new Vector3f(1, 1, 1);

    public final static class Folder {
        public static final FolderName LOCAL_DB = new FolderName("skin-database");
    }

    public final static class NBT {
        public static final String SKIN = "ArmourersWorkshop";
        public static final String SKIN_TYPE = "SkinType";
        public static final String SKIN_IDENTIFIER = "Identifier";

        public static final String TEXTURE_URL = "URL";
        public static final String TEXTURE_PROFILE = "User";

        public static final String COLOR = "Color";

        public static final String ENTITY = "EntityTag";

        public static final String ENTITY_SCALE = "Scale";

        public static final String ENTITY_IS_SMALL = "Small";
        public static final String ENTITY_IS_FLYING = "Flying";
        public static final String ENTITY_IS_GHOST = "Ghost";
        public static final String ENTITY_IS_VISIBLE = "ModelVisible";
        public static final String ENTITY_EXTRA_RENDER = "ExtraRender";
        public static final String ENTITY_TEXTURE = "Texture";
        public static final String ENTITY_POSE = "Pose";

        public static final String TILE_ENTITY_ANGLE = "Angle";
        public static final String TILE_ENTITY_OFFSET = "Offset";
        public static final String TILE_ENTITY_ROTATION_SPEED = "RotSpeed";
        public static final String TILE_ENTITY_ROTATION_OFFSET = "RotOffset";

        public static final String TILE_ENTITY_POWER_MODE = "PowerMode";
        public static final String TILE_ENTITY_IS_GLOWING = "Glowing";
        public static final String TILE_ENTITY_POWERED = "Powered";
    }
}
