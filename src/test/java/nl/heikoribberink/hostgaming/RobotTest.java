package nl.heikoribberink.hostgaming;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.junit.Test;

import nl.heikoribberink.hostgaming.utils.ConsoleWindow;

public class RobotTest {
	@Test
	public void robotTest() {
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.KEY_FIRST);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void windowTest() {
		ConsoleWindow console = new ConsoleWindow("title", 24, 120);
		for (int i = 0; i < 100; i++) {
			console.getOut().println("Hello, User" + i + ".");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		console.dispose();
	}
}
