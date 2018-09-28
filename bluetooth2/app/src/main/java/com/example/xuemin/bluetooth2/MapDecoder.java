package com.example.xuemin.bluetooth2;

public class MapDecoder {
    /*
     * Variable and constants
     */
    private String robotPosStr;
    private String exploredMapStr;
    private String mapObjectStr;
    private int[]waypoint = new int[2];
    private boolean wpSet=false;
    //all 0 by default ie. unexplored
    private static int[] mapArray = new int[300];

    public MapDecoder(){
        super();
        clearMapArray();
    }


    public void clearMapArray(){
        mapArray = new int[300];
        robotPosStr = "-1,-1,-1";
        exploredMapStr = "0000000000000000000000000000000000000000000000000000000000000000000000000000";
        mapObjectStr = "0";
    }

    public void updateMapArray(String exploredMap,String obstacleMap){
        mapArray=new int[300];
      /*  waypoint[0]=18;
        waypoint[1]=14;*/
        exploredMapStr = exploredMap;

        mapObjectStr = obstacleMap;

    }

    //update actual robot position
    public void updateRobotPos(String robotPos){
        String robo[] =robotPos.split("\\|");
        if(robo[2].equals("NORTH")){
            robo[2]="180";
        }else if (robo[2].equals("WEST")){
            robo[2]="270";
        }else if(robo[2].equals("EAST")){
            robo[2]="90";
        }else if(robo[2].equals("SOUTH")){
            robo[2]="0";
        }
        String tempStr= String.format("%s,%s,%s",robo[0],robo[1],robo[2]);
        robotPosStr=tempStr;
    }

    //Show Waypoint
    public void showWp(){
        wpSet=true;
    }

    //Hide Waypoint
    public void hideWp(){
        wpSet=false;
    }

    public void updateWaypoint(int x,int y){
        waypoint[0]=y;
        waypoint[1]=x;
    }


    //call this method after updating the map descriptors
    public int[][] decodeMapDescriptor(){

        int[] robotPosArr;
        int[] exploredMapArr;
        int[] mapObjectArr;
        int[][] currentMap;

        robotPosArr = decodeRobotPos();
        exploredMapArr = decodeExploredMap();
        mapObjectArr = decodeMapObject();
        currentMap = updateMap(robotPosArr,exploredMapArr,mapObjectArr,waypoint);
        return currentMap;
    }

    //[1] = row, [2] = col, [3] = orientation
    private int[] decodeRobotPos(){

        //Remove P0 prefix
        //String coordString = robotPosStr.substring(2);
        int [] coord = new int[3];
        String[] coordArr = robotPosStr.split(",");
        coord[0] = 19-Integer.parseInt(coordArr[0].trim()); //y
        coord[1] = Integer.parseInt(coordArr[1].trim()); //x
        coord[2] = Integer.parseInt(coordArr[2].trim()); //orientation

        return coord;
    }

    //value 1: explored, value 0: unexplored
    private int[] decodeExploredMap(){

        //Remove P1 prefix
        //String hexString = exploredMapStr.substring(2);
        int[] binArray = new int[300];
        int arrPos = 0;
        String binString;

        //hexString should be 76 characters
        for (int i= 0; i < exploredMapStr.length(); i++){
            binString = hexToBinary(String.valueOf(exploredMapStr.charAt(i)));
            if (i == 0){
                //ignore the first 2 padding bits
                binArray[arrPos++] = binString.charAt(2) - '0';
                binArray[arrPos++] = binString.charAt(3) - '0';
            }
            else if (i == exploredMapStr.length()-1){
                //ignore the last 2 padding bits
                binArray[arrPos++] = binString.charAt(0) - '0';
                binArray[arrPos++] = binString.charAt(1) - '0';
            }else{
                binArray[arrPos++] = binString.charAt(0) - '0';
                binArray[arrPos++] = binString.charAt(1) - '0';
                binArray[arrPos++] = binString.charAt(2) - '0';
                binArray[arrPos++] = binString.charAt(3) - '0';
            }
        }
        return binArray;
    }

