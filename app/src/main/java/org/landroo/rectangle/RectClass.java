package org.landroo.rectangle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * RectClass
 * Created by rkovacs on 2015.07.06..
 */
public class RectClass
{
    private static final String TAG = "RectClass";
    private int DEBUG = 0;

    public List<PointF> points = new ArrayList<>();// point array

    public float px;// position x
    public float py;// position y

    public float ox;// original position x
    public float oy;// original position y

    public float tx;// temp position x
    public float ty;// temp position y

    public float iw;// item width
    public float ih;// item height

    public int ln;// line
    public int cl;// column

    public PointF lt = new PointF();
    public PointF rb = new PointF();

    private int groupId = -1;// group id

    private Paint whitePaint = new Paint();
    private Paint rectPaint = new Paint();// rectangle paint
    private Paint strokePaint = new Paint();
    private Paint paint = new Paint();// text paint
    private Path path = new Path();// path to draw

    public boolean visible = true;

    public int[] sides = new int[4];

    public List<PointF> sideLines = null;

    /**
     * constructor
     * @param x x
     * @param y x
     * @param line line
     * @param col column
     * @param width width
     * @param height height
     * @param visible visible
     */
    public RectClass(float x, float y, int line, int col, int width, int height, boolean visible)
    {
        this.px = x;
        this.py = y;
        this.ox = x;
        this.oy = y;
        this.ln = line;
        this.cl = col;
        this.iw = width;
        this.ih = height;
        this.visible = visible;

        points.add(new PointF(0, 0));
        points.add(new PointF(iw, 0));
        points.add(new PointF(iw, ih));
        points.add(new PointF(0, ih));

        //rectPaint.setAntiAlias(true);
        //rectPaint.setDither(true);
        rectPaint.setColor(0xFF00FF00);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setStrokeJoin(Paint.Join.ROUND);
        rectPaint.setStrokeCap(Paint.Cap.ROUND);
        rectPaint.setAlpha(191);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5);
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);

        whitePaint.setColor(0xFFFFFFFF);
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setStrokeJoin(Paint.Join.ROUND);
        whitePaint.setStrokeCap(Paint.Cap.ROUND);
        whitePaint.setAlpha(191);

        paint.setTextSize(20);

        setLefTopRightBottom();
    }

    /**
     * draw to canvas
     * @param canvas canvas
     * @param zx zoom x
     * @param zy zoom y
     */
    public void drawRect(Canvas canvas, float zx, float zy, boolean isWhite)
    {
        if(!visible)
            return;

        path.reset();
        path.moveTo(points.get(0).x * zx, points.get(0).y * zy);
        for(int i = 1; i < points.size(); i++)
            path.lineTo(points.get(i).x * zx, points.get(i).y * zy);
        if(isWhite)
            canvas.drawPath(path, whitePaint);
        else
            canvas.drawPath(path, rectPaint);
/*
        if(sides[0] == 1)
            canvas.drawLine(lt.x * zx, lt.y * zy, lt.x * zx, (lt.y + ih) * zy, strokePaint);
        if(sides[2] == 1)
            canvas.drawLine((lt.x + iw) * zx, lt.y * zy, (lt.x + iw) * zx, (lt.y + ih) * zy, strokePaint);
        if(sides[1] == 1)
            canvas.drawLine(lt.x * zx, lt.y * zy, (lt.x + iw) * zx, lt.y * zy, strokePaint);
        if(sides[3] == 1)
            canvas.drawLine(lt.x * zx, (lt.y + ih) * zy, (lt.x + iw) * zx, (lt.y + ih) * zy, strokePaint);
*/
        if(sideLines != null)
        {
            for (int i = 0; i < sideLines.size(); i += 2)
            {
                PointF pnt1 = sideLines.get(i);
                PointF pnt2 = sideLines.get(i + 1);

                canvas.drawLine(pnt1.x * zx, pnt1.y * zy, pnt2.x * zx, pnt2.y * zy, strokePaint);
            }
        }

        if((DEBUG & 2) == 2)
            canvas.drawText("" + groupId, (lt.x + 40) * zx, (lt.y + 40) * zy, paint);
        if((DEBUG & 4) == 4)
            canvas.drawText("" + sides[0] + sides[1] + sides[2] + sides[3], (lt.x + 10) * zx, (lt.y + 40) * zy, paint);
        if((DEBUG & 8) == 8)
            canvas.drawText("" + ln + " " + cl, (lt.x + 40) * zx, (lt.y + 40) * zy, paint);
    }

    public void setGroup(int id)
    {
        groupId = id;
        int color = colors(id + 10, 191);
        rectPaint.setColor(color);
    }

    public void setColor(int color)
    {
        rectPaint.setColor(color);
    }

    public void setAlpha(int alpha)
    {
        rectPaint.setAlpha(alpha);
    }

    public int getGroup()
    {
        return groupId;
    }

    public static int colors(int color, int alpha)
    {
        int uRetCol = 0;
        int colors[] = {
                Color.RED,Color.GREEN,Color.BLUE,Color.MAGENTA,Color.YELLOW,Color.CYAN,Color.WHITE,Color.GRAY,
                0xFFE91C63,0xFF9C27B0,0xFF673AB7,0xFF4051B5,0xFF2196F3,0xFF03A9F4,0xFF00BCD4,0xFF009688,0xFF4CAF50,
                0xFF8BC34A,0xFFCDDC39,0xFFFFEB3B,0xFFFFC107,0xFFFF9800,0xFF795548,0xFF607D8B,0xFF9E9E9E,0xFFFF5722,
                0xFFF44336,0xFFC2165C,0xFF7B1FA2,0xFF512DA8,0xFF32409F,0xFF1976D2,0xFF0288D1,0xFF0097A7,0xFF00796B,
                0xFF388E3C,0xFF689F38,0xFFAFB42B,0xFFFBC02D,0xFFFFA000,0xFFF57C00,0xFF5D4037,0xFF455A64,0xFF616161,
                0xFFE64A19,0xFFD32F2F
        };
        if(color < colors.length)
            uRetCol = colors[color];

        uRetCol = uRetCol & ((alpha << 24) | 0xFFFFFF);

        return uRetCol;
    }

    /**
     * point is inside the polygon
     * @param x x
     * @param y y
     * @param zx zoom x
     * @param zy zoom y
     * @return isInside
     */
    public boolean isInside(float x, float y, float zx, float zy)
    {
        if(!visible)
            return false;

        boolean bIn;
        float[] xa = new float[points.size()];
        float[] ya = new float[points.size()];
        int i = 0;
        for (PointF pnt : points)
        {
            xa[i] = (pnt.x + px) * zx;
            ya[i] = (pnt.y + py) * zy;
            i++;
        }
        bIn = Utils.ponitInPoly(points.size(), xa, ya, x, y);
        if((DEBUG & 1) == 1)
            Log.i(TAG, "" + x + " " + y + " " + xa[0] + " " + ya[0] + " " + bIn);
        return bIn;
    }

    public void setLefTopRightBottom()
    {
        float l = Integer.MAX_VALUE;
        float t = Integer.MAX_VALUE;
        float r = 0;//Integer.MIN_VALUE;
        float b = 0;//Integer.MIN_VALUE;
        for (PointF pnt : points)
        {
            if (pnt.x < l) l = (int) pnt.x;
            if (pnt.y < t) t = (int) pnt.y;
            if (pnt.x > r) r = (int) pnt.x;
            if (pnt.y > b) b = (int) pnt.y;
        }

        lt.set(l, t);
        rb.set(b, r);
    }

}
