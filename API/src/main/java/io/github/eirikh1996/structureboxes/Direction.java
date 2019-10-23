package io.github.eirikh1996.structureboxes;

public enum Direction {
    NORTH(360),
    EAST(90),
    SOUTH(180),
    WEST(270);

    private final int angle;
    Direction(int angle){
        this.angle = angle;
    }
    public static Direction fromYaw(final float yaw){
        Direction dir = null;
        double angle = yaw > 0 ? yaw : 360f + yaw;
        if (angle > 315f || angle <=45f){
            dir = SOUTH;
        } else if (angle > 45f && angle <=135f){
            dir = WEST;
        } else if (angle > 135f && angle <=225f){
            dir = NORTH;
        } else if (angle > 225f && angle <=315f){
            dir = EAST;
        }
        return dir;
    }

    public int getAngle(Direction direction){
        int angle = 0;
        switch (this){
            case EAST:
                switch (direction){
                    case EAST:
                        angle = 0;
                        break;
                    case SOUTH:
                        angle = 90;
                        break;
                    case WEST:
                        angle = 180;
                        break;
                    case NORTH:
                        angle = 270;
                        break;
                }
                break;
            case WEST:
                switch (direction){
                    case EAST:
                        angle = 180;
                        break;
                    case SOUTH:
                        angle = 270;
                        break;
                    case WEST:
                        angle = 0;
                        break;
                    case NORTH:
                        angle = 90;
                        break;
                }
                break;
            case NORTH:
                switch (direction){
                    case EAST:
                        angle = 90;
                        break;
                    case SOUTH:
                        angle = 180;
                        break;
                    case WEST:
                        angle = 270;
                        break;
                    case NORTH:
                        angle = 0;
                        break;
                }
                break;
            case SOUTH:
                switch (direction){
                    case EAST:
                        angle = 270;
                        break;
                    case SOUTH:
                        angle = 0;
                        break;
                    case WEST:
                        angle = 90;
                        break;
                    case NORTH:
                        angle = 180;
                        break;
                }
                break;
        }
        return angle;
    }

    public int getAngle() {
        return angle;
    }
}
