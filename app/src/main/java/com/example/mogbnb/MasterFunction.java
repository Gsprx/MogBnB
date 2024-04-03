package com.example.mogbnb;

public enum MasterFunction {
    INIT_ROOMS {
        @Override
        public int getEncoded() {
            return 0;
        }
    },
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

    FIND_ROOM_BY_NAME{
        @Override
        public int getEncoded(){return 4;}
    },
    SEARCH_ROOM{
        @Override
        public int getEncoded(){return 5;}
    },
    RATE_ROOM{
        @Override
        public int getEncoded(){return 6;}
    },
    SHOW_BOOKINGS{
        @Override
        public int getEncoded(){return 7;}
    };

    public abstract int getEncoded();
}
