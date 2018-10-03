package com.example.xuemin.bluetooth2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PixelGridView extends View {
    private int numColumns, numRows;
    private int cellWidth, cellHeight;
    private int totalWidth, totalHeight;
  //  private Paint whitePaint = new Paint();
    private Paint blackPaint = new Paint();
/*
    private Paint bluePaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint wpPaint = new Paint();
    private Paint bodyPaint=new Paint();
    private Paint headPaint = new Paint();
*/
    private Paint whitePaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint robotPaint = new Paint();
    private Paint robotbackPaint = new Paint();
    private Paint waypointPaint = new Paint();


    private int[] arrowCoord = new int[3];
    ArrayList<Integer> arx = new ArrayList<Integer>();
    ArrayList<Integer> ary = new ArrayList<Integer>();
    ArrayList<Integer> ard = new ArrayList<Integer>();
    public boolean arrowpost= false;
    private boolean deleteBitmap = false;
    private Bitmap myImg ;

    private boolean[][] cellChecked;
    private boolean[][] startCellChecked;
    MapDecoder md = new MapDecoder();

    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
/*
        whitePaint.setColor(Color.WHITE);
        bluePaint.setColor(Color.BLUE);
        redPaint.setColor(Color.RED);
        greenPaint.setColor(Color.GREEN);
        headPaint.setColor(Color.MAGENTA);
        wpPaint.setColor(Color.GRAY);
        bodyPaint.setColor(Color.rgb(219,159,68));
*/
        //blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        blackPaint.setColor(Color.BLACK);
        redPaint.setColor(Color.RED);
        greenPaint.setColor(Color.GREEN);
        robotPaint.setColor(Color.GRAY);
        robotbackPaint.setColor(Color.DKGRAY);
        waypointPaint.setColor(Color.BLUE);

    }
    public void setTotalWidth(int totalWidth){
        this.totalWidth = totalWidth;
        calculateDimensions();
    }

    public int getTotalWidth(){
        return totalWidth;
    }

    public void setTotalHeight(int totalHeight){
        this.totalHeight = totalHeight;
        calculateDimensions();
    }

    public int getTotalHeight(){
        return totalHeight;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        calculateDimensions();
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
        calculateDimensions();
    }

    public int getNumRows() {
        return numRows;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        if (numColumns < 1 || numRows < 1) {
            return;
        }
        cellWidth = getMeasuredWidth() / numColumns;
        cellHeight = getMeasuredHeight()/ numRows;

        cellChecked = new boolean[numColumns][numRows];
        startCellChecked = new boolean[numColumns][numRows];
        invalidate();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        int[][] testMap = md.decodeMapDescriptor();

        if (numColumns == 0 || numRows == 0) {
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int halfWidth = cellWidth / 2;
        Path path = new Path();

        //startCellChecked[13][2] = true;

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {

                /*if (testMap[j][i] == 0)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, whitePaint);
                }*/
                if (testMap[j][i] == 1)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
                }
                if (testMap[j][i] == 2)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, redPaint);
                }
                if (testMap[j][i] == 3)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, robotbackPaint);
                }

                if (testMap[j][i] == 4)
                {

                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, robotPaint);
                    /*if (i > 0 && i < 14 && j > 0 && j < 19) {
                        path.moveTo((i*cellWidth) + halfWidth, (j*cellHeight) - cellHeight); // Top
                        path.lineTo((i*cellWidth) - cellWidth, (j*cellHeight) + (2*cellHeight)); // Bottom left
                        path.lineTo((i*cellWidth) + (2*cellWidth), (j*cellHeight) + (2*cellHeight)); // Bottom right
                        path.lineTo((i*cellWidth) + halfWidth, (j*cellHeight) - cellHeight); // Back to Top
                        path.close();
                        canvas.drawPath(path, robotPaint);
                    }*/
                }
                if(testMap[j][i] == 5){

                }
                if(testMap[j][i] == 6){

                }

                if(testMap[j][i] == 7) {

                }
                if(testMap[j][i] == 8){
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, waypointPaint);
                }

                if(startCellChecked[i][j]){
                    if (i > 0 && i < 14 && j > 0 && j < 19) {
                        path.moveTo((i*cellWidth) + halfWidth, (j*cellHeight) - cellHeight); // Top
                        path.lineTo((i*cellWidth) - cellWidth, (j*cellHeight) + (2*cellHeight)); // Bottom left
                        path.lineTo((i*cellWidth) + (2*cellWidth), (j*cellHeight) + (2*cellHeight)); // Bottom right
                        path.lineTo((i*cellWidth) + halfWidth, (j*cellHeight) - cellHeight); // Back to Top
                        path.close();
                        canvas.drawPath(path, robotPaint);
                    }
                   /* path.moveTo((i*cellWidth) + halfWidth, (j*cellHeight)); // Top
                    path.lineTo((i*cellWidth), (j*cellHeight) + cellHeight); // Bottom left
                    path.lineTo((i*cellWidth) + cellWidth, (j*cellHeight) + cellHeight); // Bottom right
                    path.lineTo((i*cellWidth) + halfWidth, (j*cellHeight)); // Back to Top
                    path.close();
                    canvas.drawPath(path, robotPaint);*/


                }
                if (cellChecked[i][j]) {

                    /*path.moveTo(i + halfWidth, j); // Top
                    path.lineTo(i, j + cellWidth); // Bottom left
                    path.lineTo(i + cellWidth, j + cellWidth); // Bottom right
                    path.lineTo(i + halfWidth, j); // Back to Top
                    path.close();
                    canvas.drawPath(path, robotPaint);*/

                    /*canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,

                            greenPaint);

                            blackPaint); */

                }
            }
        }

        for (int i = 1; i <= numColumns; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 1; i <= numRows; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }

        if(arrowpost){
            int x  = arrowCoord[0];
            int y  = arrowCoord[1];
            int d = arrowCoord[2];
            arx.add(x);
            ary.add(y);
            ard.add(d);
            myImg = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
            for(int i=0;i<arx.size();i++) {
                Bitmap scaledImg = getResizedBitmap(myImg,ard.get(i));
                canvas.drawBitmap(scaledImg,(arx.get(i)*cellWidth)+5,(ary.get(i)*cellHeight)+10,null);
            }
        }

        if(deleteBitmap){
            myImg = BitmapFactory.decodeResource(getResources(), R.drawable.rectangle);
            for(int i=0;i<arx.size();i++) {
                Bitmap scaledImg = getResizedBitmap(myImg, ard.get(i));
                canvas.drawBitmap(scaledImg, (arx.get(i) * cellWidth) + 5, (ary.get(i) * cellHeight) + 10, null);
            }
        }


    }
    public Bitmap getResizedBitmap(Bitmap bm,int direction) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = (float)0.07620991;
        float scaleHeight = (float)0.07620991;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        Log.d("this is my array", "arr: " +scaleWidth);
        Log.d("this is my array", "arr: " +scaleHeight);
        matrix.postScale(scaleWidth, scaleHeight);
        switch (direction){
            case 0:
                matrix.postRotate(0);
                break;
            case 1:
                matrix.postRotate(90);
                break;
            case 2:
                matrix.postRotate(180);
                break;
            case 3:
                matrix.postRotate(270);
                break;

            default:
                break;
        }

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }


    public void clearMap(){
        md.clearMapArray();
        deleteBitmap = true;
        arrowpost = false;
        arx.clear();
        ary.clear();
        ard.clear();
    }

    public void wpShow(){
        md.showWp();
    }

    public void wpHide(){
        md.hideWp();
    }

    public void setWaypoint(int x,int y){
        md.updateWaypoint(y,x);
    }

    public void updateArena(String exploredMap, String obstacleMap){
        md.updateMapArray(exploredMap,obstacleMap);
    }

    public void updateRobotPos(String robotPos){
        md.updateRobotPos(robotPos);
    }


    public void getArrowPosition(String arrowPost){
        arrowCoord = md.decodeArrowPosition(arrowPost);
        // MazeActivity.setCoordinates(arrowCoord[0],arrowCoord[1]);
    }

    public void setCellchecked(int x, int y){

      cellChecked[x][y] = !cellChecked[x][y];
      invalidate();
  }
    public void updateDemoArenaMap(String obstacleMapDes){
        md.updateDemoMapArray(obstacleMapDes);
    }


    public void updateDemoRobotPos(String robotPos){
        md.updateDemoRobotPos(robotPos);
    }

  public void setStartPoint(int x, int y){
      startCellChecked[x][y] = true;
      invalidate();
  }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int column = (int)(event.getX() / cellWidth);
            int row = (int)(event.getY() / cellHeight);

            //cellChecked[column][row] = !cellChecked[column][row];
            MazeActivity.setCoordinates(column,19-row);
            invalidate();
        }

        return true;
    }
}
