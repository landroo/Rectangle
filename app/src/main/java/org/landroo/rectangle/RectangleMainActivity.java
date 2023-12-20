package org.landroo.rectangle;
/*
Rectangles

v 1.0

Kockás

v 1.0

tasks:
smooth places   	__  smooth rotation		__  rotate button		OK  flip button			OK
time line			OK  score list          OK  edit function       __  network score list  __
flip item           OK  demo                __  play each other     __  main menu           OK
help                __  exit popup          __

bugs:
rotation center     OK  zoomed placed item  OK  click outside win   OK  flip not work       OK
zoomed center       __  top list border     __

*/
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import org.landroo.ui.UI;
import org.landroo.ui.UIInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RectangleMainActivity extends Activity implements UIInterface
{
    private static final String TAG = "RectangleMainActivity";
    private static final int SCROLL_INTERVAL = 10;
    private static final int SCROLL_ALPHA = 500;
    private static final int PREVIEW_WAIT = 300;
    private static final int SCROLL_SIZE = 10;
    private int DEBUG = 0;
    private static final int GAP = 10;

    private int displayWidth;
    private int displayHeight;
    private int pictureWidth;
    private int pictureHeight;

    private UI ui = null;
    private RectangleView rectView;
    private ScaleView scaleView;

    private RectGroup rectGroup = new RectGroup();

    private int rectSize = 80;
    private int tileSize = 80;
    private Bitmap backBitmap;
    private Drawable backDrawable;// background bitmap drawable
    private boolean staticBack = false;// fix or scrollable background
    private int backColor = Color.GRAY;// background color

    private float rotation = 0;
    private float rx = 0;
    private float ry = 0;

    private float sX = 0;
    private float sY = 0;
    private float mX = 0;
    private float mY = 0;

    private float zoomX = 1;
    private float zoomY = 1;

    private float xPos;
    private float yPos;

    private float scrollX = 0;
    private float scrollY = 0;
    private float scrollMul = 1;
    private Timer scrollTimer = null;
    private Paint scrollPaint1 = new Paint();
    private Paint scrollPaint2 = new Paint();
    private int scrollAlpha = SCROLL_ALPHA;
    private int scrollBar = 0;
    private float barPosX = 0;
    private float barPosY= 0;

    private float xDest;
    private float yDest;
    private int halfX;
    private int halfY;
    private boolean scrollTo = false;

    private int selGroupId = -1;

    private Paint infoPaint = new Paint();
    private Paint secPaint = new Paint();
    private Paint counterPaint = new Paint();
    private Paint linePaint = new Paint();

    private int state = 0;
    private boolean afterMove = false;

    private Timer secTimer;// new game timer
    private int sec = -1;

    private int counter = 0;
    private int counterAll = 0;

    private Timer counterTimer;// game timer
    private int score = 0;// score
    private int minScore = 0;
    private int retry = 3;// retry
    private int gameCnt = 0;// game counter
    private boolean continued = false;
    private boolean backPressed = true;
    private boolean bFirst = true;

    private int rotQuoter = 1;
    private double actRot = 0;
    private int rotItem = 0;
    private ImageButton rotButton;
    private Timer rotTimer;

    private boolean flipVer = false;
    private ImageButton flipButton;

    private int helpCnt = 0;
    private ImageButton helpButton;

    private boolean rotFunc = true;

    private int easy = 0;

    private String playerName;
    private String wow, result, lives, cont;

    private boolean popupShown = false;

    private boolean isWhite = false;

    private MainMenu mainMenu;
    private MessageTool messageTool;
/*
    static class IncomingHandler extends Handler
    {
        private final WeakReference<RectangleMainActivity> activity;

        IncomingHandler(RectangleMainActivity service)
        {
            activity = new WeakReference<RectangleMainActivity>(service);
        }
        @Override
        public void handleMessage(Message msg)
        {
            RectangleMainActivity act = activity.get();
            if (act != null)
            {
                //act.handleMessage(msg);
            }
        }
    }
*/
    //private Handler mainHandler = new Handler()
    private Handler mainHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 1:// give up or continue
                    messageTool.showMessagePoup(cont, 100, 101, 1);
                    popupShown = true;
                    break;
                case 100:// give up
                    continued = false;
                    popupShown = false;
                    newGame(PREVIEW_WAIT);
                    helpButton.setVisibility(View.VISIBLE);
                    break;
                case 101:// continue
                    counter = rectGroup.rectNum * 10;
                    counterAll = counter;
                    continued = true;
                    counterTimer = new Timer();
                    counterTimer.schedule(new counterClass(), 0, 1000);
                    popupShown = false;
                    helpButton.setVisibility(View.VISIBLE);
                    break;
                case 102:// resume
                    if(sec != -10)
                    {
                        secTimer = new Timer();
                        secTimer.schedule(new secClass(), 0, 10);
                    }
                    else
                    {
                        counterTimer = new Timer();
                        counterTimer.schedule(new counterClass(), 0, 1000);
                    }
                    popupShown = false;
                    break;
                case 103:// new game
                    if(!messageTool.resText.equals(""))
                        playerName = messageTool.resText;
                    mainMenu.showMessagePoup("", 200, 201, 202);
                    popupShown = true;
                    backPressed = true;
                    continued = false;

                    updateScore(playerName, score);

                    counter = 0;
                    break;
                case 104:// end game
                    if(minScore < score)
                    {
                        // get player name
                        messageTool.showMessagePoup(playerName, 103, 103, 3);
                        popupShown = true;
                    }
                    else
                        mainMenu.showMessagePoup("", 200, 201, 202);

                    retry = 3;
                    score = 0;
                    counter = 0;
                    continued = false;
                    break;
                case 200:// easy
                    easy = 0;
                    rotButton.setVisibility(View.INVISIBLE);
                    flipButton.setVisibility(View.INVISIBLE);
                    helpButton.setVisibility(View.VISIBLE);

                    xPos = scaleView.xPos();
                    yPos = scaleView.yPos();

                    newGame(PREVIEW_WAIT);

                    popupShown = false;
                    backPressed = false;

                    break;
                case 201:// normal
                    easy = 1;
                    rotButton.setVisibility(View.VISIBLE);
                    flipButton.setVisibility(View.INVISIBLE);
                    helpButton.setVisibility(View.VISIBLE);

                    xPos = scaleView.xPos();
                    yPos = scaleView.yPos();

                    newGame(PREVIEW_WAIT);

                    popupShown = false;
                    backPressed = false;

                    break;
                case 202:// hard
                    easy = 3;
                    rotButton.setVisibility(View.VISIBLE);
                    flipButton.setVisibility(View.VISIBLE);

                    xPos = scaleView.xPos();
                    yPos = scaleView.yPos();

                    newGame(PREVIEW_WAIT);

                    popupShown = false;
                    backPressed = false;

                    break;
                case 300:// back pressed once
                    popupShown = false;
                    break;
            }

            return true;
        }
    });

    // main view
    private class RectangleView extends ViewGroup
    {

        public RectangleView(Context context)
        {
            super(context);
        }

        // draw items
        @Override
        protected void dispatchDraw(Canvas canvas)
        {
            drawBack(canvas);
            drawItems(canvas);
            drawInfo(canvas);
            drawScrollBars(canvas);

            super.dispatchDraw(canvas);
        }

        @Override
        protected void onLayout(boolean b, int i, int i1, int i2, int i3)
        {
            // main
            View child = this.getChildAt(0);
            child.layout(0, 0, displayWidth, displayHeight);

            if(bFirst)
            {
                mainMenu.showMessagePoup("", 200, 201, 202);
                popupShown = true;

                bFirst = false;
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
            setMeasuredDimension(displayWidth, displayHeight);
            // main
            View child = this.getChildAt(0);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * enter point
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_rectangle_main);

        Display display = getWindowManager().getDefaultDisplay();
        displayWidth = display.getWidth();
        displayHeight = display.getHeight();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grid);
        backDrawable = new BitmapDrawable(backBitmap);
        backDrawable.setBounds(0, 0, backBitmap.getWidth(), backBitmap.getHeight());

        rectView = new RectangleView(this);
        setContentView(rectView);

        ui = new UI(this);

        pictureWidth = displayWidth * 3;
        pictureHeight = displayHeight * 3;
        scaleView = new ScaleView(displayWidth, displayHeight, pictureWidth, pictureHeight, rectView);

        LayoutInflater layoutInflater = (LayoutInflater) RectangleMainActivity.this.getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        messageTool = new MessageTool(RectangleMainActivity.this, rectView, layoutInflater, displayWidth, displayHeight / 2, mainHandler);
        mainMenu = new MainMenu(RectangleMainActivity.this, rectView, layoutInflater, displayWidth, displayHeight, mainHandler);

        RelativeLayout mainView = (RelativeLayout)getLayoutInflater().inflate(R.layout.activity_rectangle_main, null);
        rectView.addView(mainView);

        rotButton = (ImageButton) findViewById(R.id.main_rotate);
        rotButton.setBackgroundResource(R.drawable.btn_states2);
        rotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap;
                if (rotQuoter > 0)
                {
                    rotQuoter = -1;
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_refresh_black_48dp2);
                } else {
                    rotQuoter = 1;
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_refresh_black_48dp1);
                }
                rotButton.setImageBitmap(bitmap);

                rotButton.setBackgroundResource(R.drawable.btn_states2);
                flipButton.setBackgroundResource(R.drawable.btn_states1);
                rotFunc = true;
            }
        });
        flipButton = (ImageButton) findViewById(R.id.main_flips);
        flipButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Bitmap bitmap;
                if (flipVer)
                {
                    flipVer = false;
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_flip_black_48dp1);
                }
                else
                {
                    flipVer = true;
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_flip_black_48dp2);
                }
                flipButton.setImageBitmap(bitmap);

                flipButton.setBackgroundResource(R.drawable.btn_states2);
                rotButton.setBackgroundResource(R.drawable.btn_states1);
                rotFunc = false;
            }
        });

        helpButton = (ImageButton) findViewById(R.id.main_help);
        helpButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                helpButton.setVisibility(View.INVISIBLE);
                isWhite = true;
                helpCnt = 3;
                rectGroup.rotateBack(true);

            }
        });

        scrollTimer = new Timer();
        scrollTimer.scheduleAtFixedRate(new ScrollTask(), 0, SCROLL_INTERVAL);

        scrollPaint1.setColor(Color.GRAY);
        scrollPaint1.setAntiAlias(true);
        scrollPaint1.setDither(true);
        scrollPaint1.setStyle(Paint.Style.STROKE);
        scrollPaint1.setStrokeJoin(Paint.Join.ROUND);
        scrollPaint1.setStrokeCap(Paint.Cap.ROUND);
        scrollPaint1.setStrokeWidth(SCROLL_SIZE);

        scrollPaint2.setColor(Color.CYAN);
        scrollPaint2.setAntiAlias(true);
        scrollPaint2.setDither(true);
        scrollPaint2.setStyle(Paint.Style.STROKE);
        scrollPaint2.setStrokeJoin(Paint.Join.ROUND);
        scrollPaint2.setStrokeCap(Paint.Cap.ROUND);
        scrollPaint2.setStrokeWidth(SCROLL_SIZE);

        //infoPaint.setColor(0xFF444444);
        infoPaint.setTextSize(displayWidth / 16);
        infoPaint.setFakeBoldText(true);
        infoPaint.setShadowLayer(3, 0, 0, Color.WHITE);

        secPaint.setTextSize(displayHeight / 2);
        secPaint.setFakeBoldText(true);
        secPaint.setShadowLayer(3, 0, 0, Color.WHITE);

        counterPaint.setTextSize(displayWidth / 6);
        counterPaint.setFakeBoldText(true);
        counterPaint.setShadowLayer(3, 0, 0, Color.WHITE);

        int colors[] = new int[2];
        colors[0] = Color.RED;
        colors[1] = Color.BLACK;
        Shader shader = new LinearGradient(200, 0, displayWidth / 4 * 3, 0, colors, null, android.graphics.Shader.TileMode.CLAMP);
        linePaint.setShader(shader);
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(SCROLL_SIZE * 4);

        rectGroup.extractAssets(this);
        rectGroup.loadPatterns(this);

        wow =  getResources().getString(R.string.wow);
        result = getResources().getString(R.string.score);
        lives = getResources().getString(R.string.lives);
        cont =  getResources().getString(R.string.cont);

        SharedPreferences inSettings = getSharedPreferences("org.landroo.rectangle_preferences", MODE_PRIVATE);
        playerName = inSettings.getString("name", "Player");
    }

    // main touch event
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return ui.tapEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rectangle_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDown(float x, float y)
    {
        afterMove = false;
        scrollAlpha = SCROLL_ALPHA;

        if(actRot != 0)
            return;


        scrollBar = checkBars(x, y);
        if(scrollBar == 1) {
            barPosX = x - barPosX;
        }
        else if(scrollBar == 2) {
            barPosY = y - barPosY;
        }
        else if(state == 0 && !popupShown)
        {
            // reverse order than the drawing order
            for (int i = rectGroup.rectList.size() - 1; i >= 0; i--)
            {
                RectClass rect = rectGroup.rectList.get(i);
                if (rect.isInside(x - xPos, y - yPos, zoomX, zoomY) && selGroupId == -1)
                {
                    selGroupId = rect.getGroup();
                    rectGroup.setGroupAlpha(selGroupId, 127);
                    rectGroup.orderGroups(selGroupId);

                    break;
                }
            }
        }

        scaleView.onDown(x, y);

        sX = x / zoomX;
        sY = y / zoomY;

        mX = x / zoomX;
        mY = y / zoomY;

        rectView.postInvalidate();
    }

    @Override
    public void onUp(float x, float y)
    {
        scaleView.onUp(x, y);

        if(state == 1)
        {
            state = 2;

            newGame(PREVIEW_WAIT);
        }

        //if(actRot == 0)
            unselGroup();

    }

    private void unselGroup()
    {
        if(selGroupId != -1)
        {
            Log.i(TAG, "Drop item!");

            //if(!rectGroup.checkOver(selGroup, zoomX, zoomY))
            rectGroup.setGroupAlpha(selGroupId, 191);

            rectGroup.setToPos(selGroupId, rectSize / 2);
            rectGroup.setBoundRect(selGroupId);

            scrollX = 0;
            scrollY = 0;

            if(rectGroup.checkEnd())
            {
                state = 1;
                counterTimer.cancel();
                counterTimer = null;
                if(!continued)
                    score += counter;
                continued = true;
                counter = 0;
                counterPaint.setColor(Color.BLACK);
            }
            selGroupId = -1;

            afterMove = true;
        }
    }

    @Override
    public void onTap(float x, float y)
    {
        scrollAlpha = SCROLL_ALPHA;

        xPos = scaleView.xPos();
        yPos = scaleView.yPos();

        boolean bSelect = false;
        if(state == 0 && !popupShown)
        {
            // backward check
            for (int i = rectGroup.rectList.size() - 1; i >= 0; i--)
            {
                RectClass rect = rectGroup.rectList.get(i);
                if (rect.isInside(x - xPos, y - yPos, zoomX, zoomY) && selGroupId == -1)
                {
                    selGroupId = rect.getGroup();
                    if(rotFunc && (easy & 1) == 1)
                    {
                        RectF rectF = rectGroup.getGroupRect(selGroupId);

                        // TODO slim rotate
                        rectGroup.rotateGroup(selGroupId, rotQuoter, true);
                    }
                    else if((easy & 2) == 2)
                    {
                        rectGroup.flipGroup(selGroupId, flipVer);
                        rectGroup.setGroupBorder(selGroupId);
                    }

                    rectGroup.setToPos(selGroupId, rectSize / 2);
                    rectGroup.setBoundRect(selGroupId);
                    if (rectGroup.checkEnd())
                    {
                        state = 1;
                        counterTimer.cancel();
                        counterTimer = null;
                        if (!continued)
                            score += counter;
                        continued = true;
                        counter = 0;
                        counterPaint.setColor(Color.BLACK);
                    }
                    selGroupId = -1;
                    bSelect = true;
                    break;
                }
            }
        }

        if(bSelect)
            rectView.postInvalidate();
    }

    @Override
    public void onHold(float x, float y)
    {
    }

    @Override
    public void onMove(float x, float y)
    {
        scrollAlpha = SCROLL_ALPHA;

        mX = x / zoomX;
        mY = y / zoomY;

        if(scrollBar != 0) {
            // vertical scroll
            if(scrollBar == 1) {
                float xp = -(x - barPosX) / (displayWidth / (pictureWidth * zoomX));
                //Log.i(TAG, "" + xp);
                if(xp < 0 && xp > displayWidth - pictureWidth * zoomX) {
                    xPos = xp;
                }
            }
            else {
                float yp = -(y - barPosY) / (displayHeight / (pictureHeight * zoomY));
                //Log.i(TAG, "" + yp);
                if(yp < 0 && yp > displayHeight - pictureHeight * zoomY) {
                    yPos = yp;
                }
            }
            scaleView.setPos(xPos, yPos);
            rectView.postInvalidate();
        }

        // check items
        if(selGroupId != -1)
        {
            moveGroup(selGroupId);
            rectView.postInvalidate();
        }
        else
            scaleView.onMove(x, y);

        sX = mX;
        sY = mY;
    }

    @Override
    public void onSwipe(int direction, float velocity, float x1, float y1, float x2, float y2)
    {
        if(!afterMove)
            scaleView.onSwipe(direction, velocity, x1, y1, x2, y2);
    }

    @Override
    public void onDoubleTap(float x, float y)
    {
    }

    @Override
    public void onZoom(int mode, float x, float y, float distance, float xdiff, float ydiff)
    {
        scaleView.onZoom(mode, x, y, distance, xdiff, ydiff);

        zoomX = scaleView.getZoomX();
        zoomY = scaleView.getZoomY();

        if(selGroupId != -1)
        {
            rectGroup.setGroupAlpha(selGroupId, 191);
            rectGroup.setToPos(selGroupId, rectSize / 2);
            selGroupId = -1;
        }
    }

    @Override
    public void onRotate(int mode, float x, float y, float angle)
    {
    }

    @Override
    public void onFingerChange()
    {
    }

    // scroll task for scrolling background
    class ScrollTask extends TimerTask
    {
        public void run()
        {
            boolean redraw = false;

            // scroll to selected object
            if(scrollMul < .05f)
            {
                scrollTo = false;
                scrollX = 0;
                scrollY = 0;
                rectGroup.setToPos(selGroupId, rectSize / 2);
            }
            else
            {
                if(scrollTo)
                {
                    if((int)Math.abs(xDest - xPos) < halfX || (int)Math.abs(yDest - yPos) < halfY) scrollMul -= 0.05f;
                    else scrollMul += 0.05f;
                    redraw = true;
                }
            }

            // left and top scroll in zoomed
            if(xPos + scrollX < displayWidth - pictureWidth * zoomX || xPos + scrollX > 0)
            {
                scrollX = 0;
                rectGroup.setToPos(selGroupId, rectSize / 2);
            }
            if(yPos + scrollY < displayHeight - pictureHeight * zoomY || yPos + scrollY > 0)
            {
                scrollY = 0;
                rectGroup.setToPos(selGroupId, rectSize / 2);
            }

            // auto scroll paper
            if (scrollX != 0 || scrollY != 0)
            {
                xPos += scrollX * scrollMul;
                yPos += scrollY * scrollMul;


                for (RectClass item : rectGroup.rectList)
                {
                    if (item.getGroup() == selGroupId && item.visible)
                    {
                        item.px -= scrollX * scrollMul / zoomX;
                        item.py -= scrollY * scrollMul / zoomY;
                        redraw = true;
                    }
                }

                //if(scrollMul < 10) scrollMul += 0.05f;


                scaleView.setPos(xPos, yPos);
            }

            if(scrollAlpha > 32)
            {
                scrollAlpha--;
                if(scrollAlpha > 255) scrollPaint1.setAlpha(255);
                else scrollPaint1.setAlpha(scrollAlpha);
                redraw = true;
            }

            if(redraw) rectView.postInvalidate();
        }
    }

    // draw background
    private void drawBack(Canvas canvas)
    {
        if(backDrawable != null)
        {
            // static back or tiles
            if(staticBack)
            {
                backDrawable.setBounds(0, 0, displayWidth, displayHeight);
                backDrawable.draw(canvas);
            }
            else for(float x = 0; x < pictureWidth; x += tileSize)
            {
                for(float y = 0; y < pictureHeight; y += tileSize)
                {
                    // distance of the tile center from the rotation center
                    final float dis = (float)Utils.getDist(rx * zoomX, ry * zoomY, (x + tileSize / 2) * zoomX, (y + tileSize / 2) * zoomY);
                    // angle of the tile center from the rotation center
                    final float ang = (float)Utils.getAng(rx * zoomX, ry * zoomY, (x + tileSize / 2) * zoomX, (y + tileSize / 2) * zoomY);

                    // coordinates of the block after rotation
                    final float cx = dis * (float)Math.cos((rotation + ang) * Utils.DEGTORAD) + rx * zoomX + xPos;
                    final float cy = dis * (float)Math.sin((rotation + ang) * Utils.DEGTORAD) + ry * zoomY + yPos;

                    if(cx >= -tileSize && cx <= displayWidth + tileSize && cy >= -tileSize && cy <= displayHeight + tileSize)
                    {
                        backDrawable.setBounds(0, 0, (int)(tileSize * zoomX) + 1, (int)(tileSize * zoomY) + 1);

                        canvas.save();
                        //canvas.rotate(tile.tilRot, ((tile.offPosX + tile.tilPosX) * zoomX) + xPos + tile.stoneBitmap.getWidth() * (zoomX) / 2,
                        //		((tile.offPosY + tile.tilPosY) * zoomY) + yPos + tile.stoneBitmap.getHeight() * zoomY / 2);
                        canvas.rotate(rotation, rx * zoomX + xPos, ry * zoomY + yPos);
                        canvas.translate(x * zoomX + xPos, y * zoomY + yPos);
                        backDrawable.draw(canvas);
                        canvas.restore();
                    }
                }
            }
        }
        else
        {
            canvas.drawColor(backColor);
        }
    }

    /**
     * draw all itmen
     * @param canvas canvas
     */
    private void drawItems(Canvas canvas)
    {
        if (scaleView != null)
        {
            xPos = scaleView.xPos();
            yPos = scaleView.yPos();
        }

        // draw items
        for (RectClass item : rectGroup.rectList)
        {
            if (item != null)
            {
                float dx = xPos + item.px * zoomX;
                float dy = yPos + item.py * zoomY;
                if(isWhite)
                {
                    dx = xPos + item.ox * zoomX;
                    dy = yPos + item.oy * zoomY;
                }

                //if (dx >= -(item.l + item.iw) * zoomX && dx <= displayWidth - item.l * zoomX && dy >= -(item.t + item.ih) * zoomY && dy <= displayHeight - item.t * zoomY)
                /*if (item != null
                        && dx >= -(rectGroup.getGroupRect(item.getGroup()).left + item.iw) * zoomX
                        && dx <= displayWidth - rectGroup.getGroupRect(item.getGroup()).left * zoomX
                        && dy >= -(rectGroup.getGroupRect(item.getGroup()).top + item.ih) * zoomY
                        && dy <= displayHeight - rectGroup.getGroupRect(item.getGroup()).top * zoomY)*/
                {
                    canvas.save();
                    canvas.translate(dx, dy);

                    item.drawRect(canvas, zoomX, zoomY, isWhite);

                    canvas.restore();
                }
            }
        }

        if ((DEBUG & 1) == 1)
        {
            RectF rect;
            PointF pnt;
            for (int i = 0; i < rectGroup.groupRects.size(); i++)
            {
                pnt = rectGroup.groupPos.get(i);
                final float dx = pnt.x * zoomX + xPos;
                final float dy = pnt.y * zoomY + yPos;

                rect = rectGroup.groupRects.get(i);
                canvas.save();
                canvas.translate(dx, dy);
                canvas.drawRect(rect, rectGroup.strokePaint);
                canvas.restore();
            }
        }

        // draw border
        canvas.save();
        canvas.translate(rectGroup.sx * zoomX + xPos, rectGroup.sy * zoomY + yPos);
        rectGroup.drawBorder(canvas, zoomX, zoomY);
        canvas.restore();
    }

    /**
     * show item information
     * @param canvas canvas
     */
    private void drawInfo(Canvas canvas)
    {
        float fMes;
        int textX;
        //canvas.drawText("" + (item.px - item.boundingRect.left + item.getWidth()), 10, 70, infoPaint);

        // show success text
        if(state == 1)
        {
            fMes = counterPaint.measureText(wow);
            textX = (int)(displayWidth - fMes) / 2;
            canvas.drawText(wow, textX, counterPaint.getTextSize() + (displayHeight - counterPaint.getTextSize()) / 2, counterPaint);


        }

        // show new game preview seconds
        if(sec > -1)
        {
            fMes = secPaint.measureText("" + (sec / 100));
            textX = (int)(displayWidth - fMes) / 2;
            canvas.drawText("" + ((sec + 100) / 100), textX, secPaint.getTextSize() + (displayHeight - secPaint.getTextSize()) / 2, secPaint);
        }

        // show down counter and line
        if(counter > 0)
        {
            canvas.drawText("" + counter, 10, counterPaint.getTextSize(), counterPaint);

            float xSize = ((float)displayWidth / 3 * 2 - 40) / (float)counterAll * (float)counter;
            canvas.drawLine(displayWidth / 3, infoPaint.getTextSize() + 80, displayWidth / 3 + xSize, infoPaint.getTextSize() + 80, linePaint);
        }

        // show current score
        fMes = infoPaint.measureText(result + score);
        textX = (int)(displayWidth - fMes) / 2;
        canvas.drawText(result + score, textX, infoPaint.getTextSize(), infoPaint);

        // show lives remains
        fMes = infoPaint.measureText(lives + retry);
        textX = (int)(displayWidth - fMes - 10);
        canvas.drawText(lives + retry, textX, infoPaint.getTextSize(), infoPaint);

    }

    /**
     * show position indicators
     * @param canvas canvas
     */
    private void drawScrollBars(Canvas canvas)
    {
        float x, y;
        float xSize = displayWidth / ((pictureWidth * zoomX) / displayWidth);
        float ySize = displayHeight / ((pictureHeight * zoomY) / displayHeight);

        x = (displayWidth / (pictureWidth * zoomX)) * -xPos;
        y = displayHeight - SCROLL_SIZE - 2;
        if(xSize < displayWidth) {
            if (scrollBar == 1) {
                canvas.drawLine(x, y, x + xSize, y, scrollPaint2);
            }
            else {
                canvas.drawLine(x, y, x + xSize, y, scrollPaint1);
            }
        }

        x = displayWidth - SCROLL_SIZE - 2;
        y = (displayHeight / (pictureHeight * zoomY)) * -yPos;
        if(ySize < displayHeight) {
            if (scrollBar == 2) {
                canvas.drawLine(x, y, x, y + ySize, scrollPaint2);
            }
            else {
                canvas.drawLine(x, y, x, y + ySize, scrollPaint1);
            }
        }
    }

    /**
     * move selected group
     * @param id group id
     */
    private void moveGroup(int id)
    {
        boolean bOK = true;

        float dx = mX - sX;
        float dy = mY - sY;

        scrollX = 0;
        scrollY = 0;

        for(RectClass item: rectGroup.rectList)
        {
            if(item.getGroup() == id && item.visible)
            {
                //TODO item inside the paper
                if((item.px + item.lt.x + rectSize) + dx > pictureWidth && dx > 0) bOK = false;
                if((item.px + item.lt.x) + dx < 0 && dx < 0) bOK = false;
                if((item.py + item.lt.y + rectSize) + dy > pictureHeight && dy > 0) bOK = false;
                if((item.py + item.lt.y) + dy < 0 && dy < 0) bOK = false;

                float scx = scrollX;
                float scy = scrollY;

                //TODO scroll background under item
                if(xPos + (item.px + item.lt.x) * zoomX >= displayWidth) scrollX = -1f;// scroll left
                if(xPos + (item.px + item.lt.x) * zoomX <= 0) scrollX = 1f;// scroll right
                if(yPos + (item.py + item.lt.y) * zoomY >= displayHeight) scrollY = -1f;// scroll up
                if(yPos + (item.py + item.lt.y) * zoomY <= 0) scrollY = 1f;// scroll down

                if(scx != scrollX) scrollMul = 1;
                if(scy != scrollY) scrollMul = 1;
            }
        }

        if(bOK)
        {
            for (RectClass item : rectGroup.rectList)
            {
                if (item.getGroup() == id && item.visible)
                {
                    item.px += dx;
                    item.py += dy;
                }
            }
        }

    }

    /**
     * new game
     * @param wait wait seconds
     */
    private void newGame(int wait)
    {
        int pattern;
        if(gameCnt / 2 < rectGroup.patternList.size() - 3)
            pattern = Utils.random(gameCnt / 2, gameCnt / 2 + 2, 1);
        else
            pattern = Utils.random(rectGroup.patternList.size() - 4, rectGroup.patternList.size() - 1, 1);

        //scaleView.setZoom(1, 1);
        scaleView.setPos((displayWidth - pictureWidth) / 2 * zoomX, (displayHeight - pictureHeight) / 2 * zoomY);
        xPos = scaleView.xPos();
        yPos = scaleView.yPos();

        rectGroup.createRectList(displayWidth, displayHeight, xPos, yPos, rectGroup.patternList.get(pattern), rectSize);

        gameCnt++;

        sec = wait;
        secTimer = new Timer();
        secTimer.schedule(new secClass(), 0, 10);

        counter = rectGroup.rectNum * 10;
        counterAll = counter;

        if(easy != 3)
            helpButton.setVisibility(View.VISIBLE);

        rectView.postInvalidate();
    }

    /**
     * down counter timer
     */
    class secClass extends TimerTask
    {
        @Override
        public void run()
        {
            sec--;
            secPaint.setAlpha((sec % 100) * 2 + 25);
            if(sec == -10)
            {
                secTimer.cancel();
                secTimer = null;

                rectGroup.randomBlocks(displayWidth, rectSize / 2, easy);
                state = 0;

                if(counterTimer == null)
                {
                    counterTimer = new Timer();
                    counterTimer.schedule(new counterClass(), 0, 1000);
                }
            }
            rectView.postInvalidate();
        }
    }

    /**
     * counter timer class
     */
    class counterClass extends TimerTask
    {
        @Override
        public void run()
        {
            counter--;
            if(counter == 0)
            {
                counterTimer.cancel();
                counterTimer = null;

                retry--;
                if(retry == 0)
                    mainHandler.sendEmptyMessage(104);
                else
                    mainHandler.sendEmptyMessage(1);
            }

            if(counter < counterAll / 5)
                counterPaint.setColor(Color.RED);
            else
                counterPaint.setColor(Color.BLACK);

            if(helpCnt == 0 && isWhite)
            {
                isWhite = false;
                rectGroup.rotateBack(false);
            }
            else
            {
                helpCnt--;
            }

            rectView.postInvalidate();
        }
    }

    /**
     * override on resume
     */
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /**
     * override on pause
     */
    @Override
    public void onPause()
    {
        if(secTimer != null)
        {
            secTimer.cancel();
            secTimer = null;
        }

        if(counterTimer != null)
        {
            counterTimer.cancel();
            counterTimer = null;
        }

        if(!backPressed && counter > 0)
        {
            LayoutInflater layoutInflater = (LayoutInflater) RectangleMainActivity.this.getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            MessageTool mt = new MessageTool(RectangleMainActivity.this, rectView, layoutInflater, displayWidth, displayHeight / 2, mainHandler);
            mt.showMessagePoup("Continue?", 300, 102, 2);
            popupShown = true;
        }

        super.onPause();
    }

    /**
     * override back pressed
     */
    @Override
    public void onBackPressed()
    {
        updateScore(playerName, score);

        if(backPressed)
            super.onBackPressed();
        else
        {
            if(!popupShown)
            {
                LayoutInflater layoutInflater = (LayoutInflater) RectangleMainActivity.this.getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                MessageTool messageTool = new MessageTool(RectangleMainActivity.this, rectView, layoutInflater, displayWidth, displayHeight / 2, mainHandler);
                messageTool.showMessagePoup("Exit?", 300, 103, 1);
                popupShown = true;
            }
        }
    }

    /**
     * update score
     * @param name user name
     * @param score last score
     */
    private void updateScore(String name, int score)
    {
        SharedPreferences inSettings = getSharedPreferences("org.landroo.rectangle_preferences", MODE_PRIVATE);
        String scoreList = inSettings.getString("score", "");

        List<String> list = new ArrayList<>();
        if(scoreList.equals(""))
        {
            for(int i = 50000; i > 0; i-= 1000)
                list.add("player\teasy\t" + i);
        }
        else
        {
            String[] listStr = scoreList.split(";");
            list = Arrays.asList(listStr);
        }

        minScore = Integer.MAX_VALUE;
        int sc, cnt = 0;
        String[] pair;
        StringBuilder sb = new StringBuilder();
        for(String line: list)
        {
            pair = line.split("\t");
            if(pair.length == 3)
            {
                sc = Integer.parseInt(pair[2]);
                if (sc < score) {
                    sb.append(name);
                    sb.append("\t");
                    sb.append(score);
                    sb.append(";");
                }
                if (cnt < 50) {
                    sb.append(line);
                    sb.append(";");
                }
                cnt++;

                if (minScore > score)
                    minScore = score;
            }
        }

        SharedPreferences outSettings = getSharedPreferences("org.landroo.rectangle_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = outSettings.edit();
        editor.putString("name", name);
        editor.putString("score", sb.toString());
        editor.apply();// .commit();

        WebClass wb = new WebClass(this);
        wb.sendScores(sb.toString());

    }

    /**
     * check tap on scroll bars
     * @param x float position x
     * @param y float position y
     * @return  int 1 vertical scroll bar 2 horizontal scroll bar
     */
    private int checkBars(float x, float y) {
        float px, py;
        float xSize = displayWidth / ((pictureWidth * zoomX) / displayWidth);
        float ySize = displayHeight / ((pictureHeight * zoomY) / displayHeight);
        px = (displayWidth / (pictureWidth * zoomX)) * -xPos;
        py = displayHeight - SCROLL_SIZE - 2;
        //Log.i(TAG, "" + x + " " + xp + " " + (x+ xSize) + " " + y + " " + yp + " " + (y + SCROLL_SIZE));
        if(x > px && y > py - GAP && x < px + xSize && y < py + SCROLL_SIZE + GAP && xSize < displayWidth) {
            barPosX = px;
            return 1;
        }

        px = displayWidth - SCROLL_SIZE - 2;
        py = (displayHeight / (pictureHeight * zoomY)) * -yPos;
        if(x > px - GAP && y > py && x < px + SCROLL_SIZE + GAP && y < py + ySize && ySize < displayHeight) {
            barPosY = py;
            return 2;
        }

        return 0;
    }
}
