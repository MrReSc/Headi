package com.example.headi;

public class Constants {

    public static class ACTION {
        public static final String START_ACTION = "timer.action.start";
        public static final String STOP_ACTION = "timer.action.stop";
        public static final String SAVE_ACTION = "timer.action.save";
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
        public static final String DATA_TIMER_STATE = "timer.broadcast.data.timer_state";
        public static final String ACTION_GET_TIMER_STATE = "timer.broadcast.action.get_timer_state";
        public static final String ACTION_SEND_TIMER_STATE = "timer.broadcast.action.send_timer_state";
    }
}