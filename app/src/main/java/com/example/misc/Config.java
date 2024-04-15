package com.example.misc;

public class Config {
    public static int NUM_OF_WORKERS = 3;
    public static int USER_MASTER_PORT = 4444;
    public static int INIT_WORKER_PORT = 5000;
    public static int WORKER_REDUCER_PORT = 6000;
    public static int REDUCER_MASTER_PORT = 7000; //used by reducers to send refined results of workers to master

    public static String MASTER_IP = "localhost";
    public static String[] WORKER_IP = {"localhost", "localhost", "localhost"};
    public static String REDUCER_IP = "localhost";
}
