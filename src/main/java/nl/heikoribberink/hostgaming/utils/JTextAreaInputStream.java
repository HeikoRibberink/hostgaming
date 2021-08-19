package nl.heikoribberink.hostgaming.utils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JTextArea;

/**
 * InputStream for JTextArea.
 *
 * @author <a href="https://github.com/HeikoRibberink">Heiko Ribberink</a>
 */
public class JTextAreaInputStream extends InputStream {
	byte[] contents;
	int pointer = 0;

	public JTextAreaInputStream(final JTextArea text) {
		super();
		contents = text.getText().getBytes();
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					contents = text.getText().getBytes();
					pointer = 0;
					text.setText("");
				}
				super.keyReleased(e);
			}
		});
	}

	@Override
	public int read() throws IOException {
		if (pointer >= contents.length)
			return -1;
		return this.contents[pointer++];
	}

}
