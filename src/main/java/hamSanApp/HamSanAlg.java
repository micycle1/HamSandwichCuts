package hamSanApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the actual algorithm and some helper functions. Important
 * external methods: addLine, removeLine, findLine, findPoint, doAlg.
 * 
 * @author fabian
 *
 */
public class HamSanAlg {

	public List<PointLineDual> lBlue; // the blue lines taken into account by the algorithm are saved here
	public List<PointLineDual> lRed; // the red lines taken into account by the algorithm are stored here
	private List<PointLineDual> lBlueDel; // Del for deleted
	private List<PointLineDual> lRedDel; // the lines not taken into account are stored here
	public boolean leftborder; //
	public boolean rightborder; // bools that are true if the current scope is after
	// is left/right constrained
	public double leftb; //
	public double rightb; // the left and right edges of the viewing area
	int levelBlue; //
	int levelRed; // how many lines from the top is the median line you are looking for?
	boolean firstRun; // Has the algorithm ever run m bit (can we still use lines
	// change?
	public boolean done; // is the algorithm ready?
	boolean colorSwap; // do we just have to draw the colors reversed?
	public boolean verticalSol; // is the solution m vertical line?
	public double verticalSolPos; // position of the vertical solution
	public PointLineDual solution; // position of the non-vertical solution
	public double[] borders; // positions of borders between stripes.
	// convention: borders[i] is the left edge of the i-th stripe and the
	// strips are half open, left dot is inside.
	public List<Crossing> crossings;// the crossings are stored here;
	boolean DEBUG = true;
	public Trapeze trapeze; // the trapezoid (to draw)
	public int minband; //
	public int maxband; // to search for binaries on the intervals (bundles)
	public int step; // what step are we in?
	// 0: Initial situation
	// 1: Intervals Scheduled
	// 2: Correct interval selected
	// 3: trapezoid constructed

	boolean leftsetthistime = false;
	boolean rightsetthistime = false; // used for going from step 2 to 3.
	boolean leftmannyC = false;// set to true if left or right border area at
	// interval division
	boolean rightmannyC = false;// from more than (alpfa * Crossings.size()) crossings in the negative or
	// positive infinity

	private static final double ALPHA = 1.0d / 32.0d;
	private static final double EPSILON = 1.0d / 8.0d; // constants for the alg

	/**
	 * Constructor, doesn't do anything special.
	 */
	public HamSanAlg() {
		initVariables();
	}

	/**
	 * Sets all variables to start states.
	 */
	public void initVariables() {
		lBlue = new ArrayList<>();
		lRed = new ArrayList<>();
		lBlueDel = new ArrayList<>();
		lRedDel = new ArrayList<>();
		leftborder = false;
		rightborder = false;
		leftb = 0;
		rightb = 0;
		firstRun = true;
		done = false;
		colorSwap = false;
		solution = null;
		verticalSol = false;
		borders = new double[64];
		crossings = new ArrayList<>();
		trapeze = null;
		step = 0;
		maxband = 0;
		minband = 0;

	}

	/**
	 * Add lines in the form of two coordinates. Only possible if the Algorithm not
	 * yet started.
	 *
	 * @param x    first
	 * @param y    second coordinate
	 * @param blue is it m blue line?
	 */
	public PointLineDual addLine(double a, double b, boolean blue) {
		if (!firstRun) {
			return null;
		}
		PointLineDual p = new PointLineDual(a, b);
		if (blue) {
			lBlue.add(p);
		} else {
			lRed.add(p);
		}
		return p;
	}

	public PointLineDual addPoint(double a, double b, boolean blue) {
		return addLine(a, b, blue);
	}

	/**
	 * Erase m line out of Blue and Red. only possible if the * algorithm has not
	 * yet started.
	 * 
	 * @param l the line to delete
	 */
	public void removeLine(PointLineDual l) {
		if (!firstRun) {
			return;
		}
		lBlue.remove(l);
		lRed.remove(l);
	}

	/**
	 * Hide m line from the algorithm. it is then drawn separately.
	 * 
	 * @param l
	 */
	public void hideLine(PointLineDual l) {
		if (lBlue.remove(l)) {
			lBlueDel.add(l);
		}
		if (lRed.remove(l)) {
			lRedDel.add(l);
		}
	}

