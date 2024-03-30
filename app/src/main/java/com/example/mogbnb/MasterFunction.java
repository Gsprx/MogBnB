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
    };

    public abstract int getEncoded();
}
