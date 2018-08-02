import cn.hutool.http.HttpUtil;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutoryServiceTest {
    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    public static void main(String[] args){
        System.out.println(System.currentTimeMillis());
        Thread thread = new MyThread();
        executorService.execute(thread);
        /*executorService.execute(thread);
        executorService.execute(thread);
        executorService.execute(thread);
        executorService.execute(thread);*/
    }
    public static class MyThread extends Thread {
        @Override
        public void run() {
            for (int i=0;i<5;i++)
                HttpUtil.createGet("http://www.oschina.net/").execute();
                System.out.println(System.currentTimeMillis());
        }
    }
}
