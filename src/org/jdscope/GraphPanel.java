package org.jdscope;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author "Hovercraft Full of Eels", "Rodrigo Azevedo"
 *
 *         This is an improved version of Hovercraft Full of Eels
 *         (https://stackoverflow.com/users/522444/hovercraft-full-of-eels)
 *         answer on StackOverflow: https://stackoverflow.com/a/8693635/753012
 *
 *         GitHub user @maritaria has made some performance improvements which
 *         can be found in the comment section of this Gist.
 */
public class GraphPanel extends JPanel {
	//private int width = 800;
	//private int height = 400;
	private int padding = 10;
	private int labelPadding = 25;
	private int toplabelpad = 20;
	private Color lineColor = new Color(44, 102, 230, 180);
	private Color pointColor = new Color(100, 100, 100, 180);
	private Color gridColor = new Color(200, 200, 200, 200);
	private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
	private int pointWidth = 4;
	private int numberYDivisions = 10;
	private int numberXDivisions = 20;
	private double minX = 0.0; 
	private double maxX = 0.0;
	private double xtickdivfrac = 1.0 * numberXDivisions;
	private double xtickfactor = 1.0;
	
	private List<Double> data;
	private String toplabel = "";
	
	private NumberFormat nformat;

	public GraphPanel(List<Double> data) {
		this.data = data;
		maxX = (double) data.size();
		nformat = NumberFormat.getInstance();
		nformat.setMaximumFractionDigits(2);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		dopaint(g);
	}
	
	public void dopaint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (data.size() - 1);
		double yScale = ((double) getHeight() - 2 * padding - labelPadding - toplabelpad) / (getMaxY() - getMinY());

		List<Point> graphPoints = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			int x1 = (int) (i * xScale + padding + labelPadding);
			int y1 = (int) ((getMaxY() - data.get(i)) * yScale + padding + toplabelpad);
			graphPoints.add(new Point(x1, y1));
		}

		// draw white background
		g2.setColor(Color.WHITE);
		g2.fillRect(padding + labelPadding, padding + toplabelpad, getWidth() - (2 * padding) - labelPadding,
				getHeight() - 2 * padding - labelPadding - toplabelpad);
		g2.setColor(Color.BLACK);

		// draw the label on top
		{
		FontMetrics metrics = g2.getFontMetrics();
		//int labelWidth = metrics.stringWidth(toplabel);
		g2.drawString(toplabel, padding + labelPadding, padding + toplabelpad - metrics.getDescent() - 5 );
		}

		// create hatch marks and grid lines for y axis.
		for (int i = 0; i < numberYDivisions + 1; i++) {
			int x0 = padding + labelPadding;
			int x1 = pointWidth + padding + labelPadding;
			int y0 = getHeight()
					- ((i * (getHeight() - padding * 2 - labelPadding - toplabelpad)) / numberYDivisions +
							padding + labelPadding );
			int y1 = y0;
			if (data.size() > 0) {
				g2.setColor(gridColor);
				g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
				g2.setColor(Color.BLACK);
				String yLabel = ((int) ((getMinY()
						+ (getMaxY() - getMinY()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(yLabel);
				g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
			}

			g2.drawLine(x0, y0, x1, y1);
		}

		// and for x axis
		int graphwidth = getWidth() - padding * 2 - labelPadding;
		int tickincr = (int) ((double) graphwidth / xtickdivfrac + 0.5);
		for (int i = 0; i < numberXDivisions + 1; i++) {		
			//int x0 = i * (getWidth() - padding * 2 - labelPadding) / numberXDivisions 
			//		+ padding + labelPadding;			
			if(i*tickincr > graphwidth) break;
			int x0 = padding + labelPadding + i*tickincr;
			int x1 = x0;
			int y0 = getHeight() - padding - labelPadding ;
			int y1 = y0 - pointWidth;
			
			if (data.size() > 0) {
				g2.setColor(gridColor);
				g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding + toplabelpad);
				g2.setColor(Color.BLACK);
				//String xLabel = ((int) ((getMinX()
				//		+ (getMaxX() - getMinX()) * ((xscale * i ) / numberXDivisions)) * 100)) / 100.0 + "";
				String xLabel = nformat.format(i*xtickfactor);
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(xLabel);
				g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
			}
			g2.drawLine(x0, y0, x1, y1);
		}

		// create x and y axes
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, 
				padding	+ labelPadding, padding + toplabelpad);
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding,
				getHeight() - padding - labelPadding);

		Stroke oldStroke = g2.getStroke();
		g2.setColor(lineColor);
		g2.setStroke(GRAPH_STROKE);
		for (int i = 0; i < graphPoints.size() - 1; i++) {
			int x1 = graphPoints.get(i).x;
			int y1 = graphPoints.get(i).y;
			int x2 = graphPoints.get(i + 1).x;
			int y2 = graphPoints.get(i + 1).y;
			g2.drawLine(x1, y1, x2, y2);
		}

		g2.setStroke(oldStroke);
		g2.setColor(pointColor);
		for (int i = 0; i < graphPoints.size(); i++) {
			int x = graphPoints.get(i).x - pointWidth / 2;
			int y = graphPoints.get(i).y - pointWidth / 2;
			int ovalW = pointWidth;
			int ovalH = pointWidth;
			g2.fillOval(x, y, ovalW, ovalH);
		}
		
	}
	
//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(width, height);
//    }

	private double getMinY() {
		double minScore = Double.MAX_VALUE;
		for (Double score : data) {
			minScore = Math.min(minScore, score);
		}
		return minScore;
	}

	private double getMaxY() {
		double maxScore = Double.MIN_VALUE;
		for (Double score : data) {
			maxScore = Math.max(maxScore, score);
		}
		return maxScore;
	}

		
	public double getXtickdivfrac() {
		return xtickdivfrac;
	}

	public void setXtickdivfrac(double xtickwidfrac) {
		this.xtickdivfrac = xtickwidfrac;
	}

	public double getXtickfactor() {
		return xtickfactor;
	}
	
	public void setXtickfactor(double value) {
		xtickfactor = value;
	}
	
	public int getXDivisions() {
		return numberXDivisions;
	}
	
	public void setXDivisions(int divisions) {
		numberXDivisions = divisions;
	}
	
	public double getMinX() {
		return (double) minX;
	}

	
	public void setMinX(double value) {
		this.minX =  value;
	}

	public double getMaxX() {
		//maxX = (double) data.size();
		return maxX;
	}

	public void setMaxX(double value) {
		maxX = value;
	}
	
	public void setData(List<Double> data) {
		this.data = data;
		maxX = (double) data.size();
		invalidate();
		this.repaint();
	}

	public List<Double> getData() {
		return data;
	}

	public void setToplabel(String label) {
		toplabel = label;
	}
	
	public BufferedImage snapshot() {
		BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		dopaint(image.getGraphics());
		return image;
	}

	private static final long serialVersionUID = 1L;
}
