package com.example.mogbnb;

public enum MasterFunction {

    SHOW_ROOMS {
        @Override
        public int getEncoded() {
            return 0;
        }
    };

    public abstract int getEncoded();
}
