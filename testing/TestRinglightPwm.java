///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.slf4j:slf4j-api:2.0.12
//DEPS org.slf4j:slf4j-simple:2.0.12
//DEPS com.pi4j:pi4j-core:2.6.0
//DEPS com.pi4j:pi4j-plugin-raspberrypi:2.6.0
//DEPS com.pi4j:pi4j-plugin-gpiod:2.6.0

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.util.Console;

public class TestRinglightPwm {

    private static final int PIN_RL1 = 17;
    private static final int PIN_RL2 = 27;

    public static void main(String[] args) throws Exception {
        var console = new Console();
        var pi4j = Pi4J.newAutoContext();
        var ringlight1 = new Ringlight(pi4j, 1, PIN_RL1);
        var ringlight2 = new Ringlight(pi4j, 2, PIN_RL2);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            console.println("Terminating");
            ringlight1.thread.interrupt();
            ringlight2.thread.interrupt();
            try {
                ringlight1.thread.join();
            } catch (InterruptedException ignored) {}
            try {
                ringlight2.thread.join();
            } catch (InterruptedException ignored) {}
            console.println("Terminated");
        }, "app-shutdown"));

        int intensity = 1;
        boolean increasing = false;

        while (true) {
            if (intensity == 0) {
                increasing = true;
            }
            if (intensity == 100) {
                increasing = false;
            }
            if (increasing) {
                intensity++;
            } else {
                intensity--;
            }
            console.println("Ringlight 1: " + intensity);
            console.println("Ringlight 2: " + (100 - intensity));
            ringlight1.intensity = intensity;
            ringlight2.intensity = 100 - intensity;
            Thread.sleep(500);
        }
    }

    private static class Ringlight implements Runnable {

        private static final int PERIOD_NS = 1_000_000_000 / 100; // 100Hz
        private static final int VALUE_MULTIPLIER = PERIOD_NS / 100; // mapping value range 0-100 to period length
        private static final int SLEEP_NS = PERIOD_NS / 1000;

        private final DigitalOutput pin;
        private volatile int intensity;
        private Thread thread;

        private Ringlight(Context context, int ringlightNumber, int pinNumber) {
            pin = context.create(DigitalOutput.newConfigBuilder(context)
                    .id("rl" + ringlightNumber)
                    .name("Ringlight " + ringlightNumber)
                    .address(pinNumber)
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW));
            intensity = 0;
            thread = Thread.ofVirtual().start(this);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (intensity == 100 && pin.isLow()) {
                        pin.high();
                    } else {
                        if (System.nanoTime() % PERIOD_NS < intensity * VALUE_MULTIPLIER) {
                            if (pin.isLow()) {
                                pin.high();
                            }
                        } else if (pin.isHigh()) {
                            pin.low();
                        }
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    Thread.sleep(0, SLEEP_NS);
                }
            } catch (InterruptedException ignored) {}
        }

    }

}
