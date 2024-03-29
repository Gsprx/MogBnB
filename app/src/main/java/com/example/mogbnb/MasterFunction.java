package com.example.mogbnb;

public enum MasterFunction {

    SHOW_ROOMS {
        @Override
        public int getEncoded() {
            return 0;
        }
    },

    ADD_ROOM {
        @Override
        public int getEncoded() {
            return 1;
        }
    };

    public abstract int getEncoded();
}
