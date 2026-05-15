// Монітор — керує чергою виведення символів
class Monitor {

    private boolean dashTurn = true; // true = черга '-', false = черга '|'

    // Виводить '-' і передає чергу '|'
    public synchronized void printDash() throws InterruptedException {
        while (!dashTurn) {
            wait(); // чекає поки не буде черга '-'
        }
        System.out.print('-');
        dashTurn = false;
        notify(); // повідомляє потік '|' що тепер його черга
    }

    // Виводить '|' і передає чергу '-'
    public synchronized void printPipe() throws InterruptedException {
        while (dashTurn) {
            wait(); // чекає поки не буде черга '|'
        }
        System.out.print('|');
        dashTurn = true;
        notify(); // повідомляє потік '-' що тепер його черга
    }
}

// Клас потоку що виводить символ '-'
class DashThread extends Thread {

    private Monitor monitor;

    public DashThread(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            monitor.printDash();
        } catch (InterruptedException ex) {
        }
    }
}

// Клас потоку що виводить символ '|'
class PipeThread extends Thread {

    private Monitor monitor;

    public PipeThread(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            monitor.printPipe();
        } catch (InterruptedException ex) {
        }
    }
}

// Головна програма
public class SymbolPrinter {

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            Monitor monitor = new Monitor();

            Thread dash = new DashThread(monitor);
            Thread pipe = new PipeThread(monitor);

            dash.start();
            pipe.start();

            dash.join();
            pipe.join();

            System.out.println();
        }
    }
}
