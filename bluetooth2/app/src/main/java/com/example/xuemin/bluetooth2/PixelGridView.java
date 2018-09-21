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
    private Paint blackPaint = new Paint();
    private boolean[][] cellChecked;

    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
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

        if (numColumns == 0 || numRows == 0) {
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j]) {

                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            blackPaint);
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



    public void setCellchecked(int x, int y){
      cellChecked[x][y] = !cellChecked[x][y];
      invalidate();
  }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int column = (int)(event.getX() / cellWidth);
            int row = (int)(event.getY() / cellHeight);

            cellChecked[column][row] = !cellChecked[column][row];
            MazeActivity.setCoordinates(column,row);
            invalidate();
        }

        return true;
    }
}