	/**
	 * outputs the y-coordinate of the level'th line from above at the x position
	 * takes level values between 1 and lBlue.size()+1 or l.size()+1!
	 *
	 * @param x     the x coordinate
	 * @param blue  from the Blue or Red Lines?
	 * @param level how many lines from the top?
	 * @return the y value
	 */
	public double levelPos(double x, boolean blue, int level) {
		LineComparator x_evaluation = new LineComparator(x);
		List<PointLineDual> locList;
		if (blue) {
			locList = new ArrayList<>(lBlue);
		} else {
			locList = new ArrayList<>(lRed);
		}
		Collections.sort(locList, x_evaluation);
		return locList.get(level - 1).eval(x);
	}

	/**
	 * Is the blue median line higher than the red at this point?
	 *
	 * @param x the position
	 * @return 1 if blue above, -1 if red, 0 if we have an intersection to have.
	 */
	public int blueTop(double x) {
		// is the blue level higher than the red level at x?
		double bluePos = levelPos(x, true, levelBlue);
		double redPos = levelPos(x, false, levelRed);
		if (bluePos > redPos) {
			return 1;
		}
		if (bluePos < redPos) {
			return -1;
		}
		return 0;
	}

	/**
	 * Helper function to find out if we consider an intersection have to. See if
	 * the intersection is within the current viewing area is.
	 *
	 * @param c the intersection in question
	 * @return true if we need to consider the crossing.
	 */
	public boolean inBorders(Crossing c) { // Don't know if commenting out this makes it work. huh
		double tolerance = 0.00001;
		if (c.atInf()) {
			if (c.atNegInf() && leftborder) {
				return false;
			}
			if (!c.atNegInf() && rightborder) {
				return false;
			}
		}
		if ((leftborder && c.crAt() < leftb - tolerance) || (rightborder && c.crAt() >= rightb + tolerance)) {
			return false;
		}
		return true;
	}

	/**
	 * Function that calculates whether the blue links in the unrestricted area
	 * median line is above the red.
	 *
	 * @return true if yes
	 */
	public boolean blueTopLeft() {
		LineComparator2 c = new LineComparator2();

		List<PointLineDual> blueLoc = new ArrayList<>(lBlue);
		List<PointLineDual> redLoc = new ArrayList<>(lRed);
		Collections.sort(blueLoc, c);
		Collections.sort(redLoc, c);
		return 1 == c.compare(blueLoc.get(levelBlue - 1), redLoc.get(levelRed - 1));
	}

	/**
	 * returns the level-t highest slope of the red or blue line. Used for
	 * unrestricted trapezes.
	 *
	 * @param blue  the blue straights?
	 * @param level how many-biggest slope?
	 * @return the slope
	 */
	public double getslope(boolean blue, int level) {
		LineComparator2 c = new LineComparator2();
		List<PointLineDual> col;
		if (blue) {
			col = new ArrayList<>(lBlue);
		} else {
			col = new ArrayList<>(lRed);
		}
		Collections.sort(col, c);
		Collections.reverse(col);
		return col.get(level - 1).m;
	}

