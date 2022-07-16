package hamSanApp;

/**
 * This class represents a point/line (both) and has some helper functions.
 * <p>
 * The duality transform is defined by:
 * <ul>
 * <li>Point <i>(p<sub>x</sub>, p<sub>y</sub>)</i> --> <i>y=p<sub>x</sub>a −
 * p<sub>y</sub></i></li>
 * <li>Line <i>y = mx + c</i> --> <i>(m, -c)</i></li>
 * </ul>
 * The primal plane (how the points/lines are represented) of this class is the
 * lines.
 * 
 * @author fabian
 *
 */
public class PointLineDual {

	public double m; // first variable
	public double b; // second variable
	public final int i; // index
	static int index = 0; // so that each point has m unique index.

	public static PointLineDual fromPoint(double x, double y) {
		return new PointLineDual(x, -y);
	}
	
	public static PointLineDual fromLine(double m, double c) {
		return new PointLineDual(m, c);
	}

	/**
	 * constructor from line ax+b.
	 *
	 * @param m first variable
	 * @param b second variable
	 */
	PointLineDual(double a, double b) {
		this.m = a;
		this.b = b;
		i = PointLineDual.index;
		index++;
	}

	/**
	 * This method is called when you call e.g. println(any point).
	 */
	@Override
	public String toString() {
		return "m: " + this.m + " b: " + this.b + " i: " + this.i;
	}

	/**
	 * to output as a dot (not important, only for debugging purposes)
	 */
	public void repr_point() {
		System.out.println("point: (" + m + ", " + -b + ")");
	}

	/**
	 * to output as a straight line (not important, only for debugging purposes)
	 */
	public void repr_line() {
		System.out.println("line: y = " + m + "x + " + b);
	}

	/**
	 * Evaluates the straight line at m point.
	 *
	 * @param x evaluation point
	 * @return y value at x
	 */
	public double eval(double x) {
		return m * x + b;
	}

	/**
	 * Intersects the straight with another straight. NO STRAIGHTS WITH EQUAL CUT
	 * SLOPE!
	 *
	 * @param other The other one right now
	 * @return The x-coordinate of the cut
	 */
	public double cross(PointLineDual other) {
		if (m == other.m) {
			return 0;
		}
		return (other.b - b) / (m - other.m);
	}

	/**
	 * Returns whether lines i and j intersect above line k or below. Is not fully
	 * implemented! (I think we need it too Not)
	 *
	 * @param i first line of intersection
	 * @param j second line of intersection
	 * @param k comparison line
	 * @return 1 if above, -1 if below
	 */
	public static int op1naive(PointLineDual i, PointLineDual j, PointLineDual k) {
		// calculate the crossing point of i and j:
		if (i.m != j.m) {
			double x = (i.b - j.b) / (i.m - j.m);
			double y = i.m * x + i.b;
			double diff = y - (k.m * x + k.b);
			if (diff > 0) {
				return 1;
			}
			if (diff < 0) {
				return -1;
			}
			// -> handle
			return 0;
		} else {
			// they don't cross
			// -> handle
			return -2;
		}
	}

