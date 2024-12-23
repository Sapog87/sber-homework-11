package org.sber.pool;

public interface ThreadPool {

    /**
     * Запускает потоки
     */
    void start();

    /**
     * Добавляет <code>Runnable</code> в очередь на исполнение
     *
     * @param runnable
     */
    void execute(Runnable runnable);

    /**
     * Инициализирует остановку <code>ThreadPool</code>
     */
    void shutdown();

    /**
     * @return количество запущенных потоков
     */
    int size();
}
