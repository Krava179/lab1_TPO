// Клас лічильника
class Counter {

    private int value = 0;

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public int getValue() {
        return value;
    }
}

// Головна програма
public class CounterMain {

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

        Thread incrementThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    counter.increment();
                }
            }
        });

        Thread decrementThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    counter.decrement();
                }
            }
        });

        incrementThread.start();
        decrementThread.start();

        incrementThread.join();
        decrementThread.join();

        System.out.println("Final counter value: " + counter.getValue());
    }
}
