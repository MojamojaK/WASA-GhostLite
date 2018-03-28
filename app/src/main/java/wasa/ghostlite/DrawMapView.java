package wasa.ghostlite;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.mapbox.mapboxsdk.maps.MapView;

public class DrawMapView extends MapView {
    public int cadence = 0;
    public float air = 0.0f;
    public float ground = 0.0f;
    public float altitude = 0.0f;
    public float yaw = 0.0f;
    public float pitch = 0.0f;
    public float roll = 0.0f;
    private Paint mPaint = new Paint();
    private Path mPath1 = new Path();
    private Path mPath2 = new Path();
    private Path mPath3 = new Path();
    private static final RectF rectangle = new RectF(-400,-400,400,400);
    private int alt_colors[] = new int[10];
    private int spd_colors[] = new int[10];

    public DrawMapView(Context context) {
        super(context);
        initColors();
    }

    public DrawMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initColors();
    }

    public DrawMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initColors();
    }

    private void initColors() {
        alt_colors[0] = Color.argb(200, 255, 0, 0);
        alt_colors[1] = Color.argb(200, 255, 0, 0);
        alt_colors[2] = Color.argb(200, 255, 255, 0);
        alt_colors[3] = Color.argb(200, 255, 255, 0);
        alt_colors[4] = Color.argb(200, 0, 255, 0);
        alt_colors[5] = Color.argb(200, 0, 255, 0);
        alt_colors[6] = Color.argb(200, 0, 255, 0);
        alt_colors[7] = Color.argb(200, 0, 255, 0);
        alt_colors[8] = Color.argb(200, 0, 255, 0);
        alt_colors[9] = Color.argb(200, 255, 0, 0);
        spd_colors[0] = Color.argb(200, 255, 0, 0);
        spd_colors[1] = Color.argb(200, 255, 0, 0);
        spd_colors[2] = Color.argb(200, 255, 255, 0);
        spd_colors[3] = Color.argb(200, 255, 255, 0);
        spd_colors[4] = Color.argb(200, 0, 255, 0);
        spd_colors[5] = Color.argb(200, 0, 255, 0);
        spd_colors[6] = Color.argb(200, 0, 255, 0);
        spd_colors[7] = Color.argb(200, 0, 255, 0);
        spd_colors[8] = Color.argb(200, 0, 255, 0);
        spd_colors[9] = Color.argb(200, 255, 0, 0);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawOrientation(canvas);
        drawCadence(canvas);
        drawAltitude(canvas);
        drawSpeed(canvas);
    }

    private void drawOrientation(Canvas canvas) {
        //姿勢角関係の描画
        int w = getWidth();
        int h = getHeight();
        canvas.save(); // save (0,0)
        canvas.translate(w/2,250);
        canvas.save(); // save (w/2. 250)
        canvas.save(); // save (w/2, 250)
        mPaint.setStrokeWidth(5);

        canvas.rotate(roll);
        float pitch_mul = pitch * 15f;
        canvas.translate(0,-pitch_mul);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setARGB(200,0,167,227); // 青い方の四角
        canvas.drawRect(-500,-700,500,0,mPaint);
        mPaint.setARGB(200, 192,117,32); // 茶色の方の四角
        canvas.drawRect(-500,0,500,700,mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawLine(-50,75,50,75,mPaint);
        canvas.drawLine(-50,-75,50,-75,mPaint);
        canvas.drawLine(-100,150,100,150,mPaint);
        canvas.drawLine(-100,-150,100,-150,mPaint);
        canvas.drawLine(-50,225,50,225,mPaint);
        canvas.drawLine(-50,-225,50,-225,mPaint);
        canvas.drawLine(-100,300,100,300,mPaint);
        canvas.drawLine(-100,-300,100,-300,mPaint);
        canvas.drawLine(-50,375,50,375,mPaint);
        canvas.drawLine(-50,-375,50,-375,mPaint);
        canvas.drawLine(-500,0,500,0,mPaint);
        // canvas.drawLine(0,-700,0,700,mPaint);

        // ピッチ角の表示
        mPaint.setColor(Color.BLACK);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(40);
        canvas.drawText("10",-130,165,mPaint);
        canvas.drawText("10",130,165,mPaint);
        canvas.drawText("10",-130,-135,mPaint);
        canvas.drawText("10",130,-135,mPaint);
        canvas.drawText("20",-130,315,mPaint);
        canvas.drawText("20",130,315,mPaint);
        canvas.drawText("20",-130,-285,mPaint);
        canvas.drawText("20",130,-285,mPaint);

        // 中心を示す十字
        canvas.restore(); // restore (w/2, 250)
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(0,0,9,mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        canvas.drawCircle(0,0,10,mPaint);
        canvas.drawLine(-11,0,-450,0,mPaint);
        canvas.drawLine(11,0,450,0,mPaint);
        canvas.drawLine(0,-11,0,-175,mPaint);
        canvas.drawLine(0,11,0,175,mPaint);

        // 上のちょんちょん
        float yaw_multi = yaw * 2.5f; // 2.5 = 900 / 360
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.BLACK);
        for (int i = -495; i <= 1395; i += 45) {
            if (i % 225 != 0) canvas.drawLine(i - yaw_multi, -175, i - yaw_multi, -150, mPaint);
            else canvas.drawLine(i - yaw_multi, -175, i - yaw_multi, -130, mPaint);
        }
        canvas.drawText("S", -450 - yaw_multi,-90,mPaint);
        canvas.drawText("W", -225 - yaw_multi,-90,mPaint);
        canvas.drawText("N", - yaw_multi,-90,mPaint);
        canvas.drawText("E", 225 - yaw_multi,-90,mPaint);
        canvas.drawText("S", 450 - yaw_multi,-90,mPaint);
        canvas.drawText("W", 675 - yaw_multi,-90,mPaint);
        canvas.drawText("N", 900 - yaw_multi,-90,mPaint);
        canvas.drawText("E", 1125 - yaw_multi,-90,mPaint);
        canvas.drawText("S", 1350 - yaw_multi,-90,mPaint);

        // 周りを消すやつ (横幅:900 縦幅:450)
        canvas.restore(); // restore (w/2, 225)
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(-w/2,-225,-450,225,mPaint); // 左側
        canvas.drawRect(450,-225,w/2,225,mPaint); // 右側
        canvas.drawRect(-w/2,175,w/2,h - 250,mPaint); // 下側
        canvas.drawRect(-w/2,-275,w/2,-175,mPaint); // 上側
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(600);
        //canvas.drawCircle(0,0,600,mPaint);
        mPaint.reset();

        // 上の白い矢印
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.WHITE);
        mPath1.moveTo(0,-175);
        mPath1.lineTo(-25,-200);
        mPath1.lineTo(25,-200);
        mPath1.lineTo(0,-175);
        canvas.drawPath(mPath1,mPaint);
        canvas.restore(); // restore (0,0)

        // 下のロール角表示する部分
        /*canvas.restore(); //(w/2,h/2)
        canvas.drawRect(-185,100,185,320,mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(-183,102,183,318,mPaint);
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(175);
        String xs = String.format("%2.1f",roll);
        canvas.drawText(xs,0,310,mPaint);*/
    }

    private void drawCadence(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.save(); // save (0, 0)
        canvas.translate(w / 2, h);
        canvas.save(); // save (w/2, h)
        canvas.save(); // save (w/2, h)
        canvas.save(); // save (w/2, h)
        canvas.drawArc(rectangle, 180, 180, true, mPaint);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.rotate(30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.rotate(30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.restore();// restore (w/2,h)
        canvas.rotate(-30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.rotate(-30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.restore(); // restore (w/2, h)

        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.rotate((float) ((cadence / 240.0) * 180));
        mPath2.moveTo(0, 10);
        mPath2.lineTo(-400, 0);
        mPath2.lineTo(0, -10);
        mPath2.lineTo(0, 10);
        canvas.drawPath(mPath2, mPaint);
        canvas.restore(); // restore (w/2, h)

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(-200, -200, 200, -10, mPaint);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(-198, -198, 198, -12, mPaint);
        mPaint.setColor(Color.BLACK);
        String rots = Integer.toString(cadence);
        mPaint.setTextSize(200f);
        int deltaX = (int)(Math.log10(cadence) + 1) * -50;
        canvas.drawText(rots, deltaX, -20, mPaint);

        canvas.restore(); // restore (0,0)
    }

    private void drawAltitude(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        canvas.save(); // save (0, 0)
        canvas.translate(w - 10, h - 400);
        canvas.save(); // save (w - 10, h - 400)

        // 縦幅 800 横幅 80
        for (int i = 0; i < 10; i++) {
            mPath3.reset();
            mPath3.moveTo(0, -80 * i);
            mPath3.lineTo(0, -80 * (i+1));
            mPath3.lineTo(-80, -80 * (i+1));
            mPath3.lineTo(-80, -80 * i);
            mPath3.lineTo(0, -80 * i);
            mPaint.setStrokeWidth(1);
            mPaint.setColor(alt_colors[i]);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(mPath3, mPaint);
            mPaint.setStrokeWidth(10);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath3, mPaint);
        }
        canvas.restore(); // restore (w - 10, h - 400)
        float val = altitude * 80;
        mPaint.setStrokeWidth(20);
        mPaint.setColor(Color.rgb(255, 33, 70));
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(-190, -val, 10, -val, mPaint);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(-150, -35 - val, 0, 35 - val, mPaint);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.rgb(255, 33, 70));
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(-150, -35 - val, 0, 35 - val, mPaint);
        String alt_str = Float.toString(altitude);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setTextSize(60f);
        mPaint.setColor(Color.BLACK);
        canvas.drawText(alt_str, -125, 20 - val, mPaint);
        canvas.restore(); // restore (0, 0)
    }

    private void drawSpeed(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        canvas.save(); // save (0, 0)
        canvas.translate(90, h - 400);
        canvas.save(); // save (w - 10, h - 400)

        // 縦幅 800 横幅 80
        for (int i = 0; i < 10; i++) {
            mPath3.reset();
            mPath3.moveTo(0, -80 * i);
            mPath3.lineTo(0, -80 * (i+1));
            mPath3.lineTo(-80, -80 * (i+1));
            mPath3.lineTo(-80, -80 * i);
            mPath3.lineTo(0, -80 * i);
            mPaint.setStrokeWidth(1);
            mPaint.setColor(spd_colors[i]);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(mPath3, mPaint);
            mPaint.setStrokeWidth(10);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath3, mPaint);
        }
        canvas.restore(); // restore (w - 10, h - 400)
        float air_val = air * 80;
        float ground_val = ground * 80;

        mPaint.setStrokeWidth(20);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.rgb(255, 127, 0));
        canvas.drawLine(-90, -ground_val, 110, -ground_val, mPaint);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(-80, -35 - ground_val, 70, 35 - ground_val, mPaint);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.rgb(255, 127, 0));
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(-80, -35 - ground_val, 70, 35 - ground_val, mPaint);
        String grd_str = Float.toString(ground);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setTextSize(60f);
        mPaint.setColor(Color.BLACK);
        canvas.drawText(grd_str, -60, 20 - ground_val, mPaint);

        mPaint.setStrokeWidth(20);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.CYAN);
        canvas.drawLine(-90, -air_val, 110, -air_val, mPaint);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(-80, -35 - air_val, 70, 35 - air_val, mPaint);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.CYAN);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(-80, -35 - air_val, 70, 35 - air_val, mPaint);
        String air_str = Float.toString(air);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setTextSize(60f);
        mPaint.setColor(Color.BLACK);
        canvas.drawText(air_str, -60, 20 - air_val, mPaint);

        canvas.restore(); // restore (0, 0)
    }

    public void updateData() {
        invalidate();
    }
}
