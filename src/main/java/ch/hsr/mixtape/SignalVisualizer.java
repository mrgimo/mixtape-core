package ch.hsr.mixtape;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class SignalVisualizer extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Color LINE_COLOR = Color.black;
	private static final Color SIGNAL_COLOR = Color.cyan.darker();
	private static final Color BACKGROUND_COLOR = Color.white;

	private static final int ALPHA_X = 2;

	private int[] x;
	private int[] y;

	private int point = 1;

	private int width;
	private int height;

	private int halfHeight;

	public SignalVisualizer() {
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		createBufferStrategy(2);
		addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent event) {
				setup();
			}

		});

		setup();
	}

	private void setup() {
		width = getWidth();
		height = getHeight();

		halfHeight = height / 2;

		int numberOfPoints = width / ALPHA_X;

		x = new int[numberOfPoints];
		y = new int[numberOfPoints];

		x[numberOfPoints - 1] = width;
		for (int i = 1; i < x.length - 1; i++)
			x[i] = x[i - 1] + ALPHA_X;

		y[0] = height / 2;
		y[numberOfPoints - 1] = halfHeight;
	}

	public void addSample(double sample) {
		if (++point >= x.length - 1)
			drawSamples();

		y[point] = (int) (halfHeight * (-sample + 1));
	}

	private void drawSamples() {
		BufferStrategy bufferStrategy = getBufferStrategy();
		Graphics graphics = bufferStrategy.getDrawGraphics();

		graphics.setColor(BACKGROUND_COLOR);
		graphics.fillRect(0, 0, width, height);

		graphics.setColor(SIGNAL_COLOR);
		graphics.fillPolygon(x, y, x.length);

		graphics.setColor(LINE_COLOR);
		graphics.drawLine(0, halfHeight, width, halfHeight);

		graphics.dispose();
		bufferStrategy.show();

		Toolkit.getDefaultToolkit().sync();

		point = 1;
	}

}