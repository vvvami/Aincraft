package net.vami.aincraft.render;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class SlashEffect {

    private final ResourceLocation texture;

    private final double distance;
    private final double endDistance;

    private final double xOffset;
    private final double yOffset;
    private final double zOffset;

    private final double rotation;

    private final double radius;
    private final double thickness;
    private final double startAngle;
    private final double endAngle;

    private final int segments;
    private final int lifetime;

    private final float revealTime;
    private final float fadeStart;

    private final int startColor;
    private final int endColor;

    private final boolean fullBright;

    private SlashEffect(Builder builder) {
        this.texture = builder.texture;
        this.distance = builder.distance;
        this.endDistance = builder.endDistance;
        this.xOffset = builder.xOffset;
        this.yOffset = builder.yOffset;
        this.zOffset = builder.zOffset;
        this.rotation = builder.rotation;
        this.radius = builder.radius;
        this.thickness = builder.thickness;
        this.startAngle = builder.startAngle;
        this.endAngle = builder.endAngle;
        this.segments = builder.segments;
        this.lifetime = builder.lifetime;
        this.revealTime = builder.revealDuration;
        this.fadeStart = builder.fadeStart;
        this.startColor = builder.startColor;
        this.endColor = builder.endColor;
        this.fullBright = builder.fullBright;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public double distance() {
        return distance;
    }

    public double endDistance() {
        return endDistance;
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

    public int startColor() {
        return startColor;
    }

    public int endColor() {
        return endColor;
    }

    public boolean fullBright() {
        return fullBright;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder(ResourceLocation texture) {
        return new Builder(texture);
    }

    public static class Builder {

        private final ResourceLocation texture;

        private double distance = 1.5;
        private double endDistance = 1.5;

        private double xOffset = 0;
        private double yOffset = -0.4;
        private double zOffset = 0;

        private double rotation = 0;

        private double radius = 1.5;
        private double thickness = 0.25;
        private double startAngle = 90;
        private double endAngle = -90;

        private int segments = 12;
        private int lifetime = 8;

        private float revealDuration = 0.4f;
        private float fadeStart = 0.6f;

        private int startColor = 0xFFFFFFFF;
        private int endColor = 0xFFFFFFFF;

        private boolean fullBright = true;

        private Builder(ResourceLocation texture) {
            this.texture = texture;
        }

        private Builder(SlashEffect effect) {
            this.texture = effect.texture;
            this.distance = effect.distance;
            this.endDistance = effect.endDistance;
            this.xOffset = effect.xOffset;
            this.yOffset = effect.yOffset;
            this.zOffset = effect.zOffset;
            this.rotation = effect.rotation;
            this.radius = effect.radius;
            this.thickness = effect.thickness;
            this.startAngle = effect.startAngle;
            this.endAngle = effect.endAngle;
            this.segments = effect.segments;
            this.lifetime = effect.lifetime;
            this.revealDuration = effect.revealTime;
            this.fadeStart = effect.fadeStart;
            this.startColor = effect.startColor;
            this.endColor = effect.endColor;
            this.fullBright = effect.fullBright;
        }

        public Builder distance(double distance) {
            this.distance = distance;
            this.endDistance = distance;
            return this;
        }

        public Builder distance(double startDistance, double endDistance) {
            this.distance = startDistance;
            this.endDistance = endDistance;
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

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder thickness(double thickness) {
            this.thickness = thickness;
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

        public Builder animation(float revealDuration, float fadeStart) {
            this.revealDuration = revealDuration;
            this.fadeStart = fadeStart;
            return this;
        }

        public Builder color(int color) {
            this.startColor = color;
            this.endColor = color;
            return this;
        }

        public Builder colors(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
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

    public static final StreamCodec<RegistryFriendlyByteBuf, SlashEffect> STREAM_CODEC = StreamCodec.of(
            (buf, effect) -> {
                buf.writeResourceLocation(effect.texture());

                buf.writeDouble(effect.distance());
                buf.writeDouble(effect.endDistance());
                buf.writeDouble(effect.xOffset());
                buf.writeDouble(effect.yOffset());
                buf.writeDouble(effect.zOffset());
                buf.writeDouble(effect.rotation());

                buf.writeDouble(effect.radius());
                buf.writeDouble(effect.thickness());
                buf.writeDouble(effect.startAngle());
                buf.writeDouble(effect.endAngle());

                buf.writeVarInt(effect.segments());
                buf.writeVarInt(effect.lifetime());

                buf.writeFloat(effect.revealTime());
                buf.writeFloat(effect.fadeStart());

                buf.writeInt(effect.startColor());
                buf.writeInt(effect.endColor());

                buf.writeBoolean(effect.fullBright());
            },

            buf -> SlashEffect.builder(buf.readResourceLocation())
                    .distance(buf.readDouble(), buf.readDouble())
                    .xOffset(buf.readDouble())
                    .yOffset(buf.readDouble())
                    .zOffset(buf.readDouble())
                    .rotation(buf.readDouble())
                    .radius(buf.readDouble())
                    .thickness(buf.readDouble())
                    .angles(buf.readDouble(), buf.readDouble())
                    .segments(buf.readVarInt())
                    .lifetime(buf.readVarInt())
                    .animation(buf.readFloat(), buf.readFloat())
                    .colors(buf.readInt(), buf.readInt())
                    .fullBright(buf.readBoolean())
                    .build());
}