package hamSanApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the intersection of two straight lines and is principal used for
 * sorting.
 *
 * @author fabian
 *
 */
class Crossing implements Comparable<Crossing> {
	
	private static final Logger logger = LoggerFactory.getLogger(Crossing.class);

	PointLineDual line1; // line with smaller index
	PointLineDual line2; // Larger index line

	/**
	 * constructor
	 */
	public Crossing(PointLineDual crossline1, PointLineDual crossline2) {
		if (crossline1.i <= crossline2.i) {
			line1 = crossline1;
			line2 = crossline2;
		} else {
			line1 = crossline2;
			line2 = crossline1;
		}
	}

	/**
	 * this method is called when you print e.g. println(any crossing) executes.
	 */
	@Override
	public String toString() {
		String r = "";
		if (atInf() && atNegInf()) {
			r += "crossing at -inf";
		} else if (atInf() && !atNegInf()) {
			r += "crossing at +inf";
		} else {
			r += "crossing at " + crAt();
		}
		return r;
	}

	/**
	 * comparison function. works as expected in the interface
	 */
	@Override
	public int compareTo(Crossing other) { // TODO: test this a bit
		// returns -1 if this is more left (than other), 0 if this is other, 1 if this
		// is more right (than other)
		if (other == null) {
			throw new NullPointerException("tried to compare to null. whoops.");
		}
		if (this.equals(other)) {
			return (0);
		}
		try {
			int smallindex = Math.min(Math.min(Math.min(this.line1.i, this.line2.i), other.line1.i), other.line2.i);
			if (this.atInf() && other.atInf()) {
				if (this.atNegInf() && !other.atNegInf())// this negativ, other positiv
				{
					return -1;
				} else if (!this.atNegInf() && (other.atNegInf()))// this positiv, other negativ
				{
					return 1;
				} else if (this.atNegInf() && other.atNegInf()) {// this neg, oth neg //bei jedem Kreuzungspaar ist Gerade mit kleinerem
																	// Index weiter oben
					// Theorie: Kreuzungspaar mit kleinerem Index ist weiter rechts
					if (smallindex == this.line1.i) {
						if (smallindex == other.line1.i) {// Sonderfall: 3 Parallele Geraden
							if (this.line2.b > other.line2.b) {
								return 1;
							} else {
								return -1;
							}
						} else {
							return 1;
						}

					} else {
						return -1;
					}
				} else if (!this.atNegInf() && !other.atNegInf()) {// this positiv, other positiv
					if ((smallindex == this.line1.i) || (smallindex == this.line2.i)) {
						return -1;
					} else {
						return 1;
					}

				}
			} else if (this.atInf() && this.atNegInf()) {
				return -1;
			} else if (this.atInf() && !this.atNegInf()) {
				return 1;
			} else if (other.atInf() && other.atNegInf()) {
				return 1;
			} else if (other.atInf() && !other.atNegInf()) {
				return -1;
			}
			// case: all intersections are not at infinity and intersection points are not
			// on top of each other
			else if (this.crAt() < other.crAt()) {
				return -1;
			} else if (this.crAt() > other.crAt()) {
				return 1;
			}
			// case: all intersections are not at infinity and intersection points are on
			// top of each other
			else if (this.crAt() == other.crAt()) {
				// case: Even with the smallest index does not occur in both intersections
				if (this.line1.i != other.line1.i) {
					// Case: Intersection is to the right of zero or on the y-axis
					if (this.crAt() >= 0) {
						if (this.line1.i < other.line1.i) { // Smallest index pair is this for crossing pair
							if (this.line1.m - this.line1.m > 0) { // Even with the smallest index has m larger slope
								// Intersection this moves to the left
								return -1;
							} else {
								return 1;
							}
						} else { // smallest index is other for crossing pair
							if (other.line1.m - other.line1.m > 0) { // Even with the smallest index has m larger slope
								// intersection other moves to the left
								return 1;
							} else {
								return -1;
							}
						}

					} else { // Crosspoint is in the negative range
						if (this.line1.i < other.line1.i) { // Smallest index pair is this for crossing pair
							if (this.line1.m - this.line1.m > 0) { // Even with the smallest index has m larger slope
								// Intersection this moves to the right
								return 1;
							} else {
								return -1;
							}
						} else { // smallest index is other for crossing pair
							if (other.line1.m - other.line1.m > 0) { // Even with the smallest index has m larger slope
								// intersection other moves to the right
								return -1;
							} else {
								return 1;
							}
						}

					}
				}
				// case: Even with the smallest index occurs in both intersections
				// the two intersections are formed by 3 straight lines that intersect at one
				// point
				else {
					// Case: Intersection is to the right of zero or on the y-axis
					if (this.crAt() >= 0) {
						// Even with the smallest index has the greatest slope
						if (this.line1.m - this.line2.m > 0 && this.line1.m - other.line2.m > 0) {
							if (this.line2.m - other.line2.m > 0) { // Compare the slopes of the other two lines that don't match the
								// have smallest index
								return -1;
							} else {
								return 1;
							}

						} // Straight line with the smallest index lies between the other two straight
							// lines(has middle
							// Pitch)
							// Just other.line2 is above this.line1
						else if (this.line1.m - this.line2.m > 0 && this.line1.m - other.line2.m < 0) {
							return -1;

						} // Just other.line2 is below this.line1
						else if (this.line1.m - this.line2.m < 0 && this.line1.m - other.line2.m > 0) {
							return 1;
						}
						// Even with the smallest index has the smallest slope
						else if (this.line1.m - this.line2.m < 0 && this.line1.m - other.line2.m < 0) {
							if (this.line2.m - other.line2.m > 0) { // Compare the slopes of the other two lines that don't match the
								// have smallest index
								return -1;
							} else {
								return 1;
							}

						} else {
							logger.error("Crossing is positive");
						}
					} else {
						// Case: Intersection is in negative territory

						// Even with the smallest index has the greatest slope
						if (this.line1.m - this.line2.m > 0 && this.line1.m - other.line2.m > 0) {
							if (this.line2.m - other.line2.m > 0) { // Compare the slopes of the other two lines that don't match the
								// have smallest index
								return 1;
							} else {
								return -1;
							}

						} // Straight line with the smallest index lies between the other two straight
							// lines(has middle
							// Pitch)
							// Just other.line2 is above this.line1
						else if (this.line1.m - this.line2.m > 0 && this.line1.m - other.line2.m < 0) {
							return 1;

						} // Just other.line2 is below this.line1
						else if (this.line1.m - this.line2.m < 0 && this.line1.m - other.line2.m > 0) {
							return -1;
						}
						// Even with the smallest index has the smallest slope
						else if (this.line1.m - this.line2.m < 0 && this.line1.m - other.line2.m < 0) {
							if (this.line2.m - other.line2.m > 0) { // Compare the slopes of the other two lines that don't match the
								// have smallest index
								return 1;
							} else {
								return -1;
							}

						} else {
							logger.error("Crossing is negative");
						}

					} // end of Else case: negative crossing

				} // end crossing are formed by 3 straight lines
			} // End : Intersections are on top of each other

			// return (-1)* PointLineDual.op2naive(m, b, other.a, other.b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * function to find out if the intersection is at +- infinity located
	 *
	 * @return true if yes
	 */
	public boolean atInf() {
		return line1.m == line2.m;
	}

	/**
	 * function to find out whether the point of intersection at infinity is at - or
	 * at + is infinity
	 *
	 * @return true if at -unendl
	 */
	public boolean atNegInf() {
		if (!atInf()) {
			return false;
		}
		if (line1.i < line2.i) {
			if (line1.b < line2.b) {
				return false;
			} else {
				return true;
			}
		} else if (line1.b < line2.b) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Utility function to quickly access the x-value of the intersection/crossing.
	 *
	 * @return the X-value if there is one, otherwise 0.
	 */
	public double crAt() {
		if (!atInf()) {
			return line1.cross(line2);
		} else {
			return 0;
		}
	}
}
