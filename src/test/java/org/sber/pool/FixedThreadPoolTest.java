package org.sber.pool;

import helper.KnowUsedThreadRunnable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FixedThreadPoolTest {

    @Test
    @DisplayName("Проверка, что все задачи были исполнены 1 раз")
    void givenTasks_whenExecuteWithThreadPool_thenAllRunsOneTime() throws InterruptedException {
        ThreadPool threadPool = new FixedThreadPool(2);
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
    @DisplayName("Проверка, что задачи выполняются в разных потоках")
    void givenTasks_whenExecuteWithThreadPool_thenRunsInDifferentThread() throws InterruptedException {
        ThreadPool threadPool = new FixedThreadPool(2);
        threadPool.start();

        Runnable runnable = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        KnowUsedThreadRunnable runnable1 = new KnowUsedThreadRunnable(runnable);
        KnowUsedThreadRunnable runnable2 = new KnowUsedThreadRunnable(runnable);

        threadPool.execute(runnable1);
        threadPool.execute(runnable2);

        Thread.sleep(200);

        assertNotEquals(runnable1.getUsedThread(), runnable2.getUsedThread());
    }

    @Test
    @DisplayName("Проверка, что количество запущенных потоков всегда одинаково")
    void givenTasks_whenExecuteWithThreadPool_thenThreadCountAlwaysSame() throws InterruptedException {
        ThreadPool threadPool = new FixedThreadPool(2);

        threadPool.start();
        assertEquals(2, threadPool.size());

        threadPool.execute(() -> {});
        threadPool.execute(() -> {});
        threadPool.execute(() -> {});

        assertEquals(2, threadPool.size());

        Thread.sleep(100);

        assertEquals(2, threadPool.size());
    }

    @Test
    @DisplayName("Проверка, что потоки перезапускаются, когда задача кидает исключение")
    void givenTask_whenThrowException_thenThreadPoolRestartThread() throws InterruptedException {
        ThreadPool threadPool = new FixedThreadPool(2);
        threadPool.start();

        assertEquals(2, threadPool.size());

        threadPool.execute(() -> {
            throw new RuntimeException();
        });

        Thread.sleep(100);

        assertEquals(2, threadPool.size());
    }
}