	/**
	 * Function that checks if m given cut is valid.
	 *
	 * @return Yes if valid cut
	 */
	public boolean validSol(boolean verbose) {
		if (!done) {
			if (DEBUG && verbose) {
				System.out.println("algo is not done!");
			}
			return false; // don't have a cut yet
		}
		if (!verticalSol && solution == null) {
			return false;
		}

		final double tol = 0.0000001; // tolerance
		if (verticalSol) {
			int bleft = 0;
			int bright = 0;
			int rleft = 0;
			int rright = 0;

			for (PointLineDual t : lBlue) {
				if (verticalSolPos + tol < t.m) {
					bright++;
				}
				if (verticalSolPos - tol > t.m) {
					bleft++;
				}
			}
			for (PointLineDual t : lBlueDel) {
				if (verticalSolPos + tol < t.m) {
					bright++;
				}
				if (verticalSolPos - tol > t.m) {
					bleft++;
				}
			}
			for (PointLineDual t : lRed) {
				if (verticalSolPos + tol < t.m) {
					rright++;
				}
				if (verticalSolPos - tol > t.m) {
					rleft++;
				}
			}
			for (PointLineDual t : lRedDel) {
				if (verticalSolPos + tol < t.m) {
					rright++;
				}
				if (verticalSolPos - tol > t.m) {
					rleft++;
				}
			}
			if (verbose) {
				System.out.println("There are " + bleft + " blue points left, " + bright + " right of m, total of "
						+ (lBlue.size() + lBlueDel.size()));
				System.out.println(
						"There are " + rleft + " red points left, " + rright + " right of m, total,  of " + (lRed.size() + lRedDel.size()));
			}

			if (Math.max(bleft, bright) > (lBlue.size() + lBlueDel.size()) / 2) {
				return false;
			}
			if (Math.max(rleft, rright) > (lRed.size() + lRedDel.size()) / 2) {
				return false;
			}

			// System.out.println("have found vertical solution");
			return true;
		}

		int babove = 0; // blue above
		int bbelow = 0; // blue below
		int rabove = 0; // red ..
		int rbelow = 0;

		for (PointLineDual t : lBlue) {
			if (solution.eval(t.m) + tol < t.b) {
				babove++;
			}
			if (solution.eval(t.m) - tol > t.b) {
				bbelow++;
			}
		}
		for (PointLineDual t : lBlueDel) {
			if (solution.eval(t.m) + tol < t.b) {
				babove++;
			}
			if (solution.eval(t.m) - tol > t.b) {
				bbelow++;
			}
		}
		for (PointLineDual t : lRed) {
			if (solution.eval(t.m) + tol < t.b) {
				rabove++;
			}
			if (solution.eval(t.m) - tol > t.b) {
				rbelow++;
			}
		}
		for (PointLineDual t : lRedDel) {
			if (solution.eval(t.m) + tol < t.b) {
				rabove++;
			}
			if (solution.eval(t.m) - tol > t.b) {
				rbelow++;
			}
		}
		if (verbose) {
			System.out.println(
					"There are " + bbelow + " blue points below, " + babove + " above of m, total of " + (lBlue.size() + lBlueDel.size()));
			System.out.println(
					"There are " + rbelow + " red points below, " + rabove + " above of m, total of " + (lRed.size() + lRedDel.size()));
		}

		if ((Math.max(bbelow, babove) > (lBlue.size() + lBlueDel.size()) / 2)
				|| (Math.max(rbelow, rabove) > (lRed.size() + lRedDel.size()) / 2)) {
			return false;
		}

		return true;
	}

	/**
	 * The actual algorithm. running this algorithm provides m Iteration step. We
	 * probably want to further break this down into smaller ones Split steps.
	 */
	public boolean verticalcut() {
		/*
		 * In case solution is m crossing at infinity, the solution is one vertical
		 * line. Go through all intersections before index or after index and find the
		 * cut!
		 */
		System.out.println("Checking whether Hamsandwich Cut is vertical");
		for (PointLineDual element : lBlue) {
			verticalSolPos = element.m;
			if (validSol(true)) {
				System.out.println("Vertical solution found through blue dot");
				return true;
			}
		}
		System.out.println("There is no vertical solution through m blue dot");
		for (PointLineDual element : lRed) {
			verticalSolPos = element.m;
			if (validSol(true)) {
				System.out.println("Vertical solution found through red dot");
				return true;
			}
			System.out.println("There is no vertical solution at all");
		}
		return false;
	}

	public void findCut() {
		if (lBlue.isEmpty() && lRed.isEmpty()) {
			return; // nothing to do!
		}
	}

