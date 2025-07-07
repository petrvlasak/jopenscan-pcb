///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.slf4j:slf4j-api:2.0.12
//DEPS org.slf4j:slf4j-simple:2.0.12
//DEPS com.pi4j:pi4j-core:2.6.0
//DEPS com.pi4j:pi4j-plugin-raspberrypi:2.6.0
//DEPS com.pi4j:pi4j-plugin-gpiod:2.6.0

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.util.Console;

public class TestRinglight {

    private static final int PIN_RL1 = 17;
    private static final int PIN_RL2 = 27;

    public static void main(String[] args) throws Exception {
        var console = new Console();
        var pi4j = Pi4J.newAutoContext();
        var ringlight1 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .id("rl1")
                .name("Ringlight 1")
                .address(PIN_RL1)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW));
        var ringlight2 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .id("rl2")
                .name("Ringlight 2")
                .address(PIN_RL2)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW));
        try {
            while (true) {
                if (ringlight1.equals(DigitalState.LOW)) {
                    console.println("Ringlight 1");
                    ringlight1.high();
                    ringlight2.low();
                } else {
                    console.println("Ringlight 2");
                    ringlight2.high();
                    ringlight1.low();
                }
                Thread.sleep(500);
            }
        } finally {
            pi4j.shutdown();
        }
    }

}
