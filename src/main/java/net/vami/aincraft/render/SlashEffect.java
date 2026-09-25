package net.vami.aincraft.render;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.vami.aincraft.init.SlashEffects;

import java.awt.*;
import java.util.Arrays;

public final class SlashEffect {

    private final ResourceLocation texture;

    // distance from player
    private final double[] distances;
    // does damage scale with distance
    private final boolean scaling;

    // right-left local offset
    private final double sway;
    // up-down local offset
    private final double lift;
    // WORLD coordinate offset
    private final double xOffset;
    private final double yOffset;
    private final double zOffset;
    // local rotation
    private final double rotation;
    // slash shape (LINE, ARC)
    private final Shape shape;
    // provided line, length of line
    private final double length;
    // provided arc, radius of arc
    private final double radius;
    // thickness of slash
    private final double thickness;
    // curvature of angle, 0 start -> 360 end == full circle
    private final double startAngle;
    private final double endAngle;
    // segment count for hit/block detection
    private final int segments;
    // lifetime of slash before despawn
    private final int lifetime;
    // spawn animation from startAngle -> endAngle / revealTime
    private final float revealTime;
    private final float fadeStart;
    // color array for transitions / lifetime
    private final int[] colors;
    // idek why i have this
    private final boolean fullBright;

    public enum Shape {
        ARC,
        LINE
    }

