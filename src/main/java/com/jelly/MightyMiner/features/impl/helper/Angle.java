package com.jelly.MightyMiner.features.impl.helper;

public class Angle {
    public final float yaw;
    public final float pitch;

    public Angle(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return false;
        if(!(obj instanceof Angle)) return false;

        Angle ang = (Angle) obj;
        return this.yaw == ang.yaw && this.pitch == ang.pitch;
    }

    @Override
    public String toString() {
        return String.format("Angle(yaw = %f, pitch = %f)", this.yaw, this.pitch);
    }
}