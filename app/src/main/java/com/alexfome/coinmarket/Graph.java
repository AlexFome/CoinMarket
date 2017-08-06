package com.alexfome.coinmarket;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by grege on 06.08.2017.
 */

public class Graph extends View {

    public Context context;
    public ArrayList<Double> data;

    public Canvas canvas;

    public double height;
    public double width;

    public Graph(Context context, ArrayList<Double> data) {
        super(context);

        this.context = context;
        this.data = data;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;
        refresh();

    }


    void refresh () {

        canvas.drawColor(ContextCompat.getColor(context, R.color.white));

        height = getHeight() * 0.8f;
        double graphHeight = height * 0.8f;
        width = getWidth ();

        double minDataValue = 0;
        double maxDataValue = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) < minDataValue) {
                minDataValue = data.get(i);
            }
            if (data.get(i) > maxDataValue) {
                maxDataValue = data.get (i);
            }
        }

        double stepX = width / (data.size() - 1);
        double stepY = graphHeight * 0.8f / maxDataValue;

        Paint paint = new Paint();
        paint.setStrokeWidth(6);
        paint.setColor(ContextCompat.getColor(context, R.color.color_1));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        Point prevPoint = new Point((int) (0 * stepX), (int) (graphHeight - data.get(0) * stepY));
        ArrayList<Point> days = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Point point = new Point((int) (i * stepX), (int) (graphHeight - data.get(i) * stepY));
            canvas.drawLine (prevPoint.x, prevPoint.y, point.x, point.y, paint);
            prevPoint = point;
            if (i % 24 == 0) {
                days.add(point);
            }
        }

        paint.setStrokeWidth(0);
        paint.setColor(ContextCompat.getColor(context, R.color.light_dark));
        int textSizeSP = 10;
        textSizeSP = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, context.getResources().getDisplayMetrics()));
        paint.setTextSize(textSizeSP);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -4);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
        for (int i = 0; i < days.size(); i ++) {
            String date = dateFormat.format(calendar.getTime());
            canvas.drawText(date, days.get(i).x, (float) height, paint);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

    }

}