    private SlashEffect(Builder builder) {
        this.texture = builder.texture;
        this.distances = builder.distances;
        this.scaling = builder.scaling;
        this.sway = builder.sway;
        this.lift = builder.lift;
        this.xOffset = builder.xOffset;
        this.yOffset = builder.yOffset;
        this.zOffset = builder.zOffset;
        this.rotation = builder.rotation;
        this.shape = builder.shape;
        this.length = builder.length;
        this.radius = builder.radius;
        this.thickness = builder.thickness;
        this.startAngle = builder.startAngle;
        this.endAngle = builder.endAngle;
        this.segments = builder.segments;
        this.lifetime = builder.lifetime;
        this.revealTime = builder.revealDuration;
        this.fadeStart = builder.fadeStart;
        this.colors = builder.colors;
        this.fullBright = builder.fullBright;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public double[] distances() {
        return distances;
    }

    public boolean scaling() {
        return scaling;
    }

    public double sway() {
        return sway;
    }

    public double lift() {
        return lift;
    }

    public double xOffset() {
        return xOffset;
    }

    public double yOffset() {
        return yOffset;
    }

    public double zOffset() { return zOffset; }

    public double rotation() {
        return rotation;
    }

    public Shape shape() {
        return shape;
    }

    public double length() {
        return length;
    }

    public double radius() {
        return radius;
    }

    public double thickness() {
        return thickness;
    }

    public double startAngle() {
        return startAngle;
    }

    public double endAngle() {
        return endAngle;
    }

    public int segments() {
        return segments;
    }

    public int lifetime() {
        return lifetime;
    }

    public float revealTime() {
        return revealTime;
    }

    public float fadeStart() {
        return fadeStart;
    }

    public int[] colors() {
        return colors;
    }

    public boolean fullBright() {
        return fullBright;
    }

    public Builder edit() {
        return new Builder(this);
    }

    public static Builder builder(ResourceLocation texture) {
        return new Builder(texture);
    }

    public static class Builder {

        private final ResourceLocation texture;

        private double[] distances = new double[]{1.5d};
        private boolean scaling = true;

        private double sway = 0;
        private double lift = -0.4;
        private double xOffset = 0;
        private double yOffset = 0;
        private double zOffset = 0;

        private double rotation = 0;

        private Shape shape = Shape.ARC;
        private double length = 2.5;
        private double radius = 1.5;
        private double thickness = 0.25;
        private double startAngle = 90;
        private double endAngle = -90;

        private int segments = 12;
        private int lifetime = 8;

        private float revealDuration = 0.4f;
        private float fadeStart = 0.6f;

        private int[] colors = {Color.white.getRGB()};

        private boolean fullBright = true;

        private Builder(ResourceLocation texture) {
            this.texture = texture;
        }

        private Builder(SlashEffect effect) {
            this.texture = effect.texture;
            this.distances = effect.distances;
            this.scaling = effect.scaling;
            this.sway = effect.sway;
            this.lift = effect.lift;
            this.xOffset = effect.xOffset;
            this.yOffset = effect.yOffset;
            this.zOffset = effect.zOffset;
            this.rotation = effect.rotation;
            this.shape = effect.shape;
            this.length = effect.length;
            this.radius = effect.radius;
            this.thickness = effect.thickness;
            this.startAngle = effect.startAngle;
            this.endAngle = effect.endAngle;
            this.segments = effect.segments;
            this.lifetime = effect.lifetime;
            this.revealDuration = effect.revealTime;
            this.fadeStart = effect.fadeStart;
            this.colors = effect.colors;
            this.fullBright = effect.fullBright;
        }

        public Builder distance(double distance) {
            this.distances = new double[]{distance};
            return this;
        }

        public Builder distances(double ... distances) {
            this.distances = distances;
            return this;
        }

        public Builder push(double distance) {
            double[] distanceList = Arrays.copyOf(this.distances, this.distances.length + 1);
            distanceList[distanceList.length - 1] = distance;

            this.distances = distanceList;
            return this;
        }

        public Builder furthen(double distance) {
            distances = distances.clone();
            for (int i = 0; i < distances.length; i++) {
                distances[i] += distance;
            }

            return this;
        }

        public Builder scaling(boolean scaling) {
            this.scaling = scaling;
            return this;
        }

        public Builder sway(double sway) {
            this.sway = sway;
            return this;
        }

        public Builder lift(double lift) {
            this.lift = lift;
            return this;
        }

        public Builder xOffset(double xOffset) {
            this.xOffset = xOffset;
            return this;
        }

        public Builder yOffset(double yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Builder zOffset(double zOffset) {
            this.zOffset = zOffset;
            return this;
        }

        public Builder rotation(double rotation) {
            this.rotation = rotation;
            return this;
        }

        // adds to rotation
        public Builder rotate(double rotation) {
            this.rotation += rotation;
            return this;
        }

        public Builder shape(Shape shape) {
            this.shape = shape;
            return this;
        }

        public Builder line(double length) {
            this.shape = Shape.LINE;
            this.length = length;
            return this;
        }

        public Builder length(double length) {
            this.length = length;
            return this;
        }

        public Builder lengthen(double length) {
            this.length += length;
            return this;
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        // adds to radius
        public Builder inflate(double radius) {
            this.radius += radius;
            return this;
        }

        public Builder thickness(double thickness) {
            this.thickness = thickness;
            return this;
        }

        // adds to thickness
        public Builder thicken(double thickness) {
            this.thickness += thickness;
            return this;
        }

        public Builder angles(double startAngle, double endAngle) {
            this.startAngle = startAngle;
            this.endAngle = endAngle;
            return this;
        }

        public Builder segments(int segments) {
            this.segments = segments;
            return this;
        }

        public Builder lifetime(int lifetime) {
            this.lifetime = lifetime;
            return this;
        }

        // adds to lifetime
        public Builder liven(int lifetime) {
            this.lifetime += lifetime;
            return this;
        }

        public Builder animation(float revealDuration, float fadeStart) {
            this.revealDuration = revealDuration;
            this.fadeStart = fadeStart;
            return this;
        }

        // sets the color array to just one color
        public Builder color(int color) {
            this.colors = new int[]{color};
            return this;
        }

        // adds from START to the colors array
        public Builder splash(int color) {
            int[] newList = new int[this.colors.length + 1];
            newList[0] = color;

            System.arraycopy(this.colors, 0, newList, 1, this.colors.length);

            this.colors = newList;
            return this;
        }

        // adds from TAIL to the colors array
        public Builder paint(int color) {
            int[] colorList = Arrays.copyOf(this.colors, this.colors.length + 1);
            colorList[colorList.length - 1] = color;

            this.colors = colorList;
            return this;
        }

        // define a range of colors for the slash to transition through during lifetime
        public Builder colors(int ... colors) {
            this.colors = colors;
            return this;
        }

        public Builder fullBright(boolean fullBright) {
            this.fullBright = fullBright;
            return this;
        }

        public SlashEffect build() {
            return new SlashEffect(this);
        }
    }

    public double getDistance(float progress) {
        if (distances.length == 0) return 0;
        if (distances.length == 1) return distances[0];

        progress = Mth.clamp(progress, 0, 1);

        float scaled = progress * (distances.length - 1);
        int index = Math.min((int) scaled, distances.length - 2);
        float localProgress = scaled - index;

        return Mth.lerp(localProgress, distances[index], distances[index + 1]);
    }

    public boolean isMoving() {
        return distances.length > 1;
    }

    private static void writeDoubleArray(RegistryFriendlyByteBuf buf, double[] values) {
        buf.writeVarInt(values.length);
        for (double value : values) buf.writeDouble(value);
    }

    private static double[] readDoubleArray(RegistryFriendlyByteBuf buf) {
        int length = buf.readVarInt();
        if (length < 0 || length > 64) throw new IllegalArgumentException("distances array length is invalid: " + length);

        double[] values = new double[length];
        for (int i = 0; i < length; i++) values[i] = buf.readDouble();

        return values;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SlashEffect> STREAM_CODEC = StreamCodec.of(
            (buf, effect) -> {
                buf.writeResourceLocation(effect.texture());

                writeDoubleArray(buf, effect.distances());
                buf.writeBoolean(effect.scaling());
                buf.writeDouble(effect.sway());
                buf.writeDouble(effect.lift());
                buf.writeDouble(effect.xOffset());
                buf.writeDouble(effect.yOffset());
                buf.writeDouble(effect.zOffset());
                buf.writeDouble(effect.rotation());

                buf.writeEnum(effect.shape());
                buf.writeDouble(effect.length());
                buf.writeDouble(effect.radius());
                buf.writeDouble(effect.thickness());
                buf.writeDouble(effect.startAngle());
                buf.writeDouble(effect.endAngle());

                buf.writeVarInt(effect.segments());
                buf.writeVarInt(effect.lifetime());

                buf.writeFloat(effect.revealTime());
                buf.writeFloat(effect.fadeStart());

                buf.writeVarIntArray(effect.colors());
                buf.writeBoolean(effect.fullBright());
            },
            buf -> SlashEffect.builder(buf.readResourceLocation())
                    .distances(readDoubleArray(buf))
                    .scaling(buf.readBoolean())
                    .sway(buf.readDouble())
                    .lift(buf.readDouble())
                    .xOffset(buf.readDouble())
                    .yOffset(buf.readDouble())
                    .zOffset(buf.readDouble())
                    .rotation(buf.readDouble())
                    .shape(buf.readEnum(Shape.class))
                    .length(buf.readDouble())
                    .radius(buf.readDouble())
                    .thickness(buf.readDouble())
                    .angles(buf.readDouble(), buf.readDouble())
                    .segments(buf.readVarInt())
                    .lifetime(buf.readVarInt())
                    .animation(buf.readFloat(), buf.readFloat())
                    .colors(buf.readVarIntArray())
                    .fullBright(buf.readBoolean())
                    .build());

    public static SlashEffect getWeaponSlash(Player player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) return SlashEffects.GREEN_HORIZONTAL;

        AttributeInstance attackReach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attackReach == null) return SlashEffects.GREEN_HORIZONTAL;

        double startDist = 1 - (attackReach.getValue() / 4);

        return SlashEffects.HEAVY_RED_DIAGONAL.edit()
                .rotation(90)
                .colors(Color.white.getRGB(), Color.gray.getRGB())
                .distances(startDist, Math.max(startDist, attackReach.getValue() - 2))
                .radius(attackReach.getValue() / 3)
                .thickness(attackReach.getValue() / 12)
                .animation((float) ((0.75f / attackSpeed.getValue())), (float) (0.25f / attackSpeed.getValue()))
                .lifetime(Math.max(2, (int) ((int) attackReach.getValue() * 5 /  attackSpeed.getValue())))
                .build();
    }
}