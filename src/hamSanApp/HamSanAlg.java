package hamSanApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import view.PointType;
import view.VisualPoint;

/**
 * This class contains the actual algorithm and some helper functions. Important
 * external methods: addLine, removeLine, findLine, findPoint, doAlg.
 * 
 * @author fabian
 *
 */
public class HamSanAlg {

	public List<Point> lBlue; // hier werden die vom Alg. berücksichtigten Blauen Linien gespeichert
	public List<Point> lRed; // hier werden die vom Alg. berücksichtigten Roten Linien gespeichert
	public List<Point> lBlueDel; // Del für deleted
	public List<Point> lRedDel; // hier werden die nicht berücksichtigten linien gespeichert
	public List<Point> firstlRed;// Punktemengen zu Beginn des Algorithmus
	public List<Point> firstlBlue;
	public boolean leftborder; //
	public boolean rightborder; // bools, die wahr sind, falls der Momentane betrachtungsbereich nach
								// links/rechts beschrünkt ist
	public double leftb; //
	public double rightb; // der linke und Rechte Rand des betrachtungsbereiches
	int levelBlue; //
	int levelRed; // die wievielte linie von oben ist die gesuchte medianlinie?
	boolean firstRun; // ist der Algorithmus schonmal etwas gelaufen (künnen wir noch linien
						// veründern?
	public boolean done; // ist der Algorithmus fertig?
	boolean colorSwap; // müssen wir die Farben gerade vertauscht zeichnen?
	public boolean verticalSol; // ist die Lüsung eine Vertikale Linie?
	public double verticalSolPos; // position der vertikalen Lüsung
	public Point solution; // position der nicht-vertikalen Lüsung
	public double[] borders; // positionen der grenzen zwischen streifen.
	// konvention: borders[i] ist der linke rand von dem i-ten streifen und die
	// streifen sind halboffen, linker punkt ist drin.
	public List<Crossing> crossings;// hier werden die Kreuzungen gespeichert;
	boolean DEBUG = true;
	public Trapeze trapeze; // das trapez (zum zeichnen)
	public int minband; //
	public int maxband; // zur binüren suche auf den intervallen(bündern)
	public int step; // in welchem shritt sind wir?
						// 0: Ausgangssituation
						// 1: Intervalle Eingeteilt
						// 2: Richtiges Intervall rausgesucht
						// 3: Trapez konstruiert

	boolean leftsetthistime = false; //
	boolean rightsetthistime = false; // used for going from step 2 to 3.
	boolean leftmannyC = false;// wird auf true gesetzt, falls linker bzw rechter Randbereich bei
								// Intervalleinteilung
	boolean rightmannyC = false;// aus mehr als (alpfa * Crossings.size()) kreuzungen im negativ bzw
								// positivUnendlichen besteht

	final double alpha = 1.0d / 32.0d; //
	final double eps = 1.0d / 8.0d; // Konstanten für den Alg

	/**
	 * Konstruktor, macht nichts besonderes.
	 */
	public HamSanAlg() {
		init();
	}