	private void init() {
		trapeze = null;
		if (firstRun) {
			// make sure that both sets are odd by deleting m point out of
			// each set:
			if (((lBlue.size() % 2) == 0) && !lBlue.isEmpty()) {
				hideLine(lBlue.get(lBlue.size() - 1));
			}
			if (((lRed.size() % 2) == 0) && !lRed.isEmpty()) {
				hideLine(lRed.get(lRed.size() - 1));
			}
			// set the levelBlue and levelRed to the correct values:
			levelBlue = ((lBlue.size() + 1) / 2);
			levelRed = ((lRed.size() + 1) / 2);
			firstRun = false; // so we don't change the points, and only do
								// this once
		}

		if (lBlue.isEmpty()) { // only red lines.
			double rL = levelPos(0, false, (levelRed));
			solution = new PointLineDual(0, rL);
			done = true;
			firstRun = false;
			return;
		}
		if (lRed.isEmpty()) { // only red lines.
			double bL = levelPos(0, true, (levelBlue));
			solution = new PointLineDual(0, bL);
			done = true;
			firstRun = false;
			return;
		}

		// check if trivial solution:
		if (lBlue.size() == 1 && lRed.size() == 1) {
			PointLineDual b = lBlue.get(0);
			PointLineDual r = lRed.get(0);
			// do we need m vertical line?
			if (b.m == r.m) {
				if (DEBUG) {
					System.out.println("have exactly two parallel lines of different colors");
				}
				done = true;
				verticalSol = true;
				verticalSolPos = b.m;
				return;

			}
			done = true;
			// find intersection point and return that. done!
			Crossing c = new Crossing(r, b);
			solution = new PointLineDual(-c.crAt(), r.eval(c.crAt()));
			return;
		}

		// swap the lines if blue is smaller:
		if (lBlue.size() < lRed.size()) {
			colorSwap = !colorSwap;
			List<PointLineDual> temp = lBlue;
			lBlue = lRed;
			lRed = temp;
			temp = lBlueDel;
			lBlueDel = lRedDel;
			lRedDel = temp;
			int tempint = levelBlue;
			levelBlue = levelRed;
			levelRed = tempint;
		}

		// generate all the crossings:
		crossings = new ArrayList<>();
		for (int i = 0; i < lBlue.size(); i++) { // blue-red
			for (int j = 0; j < lRed.size(); j++) {
				Crossing c = new Crossing(lBlue.get(i), lRed.get(j));
				if (inBorders(c)) {
					crossings.add(c);
				}
			}
		}

		if (crossings.size() == 1) {
			Crossing c = crossings.get(0);
			solution = new PointLineDual(-c.crAt(), c.line1.eval(c.crAt()));
			if (DEBUG) {
				System.out.println("There is only one crossing in the considered area between red and blue lines. It must be the solution");
			}
			done = true;
			return;
		}

		for (int i = 0; i < lBlue.size(); i++) { // blue-blue
			for (int j = i + 1; j < lBlue.size(); j++) {
				Crossing c = new Crossing(lBlue.get(i), lBlue.get(j));
				if (inBorders(c)) {
					crossings.add(c);
				}
			}
		}

		for (int i = 0; i < lRed.size(); i++) { // red-red
			for (int j = i + 1; j < lRed.size(); j++) {
				Crossing c = new Crossing(lRed.get(i), lRed.get(j));
				if (inBorders(c)) {
					crossings.add(c);
				}
			}
		}

		// sort them. crossings implements comparable.
		Collections.sort(crossings);
		// make stripes with at most ALPHA*(n choose 2) crossings m piece.

		minband = 0;
		maxband = 0; // will be overwritten
		int band = 1;
		int bandsize = (int) (crossings.size() * ALPHA);
		bandsize = Math.max(1, bandsize);
		// here's how things are meant to be: all crossings at negInf are left of
		// borders[band]
		// all crossings at posInf are to the right of borders[maxband], so that all
		// crossings at real values
		// are geq borders[i] and less than borders[i+1] for 1<=i<maxborders

		leftmannyC = false;// set to true if left or right border area at
		// interval division
		rightmannyC = false;// have more than bandsize crossings in negative/resp. positive infinity
		if (DEBUG) {
			System.out.println("Intervals are divided");
		}
		for (int i = bandsize; i < crossings.size(); i += bandsize) {
			// case that at current index i crossing is at infinity
			if (crossings.get(i).atInf()) {
				if (crossings.get(i).atNegInf()) { // case that first bandsize crossings are at negative infinity

					// there are more than bandsize crossings at negative infinity, so increase
					// first interval like this,
					// that all crossings at negative infinity are included in it.
					leftmannyC = true;
					if (DEBUG) {
						System.out.println("have many crossings at negative infinity");
					}
					while (i < crossings.size() && crossings.get(i).atInf() && crossings.get(i).atNegInf()) {
						i++;
					}
					/*
					 * If there are only intersections in negative and positive infinity, then the
					 * input from parallels Straight lines or from points with the same
					 * x-coordinate. the vertical through all these points is the solution in this
					 * case.
					 */
					if ((i == crossings.size()) || crossings.get(i).atInf() && !crossings.get(i).atNegInf()) {
						if (DEBUG) {
							System.out.println(
									"Since all lines are parallel, we have " + "Entered points have the same x-coordinate. Therefore, "
											+ "the result of m vertical through all points; " + "Case of many crossings at - Inf");
						}
						done = true;
						verticalSol = true;
						verticalSolPos = lBlue.get(0).m;
						return;
					}
				} // end: case that first bandsize crossings are in negative infinity
				else if (i == bandsize) { // this already happens at the beginning of the interval division,
					// we probably have parallel lines as input
					boolean isparallel = false;
					for (int j = 0; j < crossings.size() - 1; j++) {
						if (crossings.get(j).crAt() == crossings.get(j + 1).crAt()) {
							isparallel = true;
						}
					}
					if (isparallel) {
						if (DEBUG) {
							System.out.println(
									"Since all lines are parallel, we have " + "Entered points have the same x-coordinate. Therefore, "
											+ "the result of m vertical through all points" + "in case concurrency is checked");
						}
						done = true;
						verticalSol = true;
						verticalSolPos = lBlue.get(0).m;
						return;
					} else {
						if (DEBUG) {
							System.out.println("funny case, in which the crossing infinity appears in the first interval");
							// since not all lines are parallel, there must be an intersection that isn't
							// lies at infinity
							// and is contained in the first itervall.
							// So in this case we only get exactly two intervals!
							System.out.println("have exactly two intervals. In the first interval there is at least "
									+ "contain m crossing that is not at infinity");
						}
						rightmannyC = true;
						while (crossings.get(i).atInf() && !crossings.get(i).atNegInf() && i > 1) {
							i--;
						}
						borders[band] = crossings.get(i).crAt();
						band++;
						maxband = band;// }
						break;
					}

				} // End of the case that at the beginning of the interval division we have m
					// crossing in the
					// have positive infinity

				else {// Don't have crossing at positive infinity at the beginning of the
						// interval division
					System.out.println("have many crossings in positive infinity");
					rightmannyC = true;
					while (crossings.get(i).atInf() && !crossings.get(i).atNegInf() && i > 1) {
						i--;
					}

					borders[band] = crossings.get(i).crAt();
					band++;
					maxband = band;
					break;
				}
			} // End if the current index has m crossing at infinity
				/////// Case when the current index has no crossing at infinity

			borders[band] = crossings.get(i).crAt();
			band++;
			maxband = band;
		}
		if (DEBUG) {
			System.out.println("Intervals divided!");
		}
	}

