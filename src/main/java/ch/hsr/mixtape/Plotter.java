package ch.hsr.mixtape;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class Plotter extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Color LINE_COLOR = Color.black;
	private static final Color POLYGON_COLOR = Color.cyan.darker();
	private static final Color BACKGROUND_COLOR = Color.white;

	public Plotter(String title) {
		setTitle(title);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		createBufferStrategy(2);
	}

	public void draw(double[] values) {
		BufferStrategy bufferStrategy = getBufferStrategy();
		Graphics graphics = bufferStrategy.getDrawGraphics();

		drawBackground(graphics);
		drawPolygon(values, graphics);
		drawLine(graphics);

		graphics.dispose();
		bufferStrategy.show();

		Toolkit.getDefaultToolkit().sync();
	}

	private void drawBackground(Graphics graphics) {
		graphics.setColor(BACKGROUND_COLOR);
		graphics.fillRect(0, 0, getWidth(), getHeight());
	}

	private void drawPolygon(double[] values, Graphics graphics) {
		int n = values.length;
		double alphaX = (double) getWidth() / (n + 2);

		int[] x = new int[n + 2];
		int[] y = new int[n + 2];

		x[0] = 0;
		x[n + 1] = getWidth();

		y[0] = getHeight() / 2;
		y[n + 1] = getHeight() / 2;

		for (int i = 0; i < n; i++) {
			x[i + 1] = (int) (alphaX * i);
			y[i + 1] = (int) (-values[i] * (getHeight() * 0.5 * 0.8) +  getHeight() / 2);
		}

		graphics.setColor(POLYGON_COLOR);
		graphics.fillPolygon(x, y, x.length);
	}

	private void drawLine(Graphics graphics) {
		graphics.setColor(LINE_COLOR);
		graphics.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
	}

}