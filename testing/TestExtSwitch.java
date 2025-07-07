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

public class TestExtSwitch {

    private static final int PIN_LED = 10;

    public static void main(String[] args) throws Exception {
        var console = new Console();
        var pi4j = Pi4J.newAutoContext();
        var led = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("LED Flasher")
                .address(PIN_LED)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW));
        try {
            while (true) {
                if (led.equals(DigitalState.HIGH)) {
                    console.println("LED low");
                    led.low();
                } else {
                    console.println("LED high");
                    led.high();
                }
                Thread.sleep(500);
            }
        } finally {
            pi4j.shutdown();
        }
    }

}
