package com.example.xuemin.bluetooth2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PixelGridView extends View {
    private int numColumns, numRows;
    private int cellWidth, cellHeight;
    private int totalWidth, totalHeight;
    private Paint whitePaint = new Paint();
    private Paint blackPaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint wpPaint = new Paint();
    private Paint bodyPaint=new Paint();
    private Paint headPaint = new Paint();
    private boolean[][] cellChecked;
    MapDecoder md = new MapDecoder();

    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        bluePaint.setColor(Color.BLUE);
        redPaint.setColor(Color.RED);
        greenPaint.setColor(Color.GREEN);
        headPaint.setColor(Color.MAGENTA);
        wpPaint.setColor(Color.GRAY);
        bodyPaint.setColor(Color.rgb(219,159,68));
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

        for (int i = 0; i < numColumns; i++)
        {
            for (int j = 0; j < numRows ; j++)
            {
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
                if (testMap[j][i] == 3)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, bodyPaint);
                }
                if (testMap[j][i] == 4)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, headPaint);
                }
                if(testMap[j][i] == 5){
                   /* Drawable d = getResources().getDrawable(R.drawable.waypoint_icon);
                    d.setBounds(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight);
                    d.draw(canvas);*/
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, wpPaint);
                }
            }
        }

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j]) {

                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            greenPaint);
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
    public void updateDemoArenaMap(String obstacleMapDes){
        md.updateDemoMapArray(obstacleMapDes);
    }

    public void updateDemoRobotPos(String robotPos){
        md.updateDemoRobotPos(robotPos);
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
