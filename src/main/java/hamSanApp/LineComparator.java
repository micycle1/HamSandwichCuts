package hamSanApp;

import java.util.Comparator;

/**
 * Compares two lines by their value in coordinate x.
 * <p>
 * Lines are sorted such that: The greater the value of the straight line in
 * coordinate x, the smaller the straight line in straight line order. In the
 * case of equality: if value is positive, then m line with m smaller index is
 * above; if value is negative, the straight line with m smaller index is below;
 * if value = 0, the line with the smaller index is above the other.
 * 
 * @author annette
 *
 */
class LineComparator implements Comparator<PointLineDual> {

	/**
	 * @param x pos to evalulate
	 */
	public LineComparator(double x) {
		super();
		this.x = x;
	}

	private double x;

	// (y0 < y1) iff y0 below y1 iff return 1
	@Override
	public int compare(PointLineDual arg0, PointLineDual arg1) {
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