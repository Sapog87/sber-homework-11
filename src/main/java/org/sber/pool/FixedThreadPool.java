package org.sber.pool;

import java.util.Objects;

public class FixedThreadPool extends AbstractThreadPool {
    private final int threads;

    public FixedThreadPool(int threads) {
        if (threads < 1)
            throw new IllegalArgumentException("threads must be greater than 0");

        this.threads = threads;
    }

    /**
     * Запускает потоки
     */
    @Override
    public void start() {
        startWorkers(threads);
    }

    /**
     * Добавляет <code>Runnable</code> в очередь на исполнение
     *
     * @param runnable
     */
    @Override
    public void execute(Runnable runnable) {
        Objects.requireNonNull(runnable);
        synchronized (queue) {
            queue.add(runnable);
            queue.notify();
        }
    }

    /**
     * @return задачу, или null, если
     * <ul>
     *     <li><code>ThreadPool</code> был остановлен</li>
     * </ul>
     */
    @Override
    protected Runnable getTask() {
        synchronized (queue) {
            if (running && queue.isEmpty()) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (running) {
                return queue.poll();
            }
            return null;
        }
    }

    /**
     * Перезапускает поток, если он вышел из-за <code>Exception</code>
     *
     * @param onException вышел ли поток по <code>Exception</code>, которое бросил <code>Runnable</code>
     */
    @Override
    protected void handleWorkerExit(boolean onException) {
        synchronized (workers) {
            workers.remove(Thread.currentThread());
            if (onException) {
                addWorker();
            }
        }
    }
}
