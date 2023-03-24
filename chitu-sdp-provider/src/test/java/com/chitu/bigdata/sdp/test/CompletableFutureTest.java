package com.chitu.bigdata.sdp.test;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.*;
import java.util.function.*;


/**
 * @author sutao
 * @create 2021-06-07 18:16
 *
 * CompletableFuture 是 java8 中新增的一个类，算是对 Future 的一种增强，用起来很方便。
 *
 * CompletionStage 接口
 *
 * 1.CompletionStage 代表异步计算过程中的某一个阶段，一个阶段完成以后可能会触发另外一个阶段
 * 2.一个阶段的计算执行可以是一个 Function，Consumer 或者 Runnable。比如：stage.thenApply(x -> square(x)).thenAccept(x -> System.out.print(x)).thenRun(() -> System.out.println())
 * 3.一个阶段的执行可能是被单个阶段的完成触发，也可能是由多个阶段一起触发
 *
 *
 * CompletableFuture 类
 *
 * 1.在 Java8 中，CompletableFuture 提供了非常强大的 Future 的扩展功能，可以帮助我们简化异步编程的复杂性，
 * 并且提供了函数式编程的能力，可以通过回调的方式处理计算结果，也提供了转换和组合 CompletableFuture 的方法。
 * 2.它可能代表一个明确完成的 Future，也有可能代表一个完成阶段（ CompletionStage ），它支持在计算完成以后触发一些函数或执行某些动作。
 * 3.它实现了 Future 和 CompletionStage 接口
 *
 *
 *
 * CompletableFuture提供的api方法很多,我们可以将其归类来理解，首先是以then开头的方法，如thenAccept
 * 凡是带accept的方法都没有返回值，接收的是一个消费者（Customer）
 * accept和acceptAsync的区别是,带Async的方法，可以异步执行，默认是使用forkjoinpool并且可以指定其他线程池。
 * 以apply结尾的参数是接收的一个生产者(FUNCTION)，具有返回值。
 * 以run开头的对先前的执行结果不关心，执行完毕后直接执行下一个操作。
 * 带Either的用在两个异步方法，只要取其中一个执行完毕就执行操作。
 */
public class CompletableFutureTest {


    /**
     * CompletableFuture 提供了四个静态方法来创建一个异步操作。
     * public static CompletableFuture<Void> runAsync(Runnable runnable)
     * public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
     * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
     * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
     *
     * 没有指定 Executor 的方法会使用 ForkJoinPool.commonPool() 作为它的线程池执行异步代码。如果指定线程池，则使用指定的线程池运行。以下所有的方法都类同。
     * runAsync 方法不支持返回值。
     * supplyAsync 可以支持返回值。
     *
     */