	/**
	 * Computes whether i and j intersect to the left of k and l or not. i,j,k and l
	 * must differ!
	 *
	 * @return -1 if ij intersect to the left of kl, 1 otherwise. (returns an even
	 *         inverse result when comparing crossings at infinity).
	 * @throws really shouldn't, only if you screw it up
	 */
	/*
	 * Problem: What if i and j are parallel, and k and l are parallel, but i and l
	 * are have different slopes then what order do we want at infinity?
	 */
	public static int op2naive(PointLineDual i, PointLineDual j, PointLineDual k, PointLineDual l) throws Exception {
		// if ij crosses left of kl, return -1, if right return +1
		if ((i.equals(k) && j.equals(l)) || (i.equals(l) && j.equals(k))) {
			return 0;
		}
		int smallindex = Math.min(Math.min(Math.min(i.i, j.i), k.i), l.i);
		if (i.equals(j) || k.equals(l)) {
			throw new Exception("op2 was called with stupid arguments");
		}
		double diff1 = i.m - j.m;
		double diff2 = k.m - l.m;
		if (diff1 != 0 && diff2 != 0) { // have no crossings at infinity
			double x1 = i.cross(j);
			double x2 = k.cross(l);
			if (x1 < x2) {
				return 1;
			} else if (x1 > x2) {
				return -1;
			} else { // Intersections are on top of each other
				// find the smallest index of the four
				// TODO: Possibly test whether treatment for only 3 different straight lines as
				// input
				// fits
				// the smallest index occurs only once
				int s = (int) Math.signum(x1);
				if (((i.i < j.i) && (i.i < k.i) && (i.i < l.i)) || (smallindex == i.i) && (i.i != k.i) && (i.i != l.i)) {

					if (diff1 > 0) {
						return -1 * s;
					} else {
						return 1 * s;
					}
				}
				if (((j.i < i.i) && (j.i < k.i) && (j.i < l.i)) || (smallindex == j.i) && (j.i != k.i) && (j.i != l.i)) {
					if (diff1 < 0) {
						return -1 * s;
					} else {
						return 1 * s;
					}
				}
				if (((k.i < i.i) && (k.i < j.i) && (k.i < l.i)) || (smallindex == k.i) && (k.i != i.i) && (k.i != j.i)) {
					if (diff2 < 0) {
						return -1 * s;
					} else {
						return 1 * s;
					}
				}
				if (((l.i < i.i) && (l.i < j.i) && (l.i < k.i)) || (smallindex == l.i) && (l.i != i.i) && (l.i != j.i)) {
					if (diff2 > 0) {
						return -1 * s;
					} else {
						return 1 * s;
					}
				} // Case where only three lines intersect and the smallest index occurs twice
				if (smallindex == i.i) {
					return op2naive(j, i, k, l);
				}
				if (smallindex == j.i && (j.i != k.i)) {
					return op2naive(i, j, l, k);
				}
				if ((smallindex == j.i) && (j.i == k.i)) {
					if ((diff1 < 0) && (diff2 < 0) || ((diff1 < 0) && (l.m - i.m < 0))) {
						return -1 * s;
					} else {
						return 1 * s;
					}
				}
				throw new Exception("no smallest index found, this shouldn't happen. :(, x values were " + x1 + " and " + x2
						+ " and our four lines were " + i + " " + j + " " + k + " " + l);
			}
		} // have crossings at infinity
		else {
			if (diff1 != 0) {
				if (k.i < l.i) {
					if (k.b > l.b) {
						return 1;
					} else {
						return -1;
					}
				} else if (k.b > l.b) {
					return -1;
				} else {
					return 1;
				}
			}
			if (diff2 != 0) {
				if (i.i < j.i) {
					if (i.b > j.b) {
						return -1;
					} else {
						return 1;
					}
				} else if (i.b > j.b) {
					return 1;
				} else {
					return -1;
				}
			}
			// sanity:
			if (diff1 == 0 && diff2 == 0) {
				if ((i.i < j.i && i.i < k.i && i.i < l.i) || (j.i < i.i && j.i < k.i && j.i < l.i)) {
					if (k.i < l.i) {
						if (k.b < l.b) {
							return 1;
						} else {
							return -1;
						}
					} else {
						if (k.b < l.b) {
							return -1;
						} else {
							return 1;
						}
					}
				} else if (i.i < j.i) {
					if (i.b < j.b) {
						return -1;
					} else {
						return 1;
					}
				} else {
					if (i.b < j.b) {
						return 1;
					} else {
						return -1;
					}
				}
			}

			throw new Exception("uh, something went wrong comparing");
		}

	}

	/**
	 * Third operation, we probably don't need it
	 */
	public static int op3naive(PointLineDual i, PointLineDual j, PointLineDual k, PointLineDual l, PointLineDual m) {
		// sanity: make sure i,j,k,l pairwise distinct,
		// even need to do? make sure we need this.
		return 0;
	}
}