/*
 * Copyright (c) 2011 Daniel Isheden
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.mokyu.threading.test;

import java.io.IOException;
import java.util.Scanner;
import net.mokyu.threading.GameExecutor;
import net.mokyu.threading.MultithreadedExecutor;
import net.mokyu.threading.SingleThreadExecutor;
import net.mokyu.threading.SplitTask;
import net.mokyu.threading.TaskTree;
import net.mokyu.threading.TaskTreeBuilder;

public class ThreadingTest {

    private static int iterations = 5000000;
    private static int threads = 2;
    
    private static MySplitTask task;
    private static TaskTree tree;
    private static GameExecutor singleThreadExecutor;
    private static GameExecutor multithreadedExecutor;

    public static void main(String[] args) {

        System.out.println("This is a program to measure the performance gain from multithreading.");
        System.out.println("It calculates the sum (int)(Math.sin(i)*100); of all numbers between 0 and the number of iterations.");
        System.out.println("It is possible to specify the number of iterations from the command line.");

        if (args.length > 0) {
            try {
                int i = Integer.parseInt(args[0]);
                if (i <= 0) {
                    System.out.println("Number of iterations is negative. Using the default value (" + iterations + ").");
                } else {
                    iterations = i;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Error parsing the number of iterations. Using the default value (" + iterations + ").");
            }
        }
        System.out.println("The program will now start using " + iterations + " iterations.");

        Thread sleepFix = new Thread() {

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException ex) {
                        
                    }
                }
            }
        };
        sleepFix.setDaemon(true);
        sleepFix.start();

        Scanner scanner = new Scanner(System.in);
        
        TaskTreeBuilder builder = new TaskTreeBuilder();
        task = new MySplitTask(threads);
        builder.addTask(task);
        tree = builder.build();

        singleThreadExecutor = new SingleThreadExecutor();
        initMultithreadExecutor();

        while (true) {
            try {
                if (System.in.available() > 0) {
                    String s = scanner.nextLine();
                    if (s.equalsIgnoreCase("quit")) {
                        return;
                    }
                    try {
                        int i = Integer.parseInt(s);
                        if (i <= 0) {
                            System.out.println("Number of threads is negative.");
                        } else {
                            threads = i;
                            initMultithreadExecutor();
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Error parsing command. Instructions:");
                        System.out.println(" - To change the number of threads, type a number");
                        System.out.println(" - To quit, type 'quit' without quotes.");
                    }
                }
                
                System.out.println();
                //System.out.println("Running 1 vs " + threads + " threads.");

                task.setSubtasks(1);
                long startTime = System.nanoTime();
                singleThreadExecutor.run(tree);
                double singleTime = (System.nanoTime() - startTime) / 1000.0 / 1000.0;
                
                task.setSubtasks(threads);
                startTime = System.nanoTime();
                multithreadedExecutor.run(tree);
                double multiTime = (System.nanoTime() - startTime) / 1000.0 / 1000.0;
                
                System.out.println("Single-threaded time: " + singleTime + "ms");
                System.out.println("Multi-threaded time: " + multiTime + "ms");
                System.out.println("Scaling with " + threads + " threads: " + singleTime / multiTime);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void initMultithreadExecutor() {
        if (multithreadedExecutor != null) {
            multithreadedExecutor.close();
        }
       
        multithreadedExecutor = new MultithreadedExecutor(threads);
    }

    private static class MySplitTask extends SplitTask {

        long[] counters;

        public MySplitTask(int threads) {
            super(0, 0, threads);
            counters = new long[threads];
        }

        @Override
        protected void runSubtask(int subtask) {
            for (int i = subtask; i < iterations; i += getSubtasks()) {
                counters[subtask] += (int)(Math.sin(i)*100);
            }
        }

        @Override
        public void finish() {
            long total = 0;
            for (int i = 0; i < getSubtasks(); i++) {
                total += counters[i];
                counters[i] = 0;
            }
            System.out.println("The calculated result is: " + total);
        }

        @Override
        public void setSubtasks(int subtasks) {
            super.setSubtasks(subtasks);
            counters = new long[subtasks];
        }
    }
}
