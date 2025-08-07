import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
public class MultiThreadedDataProcessor {
    // Number of worker threads
    private static final int NUM_WORKERS = 4;

    // Shared task queue and result list
    private static final BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();
    private static final List<String> resultList = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("System started.");

        // Adding 20 tasks to the shared queue
        for (int i = 1; i <= 20; i++) {
            taskQueue.add("Task-" + i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS);

        // Start worker threads
        for (int i = 0; i < NUM_WORKERS; i++) {
            executor.submit(new Worker("Worker-" + (i + 1)));
        }

        // Shutdown the executor after task completion
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted during shutdown.");
            executor.shutdownNow();
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter("output_java.txt"))) {
            for (String result : resultList) {
                writer.println(result);
            }
            System.out.println("Results saved to output_java.txt");
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }

        System.out.println("System completed.");
    }
    static class Worker implements Runnable {
        private final String name;

        public Worker(String name) {
            this.name = name;
        }
        @Override
        public void run() {
            try {
                while (true) {
                    String task = taskQueue.poll(2, TimeUnit.SECONDS);
                    if (task == null) {
                        System.out.println(name + " found no more tasks and is exiting.");
                        break;
                    }
                    System.out.println(name + " started processing " + task);
                    simulateWork(); 
                    String result = name + " completed " + task;
                    resultList.add(result);
                    System.out.println(result);
                }
            } catch (InterruptedException e) {
                System.err.println(name + " was interrupted.");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println(name + " encountered an error: " + e.getMessage());
            }
        }
        private void simulateWork() throws InterruptedException {
            Thread.sleep(500); 
        }
    }
}
