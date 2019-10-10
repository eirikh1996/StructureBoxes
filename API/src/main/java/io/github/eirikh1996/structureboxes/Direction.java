package io.github.eirikh1996.structureboxes;

import org.bukkit.Bukkit;

public enum Direction {
    NORTH(0), EAST(90), SOUTH(180), WEST(270);

    private final int angle;
    Direction(int angle){
        this.angle = angle;
    }
    public static Direction fromYaw(final float yaw){
        Direction dir = null;
        double angle = yaw > 0 ? yaw : 360f + yaw;
        Bukkit.broadcastMessage(String.valueOf(angle));
        if (angle > 315f || angle <=45f){
            dir = NORTH;
        } else if (angle > 45f && angle <=135f){
            dir = EAST;
        } else if (angle > 135f && angle <=225f){
            dir = SOUTH;
        } else if (angle > 225f && angle <=315f){
            dir = WEST;
        }
        return dir;
    }

    public int getAngle() {
        return angle;
    }
}
