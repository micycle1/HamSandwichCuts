package micycle.hscut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for Ham-sandwich cuts of point sets.
 * 
 * @author Fabian Stroh
 * @author Michael Carleton
 */
public class HamSandwichCut {

	private static final Logger logger = LoggerFactory.getLogger(HamSandwichCut.class);

	public List<PointLineDual> lBlue; // the blue lines taken into account by the algorithm are saved here
	public List<PointLineDual> lRed; // the red lines taken into account by the algorithm are stored here
	private List<PointLineDual> lBlueDel; // Del for deleted
	private List<PointLineDual> lRedDel; // the lines not taken into account are stored here

	public boolean verticalSol; // is the solution m vertical line?
	public double verticalSolPos; // position of the vertical solution
	public PointLineDual solution; // position of the non-vertical solution

	private List<PointLineDual> setA; // below / left
	private List<PointLineDual> setADeleted;
	private List<PointLineDual> setB;
	private List<PointLineDual> setBDeleted;

	private boolean leftborder; //
	private boolean rightborder; // bools that are true if the current scope is after
	// is left/right constrained
	private double leftb; //
	private double rightb; // the left and right edges of the viewing area
	private int levelBlue; //
	private int levelRed; // how many lines from the top is the median line you are looking for?
	private boolean firstRun; // Has the algorithm ever run m bit (can we still use lines
	// change?
	private boolean done; // is the algorithm ready?
	private boolean colorSwap; // do we just have to draw the colors reversed?
	private double[] borders; // positions of borders between stripes.
	// convention: borders[i] is the left edge of the i-th stripe and the
	// strips are half open, left dot is inside.
	private List<Crossing> crossings;// the crossings are stored here;
	private Trapeze trapeze; // the trapezoid (to draw)
	private int minband; //
	private int maxband; // to search for binaries on the intervals (bundles)

	/*-
	 * 0: Initial situation
	 * 1: Intervals Scheduled
	 * 2: Correct interval selected
	 * 3: trapezoid constructed
	 */
	private int step; // what step are we in?

	private boolean leftsetthistime = false;
	private boolean rightsetthistime = false; // used for going from step 2 to 3.
	private boolean leftmannyC = false;// set to true if left or right border area at interval division
	private boolean rightmannyC = false;// from more than (alpfa * Crossings.size()) crossings in the negative or
										// positive infinity

	private static final double ALPHA = 1.0d / 32.0d;
	private static final double EPSILON = 1.0d / 8.0d; // constants for the alg

	public HamSandwichCut() {
		initVariables();
	}

	public HamSandwichCut(List<PointLineDual> red, List<PointLineDual> blue) {
		this();
		lRed = new ArrayList<>(red); // copy list
		lBlue = new ArrayList<>(blue); // copy list
	}
	
	public HamSandwichCut(List<PointLineDual> blue) {
		this();
		lBlue = new ArrayList<>(blue); // copy list
	}

	/**
	 * Add lines in the form of two coordinates. Only possible if the Algorithm not
	 * yet started.
	 *
	 * @param a    first
	 * @param b    second coordinate
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

	public PointLineDual addPoint(double x, double y, boolean blue) {
		return addLine(x, y, blue);
	}

	/**
	 * Run the Ham-sandwich cut algorithm on the input.
	 */
	public void process() {
		while (!done) {
			stepAlg();
		}
	}

