package org.landroo.rectangle;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainMenu
{
	private static final String TAG = "MainMenu";

	private ViewGroup view;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private Context context;
	private ListView listView;

	private int callEasy = 0;
	private int callNormal = 0;
	private int callHard = 0;

	public int resMode = -1;

	private Timer scrollTimer = null;
	private int scrollCnt = 1;

	public MainMenu(Context c, ViewGroup v, LayoutInflater inflater, int w, int h, Handler handler)
	{
		this.context = c;
		this.view = v;
		this.layoutInflater = inflater;
		this.handler = handler;
		this.displayWidth = w;
		this.displayHeight = h;
	}
	
	// text popup window
	public void showMessagePoup(String sText, int easy, int normal, int hard)
	{
		callEasy = easy;
		callNormal = normal;
		callHard = hard;

		View popupView = layoutInflater.inflate(R.layout.main_menu, null);
		int w = displayWidth - displayWidth / 10;
		int h = displayHeight - displayHeight / 10;
		popupWindow = new PopupWindow(popupView, w, h);

		Bitmap bitmap = drawBack(w, h, 0, 0, false, false, context);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, w, h);
		popupWindow.setBackgroundDrawable(drawable);
		popupWindow.setFocusable(false);
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(false);
		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				scrollTimer.cancel();
			}
		});

		listView = (ListView) popupView.findViewById(R.id.menu_score);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				scrollTimer.cancel();
			}
		});

		List<String> list = scoreList();
		ScoreListAdapter adapter = new ScoreListAdapter(context, list);
		listView.setAdapter(adapter);

		Button btn1 = (Button) popupView.findViewById(R.id.menu_easy);
		btn1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				resMode = 0;
				popupWindow.dismiss();
				scrollTimer.cancel();
				handler.sendEmptyMessage(callEasy);
			}
		});
		
		Button btn2 = (Button) popupView.findViewById(R.id.menu_normal);
		btn2.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resMode = 1;
				popupWindow.dismiss();
				scrollTimer.cancel();
				handler.sendEmptyMessage(callNormal);
			}
		});

		Button btn3 = (Button) popupView.findViewById(R.id.menu_hard);
		btn3.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				resMode = 2;
				popupWindow.dismiss();
				scrollTimer.cancel();
				handler.sendEmptyMessage(callHard);
			}
		});

		popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);

		scrollTimer = new Timer();
		scrollTimer.scheduleAtFixedRate(new ScrollTask(), 1000, 1000);
	}

	// draw drawer background
	public static Bitmap drawBack(int w, int h, int xOff, int yOff, boolean gr, boolean border, Context context)
	{
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		int BLACK = 0xAA303437;
		int WHITE = 0xAAC5C6C7;

		int[] colors = new int[3];
		colors[0] = WHITE;
		colors[1] = BLACK;
		colors[2] = WHITE;

		LinearGradient gradient;
		if (gr) gradient = new LinearGradient(0, 0, w, 0, colors, null, android.graphics.Shader.TileMode.CLAMP);
		else gradient = new LinearGradient(0, 0, 0, h, colors, null, android.graphics.Shader.TileMode.CLAMP);
		paint.setShader(gradient);

		RectF rect = new RectF();
		rect.left = -xOff;
		rect.top = -yOff;
		rect.right = w;
		rect.bottom = h;
		float rx = dipToPixels(context, 20);
		float ry = dipToPixels(context, 20);

		canvas.drawRoundRect(rect, rx, ry, paint);

		if (border)
		{
			paint.setStyle(Paint.Style.STROKE);
			paint.setShader(null);
			paint.setColor(0xff000000);
			paint.setStrokeWidth(dipToPixels(context, 3));
			canvas.drawRoundRect(rect, rx, rx, paint);
		}

		return bitmap;
	}

	// calculate pixel size
	public static float dipToPixels(Context context, float dipValue)
	{
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}

	private List scoreList()
	{
		SharedPreferences inSettings = context.getSharedPreferences("org.landroo.rectangle_preferences", Context.MODE_PRIVATE);
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
			for(int i = 0; i < listStr.length; i++)
				list.add(listStr[i]);
		}

		return list;
	}

	private class ScoreListAdapter extends BaseAdapter
	{
		List<String> lines;
		private LayoutInflater mInflater;

		public ScoreListAdapter(Context context, List<String> lines)
		{
			this.lines = lines;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount()
		{
			return lines.size();
		}

		@Override
		public Object getItem(int position)
		{
			return lines.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.text_row, null);

			String line = lines.get(position);
			String[] lineVal = line.split("\t");

			TextView title = (TextView) convertView.findViewById(R.id.name_text);
			if(lineVal.length > 2) {
				title.setText(lineVal[0]);

				title = (TextView) convertView.findViewById(R.id.skil_text);
				title.setText(lineVal[1]);

				title = (TextView) convertView.findViewById(R.id.score_text);
				title.setText(lineVal[2]);
			}
			return convertView;
		}
	}

	class ScrollTask extends TimerTask
	{
		public void run()
		{
			scrollCnt++;
			if(scrollCnt > listView.getAdapter().getCount())
				scrollCnt = 0;

			listView.smoothScrollToPosition(scrollCnt);
		}
	}
}
