package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class SecondlyTriggerTest {
    @Test
    public void delayInSeconds() {
        SecondlyTrigger trigger = new SecondlyTrigger(null, null, 5);
        assertEquals(Duration.ZERO, trigger.delay(0, 0));
        assertEquals(Duration.ofNanos(999999500L), trigger.delay(4, 500));
        assertEquals(Duration.ZERO, trigger.delay(5, 0));
        assertEquals(Duration.ofSeconds(4), trigger.delay(6, 0));
        assertEquals(Duration.ZERO, trigger.delay(10, 0));

        trigger = new SecondlyTrigger(null, null, 7);
        assertEquals(Duration.ZERO, trigger.delay(0, 0));
        assertEquals(Duration.ofNanos(2999999000L), trigger.delay(4, 1000));
        assertEquals(Duration.ofSeconds(5), trigger.delay(30, 0));

        trigger = new SecondlyTrigger(null, null, 120);
        assertEquals(Duration.ZERO, trigger.delay(0, 0));
        assertEquals(Duration.ofSeconds(115), trigger.delay(5, 0));
        assertEquals(Duration.ofMillis(89500), trigger.delay(30, 500000000));
    }
}