	/**
	 * Sets all variables to start states.
	 */
	public void init() {
		lBlue = new ArrayList<Point>();
		lRed = new ArrayList<Point>();
		lBlueDel = new ArrayList<Point>();
		lRedDel = new ArrayList<Point>();
		firstlRed = new ArrayList<Point>();
		firstlBlue = lRed;
		firstlBlue = new ArrayList<Point>();
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
		crossings = new ArrayList<Crossing>();
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
	 * @param blue is it a blue line?
	 */
	public Point addLine(double x, double y, boolean blue) {
		if (!firstRun) {
			return null;
		}
		Point p = new Point(x, y);
		if (blue) {
			lBlue.add(p);
		} else {
			lRed.add(p);
		}
		return p;
	}

	/**
	 * Erase a line out of Blue and Red. only possible if the * algorithm has not
	 * yet started.
	 * 
	 * @param l the line to delete
	 */
	public void removeLine(Point l) {
		if (!firstRun) {
			return;
		}
		lBlue.remove(l);
		lRed.remove(l);
	}

	/**
	 * hide a line from the algorithm. it is then drawn separately.
	 * 
	 * @param l
	 */
	public void hideLine(Point l) {
		if (lBlue.remove(l)) {
			lBlueDel.add(l);
		}
		if (lRed.remove(l)) {
			lRedDel.add(l);
		}
	}

	/**
	 * * Function that returns a point near position (x,y).
	 * 
	 * @param tolerance how far (x,y) can be from the point
	 * @return the dot
	 */
	public Point findPoint(double x, double y, double tolerance) {
		Point best = null;
		double bestdist = 9999;
		for (Point test : lBlue) {
			double dist = Math.sqrt((test.a - x) * (test.a - x) + (test.b - y) * (test.b - y));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		for (Point test : lRed) {
			double dist = Math.sqrt((test.a - x) * (test.a - x) + (test.b - y) * (test.b - y));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		return best;
	}

	/**
	 * Funktion, die eine Gerade zurückgibt, der in der nühe der position (x,y) ist.
	 * 
	 * @param tolerance wie weit entfernt (x,y) von dem Punkt sein darf;
	 * @return der Punkt
	 */
	public Point findLine(double x, double y, double tolerance) {
		Point best = null;
		double bestdist = 9999;
		for (Point test : lBlue) {
			double dist = (Math.abs(y - test.eval(x))) * Math.cos(Math.atan(test.a));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		for (Point test : lRed) {
			double dist = (Math.abs(y - test.eval(x))) * Math.cos(Math.atan(test.a));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		return best;
	}

	public boolean TestLineSort(double x, boolean blue) {
		boolean r = true;
		LineComparator x_evaluation = new LineComparator(x);
		List<Point> locList;
		if (blue) {
			locList = new ArrayList<Point>(lBlue);
		} else {
			locList = new ArrayList<Point>(lRed);
		}
		Collections.sort(locList, x_evaluation);
		for (int i = 0; i < locList.size(); i++) {
			if (i + 1 != locList.size()) {
				if ((locList.get(i).eval(x) == locList.get(i + 1).eval(x)) && (locList.get(i).i <= locList.get(i + 1).i)) {
					if (x < 0) {
						r = false;
					}
				} else if (locList.get(i).i > locList.get(i + 1).i) {
					if (x >= 0) {
						r = false;
					}
				}
			}

		}
		return r;
	}

	public List<Point> TestLineSort2(double x, boolean blue) {
		LineComparator x_evaluation = new LineComparator(x);
		List<Point> locList;
		if (blue) {
			locList = new ArrayList<Point>(lBlue);
		} else {
			locList = new ArrayList<Point>(lRed);
		}
		Collections.sort(locList, x_evaluation);

		return locList;
	}

	/**
	 * gibt die y-Koordinate der level'ten linie von Oben an der stelle x aus Dabei
	 * nimmt Level Werte zwischen 1 und lBlue.size()+1 bzw l.size()+1 an!
	 * 
	 * @param x     die x-Koordinate
	 * @param blue  von den Blauen oder Roten linien?
	 * @param level wievielte linie von oben?
	 * @return der y-Wert
	 */

	public double levelPos(double x, boolean blue, int level) {
		LineComparator x_evaluation = new LineComparator(x);
		List<Point> locList;
		if (blue) {
			locList = new ArrayList<Point>(lBlue);
		} else {
			locList = new ArrayList<Point>(lRed);
		}
		Collections.sort(locList, x_evaluation);
		return locList.get(level - 1).eval(x);
	}

	/**
	 * Ist an der stelle die Blaue Medianlinie hüher als die Rote?
	 * 
	 * @param x die Stelle
	 * @return 1, falls blau oben, -1 falls rot, 0 falls wir einen Schnittpunkt
	 *         haben.
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
	 * Funktion, die errechnet, ob im unbeschrünkten bereich links die blaue
	 * medianlinie über der Roten ist
	 * 
	 * @return true falls ja
	 */
	public boolean blueTopLeft() { // TODO Testme
		LineComparator2 c = new LineComparator2();

		List<Point> blueLoc = new ArrayList<Point>(lBlue);
		List<Point> redLoc = new ArrayList<Point>(lRed);
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
		List<Point> col;
		if (blue) {
			col = new ArrayList<Point>(lBlue);
		} else {
			col = new ArrayList<Point>(lRed);
		}
		Collections.sort(col, c);
		Collections.reverse(col);
		return col.get(level - 1).a;
	}

	/**
	 * function that checks if a given cut is valid.
	 *
	 * @return Yes if valid cut
	 */
	public boolean validSol(boolean verbose) {
		if (!done) {
			if (DEBUG && verbose) {
				System.out.println("algo is not done!");
			}
			return false; // haben noch keinen schnitt.
		}

		if (!verticalSol && solution == null) {
			return false;
		}
		double tol = 0.0000001; // tolerance
		if (verticalSol) {
			int bleft = 0;
			int bright = 0;
			int rleft = 0;
			int rright = 0;

			for (Point t : lBlue) {
				if (verticalSolPos + tol < t.a) {
					bright++;
				}
				if (verticalSolPos - tol > t.a) {
					bleft++;
				}
			}
			for (Point t : lBlueDel) {
				if (verticalSolPos + tol < t.a) {
					bright++;
				}
				if (verticalSolPos - tol > t.a) {
					bleft++;
				}
			}
			for (Point t : lRed) {
				if (verticalSolPos + tol < t.a) {
					rright++;
				}
				if (verticalSolPos - tol > t.a) {
					rleft++;
				}
			}
			for (Point t : lRedDel) {
				if (verticalSolPos + tol < t.a) {
					rright++;
				}
				if (verticalSolPos - tol > t.a) {
					rleft++;
				}
			}
			if (verbose) {
				System.out.println(
						"There are " + bleft + " blue points left, " + bright + " right of a total of " + (lBlue.size() + lBlueDel.size()));
				System.out.println(
						"There are " + rleft + " red points left, " + rright + " right of a total of " + (lRed.size() + lRedDel.size()));
			}

			if (Math.max(bleft, bright) > (lBlue.size() + lBlueDel.size()) / 2) {
				return false;
			}
			if (Math.max(rleft, rright) > (lRed.size() + lRedDel.size()) / 2) {
				return false;
			}

			// System.out.println("haben Vertikale Lösung gefunden");
			return true;
		}

		int babove = 0; // blue above
		int bbelow = 0; // blue below
		int rabove = 0; // red ..
		int rbelow = 0;

		for (Point t : lBlue) {
			if (solution.eval(t.a) + tol < t.b) {
				babove++;
			}
			if (solution.eval(t.a) - tol > t.b) {
				bbelow++;
			}
		}
		for (Point t : lBlueDel) {
			if (solution.eval(t.a) + tol < t.b) {
				babove++;
			}
			if (solution.eval(t.a) - tol > t.b) {
				bbelow++;
			}
		}
		for (Point t : lRed) {
			if (solution.eval(t.a) + tol < t.b) {
				rabove++;
			}
			if (solution.eval(t.a) - tol > t.b) {
				rbelow++;
			}
		}
		for (Point t : lRedDel) {
			if (solution.eval(t.a) + tol < t.b) {
				rabove++;
			}
			if (solution.eval(t.a) - tol > t.b) {
				rbelow++;
			}
		}
		if (verbose) {
			System.out.println(
					"There are " + bbelow + " blue points below, " + babove + " above of a total of " + (lBlue.size() + lBlueDel.size()));
			System.out.println(
					"There are " + rbelow + " red points below, " + rabove + " above of a total of " + (lRed.size() + lRedDel.size()));
		}

		if ((Math.max(bbelow, babove) > (lBlue.size() + lBlueDel.size()) / 2)
				|| (Math.max(rbelow, rabove) > (lRed.size() + lRedDel.size()) / 2)) {
			return false;
		}

		return true;
	}

	/**
	 * the actual algorithm. running this algorithm provides a Iteration step. We
	 * probably want to further break this down into smaller ones Split steps.
	 */
	// In case solution is a crossing at infinity, the solution is one
	// vertical line.
	// Go through all intersections before index or after index and find the cut!
	public boolean verticalcut() {
		System.out.println("Are in case Hamsandwichcut is a vertical");
		for (Point element : lBlue) {
			verticalSolPos = element.a;
			if (validSol(true)) {
				System.out.println("Vertical solution found through blue dot");
				return true;
			}
		}
		System.out.println("There is no vertical solution through a blue dot");
		for (Point element : lRed) {
			verticalSolPos = element.a;
			if (validSol(true)) {
				System.out.println("Vertical solution found through red dot");
				return true;
			}
			System.out.println("There is no vertical solution at all");
		}
		return false;
	}

	public void doAlg() { // sets done to true iff it has found a solution
		if (done) {
			if (validSol(true)) {
				System.out.println("Yay it worked");
			}
			return;
		}
		if (lBlue.size() == 0 && lRed.size() == 0) {
			return; // nix zu tun!
		}
		switch (step) {

			case 0 :
				trapeze = null;
				if (firstRun) {
					// speichere Anfangskonstellation der Punkte ab
					firstlRed = new ArrayList<Point>(lRed);
					firstlBlue = lRed;// Punktemengen zu Beginn des Algorithmus
					firstlBlue = new ArrayList<Point>(lBlue);
					// make sure that both sets are odd by deleting a point out of
					// each set:
					if (((lBlue.size() % 2) == 0) && lBlue.size() > 0) {
						hideLine(lBlue.get(0));
					}
					if (((lRed.size() % 2) == 0) && lRed.size() > 0) {
						hideLine(lRed.get(0));
					}
					// set the levelBlue and levelRed to the correct values:
					levelBlue = ((lBlue.size() + 1) / 2);
					levelRed = ((lRed.size() + 1) / 2);
					firstRun = false; // so we don't change the points, and only do
										// this once
				}

				if (lBlue.size() == 0) { // only red lines.
					double rL = levelPos(0, false, (levelRed));
					solution = new Point(0, rL);
					done = true;
					firstRun = false;
					return;
				}
				if (lRed.size() == 0) { // only red lines.
					double bL = levelPos(0, true, (levelBlue));
					solution = new Point(0, bL);
					done = true;
					firstRun = false;
					return;
				}

				// check if trivial solution:
				if (lBlue.size() == 1 && lRed.size() == 1) {
					Point b = lBlue.get(0);
					Point r = lRed.get(0);
					// do we need a vertical line?
					if (b.a == r.a) {
						if (DEBUG) {
							System.out.println("have exactly two parallel lines of different colors");
						}
						done = true;
						verticalSol = true;
						verticalSolPos = b.a;
						return;

					}
					done = true;
					// find intersection point and return that. done!
					Crossing c = new Crossing(r, b);
					solution = new Point(-c.crAt(), r.eval(c.crAt()));
					return;
				}

				// swap the lines if blue is smaller:
				if (lBlue.size() < lRed.size()) {
					colorSwap = !colorSwap;
					List<Point> temp = lBlue;
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
				crossings = new ArrayList<Crossing>();

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
					solution = new Point(-c.crAt(), c.line1.eval(c.crAt()));
					if (DEBUG) {
						System.out.println(
								"es gibt nur eine Kreuzung im Betrachteten Bereich zwischen roten und blauen Linien. es muss die Loesung sein");
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
				// make stripes with at most alpha*(n choose 2) crossings a piece.

				minband = 0;
				maxband = 0; // wird überschrieben.
				int band = 1;
				int bandsize = (int) (crossings.size() * alpha);
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
							// If there are only intersections in negative and positive infinity, then the
							// input from parallels
							// Straight lines or from points with the same x-coordinate. the vertical
							// through all
							// these points
							// is the solution in this case
							if ((i == crossings.size()) || crossings.get(i).atInf() && !crossings.get(i).atNegInf()) {
								if (DEBUG) {
									System.out.println("Since all lines are parallel, we have "
											+ "Entered points have the same x-coordinate. Therefore, "
											+ "the result of a vertical through all points; " + "Case of many crossings at - Inf");
								}
								done = true;
								verticalSol = true;
								verticalSolPos = lBlue.get(0).a;
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
									System.out.println("Since all lines are parallel, we have "
											+ "Entered points have the same x-coordinate. Therefore, "
											+ "the result of a vertical through all points" + "in case concurrency is checked");
								}
								done = true;
								verticalSol = true;
								verticalSolPos = lBlue.get(0).a;
								return;
							} else {
								if (DEBUG) {
									System.out.println("funny case, in which the crossing infinity appears in the first interval");
									// since not all lines are parallel, there must be an intersection that isn't
									// lies at infinity
									// and is contained in the first itervall.
									// So in this case we only get exactly two intervals!
									System.out.println("have exactly two intervals. In the first interval there is at least "
											+ "contain a crossing that is not at infinity");
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

						} // End of the case that at the beginning of the interval division we have a
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
							maxband = band;// }
							break;
						}
					} // End if the current index has a crossing at infinity
						/////// Case when the current index has no crossing at infinity

					borders[band] = crossings.get(i).crAt();
					band++;
					maxband = band;
				}
				step++;
				if (DEBUG) {
					System.out.println("Intervals divided!");
				}
				break;
			case 1 :
				// find strip with odd number of intersections by binary search:
				boolean bluetop;
				if (leftborder) {
					int res = blueTop(leftb);
					if (res == 0) {
						if (DEBUG) {
							System.out.println("schnittpunkt zufaellig bei binaerer Suche gefunden!");
						}
						done = true;
						solution = new Point(-leftb, levelPos(leftb, true, levelBlue));
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
						if (DEBUG) {
							System.out.println("schnittpunkt zufaellig bei binaerer Suche gefunden!");
						}
						done = true;
						solution = new Point(-borders[testband], levelPos(borders[testband], true, levelBlue));
						return;
					}

				}
				step++;
				if (DEBUG) {
					System.out.println("Richtiges Intervall rausgesucht");
				}
				break;
			case 2 :

				// grenzen nur setzen, falls wir wissen, dass da welche sind.
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

				// prÃ¼fe, ob Betrachtungsbereich nur Kreuzungen bei - inf oder + inf hat und
				// berechne
				// in diesem Fall die Vertikale Lösung
				if ((!leftborder && leftmannyC) || (!rightborder && rightmannyC)) {
					System.out.println("Sind im Fall, dass es links viele Kreuzungen bei - inf gibt");
					done = true;
					verticalSol = true;
					verticalcut();// hier wierd verticalSolPos berechnet
					return;
				}

				int delta = (int) Math.round(eps * lBlue.size());

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
							System.out.println("making a trapeze open to the left:");
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
							System.out.println("making a trapeze open to the right");
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
					System.out.println("Trapez konstruiert");
				}
				borders = new double[64];
				minband = 0;
				maxband = 0;
				break;
			case 3 :
				step++;
				break;
			case 4 :

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
					System.out.println(deleted + " Linien ausserhalb des intervalls entfernt.");
				}
				if (deleted == 0) { // ya done goof'd
					done = true;
					return;
				}
				break;
		}
	}

	public List<VisualPoint> getVisualPoints() {
		List<VisualPoint> result = new ArrayList<VisualPoint>();
		for (Point p : lBlue) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, false, p);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, false, p);
				result.add(newPoint);
			}
		}

		for (Point p : lRed) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, false, p);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, false, p);
				result.add(newPoint);
			}
		}

		for (Point p : lBlueDel) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, true, p);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, true, p);
				result.add(newPoint);
			}
		}

		for (Point p : lRedDel) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, true, p);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, true, p);
				result.add(newPoint);
			}
		}

		return result;
	}
}
