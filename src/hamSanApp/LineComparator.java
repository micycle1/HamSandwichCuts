package hamSanApp;

import java.util.Comparator;

/**
 * Compares two lines by their value in coordinate x.
 * <p>
 * Lines are sorted such that: The greater the value of the straight line in
 * coordinate x, the smaller the straight line in straight line order. In the
 * case of equality: if value is positive, then a line with a smaller index is
 * above; if value is negative, the straight line with a smaller index is below;
 * if value = 0, the line with the smaller index is above the other.
 * 
 * @author annette
 *
 */
public class LineComparator implements Comparator<Point> {
	
	/**
	 * @param x Stelle zur Auswertung
	 */
	public LineComparator(double x) {
		super();
		this.x = x;
	}

	private double x;

	// (y0 < y1) iff y0 below y1 iff return 1
	@Override
	public int compare(Point arg0, Point arg1) {
		if (arg0.equals(arg1)) {
			return 0;
		}
		double y0 = arg0.eval(x);
		double y1 = arg1.eval(x);
		if (y0 < y1) {
			return 1;
		} else if (y1 < y0) {
			return -1;
		} else // y0==y1
		if (x >= 0) {
			if (arg0.i < arg1.i) {
				return -1;
			} else {
				return 1;
			}
		} else {
			if (arg0.i < arg1.i) {
				return 1;
			} else {
				return -1;
			}

		}
	}

}