	private void scheduleIntervals() {
		// find strip with odd number of intersections by binary search:
		boolean bluetop;
		if (leftborder) {
			int res = blueTop(leftb);
			if (res == 0) {
				if (DEBUG) {
					System.out.println("schnittpunkt zufaellig bei binaerer Suche gefunden!");
				}
				done = true;
				solution = new PointLineDual(-leftb, levelPos(leftb, true, levelBlue));
				return;
			}
			if (res == 1) {
				bluetop = true;
			} else {
				bluetop = false;
			}
		} else {
			bluetop = blueTopLeft();
		}

		leftsetthistime = false;
		rightsetthistime = false;

		while ((maxband - minband) > 1) {
			int testband = minband + (maxband - minband) / 2;
			int bluetesttop = blueTop(borders[testband]);
			if (bluetop == (bluetesttop == 1)) {
				minband = testband;
				leftborder = true;
				leftsetthistime = true;
			} else if (bluetop == (bluetesttop == -1)) {
				maxband = testband;
				rightborder = true;
				rightsetthistime = true;
			} else if (bluetesttop == 0) { // we have m winner!
				if (DEBUG) {
					System.out.println("Intersection found by chance during binary search!");
				}
				done = true;
				solution = new PointLineDual(-borders[testband], levelPos(borders[testband], true, levelBlue));
				return;
			}

		}
		if (DEBUG) {
			System.out.println("Correct interval selected");
		}
	}

