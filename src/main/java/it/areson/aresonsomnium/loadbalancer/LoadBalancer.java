package it.areson.aresonsomnium.loadbalancer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class LoadBalancer extends BukkitRunnable {

    private static final int MAX_MILLIS_PER_TICK = 5;
    public static long LAST_TICK_START_TIME = 0;

    private final String id;
    private final ArrayDeque<Job> jobs;
    private final Semaphore mutex;
    private final CompletableFuture<Long> completableFuture;
    private long totalTicks;

    public LoadBalancer(String id) {
        this.id = id;
        this.mutex = new Semaphore(1);
        jobs = new ArrayDeque<>();
        totalTicks = 0;
        completableFuture = new CompletableFuture<>();
    }

    public synchronized void addJob(Job job) {
        try {
            mutex.acquire();
            jobs.add(job);
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isDone() {
        try {
            mutex.acquire();
            boolean empty = jobs.isEmpty();
            mutex.release();
            return empty;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public synchronized CompletableFuture<Long> start(JavaPlugin plugin) {
        if (!completableFuture.isDone()) {
            System.out.println("Load balancer '" + id + "' started");
            runTaskTimer(plugin, 0L, 1L);
            return completableFuture;
        } else {
            throw new RuntimeException("Load balancer '" + id + "' already finished");
        }
    }

    @Override
    public synchronized void run() {
        try {
            if (isDone()) {
                completableFuture.complete(totalTicks);
                System.out.println("Load balancer '" + id + "' finished");
                this.cancel();
            }
            long stopTime = System.currentTimeMillis() + MAX_MILLIS_PER_TICK;
            mutex.acquire();
            if (System.currentTimeMillis() + MAX_MILLIS_PER_TICK < LAST_TICK_START_TIME + 50) {
                while (!jobs.isEmpty() && System.currentTimeMillis() <= stopTime) {
                    Job poll = jobs.poll();
                    if (poll != null) {
                        poll.compute();
                    }
                }
            }
            totalTicks++;
            mutex.release();
        } catch (InterruptedException e) {
            completableFuture.completeExceptionally(e);
            e.printStackTrace();
        }
    }
}
