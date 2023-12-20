package org.landroo.rectangle;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * RectGroup
 * Created by rkovacs on 2015.07.15..
 */
public class RectGroup
{
    private static final String TAG = "RectGroup";
    private int DEBUG = 8;

    public List<RectClass> rectList = new ArrayList<>();// main item list
    public List<Integer> groupIds = new ArrayList<>();// group ids
    public List<RectF> groupRects = new ArrayList<>();// group bounds
    public List<PointF> groupPos = new ArrayList<>();// group positions
    public List<Integer> groupRot = new ArrayList<>();// group rotations

    public List<String[]> patternList = new ArrayList<>();

    public List<SideClass> strokePath = new ArrayList<>();
    public RectF strokeRect = new RectF();
    public Paint strokePaint = new Paint();
    public int sx, sy;

    public int rectNum = 0;

    private int xSize;
    private int ySize;
    private int rectSize;
    private String[] shape;

    private class SideClass
    {
        public float sx;
        public float sy;
        public float ex;
        public float ey;

        public SideClass(float sx, float sy, float ex, float ey)
        {
            this.sx = sx;
            this.sy = sy;
            this.ex = ex;
            this.ey = ey;
        }
    }

    /**
     * constructor
     */
    public RectGroup()
    {
        if((DEBUG & 1) == 1)
            Log.i(TAG, "RectGroup constructor");

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5);
    }

    /**
     * create rectangle shape
     * @param width width
     * @param height heigth
     * @param xPos position x
     * @param yPos posititn y
     * @param pattern
     * @param size
     */
    public void createRectList(float width, float height, float xPos, float yPos, String[] pattern, int size)
    {
        rectList.clear();

        rectSize = size;
        shape = pattern;

        xSize = pattern[0].length();
        ySize = pattern.length;

        float px = (width - xSize * rectSize) / 2;
        float py = (height - ySize * rectSize) / 2;

        // round position to the size
        px = (((int)((px + rectSize / 2) / rectSize)) * rectSize);
        py = (((int)((py + rectSize / 2) / rectSize)) * rectSize);

        xPos = (((int)((xPos + rectSize / 2) / rectSize)) * rectSize);
        yPos = (((int)((yPos + rectSize / 2) / rectSize)) * rectSize);

        rectNum = 0;

        int x = 0;
        int y;
        boolean vis;
        for(y = 0; y < ySize; y++)
        {
            for(x = 0; x < xSize; x++)
            {
                vis = pattern[y].substring(x, x + 1).equals("x");
                RectClass rect = new RectClass(px + x * rectSize - xPos, py + y * rectSize - yPos, x, y, rectSize, rectSize, vis);
                rectList.add(rect);
                if(vis)
                    rectNum++;
            }
        }

        sx = (int)(px - xPos);
        sy = (int)(py - yPos);
        strokeRect.set(0, 0, x * rectSize, y * rectSize);
        //Log.i(TAG, "" + " " + sx + " " + sy + " " + strokeRect);

        connectRects();
    }

    /**
     * make rectangle groups
     */
    public void connectRects()
    {
        int group = 0;
        int act = 0;
        RectClass item;

        // create random shapes
        do
        {
            item = rectList.get(act);
            addToGroup(item, group, 1);
            group++;
            act = getAlone();
        }
        while(act != -1);

        // add remains to the closest group
        addAlone(group);

        // set groups position
        setGroupData(group);

        // order grpus
        orderGroups(-1);

        // set whole shape border
        setBorder();

        // set group border
        setGroupBorders();
    }

    /**
     * set groups position list and bounding rectangle list
     * @param group group
     */
    public void setGroupData(int group)
    {
        groupIds.clear();
        groupPos.clear();
        groupRects.clear();
        groupRot.clear();

        RectF rect;
        PointF pnt;
        for(int i = 0; i < group; i++)
        {
            // calculate the left top and the right bottom of the group
            rect = calcGroupPosRect(i);
            if(rect != null && rect.left != 0)
            {
                groupIds.add(i);
                pnt = new PointF(rect.left, rect.top);
                groupPos.add(pnt);
                rect.set(0, 0, rect.right - rect.left, rect.bottom - rect.top);
                //Log.i(TAG, "" + (rect.right - rect.left)  + " " + (rect.bottom - rect.top));
                groupRects.add(rect);
                groupRot.add(0);
                if((DEBUG & 2) == 2)
                    Log.i(TAG, "" + i + " " + rect + " " + pnt);
            }
        }
    }

    /**
     * add a rectangle to a group recursive
     * @param item item
     * @param group group
     * @param cnt cnt
     * @return num
     */
    private int addToGroup(RectClass item, int group, int cnt)
    {
        // fragment number cannot be higher than the size
        if(cnt > Math.sqrt(rectList.size()))
            return cnt;

        cnt++;

        List<RectClass> sides = new ArrayList<>();
        RectClass tmp;

        item.setGroup(group);

        // left
        tmp = getOnPos(item.ln - 1, item.cl, 1, 0);
        if(tmp != null)
            sides.add(tmp);
        // right
        tmp = getOnPos(item.ln + 1, item.cl, 1, 0);
        if(tmp != null)
            sides.add(tmp);
        // top
        tmp = getOnPos(item.ln, item.cl - 1, 1, 0);
        if(tmp != null)
            sides.add(tmp);
        // bottom
        tmp = getOnPos(item.ln, item.cl + 1, 1, 0);
        if(tmp != null)
            sides.add(tmp);

        int rnd;
        switch(sides.size())
        {
            case 1:
                cnt = addToGroup(sides.get(0), group, cnt);
                break;
            case 2:
                rnd = Utils.random(0, 12, 1);
                if(rnd < 6)
                    cnt = addToGroup(sides.get(0), group, cnt);
                else
                    cnt = addToGroup(sides.get(1), group, cnt);
                break;
            case 3:
                rnd = Utils.random(0, 12, 1);
                if(rnd < 4)
                {
                    cnt = addToGroup(sides.get(0), group, cnt);
                    cnt = addToGroup(sides.get(1), group, cnt);
                }
                else if(rnd >= 4 && rnd < 8)
                {
                    cnt = addToGroup(sides.get(1), group, cnt);
                    cnt = addToGroup(sides.get(2), group, cnt);
                }
                else
                {
                    cnt = addToGroup(sides.get(0), group, cnt);
                    cnt = addToGroup(sides.get(2), group, cnt);
                }
                cnt++;
                break;
            default:
        }

        return cnt;
    }

    /**
     * get a rect without group
     * @return id
     */
    private int getAlone()
    {
        for(int i = 0; i < rectList.size(); i++)
        {
            if(rectList.get(i).getGroup() == -1)
                return i;
        }

        return -1;
    }

    /**
     * get a rectangle by x, y position
     * @param x x
     * @param y y
     * @param mode mode
     * @return rect
     */
    private RectClass getOnPos(int x, int y, int mode, int group)
    {
        for(RectClass item: rectList)
        {
            if(mode == 1)
            {
                if (item.ln == x && item.cl == y && item.getGroup() == -1 && item.visible)
                    return item;
            }
            if(mode == 2)
            {
                if(item.ln == x && item.cl == y && item.visible)
                    return item;
            }
            if(mode == 3)
            {
                if(item.ln == x && item.cl == y && item.visible && item.getGroup() == group)
                    return item;
            }
        }

        return null;
    }

    /**
     * count the member of the group by the id
     * @param id group id
     * @return rectangle number
     */
    private int countGroup(int id)
    {
        int cnt = 0;
        for(RectClass item: rectList)
        {
            if(item.getGroup() == id && item.visible)
                cnt++;
        }

        return cnt;
    }

    /**
     * add rectangle to the nearest group if the group has only one member
     * @param group group
     */
    private void addAlone(int group)
    {
        int cnt;
        RectClass item;

        // count group members
        for(int i = 0; i < group; i++)
        {
            cnt = countGroup(i);
            //Log.i(TAG, "" + i + " " + cnt + " " + group);
            // if group has only one member
            if(cnt == 1)
            {
                // find the group id
                for (RectClass rect : rectList)
                {
                    if (rect.getGroup() == i && rect.visible)
                    {
                        // get item neighborhoods
                        List<Integer> ids = new ArrayList<>();
                        List<Integer> num = new ArrayList<>();
                        // left
                        item = getOnPos(rect.ln - 1, rect.cl, 2, 0);
                        if(item != null && !ids.contains(item.getGroup()))
                        {
                            ids.add(item.getGroup());
                            num.add(countGroup(item.getGroup()));
                        }
                        // right
                        item = getOnPos(rect.ln + 1, rect.cl, 2, 0);
                        if(item != null && !ids.contains(item.getGroup()))
                        {
                            ids.add(item.getGroup());
                            num.add(countGroup(item.getGroup()));
                        }
                        // top
                        item = getOnPos(rect.ln, rect.cl - 1, 2, 0);
                        if(item != null && !ids.contains(item.getGroup()))
                        {
                            ids.add(item.getGroup());
                            num.add(countGroup(item.getGroup()));
                        }
                        // bottom
                        item = getOnPos(rect.ln, rect.cl + 1, 2, 0);
                        if(item != null && !ids.contains(item.getGroup()))
                        {
                            ids.add(item.getGroup());
                            num.add(countGroup(item.getGroup()));
                        }
                        //Log.i(TAG, "side " + ids + " " + num);
                        // select the smaller
                        int big = Integer.MAX_VALUE;
                        int sm = 0;
                        for(int j = 0; j < num.size(); j++)
                        {
                            if(num.get(j) < big && num.get(j) > 1)
                            {
                                big = num.get(j);
                                sm = ids.get(j);
                            }
                        }
                        //Log.i(TAG, "sm " + sm);
                        // add item to group
                        if(ids.size() > 0)
                            rect.setGroup(sm);

                        break;
                    }
                }
            }
        }
    }

    /**
     * calculate the bounding rectangle of the group
     * @param id id
     * @return rect
     */
    private RectF calcGroupPosRect(int id)
    {
        RectF rect = new RectF();
        float gl = Float.MAX_VALUE;
        float gt = Float.MAX_VALUE;
        float gr = Float.MIN_VALUE;
        float gb = Float.MIN_VALUE;

        for(RectClass item: rectList)
        {
            if (item.getGroup() == id && item.visible)
            {
                if(item.px < gl) gl = item.px;
                if(item.py < gt) gt = item.py;
                if(item.px + item.iw > gr) gr = item.px + item.iw;
                if(item.py + item.ih > gb) gb = item.py + item.ih;

                //Log.i(TAG, "" + id + " " + gl + " " + item.px + " " + gt + " " + item.py + " " + gr + " " + gb);

                rect.set(gl, gt, gr, gb);
            }
        }

        return rect;
    }

    /**
     * calculate the size of the group
     * @param id group id
     * @return bounding rectangle
     */
    private RectF calcGroupRect(int id)
    {
        RectF rect = new RectF();
        float gl = Float.MAX_VALUE;
        float gt = Float.MAX_VALUE;
        float gr = 0;//Float.MIN_VALUE;
        float gb = 0;//Float.MIN_VALUE;

        for(RectClass item: rectList)
        {
            if (item.getGroup() == id && item.visible)
            {
                for(PointF pnt: item.points)
                {
                    if (item.px + pnt.x < gl) gl = item.px + pnt.x;
                    if (item.py + pnt.y < gt) gt = item.py + pnt.y;
                    if (item.px + pnt.x > gr) gr = item.px + pnt.x;
                    if (item.py + pnt.y > gb) gb = item.py + pnt.y;
                }
                rect.set(gl, gt, gr, gb);
            }
        }

        return rect;
    }

    /**
     * set the alpha channel of the item
     * @param id id
     * @param alpha alpha
     */
    public void setGroupAlpha(int id, int alpha)
    {
        for(RectClass item: rectList)
        {
            if(item.getGroup() == id)
                item.setAlpha(alpha);
        }
    }

    /**
     * rotate the selected item by angle
     * @param id int
     * @param quoter int
     * @param setRot boolean
     */
    public void rotateGroup(int id, int quoter, boolean setRot)
    {
        double u, v, rad;
        int rot = 0;
        PointF pnt;
        RectF rect;
        for(RectClass item: rectList)
        {
            if (item.getGroup() == id)
            {
                rad = quoter * Math.PI / 2;
                pnt = getGroupPos(id);
                rect = getGroupRect(id);
                u = pnt.x - item.px + rect.width() / 2;
                v = pnt.y - item.py + rect.height() / 2;
                //Log.i(TAG, "u " + u + " v " + v + " r " + quoter);
                for(PointF p: item.points)
                {
                    pnt = Utils.rotatiePnt(u, v, p.x, p.y, rad);
                    //Log.i(TAG, "rx " + p.x + " ry " + p.y);
                    //p.set(((int)((pnt.x + 2) / 4) * 4), ((int)((pnt.y + 2) / 4) * 4));
                    p.set(Math.round(pnt.x), Math.round(pnt.y));
                }
                //item.px += dx;
                //item.py += dy;

                //Log.i(TAG, "u " + u + " v " + v);
                if(item.sideLines != null)
                {
                    for (PointF p: item.sideLines)
                    {
                        if (p != null)
                        {
                            pnt = Utils.rotatiePnt(u, v, p.x, p.y, rad);
                            //Log.i(TAG, "sx " + p.x + " sy " + p.y);
                            //p.set(((int) ((pnt.x + 2) / 4) * 4), ((int) ((pnt.y + 2) / 4) * 4));
                            p.set(Math.round(pnt.x), Math.round(pnt.y));
                        }
                    }
                }

                item.setLefTopRightBottom();
            }
        }

        if(setRot)
        {
            rot = getGroupRot(id);
            rot += quoter;
            setGroupRot(id, rot);
        }

    }

    /**
     * order each other group members or to last the selected if id is -1
     * @param id id
     */
    public void orderGroups(int id)
    {
        if(id == -1)
        {
            Collections.sort(rectList, new CustomComparator());
        }
        else
        {
            List<RectClass> newList = new ArrayList<>();
            for(RectClass item: rectList)
            {
                if (item.getGroup() != id)
                    newList.add(item);
            }
            for(RectClass item: rectList)
            {
                if (item.getGroup() == id)
                    newList.add(item);
            }
            rectList = newList;
        }
    }

    /**
     * ordering template class
     */
    public class CustomComparator implements Comparator<RectClass>
    {
        @Override
        public int compare(RectClass o1, RectClass o2)
        {
            return o1.getGroup() < o2.getGroup() ? 0: 1;
        }
    }

    /**
     * set the group to rounded position
     * @param id id
     * @param round round
     */
    public void setToPos(int id, int round)
    {
        float dx = -1;
        float dy = -1;
        for(RectClass item: rectList)
        {
            if (item.getGroup() == id && item.visible)
            {
                if(dx == -1 && dy == -1)
                {
                    dx = (((int)((item.px + round / 2) / round)) * round);
                    dy = (((int)((item.py + round / 2) / round)) * round);
                    dx -= item.px;
                    dy -= item.py;
                }
                item.px += dx;
                item.py += dy;
                //Log.i(TAG, "" + item.px + " " + item.py);
                item.setLefTopRightBottom();
            }
        }
        Log.i(TAG, "" + dx + " " + dy);
    }

    /**
     * return the bounding rectangle of the selected item
     * @param id id
     * @return rect
     */
    public RectF getGroupRect(int id)
    {
        for(int i = 0; i < groupIds.size(); i++)
        {
            if (groupIds.get(i) == id)
            {
                return groupRects.get(i);
            }
        }

        return null;
    }

    /**
     * return the position of the selected item
     * @param id group id
     * @return group position
     */
    public PointF getGroupPos(int id)
    {
        for(int i = 0; i < groupIds.size(); i++)
        {
            if (groupIds.get(i) == id)
            {
                return groupPos.get(i);
            }
        }

        return null;
    }

    /**
     * position the group
     * @param id group id
     * @param x x
     * @param y y
     */
    public void setGroupPos(int id, float x, float y)
    {
        for(int i = 0; i < groupIds.size(); i++)
        {
            if (groupIds.get(i) == id)
            {
                groupPos.get(i).set(x, y);
            }
        }
    }

    public int getGroupRot(int id)
    {
        for(int i = 0; i < groupIds.size(); i++)
        {
            if (groupIds.get(i) == id)
            {
                return groupRot.get(i);
            }
        }

        return 0;
    }

    public void setGroupRot(int id, int rot)
    {
        for(int i = 0; i < groupIds.size(); i++)
        {
            if (groupIds.get(i) == id)
            {
                groupRot.set(i, rot);
                return;
            }
        }
    }

    /**
     * recalculate the bounding rectangle of an item
     * @param id id
     */
    public void setBoundRect(int id)
    {
        PointF pnt;
        RectF rect = calcGroupRect(id);
        for(int i = 0; i < groupIds.size(); i++)
        {
            if (groupIds.get(i) == id)
            {
                pnt = new PointF(rect.left, rect.top);
                groupPos.set(i, pnt);
                rect.set(0, 0, rect.right - rect.left, rect.bottom - rect.top);
                //Log.i(TAG, "" + (rect.right - rect.left)  + " " + (rect.bottom - rect.top));
                groupRects.set(i, rect);
                if((DEBUG & 4) == 4)
                    Log.i(TAG, "" + id + " " + rect);
                break;
            }
        }
    }

    /**
     * check every rectangle placed on right place
     * @return end
     */
    public boolean checkEnd()
    {
        boolean bOK = false;
        int x;
        int y;
        for(y = 0; y < ySize; y++)
        {
            for(x = 0; x < xSize; x++)
            {
                if(shape[y].substring(x, x + 1).equals("x"))
                {
                    bOK = checkPos(x, y);
                    if(!bOK)
                        return false;
                }
            }
        }

        return bOK;
    }

    /**
     * check rectangle left up position
     * @param x left
     * @param y up
     * @return found on this position
     */
    private boolean checkPos(int x, int y)
    {
        for(RectClass rect: rectList)
        {
            if (rect.visible)
            {
                //Log.i(TAG, "" + rect.l + " " + (sx + x * rectSize));
                //if(Math.round(rect.px + rect.lt.x) == sx + x * rectSize && Math.round(rect.py + rect.lt.y) == sy + y * rectSize)
                    //return true;

                if(Math.abs(Math.round(rect.px + rect.lt.x) - (sx + x * rectSize)) < 2 && Math.abs(Math.round(rect.py + rect.lt.y) - (sy + y * rectSize)) < 2)
                    return true;
            }
        }
        //Log.i(TAG, "" + x + " " + y);
        return false;
    }

    /**
     * create all shapes border line
     */
    private void setBorder()
    {
        strokePath.clear();

        RectClass item;
        for(RectClass rect: rectList)
        {
            if(rect.visible)
            {
                // left
                item = getOnPos(rect.ln - 1, rect.cl, 2, 0);
                if (item == null)
                    strokePath.add(new SideClass(rect.px - sx, rect.py - sy, rect.px - sx, rect.py + rectSize - sy));
                // right
                item = getOnPos(rect.ln + 1, rect.cl, 2, 0);
                if (item == null)
                    strokePath.add(new SideClass(rect.px + rectSize - sx, rect.py - sy, rect.px + rectSize - sx, rect.py + rectSize - sy));
                // top
                item = getOnPos(rect.ln, rect.cl - 1, 2, 0);
                if (item == null)
                    strokePath.add(new SideClass(rect.px - sx, rect.py - sy, rect.px + rectSize - sx, rect.py - sy));
                // bottom
                item = getOnPos(rect.ln, rect.cl + 1, 2, 0);
                if (item == null)
                    strokePath.add(new SideClass(rect.px - sx, rect.py + rectSize - sy, rect.px + rectSize - sx, rect.py + rectSize - sy));
            }
        }
    }

    // draw shape border
    public void drawBorder(Canvas canvas, float zx, float zy)
    {
        for(SideClass side: strokePath)
            canvas.drawLine(side.sx * zx, side.sy * zy, side.ex * zx, side.ey* zy, strokePaint);
    }

    /**
     * create group border line
     * @param id int
     */
    public void setGroupBorder(int id)
    {
        for(RectClass rect: rectList)
        {
            if(rect.getGroup() == id && rect.visible)
            {
                // left
                rect.sides[0] = 0;
                if(!getNext(rect.px + rect.lt.x - rectSize, rect.py + rect.lt.y, id))
                    rect.sides[0] = 1;
                // right
                rect.sides[2] = 0;
                if(!getNext(rect.px + rect.lt.x + rectSize, rect.py + rect.lt.y, id))
                    rect.sides[2] = 1;
                // top
                rect.sides[1] = 0;
                if(!getNext(rect.px + rect.lt.x, rect.py + rect.lt.y - rectSize, id))
                    rect.sides[1] = 1;
                // bottom
                rect.sides[3] = 0;
                if(!getNext(rect.px + rect.lt.x, rect.py + rect.lt.y + rectSize, id))
                    rect.sides[3] = 1;

                List<PointF> pnts = new ArrayList();
                addSides(rect, pnts);
                rect.sideLines = pnts;
            }
        }
    }

    /**
     * add side points
     * @param rect RectClass
     * @param pnts List<PointF
     */
    private void addSides(RectClass rect, List<PointF> pnts)
    {
        PointF pnt;
        // left
        if(rect.sides[0] == 1)
        {
            pnt = new PointF(rect.lt.x, rect.lt.y);
            pnts.add(pnt);
            pnt = new PointF(rect.lt.x, rect.lt.y + rectSize);
            pnts.add(pnt);
        }
        // right
        if(rect.sides[2] == 1)
        {
            pnt = new PointF(rect.lt.x + rectSize, rect.lt.y);
            pnts.add(pnt);
            pnt = new PointF(rect.lt.x + rectSize, rect.lt.y + rectSize);
            pnts.add(pnt);
        }
        // top
        if(rect.sides[1] == 1)
        {
            pnt = new PointF(rect.lt.x, rect.lt.y);
            pnts.add(pnt);
            pnt = new PointF(rect.lt.x + rectSize, rect.lt.y);
            pnts.add(pnt);
        }
        // bottom
        if(rect.sides[3] == 1)
        {
            pnt = new PointF(rect.lt.x, rect.lt.y + rectSize);
            pnts.add(pnt);
            pnt = new PointF(rect.lt.x + rectSize, rect.lt.y + rectSize);
            pnts.add(pnt);
        }
    }

    /**
     * next rectangle is a group member
     * @param x float
     * @param y float
     * @param id int
     * @return
     */
    private boolean getNext(float x, float y, int id)
    {
        for(RectClass rect: rectList)
        {
            if(rect.visible && rect.getGroup() == id
                    && Math.round(rect.px + rect.lt.x) > Math.round(x - 5) && Math.round(rect.px + rect.lt.x) < Math.round(x + 5)
                    && Math.round(rect.py + rect.lt.y) > Math.round(y - 5) && Math.round(rect.py + rect.lt.y) < Math.round(y + 5))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * set all group border
     */
    public void setGroupBorders()
    {
        for(int i = 0; i < groupIds.size(); i++)
            setGroupBorder(groupIds.get(i));
    }

    /**
     * place the elements to a random place
     * @param dist distance
     * @param round grid points
     */
    public void randomBlocks(int dist, int round, int mode)
    {
        for (int i = 0; i < groupIds.size(); i++)
        {
            int dx = Utils.random(0, dist, 1) - dist / 2;
            int dy = Utils.random(0, dist, 1) - dist / 2;
            dx = (((dx + round / 2) / round)) * round;
            dy = (((dy + round / 2) / round)) * round;

            for (RectClass rect : rectList)
            {
                if (rect.visible && rect.getGroup() == groupIds.get(i))
                {
                    rect.px += dx;
                    rect.py += dy;
                }
            }

            setBoundRect(groupIds.get(i));
        }

        // flip
        if ((mode & 2) == 2)
        {
            for (int i = 0; i < groupIds.size(); i++)
            {
                int mul = Utils.random(0, 2, 1);
                if (mul == 0)
                    flipGroup(groupIds.get(i), true);
                if (mul == 1)
                    flipGroup(groupIds.get(i), false);
            }
        }

        // rotate
        if ((mode & 1) == 1)
        {
            for (int i = 0; i < groupIds.size(); i++)
            {
                int mul = Utils.random(0, 3, 1);
                rotateGroup(groupIds.get(i), mul, true);
            }
        }

    }

    /**
     * load patterns to a list
     * @param context context
     */
    public void loadPatterns(Context context)
    {
        String sFile = context.getCacheDir().getPath();
        File f = new File(sFile);
        File[] files = f.listFiles();
        for (File file : files)
        {
            if (!file.isDirectory() && file.getName().contains("txt"))
            {
                addFile(file);
            }
        }
    }

    /**
     * add a pattern file to the list
     * @param file file
     */
    private void addFile(File file)
    {
        try
        {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            int cnt = 0;
            String line = "";

            while(line != null)
            {
                line = br.readLine();
                if(line != null)
                    cnt++;
            }
            br.close();

            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String[] pattern = new String[cnt];
            line = "";
            cnt = 0;
            while(line != null)
            {
                line = br.readLine();
                if(line != null)
                    pattern[cnt] = line;
                cnt++;
            }

            patternList.add(pattern);

            br.close();
        }
        catch(Exception ex)
        {
            Log.i(TAG, "Error in load pattern! " + ex);
        }
    }

    /**
     * extract asset files to the temporary directory
     * @param context context
     */
    public synchronized void extractAssets(Context context)
    {
        // check first asset file is copied
        String sFile = context.getCacheDir().getPath() + "/025_00.txt";
        File f = new File(sFile);
        if (!f.exists())
        {
            copyAllAsset(context);
        }
    }

    /**
     * copy asset file to the temporary directory
     * @param context context
     */
    private void copyAllAsset(Context context)
    {
        AssetManager assetManager = context.getAssets();
        String[] files;
        try
        {
            files = assetManager.list("");
        }
        catch (Exception ex)
        {
            Log.i(TAG, "Error in copyAssets! " + ex);
            return;
        }

        String sTmpFolder = context.getCacheDir().getPath();

        for (String filename : files)
        {
            InputStream in;
            OutputStream out;
            if(filename.contains("txt"))
            {
                try
                {
                    in = assetManager.open(filename);
                    out = new FileOutputStream(sTmpFolder + "/" + filename);
                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();
                }
                catch (Exception ex)
                {
                    Log.i(TAG, "Error in copyFile " + ex);
                }
            }
        }
    }

    /**
     * copy a file
     * @param fin inFile
     * @param fout outFile
     * @throws IOException
     */
    private void copyFile(InputStream fin, OutputStream fout) throws IOException
    {
        byte[] b = new byte[65536];
        int noOfBytes;

        // read bytes from source file and write to destination file
        while ((noOfBytes = fin.read(b)) != -1)
            fout.write(b, 0, noOfBytes);
    }

    public boolean checkOver(int id, float zx, float zy)
    {
        for(RectClass rect: rectList)
        {
            if(rect.getGroup() == id)
            {
                for(RectClass item: rectList)
                {
                    if(item.getGroup() != id)
                    {
                        for (PointF pnt : rect.points)
                        {
                            if (item.isInside(item.px + pnt.x, item.py + pnt.y, zx, zy))
                                return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * flip let to right or up to down the group
     * @param id group id
     * @param vertical vertical or horizontal flip
     */
    public void flipGroup(int id, boolean vertical)
    {
        RectF rect = getGroupRect(id);
        int w = Math.round(rect.width() / rectSize);
        int h = Math.round(rect.height() / rectSize);
        PointF pnt = getGroupPos(id);
        //Log.i(TAG, "" + pnt);
        //Log.i(TAG, "" + w + " " + h);
        int dec;
        if(vertical)
        {
            for(int x = 0; x < w; x++)
            {
                dec = h - 1;
                for(int y = 0; y < h; y++)
                {
                    setNewPos(Math.round(pnt.x) + x * rectSize,  Math.round(pnt.y) + y * rectSize, id, true, dec * rectSize);
                    dec -= 2;
                }
            }
        }
        else
        {
            for(int y = 0; y < h; y++)
            {
                dec = w - 1;
                for(int x = 0; x < w; x++)
                {
                    setNewPos(Math.round(pnt.x) + x * rectSize,  Math.round(pnt.y) + y * rectSize, id, false, dec * rectSize);
                    dec -= 2;
                }
            }
        }

        for (RectClass item : rectList)
        {
            if (item.getGroup() == id && item.visible)
            {
                if(vertical)
                    item.py = item.ty;
                else
                    item.px = item.tx;
            }
        }

        setGroupBorder(id);
    }

    /**
     * replace the rectangle
     * @param x x pos
     * @param y y pos
     * @param id group id
     * @param vert vertical
     * @param dec new position
     */
    private void setNewPos(int x, int y, int id, boolean vert, int dec)
    {
        int px, py;
        for (RectClass item : rectList)
        {
            px = Math.round(item.px + item.lt.x);
            py = Math.round(item.py + item.lt.y);
            if (item.getGroup() == id && item.visible && px == x && py == y)
            {
                //Log.i(TAG, "" + y + " " + dec);
                if(vert)
                    item.ty = item.py + dec;
                else
                    item.tx = item.px + dec;

                break;
            }
        }
    }

    /**
     * rotate to the original angle
     * @param original
     */
    public void rotateBack(boolean original)
    {
        int id;
        int rot;
        for (int i = 0; i < groupIds.size(); i++)
        {
            id = groupIds.get(i);
            rot = getGroupRot(id);
            Log.i(TAG, "" + rot);
            if(original)
                rotateGroup(id, -rot, false);
            else
                rotateGroup(id, rot, false);
        }
    }
}
