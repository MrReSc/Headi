package com.example.headi;

import android.graphics.Color;

public class Constants {

    public static class ACTION {
        public static final String START_ACTION = "timer.action.start";
        public static final String STOP_ACTION = "timer.action.stop";
        public static final String END_ACTION = "timer.action.end";
        public static final String NONE_ACTION = "timer.action.none";
        public static final String INIT_ACTION = "timer.action.init";
    }

    public static class SERVICE {
        public static final int NOTIFICATION_ID_TIMER_SERVICE = 8466503;
        public static final String NOTIFICATION_CHANEL_ID = "7894";
    }

    public static class BROADCAST {
        public static final String DATA_CURRENT_TIME = "timer.broadcast.data.current_time";
        public static final String ACTION_CURRENT_TIME = "timer.broadcast.action.current_time";
    }

    public static class SHAREDPREFS {
        public static final String TIMER_SPINNER_PAINS = "sharedprefs.timer.spinner_pains";
    }

    public static final int[] MATERIAL_COLORS_500 = {
            rgb("#ff5722"), rgb("#3f51b5"), rgb("#009688"), /*rgb("#00bcd4"),*/
            rgb("#e91e63"), /*rgb("#5677fc"),*/ /*rgb("#ffeb3b"),*/ rgb("#607d8b"),
            /*rgb("#9e9e9e"),*/ rgb("#259b24"), /*rgb("#8bc34a"),*/ rgb("#cddc39"),
            /*rgb("#cddc39"),*/ /*rgb("#ff9800"),*/ /*rgb("#03a9f4"),*/ rgb("#795548"),
            rgb("#673ab7"), rgb("#e51c23")
    };

    public static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;
        return Color.rgb(r, g, b);
    }

    public static int getColorById(long id) {
        return MATERIAL_COLORS_500[(int)id % MATERIAL_COLORS_500.length];
    }

}
