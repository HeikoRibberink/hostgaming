package nl.heikoribberink.hostgaming;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

	public int enumTest(String keyName) throws SecurityException, IllegalArgumentException, IllegalAccessException {
		String fieldName = "VK_" + keyName;
		fieldName = fieldName.toUpperCase();
		Field f;
		try {
			f = KeyEvent.class.getField(fieldName);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Key code is invalid.");
		}
		if(f.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) throw new IllegalArgumentException("Key code is invalid.");
		return f.getInt(this);
	}

	@Test
	public void debugTest() {
		System.out.println(KeyEvent.getKeyText(68));
	}
}
