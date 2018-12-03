import cn.hutool.http.HttpUtil;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutoryServiceTest {

    private int stackLength = 1;

    public void stackLeak() {
        int a=1;
        int b=2;
        stackLength++;
        stackLeak();
    }
    public static void main(String[] args) throws Throwable {

        /*ExecutoryServiceTest executoryServiceTest = new ExecutoryServiceTest();
        try {
            executoryServiceTest.stackLeak();
        }catch (Throwable e) {
            System.out.println("栈深度" + executoryServiceTest.stackLength);
            throw e;
        }*/
        /*System.out.println("AaAa".hashCode());
        System.out.println("BBBB".hashCode());
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        concurrentHashMap.computeIfAbsent();*/
    }

}
