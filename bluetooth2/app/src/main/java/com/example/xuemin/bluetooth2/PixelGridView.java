package com.example.xuemin.bluetooth2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PixelGridView extends View {
    private int numColumns, numRows;
    private int cellWidth, cellHeight;
    private int totalWidth, totalHeight;
    private Paint blackPaint = new Paint();
    private Paint whitePaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint robotPaint = new Paint();
    private Paint waypointPaint = new Paint();
    private boolean[][] cellChecked;
    private boolean[][] startCellChecked;
    MapDecoder md = new MapDecoder();

    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        blackPaint.setColor(Color.BLACK);
        redPaint.setColor(Color.RED);
        greenPaint.setColor(Color.GREEN);
        robotPaint.setColor(Color.GRAY);
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

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {

                if (testMap[j][i] == 0)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, whitePaint);
                }
                if (testMap[j][i] == 1)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
                }
                if (testMap[j][i] == 2)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, redPaint);
                }
                /*if (testMap[j][i] == 3)
                {
                    path.moveTo(i + halfWidth, j); // Top
                    path.lineTo(i, j + width); // Bottom left
                    path.lineTo(i + width, j + width); // Bottom right
                    path.lineTo(i + halfWidth, j); // Back to Top
                    path.close();
                    canvas.drawPath(path, robotPaint);
                }
                if (testMap[j][i] == 4)
                {
                    path.moveTo(j + width, i + halfWidth); // Top
                    path.lineTo(j, i); // Bottom left
                    path.lineTo(j, i + width); // Bottom right
                    path.lineTo(j + width, i + halfWidth); // Back to Top
                    path.close();
                    canvas.drawPath(path, robotPaint);
                }
                if(testMap[j][i] == 5){
                    path.moveTo(i + halfWidth, j + width); // Top
                    path.lineTo(i + width, j); // Bottom left
                    path.lineTo(i, j); // Bottom right
                    path.lineTo(i + halfWidth, j + width); // Back to Top
                    path.close();
                    canvas.drawPath(path, robotPaint);
                }
                if(testMap[j][i] == 6){
                    path.moveTo(i, j + halfWidth); // Top
                    path.lineTo(i + width , j + width); // Bottom left
                    path.lineTo(i + width, j); // Bottom right
                    path.lineTo(i, j + halfWidth); // Back to Top
                    path.close();
                    canvas.drawPath(path, robotPaint);
                }

                if(testMap[j][i] == 7) {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, waypointPaint);
                }*/

                if(startCellChecked[i][j]){
                    path.moveTo((i*cellWidth) + halfWidth, (j*cellHeight)); // Top
                    path.lineTo((i*cellWidth), (j*cellHeight) + cellHeight); // Bottom left
                    path.lineTo((i*cellWidth) + cellWidth, (j*cellHeight) + cellHeight); // Bottom right
                    path.lineTo((i*cellWidth) + halfWidth, (j*cellHeight)); // Back to Top
                    path.close();
                    canvas.drawPath(path, robotPaint);
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


    }

    public void clearMap(){
        md.clearMapArray();
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


    public void setCellchecked(int x, int y){
      cellChecked[x][y] = !cellChecked[x][y];
      invalidate();
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
