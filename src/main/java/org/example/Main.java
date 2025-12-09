package org.example;

import org.example.config.Config;
import org.example.worker.ClassWorker;

public class Main {
    //To run the application switch wrokers as needed by uncommenting them
    public static void main(String[] args) throws Exception {
        Config config = Config.get();
//        MethodWorker worker = new MethodWorker(config);
        ClassWorker worker = new ClassWorker(config);
        worker.run();
    }
}