package hamSanApp;

/**
 * This class represents a point/line (both) and has some helper functions
 *
 * @author fabian
 *
 */
public class Point {

	public double a; // first variable
	public double b; // second variable
	public final int i; // index
	static int index = 0; // so that each point has a unique index.

	/**
	 * constructor
	 *
	 * @param x first variable
	 * @param y second variable
	 */
	Point(double x, double y) {
		a = x;
		b = y;
		i = Point.index;
		index++;
	}

	/**
	 * This method is called when you call e.g. println(any point).
	 */
	@Override
	public String toString() {
		return "a: " + this.a + " b: " + this.b + " i: " + this.i;
	}

	/**
	 * to output as a dot (not important, only for debugging purposes)
	 */
	public void repr_point() {
		System.out.println("point at " + a + " " + b);
	}

	/**
	 * to output as a straight line (not important, only for debugging purposes)
	 */
	public void repr_line() {
		System.out.println("line: y= " + a + "x + " + b);
	}

	/**
	 * evaluates the straight line at a point.
	 *
	 * @param x evaluation point
	 * @return calculated y value
	 */
	public double eval(double x) {
		return a * x + b;
	}

	/**
	 * Intersects the straight with another straight. NO STRAIGHTS WITH EQUAL CUT
	 * SLOPE!
	 *
	 * @param other The other one right now
	 * @return The x-coordinate of the cut
	 */
	public double cross(Point other) {
		if (a == other.a) {
			return 0;
		}
		return (other.b - b) / (a - other.a);
	}

	/**
	 * calculates a determinant. not important.
	 */
	private static double det3(double a11, double a12, double a13, double a21, double a22, double a23, double a31, double a32, double a33) {
		return a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a11 * a23 * a32 - a12 * a21 * a33 - a13 * a22 * a31;
	}

	/**
	 * calculates a determinant. not important.
	 */
	private static double det2(double a11, double a12, double a21, double a22) {
		return a11 * a22 - a12 * a21;
	}

	/**
	 * non-working version of operation 1. delete me.
	 */
	private static int op1(Point i, Point j, Point k) {
		double Delta1;
		if (i.i < j.i) {
			Delta1 = det3(i.a, i.b, 1, j.a, j.b, 1, k.a, k.b, 1);
		} else {
			Delta1 = det3(j.a, j.b, 1, i.a, i.b, 1, k.a, k.b, 1);
		}

		System.out.println("determinant evaluated to " + Delta1);
		if (Delta1 > 0) {
			return 1;
		}
		if (Delta1 < 0) {
			return -1;
		}
		return 0;
	}

	/**
	 * returns whether lines i and j intersect above line k or below is not fully
	 * implemented! (I think we need it too Not)
	 *
	 * @param i first line of intersection
	 * @param j second line of intersection
	 * @param k comparison line
	 * @return 1 if above, -1 if below
	 */
	public static int op1naive(Point i, Point j, Point k) {
		// calculate the crossing point of i and j:
		if (i.a != j.a) {
			double x = (i.b - j.b) / (i.a - j.a);
			double y = i.a * x + i.b;
			double diff = y - (k.a * x + k.b);
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
	 * computes whether i and j intersect to the left of k and l or not. i,j,k and l
	 * must differ!
	 *
	 * @return -1 if ij intersect to the left of kl, 1 otherwise
	 * @throws really shouldn't, only if you screw it up
	 */
	// returns an even inverse result when comparing crossings at infinity.
	// Problem: What if i and j are parallel, and k and l are parallel, but i and l
	// are
	// have different slopes
	// then what order do we want at infinity?
	public static int op2naive(Point i, Point j, Point k, Point l) throws Exception {
		// if ij crosses left of kl, return -1, if right return +1
		if ((i.equals(k) && j.equals(l)) || (i.equals(l) && j.equals(k))) {
			return 0;
		}
		int smallindex = Math.min(Math.min(Math.min(i.i, j.i), k.i), l.i);
		if (i.equals(j) || k.equals(l)) {
			throw new Exception("op2 was called with stupid arguments");
		}
		double diff1 = i.a - j.a;
		double diff2 = k.a - l.a;
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
				} // Fall, dass sich nur drei Geraden schneiden und kleinster Index doppelt
					// vorkommt
				if (smallindex == i.i) {
					return op2naive(j, i, k, l);
				}
				if (smallindex == j.i && (j.i != k.i)) {
					return op2naive(i, j, l, k);
				}
				if ((smallindex == j.i) && (j.i == k.i)) {
					if ((diff1 < 0) && (diff2 < 0) || ((diff1 < 0) && (l.a - i.a < 0))) {
						return -1 * s;
					} else {
						return 1 * s;
					}
				}
				/*
				 * 
				 * }//Fall, dass sich nur drei Geraden schneiden und kleinster Index doppelt
				 * vorkommt if (smallindex==i.i) {return op2naive(j,i,k,l);} if
				 * (smallindex==j.i&& (j.i!=k.i)) {return op2naive(i,j,l,k);} if (
				 * (smallindex==j.i)&& (j.i==k.i) ){ if ((diff1<0)&&(diff2<0)
				 * ||((diff1<0)&&(l.a-i.a<0)) ){return -1*s;} else {return 1*s;} }
				 */

				throw new Exception("no smallest index found, this shouldn't happen. :(, x values were " + x1 + " and " + x2
						+ " and our four lines were " + i + " " + j + " " + k + " " + l);
			}
		} // haben Kreuzungen im Unendlichen
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
	public static int op3naive(Point i, Point j, Point k, Point l, Point m) {
		// sanity: make sure i,j,k,l pairwise distinct,
		// even need to do? make sure we need this.
		return 0;
	}
}