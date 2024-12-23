package org.sber.pool;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public abstract class AbstractThreadPool implements ThreadPool {
    protected volatile boolean running;
    protected volatile int activeTasks;
    protected final Queue<Runnable> queue;
    protected final Set<Thread> workers;

    protected AbstractThreadPool() {
        this.workers = new HashSet<>();
        this.queue = new LinkedList<>();
        this.running = false;
        this.activeTasks = 0;
    }

    /**
     * Запускает <code>n</code> потоков
     *
     * @param n
     */
    protected void startWorkers(int n) {
        synchronized (workers) {
            if (running)
                return;

            running = true;
            for (int i = 0; i < n; i++) {
                addWorker();
            }
        }
    }


    /**
     * Создает новый поток
     */
    protected void addWorker() {
        Thread worker = new Thread(new Worker());
        worker.setUncaughtExceptionHandler(
                (t, e) -> e.printStackTrace()
        );
        workers.add(worker);
        worker.start();
    }

    /**
     * Инициализирует остановку <code>ThreadPool</code>
     */
    @Override
    public void shutdown() {
        synchronized (queue) {
            running = false;
            queue.notifyAll();
        }

        // ждем пока все потоки закончат исполнение текущих задач
        while (true) {
            synchronized (workers) {
                if (workers.isEmpty()) {
                    return;
                }
            }
        }
    }

    /**
     * @return количество запущенных потоков
     */
    @Override
    public int size() {
        return workers.size();
    }

    /**
     * Возвращает следующую задачу из очереди
     *
     * @return задачу или null, если текущий поток должен выйти
     */
    protected abstract Runnable getTask();

    /**
     * Обрабатывает выход потока
     *
     * @param onException вышел ли поток по <code>Exception</code>, которое бросил <code>Runnable</code>
     */
    protected abstract void handleWorkerExit(boolean onException);

    /**
     * Класс, который исполняет <code>Runnable</code>, переданные в <code>ThreadPool</code>
     */
    private class Worker implements Runnable {
        @Override
        public void run() {
            Runnable task;
            boolean onException = true;
            try {
                while ((task = getTask()) != null) {
                    synchronized (workers) {
                        activeTasks++;
                    }

                    task.run();

                    synchronized (workers) {
                        activeTasks--;
                    }
                }
                onException = false;
            } finally {
                handleWorkerExit(onException);
            }
        }
    }
}
