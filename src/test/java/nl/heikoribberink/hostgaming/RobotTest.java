package nl.heikoribberink.hostgaming;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.AWTException;

import org.junit.Test;

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
}
