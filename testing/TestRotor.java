///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.slf4j:slf4j-api:2.0.12
//DEPS org.slf4j:slf4j-simple:2.0.12
//DEPS com.pi4j:pi4j-core:2.6.0
//DEPS com.pi4j:pi4j-plugin-raspberrypi:2.6.0
//DEPS com.pi4j:pi4j-plugin-gpiod:2.6.0

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class TestRotor {

    private static final int PIN_EN = 3;
    private static final int PIN_DIR = 5;
    private static final int PIN_STEP = 6;

    private static final int STEPS_PER_REV = 200 * 16;

    public static void main(String[] args) throws Exception {
        var pi4j = Pi4J.newAutoContext();
        var en = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .id("en")
                .name("Stepper Motor Enabled")
                .address(PIN_EN)
                .shutdown(DigitalState.HIGH)
                .initial(DigitalState.HIGH));
        var dir = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .id("dir")
                .name("Stepper Motor Direction")
                .address(PIN_DIR)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW));
        var step = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .id("step")
                .name("Stepper Motor Step")
                .address(PIN_STEP)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW));

        // Enable motor
        en.low();

        // Set motor direction clockwise
        dir.high();

        // Spin motor one rotation slowly
        for (int x = 0; x < (STEPS_PER_REV / 2); x++) {
            step.high();
            Thread.sleep(0, 500000);
            step.low();
            Thread.sleep(0, 500000);
        }

        // Pause for one second
        Thread.sleep(1000);

        // Set motor direction counterclockwise
        dir.low();

        // Spin motor two rotations quickly
        for(int x = 0; x < (STEPS_PER_REV / 2); x++) {
            step.high();
            Thread.sleep(0, 500000);
            step.low();
            Thread.sleep(0, 500000);
        }

        // Pause for one second
        Thread.sleep(1000);

        // Disable motor
        en.high();

        pi4j.shutdown();
    }

}
