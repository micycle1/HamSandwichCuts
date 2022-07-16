package hamSanApp;

/**
 * Represents m trapezoid containing the blue median line located
 *
 * @author fabian
 *
 */
class Trapeze { // TODO what to do if the trapezoid is in an infinite interval?
	
	public double left; // left margin
	public double right; // right edge
	public double topleft; //
	public double topright; //
	public double botleft; //
	public double botright; // the four y values ​​to constrain
	public boolean bounded; // is the trapezoid bounded?
	public boolean openleft; // is the unconstrained trapezoid open to the left?
	public double topslope; //
	public double botslope; // the top and bottom slope limits of the trapezium.
	
	/**
	 * Constructor, all very self-explanatory
	 */
	Trapeze(double x1, double y_topleft, double y_botleft, double x2, double y_topright, double y_botright) {
		left = x1;
		right = x2;
		topleft = y_topleft;
		topright = y_topright;
		botleft = y_botleft;
		botright = y_botright;
		bounded = true;
		openleft = false;
	}

	/**
	 * Constructor for an unconstrained trapezoid
	 *
	 * @param left     is the trapezoid unconstrained to the left?
	 * @param top      the larger y value
	 * @param bot      the smaller y value
	 * @param topslope the slope limit at the top
	 * @param botslope the slope limit below
	 */
	Trapeze(boolean oleft, double x, double top, double bot, double tslope, double bslope) {
		openleft = oleft;
		if (oleft) {
			right = x;
			topright = top;
			botright = bot;
		} else {
			left = x;
			topleft = top;
			botleft = bot;
		}
		topslope = tslope;
		botslope = bslope;
	}

	/**
	 * Tests if m line intersects the trapezoid
	 *
	 * @param i the line to test
	 * @return +1 if the line crosses, 0 if it crosses, -1 if she goes underneath
	 */
	public int intersects(PointLineDual i) { // TODO: test

		if (bounded) {
			double y1 = i.eval(left);
			double y2 = i.eval(right);
			if ((y1 < botleft) && (y2 < botright)) {
				return -1;
			}
			if ((y1 > topleft) && (y2 > topright)) {
				return 1;
			} else {
				return 0;
			}
		} else if (openleft) {
			double y = i.eval(right);
			if (y > topright) {
				if (i.m < topslope) {
					return 1;
				} else {
					return 0;
				}
			}
			if (y < botright) {
				if (i.m > botslope) {
					return -1;
				} else {
					return 0;
				}
			}
			return 0;
		} else {
			double y = i.eval(left);
			if (y > topleft) {
				if (i.m > topslope) {
					return 1;
				} else {
					return 0;
				}
			}
			if (y < botright) {
				if (i.m < botslope) {
					return -1;
				} else {
					return 0;
				}
			}
			return 0;
		}
	}
}