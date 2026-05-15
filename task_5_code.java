// Клас потоку що виводить символ '-'
class DashThread extends Thread {

    @Override
    public void run() {
        System.out.print('-');
    }
}

// Клас потоку що виводить символ '|'
class PipeThread extends Thread {

    @Override
    public void run() {
        System.out.print('|');
    }
}

// Головна програма
public class SymbolPrinter {

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            Thread dash = new DashThread();
            Thread pipe = new PipeThread();

            dash.start();
            pipe.start();

            // Чекаємо завершення обох потоків перед переходом на новий рядок
            dash.join();
            pipe.join();

            System.out.println();
        }
    }
}