	/**
	 * Checks if the cut is valid. Assumes {@link #process()} has been called.
	 *
	 * @return true if cut is valid (divides the input in half)
	 */
	public boolean isValid() {
		if (!done) {
			logger.warn("Not cut to check for validity!");
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
			logger.info("Blue: {} points left of the line; {} to the right; total={}", bleft, bright, lBlue.size() + lBlueDel.size());
			logger.info("Red: {} points left of the line; {} to the right; total={}", rleft, rright, lRed.size() + lRedDel.size());

			if (Math.max(bleft, bright) > (lBlue.size() + lBlueDel.size()) / 2) {
				return false;
			}
			if (Math.max(rleft, rright) > (lRed.size() + lRedDel.size()) / 2) {
				return false;
			}

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
		logger.info("Blue: {} points below the line; {} above; total={}", babove, bbelow, lBlue.size() + lBlueDel.size());
		logger.info("Red: {} points below the line; {} above; total={}", rbelow, rabove, lRed.size() + lRedDel.size());

		if ((Math.max(bbelow, babove) > (lBlue.size() + lBlueDel.size()) / 2)
				|| (Math.max(rbelow, rabove) > (lRed.size() + lRedDel.size()) / 2)) {
			return false;
		}

		return true;
	}

	/**
	 * Get the set of points that lie below the cutting line.
	 * 
	 * @param includeDeleted
	 * @return
	 */
	public List<PointLineDual> getParitionA(boolean includeDeleted) {
		findParitions();

		List<PointLineDual> out;
		if (includeDeleted) {
			out = new ArrayList<>(setA);
			out.addAll(setADeleted);
		} else {
			out = setA;
		}
		return out;
	}

	/**
	 * Get the set of points that lie above the cutting line.
	 * 
	 * @param includeDeleted
	 * @return
	 */
	public List<PointLineDual> getParitionB(boolean includeDeleted) {
		findParitions();

		List<PointLineDual> out;
		if (includeDeleted) {
			out = new ArrayList<>(setB);
			out.addAll(setBDeleted);
		} else {
			out = setB;
		}
		return out;
	}

	/**
	 * Sets all variables to start states.
	 */
	private void initVariables() {
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

	private void stepAlg() { // sets done to true when a solution is found
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
				logger.debug("have exactly two parallel lines of different colors");
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
			logger.debug("There is only one crossing in the considered area between red and blue lines. It must be the solution");
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

		Collections.sort(crossings); // sort them. crossings implements comparable.

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

		leftmannyC = false;// set to true if left or right border area at interval division
		rightmannyC = false;// have more than bandsize crossings in negative/resp. positive infinity
		logger.debug("Intervals are divided");
		for (int i = bandsize; i < crossings.size(); i += bandsize) {
			// case that at current index i crossing is at infinity
			if (crossings.get(i).atInf()) {
				if (crossings.get(i).atNegInf()) { // case that first bandsize crossings are at negative infinity

					/*
					 * There are more than bandsize crossings at negative infinity, so increase
					 * first interval like this, so that all crossings at negative infinity are
					 * included in it.
					 */
					leftmannyC = true;
					logger.debug("Have many crossings at negative infinity");
					while (i < crossings.size() && crossings.get(i).atInf() && crossings.get(i).atNegInf()) {
						i++;
					}
					/*
					 * If there are only intersections in negative and positive infinity, then the
					 * input is parallel straight lines or from points with the same x-coordinate.
					 * The vertical through all these points is the solution in this case.
					 */
					if ((i == crossings.size()) || crossings.get(i).atInf() && !crossings.get(i).atNegInf()) {
						logger.debug(
								"All lines are parallel (points have the same x-coordinate), so the result is a vertical through all points.");
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
						logger.debug(
								"All lines are parallel (points have the same x-coordinate), so the result is a vertical through all points.");
						done = true;
						verticalSol = true;
						verticalSolPos = lBlue.get(0).m;
						return;
					} else {
						logger.debug(
								"Hit a funny case, in which we have exactly two intervals,. and in the first interval there is at least a crossing that is not at infinity");
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
					logger.debug("Have many crossings in positive infinity");
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
		logger.debug("Intervals divided!");
	}

	private void scheduleIntervals() {
		// find strip with odd number of intersections by binary search:
		boolean bluetop;
		if (leftborder) {
			int res = blueTop(leftb);
			if (res == 0) {
				logger.debug("Point of intersection accidentally found in a binary search!");
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
			} else if (bluetesttop == 0) { // we have a winner!
				logger.debug("Point of intersection accidentally found in a binary search!");
				done = true;
				solution = new PointLineDual(-borders[testband], levelPos(borders[testband], true, levelBlue));
				return;
			}

		}
		logger.debug("Correct interval selected");
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
			logger.error("no bounds were set. do we even have crossings?");
			return;
		}

		/*
		 * Check if scope only has crossings at - inf or + inf and calculate in case
		 * this is the vertical solution.
		 */
		if ((!leftborder && leftmannyC) || (!rightborder && rightmannyC)) {
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
				logger.error("levelBlue is fubar!");
			}
			if (topLvl < 1) {
				logger.debug("toplvl too small: fixing");
				topLvl = 1;
			}
			if (botLvl >= lBlue.size()) {
				logger.debug("botlvl too big: fixing");
				botLvl = lBlue.size();
			}
		}
		if (!leftborder || !rightborder) {
			if (!leftborder) { // open to the right
				double tr = levelPos(rightb, true, topLvl);
				double br = levelPos(rightb, true, botLvl);
				double ts = getslope(true, topLvl);
				double bs = getslope(true, botLvl);
				trapeze = new Trapeze(true, rightb, tr, br, ts, bs);
				logger.debug("Making a trapeze (open to the left) : rightb={}, tr={}, br={}, ts={}, bs={}", rightb, tr, br, ts, bs);

			} else if (!rightborder) { // open to the left
				double tl = levelPos(leftb, true, topLvl);
				double bl = levelPos(leftb, true, botLvl);
				double ts = getslope(true, lBlue.size() - topLvl);
				double bs = getslope(true, lBlue.size() - botLvl);
				trapeze = new Trapeze(false, leftb, tl, bl, ts, bs);
				logger.debug("Making a trapeze (open to the right) : rightb={}, tl={}, bl={}, ts={}, bs={}", rightb, tl, bl, ts, bs);
			}
		} else {
			double tl = levelPos(leftb, true, topLvl);
			double tr = levelPos(rightb, true, topLvl);
			double bl = levelPos(leftb, true, botLvl);
			double br = levelPos(rightb, true, botLvl);
			trapeze = new Trapeze(leftb, tl, bl, rightb, tr, br);
		}
		step++;

		logger.debug("Trapezoid constructed");
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

		logger.debug("Removed {} out-of-interval lines", deleted);
		if (deleted == 0) { // ya done goof'd
			done = true;
		}
	}

	/**
	 * Hide a line from the algorithm processing.
	 * 
	 * @param l
	 */
	private void hideLine(PointLineDual l) {
		if (lBlue.remove(l)) {
			lBlueDel.add(l);
		}
		if (lRed.remove(l)) {
			lRedDel.add(l);
		}
	}

	/**
	 * Outputs the y-coordinate of the level'th line from above at the x position
	 * takes level values between 1 and lBlue.size()+1 or l.size()+1!
	 *
	 * @param x     the x coordinate
	 * @param blue  from the Blue or Red Lines?
	 * @param level how many lines from the top?
	 * @return the y value
	 */
	private double levelPos(double x, boolean blue, int level) {
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
	private int blueTop(double x) {
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
	 * Helper function to find out if we have to consider an intersection.
	 *
	 * @param c the intersection in question
	 * @return true if we need to consider the crossing.
	 */
	private boolean inBorders(Crossing c) { // Don't know if commenting out this makes it work. huh
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
	 * Calculates whether the blue links in the unrestricted area median line is
	 * above the red.
	 *
	 * @return true if yes
	 */
	private boolean blueTopLeft() {
		LineComparator2 c = new LineComparator2();

		List<PointLineDual> blueLoc = new ArrayList<>(lBlue);
		List<PointLineDual> redLoc = new ArrayList<>(lRed);
		Collections.sort(blueLoc, c);
		Collections.sort(redLoc, c);
		return 1 == c.compare(blueLoc.get(levelBlue - 1), redLoc.get(levelRed - 1));
	}

	/**
	 * Returns the level-t highest slope of the red or blue line. Used for
	 * unrestricted trapezes.
	 *
	 * @param blue  the blue straights?
	 * @param level how many-biggest slope?
	 * @return the slope
	 */
	private double getslope(boolean blue, int level) {
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
	 * Find the two sets of points partitioned by the cut solution.
	 */
	private void findParitions() {
		if (setA == null && done) {
			setA = new ArrayList<>();
			setADeleted = new ArrayList<>();
			setB = new ArrayList<>();
			setBDeleted = new ArrayList<>();

			final double tol = 0.0000001; // tolerance
			for (PointLineDual t : lBlue) {
				if (solution.eval(t.m) + tol < t.b) {
					setA.add(t);
				}
				if (solution.eval(t.m) - tol > t.b) {
					setB.add(t);
				}
			}
			for (PointLineDual t : lRed) {
				if (solution.eval(t.m) + tol < t.b) {
					setA.add(t);
				}
				if (solution.eval(t.m) - tol > t.b) {
					setB.add(t);
				}
			}
			for (PointLineDual t : lBlueDel) {
				if (solution.eval(t.m) + tol < t.b) {
					setADeleted.add(t);
				}
				if (solution.eval(t.m) - tol > t.b) {
					setBDeleted.add(t);
				}
			}
			for (PointLineDual t : lRedDel) {
				if (solution.eval(t.m) + tol < t.b) {
					setADeleted.add(t);
				}
				if (solution.eval(t.m) - tol > t.b) {
					setBDeleted.add(t);
				}
			}
		}
	}

	/**
	 * The actual algorithm. running this algorithm provides m Iteration step. We
	 * probably want to further break this down into smaller ones Split steps.
	 */
	private boolean verticalcut() {
		/*
		 * In case solution is m crossing at infinity, the solution is one vertical
		 * line. Go through all intersections before index or after index and find the
		 * cut!
		 */
		logger.info("Checking whether Hamsandwich Cut is vertical");
		for (PointLineDual element : lBlue) {
			verticalSolPos = element.m;
			if (isValid()) {
				logger.info("Vertical solution found through blue dot");
				return true;
			}
		}
		logger.info("There is no vertical solution through m blue dot");
		for (PointLineDual element : lRed) {
			verticalSolPos = element.m;
			if (isValid()) {
				logger.info("Vertical solution found through red dot");
				return true;
			}
			logger.info("There is no vertical solution at all");
		}
		return false;
	}
}
