package nl.heikoribberink.hostgaming.utils;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nl.heikoribberink.hostgaming.BotMain;

/**
 * Used for simplifying the creation and use of virtual consoles using
 * {@link javax.swing.JFrame JFrame}.
 * 
 * @author <a href="https://github.com/HeikoRibberink">Heiko Ribberink</a>
 */

public class ConsoleWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private Canvas canvas;
	private JTextArea outputArea, inputArea;
	private JScrollPane scrollPane;

	/**
	 * Creates a new virtual window with a specified title, width and height.
	 * 
	 * @param title
	 * @param rows
	 * @param columns
	 */

	public ConsoleWindow(String title, int rows, int columns) {
		// setSize(width, height);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(title);
		setVisible(true);
		setLocation(new Point(0, 0));

		// Output Text Area with scrolling
		outputArea = new JTextArea(rows, columns);
		outputArea.setBackground(Color.black);
		outputArea.setForeground(Color.green);
		outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		outputArea.setVisible(true);
		outputArea.setFocusable(true);
		outputArea.enableInputMethods(false);
		outputArea.setLineWrap(true);
		outputArea.setEditable(false);
		out = new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				outputArea.append(String.valueOf((char) b));
			}
		}, true, Charset.forName("UTF-8"));

		scrollPane = new JScrollPane(outputArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		AdjustmentListener al = new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (BotMain.getInEvent())
					e.getAdjustable().setValue(e.getAdjustable().getMaximum());
			}
		};
		scrollPane.getVerticalScrollBar().addAdjustmentListener(al);
		add(scrollPane, BorderLayout.CENTER);

		inputArea = new JTextArea(3, columns);
		inputArea.setBackground(Color.black);
		inputArea.setForeground(Color.green);
		inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		inputArea.setVisible(true);
		inputArea.setFocusable(true);

		in = new JTextAreaInputStream(inputArea);

		add(inputArea, BorderLayout.PAGE_END);

		pack();
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	private PrintStream out;

	public PrintStream getOut() {
		return out;
	}

	public JTextArea getOutputArea() {
		return outputArea;
	}

	private InputStream in;

	public InputStream getIn() {
		return in;
	}

	public JTextArea getInputArea() {
		return inputArea;
	}
}