package com.example.xuemin.bluetooth2;

public class Robot {
    private int x, y;
    private String direction;

    public Robot(){

    }

    public Robot(int x, int y, String direction){
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    //use to use value for robot 3x3 position, assigning position[0][0] = 5 means that robot is centered at grid 5
    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;

    }
    //return robot's current position, 3x3 matrix
    public String getPosition()
    {

        return x + "," + y;
    }
    //set where is the robot facing, head of robot
    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    //get where the robot is facing
    public String getDirection()
    {
        return direction;
    }
}