    /**
     * 无返回值
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void runAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
            }
            System.out.println("run end ...");
        });

        future.get();
    }

    /**
     * 有返回值
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void supplyAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
            }
            System.out.println("run end ...");
            return System.currentTimeMillis();
        });

        long time = future.get();
        System.out.println("time = " + time);
    }


    /**
     * 计算结果完成时的回调方法
     * <p>
     * whenComplete 和 whenCompleteAsync 的区别：
     * whenComplete：是执行当前任务的线程执行继续执行 whenComplete 的任务。
     * whenCompleteAsync：是执行把 whenCompleteAsync 这个任务继续提交给线程池来进行执行。
     *
     * @throws Exception
     */
    @Test
    public void whenComplete() throws InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " run start ...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            if (new Random().nextInt() % 2 >= 0) {
                int i = 12 / 0;
            }
            System.out.println(Thread.currentThread().getName() + " run end ...");
        });

        //future.whenCompleteAsync
        future.whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void t, Throwable action) {
                System.out.println(Thread.currentThread().getName() + " 执行完成！");
            }

        });
        future.exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable t) {
                System.out.println(Thread.currentThread().getName() + " 执行失败！" + t.getMessage());
                return null;
            }
        });

        TimeUnit.SECONDS.sleep(5);
    }


    @Test
    public void whenComplete1() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " run start ...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            System.out.println(1 / 0);
            System.out.println(Thread.currentThread().getName() + " run end ...");
            return 0;
        });

        //future.whenCompleteAsync
        future.whenComplete(new BiConsumer<Integer, Throwable>() {
            @Override
            public void accept(Integer t, Throwable action) {
                System.out.println(Thread.currentThread().getName() + " 执行完成！结果为：" + t);
            }

        });
        future.exceptionally(new Function<Throwable, Integer>() {
            @Override
            public Integer apply(Throwable t) {
                System.out.println(Thread.currentThread().getName() + " 执行失败！" + t.getMessage());
                return -1;
            }
        });

        System.out.println(future.get());

    }


    /**
     * 当一个线程依赖另一个线程时，可以使用 thenApply 方法来把这两个线程串行化。
     * Function
     * T：上一个任务返回结果的类型
     * U：当前任务的返回值类型
     *
     * @throws Exception
     */
    @Test
    public void thenApply() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                long result = new Random().nextInt(100);
                System.out.println("result1=" + result);
                return result;
            }
        }).thenApply(new Function<Long, Long>() {
            @Override
            public Long apply(Long t) {
                long result = t * 5;
                System.out.println("result2=" + result);
                return result;
            }
        });

        long result = future.get();
        System.out.println(result);
    }


    /**
     * handle 是执行任务完成时对结果的处理。
     * handle 方法和 thenApply 方法处理方式基本一样。
     * 不同的是 handle 是在任务完成后再执行，还可以处理异常的任务。
     * thenApply 只可以执行正常的任务，任务出现异常则不执行 thenApply 方法。
     *
     * @throws Exception
     */
    @Test
    public void handle() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int i = 10 / 0;
                return new Random().nextInt(10);
            }
        }).handle(new BiFunction<Integer, Throwable, Integer>() {
            @Override
            public Integer apply(Integer param, Throwable throwable) {
                int result = -1;
                if (throwable == null) {
                    result = param * 2;
                } else {
                    System.out.println(throwable.getMessage());
                }
                return result;
            }
        });
        System.out.println(future.get());
    }


    /**
     * 接收任务的处理结果，并消费处理，无返回结果。
     *
     * @throws Exception
     */
    @Test
    public void thenAccept() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return new Random().nextInt(10);
            }
        }).thenAccept(integer -> {
            System.out.println(integer);
        });
        future.get();
    }


    /**
     * 跟 thenAccept 方法不一样的是，不关心任务的处理结果。只要上面的任务执行完成，就开始执行 thenAccept 。
     */
    @Test
    public void thenRun() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return new Random().nextInt(10);
            }
        }).thenRun(() -> {
            System.out.println("thenRun  ...");
        });
        future.get();
    }


    /**
     * thenCombine 会把 两个 CompletionStage 的任务都执行完成后，把两个任务的结果一块交给 thenCombine 来处理。
     */
    @Test
    public void thenCombine() throws Exception {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                return "hello";
            }
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                return "hello";
            }
        });
        CompletableFuture<String> result = future1.thenCombine(future2, new BiFunction<String, String, String>() {
            @Override
            public String apply(String t, String u) {
                return t + "  " + u;
            }
        });
        System.out.println(result.get());
    }


    /**
     * 当两个 CompletionStage 都执行完成后，把结果一块交给 thenAcceptBoth 来进行消费
     */
    @Test
    public void thenAcceptBoth() throws InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f1=" + t);
                return t;
            }
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f2=" + t);
                return t;
            }
        });

        f1.thenAcceptBoth(f2, new BiConsumer<Integer, Integer>() {
            @Override
            public void accept(Integer t, Integer u) {
                System.out.println("f1=" + t + ";f2=" + u + ";");
            }
        });

        TimeUnit.SECONDS.sleep(10);
    }


    /**
     * 两个 CompletionStage，谁执行返回的结果快，我就用那个 CompletionStage 的结果进行下一步的转化操作。
     */
    @Test
    public void applyToEither() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f1=" + t);
                return t;
            }
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f2=" + t);
                return t;
            }
        });

        CompletableFuture<Integer> result = f1.applyToEither(f2, new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer t) {
                System.out.println(t);
                return t * 2;
            }
        });

        System.out.println(result.get());
    }


    /**
     * 两个 CompletionStage，谁执行返回的结果快，我就用那个 CompletionStage 的结果进行下一步的消费操作。
     */
    @Test
    public void acceptEither() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f1=" + t);
                return t;
            }
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f2=" + t);
                return t;
            }
        });

        f1.acceptEither(f2, new Consumer<Integer>() {
            @Override
            public void accept(Integer t) {
                System.out.println(t);
            }
        });

        TimeUnit.SECONDS.sleep(10);

    }


    /**
     * 两个 CompletionStage，任何一个完成了都会执行下一步的操作（Runnable）
     */
    @Test
    public void runAfterEither() throws InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f1=" + t);
                return t;
            }
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f2=" + t);
                return t;
            }
        });

        f1.runAfterEither(f2, new Runnable() {
            @Override
            public void run() {
                System.out.println("上面有一个已经完成了。");
            }
        });

        TimeUnit.SECONDS.sleep(10);

    }


    /**
     * 两个 CompletionStage，都完成了计算才会执行下一步的操作（Runnable）
     */
    @Test
    public void runAfterBoth() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f1=" + t);
                return t;
            }
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                try {
                    TimeUnit.SECONDS.sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("f2=" + t);
                return t;
            }
        });

        f1.runAfterBoth(f2, new Runnable() {
            @Override
            public void run() {
                System.out.println("上面两个任务都执行完成了。");
            }
        });

        TimeUnit.SECONDS.sleep(10);
    }


    /**
     * thenCompose 方法允许你对两个 CompletionStage 进行流水线操作，第一个操作完成时，将其结果作为参数传递给第二个操作。
     */
    @Test
    public void thenCompose() throws Exception {
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int t = new Random().nextInt(3);
                System.out.println("t1=" + t);
                return t;
            }
        }).thenCompose(new Function<Integer, CompletionStage<Integer>>() {
            @Override
            public CompletionStage<Integer> apply(Integer param) {
                return CompletableFuture.supplyAsync(new Supplier<Integer>() {
                    @Override
                    public Integer get() {
                        int t = param * 2;
                        System.out.println("t2=" + t);
                        return t;
                    }
                });
            }

        });
        System.out.println("thenCompose  result  :  " + f.get());
    }


    /**
     *  查询一个商品详情,需要分别去查商品信息,卖家信息,库存信息,订单信息等,这
     *  些查询相互独立,在不同的服务上,假设每个查询都需要一到两秒钟,要求总体查询时间小于2秒.
     * @throws Exception
     */
    @Test
    public void allOf() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        long start = System.currentTimeMillis();
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "商品详情";
        }, executorService);

        CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "卖家信息";
        }, executorService);

        CompletableFuture<String> futureC = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "库存信息";
        }, executorService);

        CompletableFuture<String> futureD = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "订单信息";
        }, executorService);

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futureA, futureB, futureC, futureD);
        allFuture.join();

        System.out.println(futureA.join() + futureB.join() + futureC.join() + futureD.join());
        System.out.println("总耗时:" + (System.currentTimeMillis() - start));


    }




    @Test
    public void anyOf() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        long start = System.currentTimeMillis();
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "商品详情";
        }, executorService);

        CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "卖家信息";
        }, executorService);

        CompletableFuture<String> futureC = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "库存信息";
        }, executorService);

        CompletableFuture<String> futureD = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "订单信息";
        }, executorService);

        CompletableFuture<Object> allFuture1 = CompletableFuture.anyOf(futureA, futureB, futureC, futureD);
        System.out.println(allFuture1.get());

    }









}
