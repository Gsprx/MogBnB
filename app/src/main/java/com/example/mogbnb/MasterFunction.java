package com.example.mogbnb;

public enum MasterFunction {
    SHOW_ROOMS {
        @Override
        public int getEncoded() {
            return 1;
        }
    },

    ADD_ROOM {
        @Override
        public int getEncoded() {
            return 2;
        }
    },

    BOOKINGS_PER_AREA {
        @Override
        public int getEncoded() {
            return 3;
        }
    },

    FIND_ROOM_BY_NAME{
        @Override
        public int getEncoded(){return 10;}
    };

    public abstract int getEncoded();
}
