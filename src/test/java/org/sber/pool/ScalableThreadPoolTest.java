package org.sber.pool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScalableThreadPoolTest {

    @Test
    @DisplayName("Проверка, что все задачи были исполнены 1 раз")
    void givenTasks_whenExecuteWithThreadPool_thenAllRunsOneTime() throws InterruptedException {
        ThreadPool threadPool = new ScalableThreadPool(2, 4);
        Runnable runnable1 = mock(Runnable.class);
        Runnable runnable2 = mock(Runnable.class);
        Runnable runnable3 = mock(Runnable.class);

        threadPool.start();
        threadPool.execute(runnable1);
        threadPool.execute(runnable2);
        threadPool.execute(runnable3);

        Thread.sleep(100);

        //проверяем, что все Runnable были вызваны 1 раз
        verify(runnable1).run();
        verify(runnable2).run();
        verify(runnable3).run();
    }

    @Test
    @DisplayName("Проверка, что количество потоков растет при увеличении количества задач" +
            " и уменьшается при завершении всех задач")
    void givenTasks_whenExecuteWithThreadPool_thenIncreaseOnBusyAndDecreaseOnIdle() throws InterruptedException {
        ThreadPool threadPool = new ScalableThreadPool(2, 4);
        threadPool.start();

        Thread.sleep(100);

        assertEquals(2, threadPool.size());

        Runnable runnable = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        threadPool.execute(runnable);
        threadPool.execute(runnable);
        threadPool.execute(runnable);
        threadPool.execute(runnable);

        assertEquals(4, threadPool.size());

        //ждем, пока все выполнится
        Thread.sleep(200);

        assertEquals(2, threadPool.size());
    }

    @Test
    @DisplayName("Проверка, что потоки перезапускаются, когда задача кидает исключение")
    void givenTask_whenThrowException_thenThreadPoolRestartThread() throws InterruptedException {
        ThreadPool threadPool = new ScalableThreadPool(2, 2);
        threadPool.start();

        assertEquals(2, threadPool.size());

        threadPool.execute(() -> {
            throw new RuntimeException();
        });

        Thread.sleep(100);

        assertEquals(2, threadPool.size());
    }

}