    //value 1: obstacle, value 2: empty
    private int[] decodeMapObject(){
        //Remove P2 prefix
        //String hexString = mapObjectStr.substring(2);
        int[] binArray = new int[mapObjectStr.length()*4];
        int arrPos = 0;
        String binString;

        //hexString is of undetermined length (@compile time)
        for (int i= 0; i < mapObjectStr.length(); i++){
            binString = hexToBinary(String.valueOf(mapObjectStr.charAt(i)));
            //there exist padding bits at the end (0-3 bits), to be
            //process later.
            binArray[arrPos++] = binString.charAt(0) - '0';
            binArray[arrPos++] = binString.charAt(1) - '0';
            binArray[arrPos++] = binString.charAt(2) - '0';
            binArray[arrPos++] = binString.charAt(3) - '0';
        }
        return binArray;
    }

    private String hexToBinary(String hex){
        int i = Integer.parseInt(hex, 16);
        String bin = Integer.toBinaryString(i);
        switch (bin.length()){
            case 1:
                bin = "000"+bin;
                break;
            case 2:
                bin = "00"+bin;
                break;
            case 3:
                bin = "0"+bin;
                break;
            default:
                break;
        }
        return bin;
    }

    private int[][] updateMap(int[] robot, int[] exploredMap, int[] mapObject ,int[] waypoint ){

        int mapArrayPt = 0;
        int[] obstacleMap = mapObjectToExploredMap(exploredMap, mapObject);

        //0 = unexplored
        //1 - explored
        //2 - obstacle
        //3 - robot
        //4 - robot head
        //5 - waypoint

        //loop thru unexplored positions in mapArray to update them
        for (int i =0; i< 300; i++){
            if (mapArray[i] == 0){
                //check if previous unexplored position is now explored
                if (exploredMap[i] == 1){
                    //check if there is any obstacle or is it empty
                    if (obstacleMap[i] == 0){
                        mapArray[i] = 1;
                    }else{
                        mapArray[i] = 2;
                    }
                }
            }
        }

        //convert to 2d array
        int[][] d2MapArray = new int[20][15];
        for (int i = 0; i < 20; i ++){
            for (int j = 0; j < 15; j++){
                d2MapArray[i][j] = mapArray[mapArrayPt++];
            }
        }

        switch (robot[2]){
            case 0:
                d2MapArray[robot[0]][robot[1]] = 3;
                break;
            case 90:
                d2MapArray[robot[0]][robot[1]] = 4;
                break;
            case 180:
                d2MapArray[robot[0]][robot[1]] = 5;
                break;
            case 270:
                d2MapArray[robot[0]][robot[1]] = 6;
                break;
            default:
                break;
        }
        /*if (robot[0]>0 && robot[0]<19 && robot[1]>0 && robot[1]<14){

            //set robot body
            d2MapArray[robot[0]+1][robot[1]-1] = 3;
            d2MapArray[robot[0]+1][robot[1]] = 3;
            d2MapArray[robot[0]+1][robot[1]+1] = 3;
            d2MapArray[robot[0]][robot[1]-1] = 3;
            d2MapArray[robot[0]][robot[1]] = 3;
            d2MapArray[robot[0]][robot[1]+1] = 3;
            d2MapArray[robot[0]-1][robot[1]-1] = 3;
            d2MapArray[robot[0]-1][robot[1]] = 3;
            d2MapArray[robot[0]-1][robot[1]+1] = 3;

            //set robot head based on orientation
            //north/ 0 degree is toward the 0
            switch (robot[2]){
                case 180:
                    d2MapArray[robot[0]+1][robot[1]] = 4;
                    break;
                case 90:
                    d2MapArray[robot[0]][robot[1]+1] = 4;
                    break;
                case 0:
                    d2MapArray[robot[0]-1][robot[1]] = 4;
                    break;
                case 270:
                    d2MapArray[robot[0]][robot[1]-1] = 4;
                    break;
                default:
                    break;
            }
        }*/

        if(wpSet==true)
            d2MapArray[19-waypoint[1]][waypoint[0]] = 7;

        //invert row
        int[][] invertd2MapArray = new int[20][15];
        for (int i =0; i<20;i++){
            for (int j=0;j<15;j++){
                invertd2MapArray[i][j] = d2MapArray[19-i][j];
            }
        }
        return invertd2MapArray;
    }

    private int[] mapObjectToExploredMap(int[] exploredMap, int[] mapObject){
        int[] obstacleMap = new int[300];
        int mapObjectPt = 0;
        for (int i =0; i<300; i++){
            if (exploredMap[i] == 1){
                obstacleMap[i] = mapObject[mapObjectPt++];
            }
        }
        return obstacleMap;
    }

}
