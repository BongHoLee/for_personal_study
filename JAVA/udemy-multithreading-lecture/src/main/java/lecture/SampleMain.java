package lecture;

public class SampleMain {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("we are now in thread : " + Thread.currentThread().getName());
                System.out.println("current thread priority is " + Thread.currentThread().getPriority());
                throw new RuntimeException("RUNTIME EXCEPTION!");
            }
        });

        // thread에 의미있는 이름을 부여한다.
        thread.setName("New Worker Thread");

        // 스레드의 스케쥴링 우선순위를 조정할 수 있다.
        thread.setPriority(Thread.MAX_PRIORITY);

        // 현재 스레드의 NAME 정보를 가져온다.
        System.out.println("we are in thread : " + Thread.currentThread().getName() + " before start new thread");
        thread.start();
        System.out.println("we are in thread : " + Thread.currentThread().getName() + " after start new thread");

        // 현재 스레드를 주어진 millis 만큼 멈추게 한다. -> 해당 스레드는 CPU에 스케쥴링 되지 않는다.(CPU 자원을 사용하지 않는다.)
        Thread.sleep(10000);
        System.out.println("hihi");
    }
}
