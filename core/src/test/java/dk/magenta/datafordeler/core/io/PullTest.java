package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.TaskListener;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;

@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PullTest extends GapiTestBase {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private Engine engine;

    @LocalServerPort
    private int port;


    @Test
    @Order(order=2)
    public void schedule() throws SchedulerException, InterruptedException {
        Plugin plugin = pluginManager.getPluginForSchema("demo");
        RegisterManager registerManager = plugin.getRegisterManager();
        String registerManagerId = registerManager.getClass().getName() + registerManager.hashCode();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        ListenerManager listenerManager = scheduler.getListenerManager();
        TaskListener taskListener = new TaskListener("PullTest.schedule");
        listenerManager.addJobListener(
            taskListener, KeyMatcher.keyEquals(new JobKey("pullTask-" + registerManagerId))
        );

        // A schedule to fire every second
        // Because we're down to 'every second', it will also fire immediately
        this.waitToMilliseconds(500, 50);
        engine.setupPullSchedule(plugin.getRegisterManager(), "* * * * * ?", true);

        Thread.sleep(1000);
        // One second has passed, should now have executed exactly twice (initial + 1 second)
        Assert.assertEquals(2, taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(2, taskListener.size(TaskListener.Event.jobWasExecuted));

        Thread.sleep(2000);
        // Three seconds have passed, should now have executed exactly four times (initial plus 3 seconds)
        Assert.assertEquals(4, taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(4, taskListener.size(TaskListener.Event.jobWasExecuted));


        // A schedule to fire every two seconds
        this.waitToMilliseconds(500, 50);
        taskListener.reset();
        engine.setupPullSchedule(plugin.getRegisterManager(), "0/2 * * * * ?", true);

        boolean evenSecond = OffsetDateTime.now().getSecond() % 2 == 0;
        Thread.sleep(4000);
        Assert.assertEquals(evenSecond ? 3 : 2, taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(evenSecond ? 3 : 2, taskListener.size(TaskListener.Event.jobWasExecuted));

        taskListener.reset();

        engine.setupPullSchedule(plugin.getRegisterManager(),
            (ScheduleBuilder)null, true);
        Thread.sleep(2000);
        // Should not run any further
        Assert.assertEquals(0, taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(0, taskListener.size(TaskListener.Event.jobWasExecuted));
    }

}
