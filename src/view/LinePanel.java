package view;

import hamSanApp.Crossing;
import hamSanApp.HamSanAlg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

//import java.util.Date;

public class LinePanel extends JPanel implements MouseMotionListener, MouseWheelListener, MouseInputListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1230109349211303663L;

	private HamSanAlg h;
	private double xmin = -10;
	private double xmax = 10;
	private double ymin = -10;
	private double ymax = 10;

	private double zoomFactor = 0;
	private Point2D.Double corner1, corner2;

	private boolean showCrossings = true;

	private VisualPoint highlightedPoint = null;
	
	private PointPanel pointPanel;

	private List<VisualPoint> visualPoints;

	LinePanel(HamSanAlg hsa) {
		h = hsa;
		visualPoints = hsa.getVisualPoints();
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addMouseListener(this);
	}

	public void setPointPanel(PointPanel pp) {
		this.pointPanel = pp;
	}

	public void setVisualPoints(List<VisualPoint> visualPoints) {
		this.visualPoints = visualPoints;
	}

	public void drawCross(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.gray);

		int x0 = (int) VisualPoint.aToX(0, xmin, xmax, this.getSize());
		int y0 = (int) VisualPoint.bToY(0, ymin, ymax, this.getSize());

		g.drawLine(0, y0, this.getWidth(), y0);
		g.drawLine(x0, 0, x0, this.getHeight());
	}

	public void drawPoint(Graphics g, int x, int y) {
		g.fillOval(x - 2, y - 2, 4, 4);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawCross(g);
		for (VisualPoint p : visualPoints) {
			p.drawAsLine(g, xmin, xmax, ymin, ymax, this.getSize());
		}

		if (showCrossings) {
			g.setColor(Color.GREEN);
			for (Crossing c : h.crossings) {
				if (c.atInf()) {
					continue;
				}
				double crossingA = c.crAt();
				double crossingB = c.a.a * crossingA + c.a.b;

				Point2D.Double asAB = new Point2D.Double(crossingA, crossingB);
				Point2D.Double asXY = VisualPoint.toXY(asAB, xmin, ymin, xmax, ymax, this.getSize());
				int x = (int) asXY.x;
				int y = (int) asXY.y;

				g.drawLine(x - 2, y, x + 2, y);
				g.drawLine(x, y - 2, x, y + 2);

				// drawPoint(g, (int) asXY.x, (int) asXY.y);
			}
		}

		g.setColor(Color.magenta);
		if (h.done && (h.verticalSol || h.solution != null)) {
			Point2D.Double cutAB = new Point2D.Double(-h.solution.a, h.solution.b);
			Point2D.Double cutXY = VisualPoint.toXY(cutAB, xmin, ymin, xmax, ymax, this.getSize());
			int x = (int) cutXY.x;
			int y = (int) cutXY.y;

			g.fillOval(x - 4, y - 4, 8, 8);
		}

		if (corner1 != null && corner2 != null) {
			g.setColor(Color.YELLOW);
			drawZoomRectangle(g);
		}
		
		g.setColor(Color.gray.brighter()); //draw vertical lines to distinguish intervals
		
		for (int i = Math.max(1, h.minband); i <= h.maxband; i++) {
			int x0 = (int) VisualPoint.aToX(h.borders[i], xmin, xmax, this.getSize());
			g.drawLine(x0, 0, x0, this.getHeight());
		}  
		g.setColor(Color.black);
		if (h.trapeze != null && h.trapeze.bounded) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(2.5f));
			int x1 = (int) VisualPoint.aToX(h.trapeze.left, xmin, xmax, this.getSize());
			int x2 = (int) VisualPoint.aToX(h.trapeze.right, xmin, xmax, this.getSize());
			int ytl = (int) VisualPoint.bToY(h.trapeze.topleft, ymin, ymax, this.getSize());
			int ybl = (int) VisualPoint.bToY(h.trapeze.botleft, ymin, ymax, this.getSize());
			int ybr = (int) VisualPoint.bToY(h.trapeze.botright, ymin, ymax, this.getSize());
			int ytr = (int) VisualPoint.bToY(h.trapeze.topright, ymin, ymax, this.getSize());
			g2d.drawLine(x1,ytl,x1,ybl);
			g2d.drawLine(x1,ybl,x2,ybr);
			g2d.drawLine(x2,ybr,x2,ytr);
			g2d.drawLine(x2,ytr,x1,ytl);
			g2d.setStroke(new BasicStroke());
		}
		if (h.trapeze != null && !h.trapeze.bounded) {
			if (h.trapeze.openleft){
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2.5f));
				int x2 = (int) VisualPoint.aToX(h.trapeze.right, xmin, xmax, this.getSize());
				int ybr = (int) VisualPoint.bToY(h.trapeze.botright, ymin, ymax, this.getSize());
				int ytr = (int) VisualPoint.bToY(h.trapeze.topright, ymin, ymax, this.getSize());
				double dx1 = VisualPoint.xToA(0, xmin, xmax, this.getSize());
				int x1 = (int) VisualPoint.aToX(dx1, xmin, xmax, this.getSize());
				int ytl = (int) VisualPoint.bToY(h.trapeze.topright+(dx1-h.trapeze.right)*h.trapeze.topslope, ymin, ymax, this.getSize());
				int ybl = (int) VisualPoint.bToY(h.trapeze.botright+(dx1-h.trapeze.right)*h.trapeze.botslope, ymin, ymax, this.getSize());
				
				g2d.drawLine(x1,ybl,x2,ybr);
				g2d.drawLine(x2,ybr,x2,ytr);
				g2d.drawLine(x2,ytr,x1,ytl);
				g2d.setStroke(new BasicStroke());
			}
			else{
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2.5f));
				int x1 = (int) VisualPoint.aToX(h.trapeze.left, xmin, xmax, this.getSize());
				int ybl = (int) VisualPoint.bToY(h.trapeze.botleft, ymin, ymax, this.getSize());
				int ytl = (int) VisualPoint.bToY(h.trapeze.topleft, ymin, ymax, this.getSize());
				double dx2 = VisualPoint.xToA(this.getSize().width, xmin, xmax, this.getSize());
				int x2 = (int) VisualPoint.aToX(dx2, xmin, xmax, this.getSize());
				int ytr = (int) VisualPoint.bToY(h.trapeze.topleft+(dx2-h.trapeze.left)*h.trapeze.topslope, ymin, ymax, this.getSize());
				int ybr = (int) VisualPoint.bToY(h.trapeze.botleft+(dx2-h.trapeze.left)*h.trapeze.botslope, ymin, ymax, this.getSize());
				g2d.drawLine(x1,ytl,x1,ybl);
				g2d.drawLine(x1,ybl,x2,ybr);
				
				g2d.drawLine(x2,ytr,x1,ytl);
				g2d.setStroke(new BasicStroke());
			}
		}

		g.setColor(Color.black);
		g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
	}

	private void drawZoomRectangle(Graphics g) {
		int x1 = (int) Math.min(corner1.x, corner2.x);
		int y1 = (int) Math.min(corner1.y, corner2.y);
		int x2 = (int) Math.max(corner1.x, corner2.x);
		int y2 = (int) Math.max(corner1.y, corner2.y);
		int dx = x2 - x1;
		int dy = y2 - y1;
		g.drawRect(x1, y1, dx, dy);
	}

	public void followTrapeze(){
		if (h.trapeze == null) return;
		if (!h.trapeze.bounded) return; //TODO add this feature
		double w = h.trapeze.right - h.trapeze.left;
		setMinAndMax(h.trapeze.left-w, ymin, h.trapeze.right+w, ymax);
		this.repaint();
	}
	
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
		highlightedPoint = null;
		
		for (VisualPoint v : visualPoints) {
			v.highlighted = false;
		}

		for (VisualPoint v : visualPoints) {
			if (v.containsCursorLine(e.getX(), e.getY(), xmin, xmax, ymin, ymax, this.getSize())) {
				v.highlighted = true;
				highlightedPoint = v;
				pointPanel.setVisualPoints(visualPoints);
				break;
			}
		}
		this.repaint();
		pointPanel.repaint();
	}

	public boolean isShowCrossings() {
		return showCrossings;
	}

	public void setShowCrossings(boolean showCrossings) {
		this.showCrossings = showCrossings;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double zoom = zoomFactor + e.getPreciseWheelRotation();
		this.setZoomFactor(zoom);
		this.repaint();
	}

	private void setZoomFactor(double zoomFactor) {
		this.zoomFactor = zoomFactor;
		System.out.println(zoomFactor);
		if (zoomFactor > 0) {
			xmin = (int) (zoomFactor * xmin);
			xmax = (int) (zoomFactor * xmax);
			ymin = (int) (zoomFactor * ymin);
			ymax = (int) (zoomFactor * ymax);
		} else if (zoomFactor < 0) {
			double absZoom = Math.abs(zoomFactor);
			xmin = (int) (1 / absZoom * xmin);
			xmax = (int) (1 / absZoom * xmax);
			ymin = (int) (1 / absZoom * ymin);
			ymax = (int) (1 / absZoom * ymax);
		}
		this.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton()==MouseEvent.BUTTON3 && pointPanel.addingAllowed && highlightedPoint != null) {
			h.removeLine(highlightedPoint.getMyPoint());
			visualPoints.remove(highlightedPoint);
			pointPanel.refreshAll();
		}

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			corner1 = new Point2D.Double(e.getX(), e.getY());
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
			corner2 = new Point2D.Double(e.getX(), e.getY());
			this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			corner2 = new Point2D.Double(e.getX(), e.getY());

			if (corner1.equals(corner2)) {
				return;
			}

			// set new xmin, xmax, ymin, ymax
			int x1 = (int) Math.min(corner1.x, corner2.x);
			int y1 = (int) Math.min(corner1.y, corner2.y);
			int x2 = (int) Math.max(corner1.x, corner2.x);
			int y2 = (int) Math.max(corner1.y, corner2.y);

			double aMin = VisualPoint.xToA(x1, xmin, xmax, this.getSize());
			double aMax = VisualPoint.xToA(x2, xmin, xmax, this.getSize());
			double bMax = VisualPoint.yToB(y1, ymin, ymax, this.getSize());
			double bMin = VisualPoint.yToB(y2, ymin, ymax, this.getSize());

			xmin = aMin;
			xmax = aMax;
			ymin = bMin;
			ymax = bMax;
			zoomFactor = 0;

			corner1 = null;
			corner2 = null;
			this.repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void setMinAndMax(double xmin, double ymin, double xmax, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.zoomFactor = 0;
	}

}
