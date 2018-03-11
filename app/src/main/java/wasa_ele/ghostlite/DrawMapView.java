package wasa_ele.ghostlite;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.mapbox.mapboxsdk.maps.MapView;

public class DrawMapView extends MapView {
    private int cadence = 0;
    private int lastCadence = -1;
    private Paint mPaint = new Paint();
    private Path mPath2 = new Path();
    private static final RectF rectangle = new RectF(-400,-400,400,400);

    public DrawMapView(Context context) {
        super(context);
    }

    public DrawMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawCadence(canvas);
    }

    private void drawCadence(Canvas canvas) {
        canvas.save();
        int w = getWidth();
        int h = getHeight();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.translate(w / 2, h);
        canvas.save();
        canvas.save();
        canvas.save();
        canvas.drawArc(rectangle, 180, 180, true, mPaint);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.rotate(30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.rotate(30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.restore();//(w/2,h)
        canvas.rotate(-30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.rotate(-30);
        canvas.drawLine(0, -400, 0, -360, mPaint);
        canvas.restore();

        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.rotate((float) ((cadence / 240.0) * 180));
        mPath2.moveTo(0, 10);
        mPath2.lineTo(-400, 0);
        mPath2.lineTo(0, -10);
        mPath2.lineTo(0, 10);
        canvas.drawPath(mPath2, mPaint);
        canvas.restore();

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
        canvas.restore();
        lastCadence = cadence;
    }
}