	private void selectCorrectInterval() {
		// only set limits if we know there are any.
		if (leftborder && leftsetthistime) {
			leftb = borders[minband];
		}
		if (rightborder && rightsetthistime) {
			rightb = borders[maxband];
		}

		if (!leftborder && !rightborder) {
			if (DEBUG) {
				System.out.println("nope, this shouldn't ever happen. no bounds were set. do we even have crossings?");
			}
			return;
		}

		// check if scope only has crossings at - inf or + inf and
		// calculate
		// in this case the vertical solution
		if ((!leftborder && leftmannyC) || (!rightborder && rightmannyC)) {
			System.out.println("These are in case there are many crossings on the left at - inf");
			done = true;
			verticalSol = true;
			verticalcut();// verticalSolPos is calculated here
			return;
		}

		int delta = (int) Math.round(EPSILON * lBlue.size());

		int topLvl = levelBlue - delta;
		int botLvl = levelBlue + delta;
		if (true) { // sanity check
			if (levelBlue < 1 || levelBlue >= lBlue.size()) {
				if (DEBUG) {
					System.out.println("REALLY BAD ERROR: yeah, levelBlue is fubar. go home and try again.");
				}
			}
			if (topLvl < 1) {
				if (DEBUG) {
					System.out.println("toplvl to small: fixing");
				}
				topLvl = 1;
			}
			if (botLvl >= lBlue.size()) {
				if (DEBUG) {
					System.out.println("botlvl to big: fixing");
				}
				botLvl = lBlue.size();
			}
		}
		if (!leftborder || !rightborder) {

			if (!leftborder) { // nach rechts offen
				double tr = levelPos(rightb, true, topLvl);
				double br = levelPos(rightb, true, botLvl);
				double ts = getslope(true, topLvl);
				double bs = getslope(true, botLvl);
				trapeze = new Trapeze(true, rightb, tr, br, ts, bs);
				if (DEBUG) {
					System.out.println("making m trapeze open to the left:");
				}
				if (DEBUG) {
					System.out.println("rightb: " + rightb + " tr: " + tr + " br: " + br + " ts: " + ts + " bs: " + bs);
				}

			} else if (!rightborder) { // nach links offen
				double tl = levelPos(leftb, true, topLvl);
				double bl = levelPos(leftb, true, botLvl);
				double ts = getslope(true, lBlue.size() - topLvl);
				double bs = getslope(true, lBlue.size() - botLvl);
				trapeze = new Trapeze(false, leftb, tl, bl, ts, bs);
				if (DEBUG) {
					System.out.println("making m trapeze open to the right");
				}
				if (DEBUG) {
					System.out.println("rightb: " + rightb + " tl: " + tl + " bl: " + bl + " ts: " + ts + " bs: " + bs);
				}
			}

		} else {
			double tl = levelPos(leftb, true, topLvl);
			double tr = levelPos(rightb, true, topLvl);
			double bl = levelPos(leftb, true, botLvl);
			double br = levelPos(rightb, true, botLvl);
			if (DEBUG) {
				System.out.println("lefftb:" + leftb + " rightb:" + rightb + " tl:" + tl + " bl:" + bl + " tr:" + tr + " br:" + br);
				System.out.println("blue top at leftb: " + blueTop(leftb) + " blue top at rightb: " + blueTop(rightb));
			}
			trapeze = new Trapeze(leftb, tl, bl, rightb, tr, br);
		}
		step++;

		if (DEBUG) {
			System.out.println("Trapezoid constructed");
		}
		borders = new double[64];
		minband = 0;
		maxband = 0;
	}

	private void cutAwayLines() {
		// cut away lines, count and make sure levelB/R are correct:
		int deleted = 0;
		for (int i = 0; i < lBlue.size();) {
			int s = trapeze.intersects(lBlue.get(i));
			if (s != 0) {
				if (s > 0) {
					levelBlue--;
				}
				hideLine(lBlue.get(i));
				deleted++;
			} else {
				i++;
			}
		}
		for (int i = 0; i < lRed.size();) {
			int s = trapeze.intersects(lRed.get(i));
			if (s != 0) {
				if (s > 0) {
					levelRed--;
				}
				hideLine(lRed.get(i));
				deleted++;
			} else {
				i++;
			}
		}
		step = 0;

		if (DEBUG) {
			System.out.println(deleted + " Removed out-of-interval lines.");
		}
		if (deleted == 0) { // ya done goof'd
			done = true;
		}
	}
	
	public void process() {
		while (!done) {
			doAlg();
		}
	}

	private void doAlg() { // sets done to true iff it has found m solution
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		switch (step) {
			case 0 : // Initial situation
				init();
				step++;
				break;
			case 1 : // Intervals Scheduled
				scheduleIntervals();
				step++;
				break;
			case 2 : // Correct interval selected
				selectCorrectInterval();
				step++;
				break;
			case 3 : // trapezoid constructed
				step++;
				break;
			case 4 : // trapezoid constructed
				cutAwayLines();
				break;
		}
	}
}
