package hamSanApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.Math;

import view.PointType;
import view.VisualPoint;

/**
 * Diese Klasse beinhaltet den eigentlichen Algorithmus und einige Hilfsfunktionen.
 * wichtigie Methoden von au�en: 
 * addLine, removeLine, findLine, findPoint, doAlg
 * @author fabian
 *
 */
public class HamSanAlg {

	public List<Point> lBlue; 		//hier werden die vom Alg. ber�cksichtigten Blauen Linien gespeichert
	public List<Point> lRed;		//hier werden die vom Alg. ber�cksichtigten Roten Linien gespeichert
	public List<Point> lBlueDel;	// Del f�r deleted
	public List<Point> lRedDel;	//hier werden die nicht ber�cksichtigten linien gespeichert
	boolean leftborder;		//
	boolean rightborder;	//bools, die wahr sind, falls der Momentane betrachtungsbereich nach links/rechts beschr�nkt ist
	double leftb;			//
	double rightb;			//der linke und Rechte Rand des betrachtungsbereiches
	int levelBlue;			//
	int levelRed;			//die wievielte linie von oben ist die gesuchte medianlinie?
	boolean firstRun;		//ist der Algorithmus schonmal etwas gelaufen (k�nnen wir noch linien ver�ndern?
	public boolean done;			//ist der Algorithmus fertig?
	boolean colorSwap;		//m�ssen wir die Farben gerade vertauscht zeichnen?
	public boolean verticalSol;	//ist die L�sung eine Vertikale Linie?
	public double verticalSolPos;	//position der vertikalen L�sung
	public Point solution;			//position der nicht-vertikalen L�sung
	double [] borders;		//positionen der grenzen zwischen streifen.
								//konvention: borders[i] ist der linke rand von dem i-ten streifen und die streifen sind halboffen, linker punkt ist drin.
	public List<Crossing> crossings;// hier werden die Kreuzungen gespeichert;
	boolean DEBUG = true;
	
	final double alpha = 1.0d/32.0d; 	//
	final double eps = 1.0d/8.0d;		//Konstanten f�r den Alg
	
	/**
	 * Konstruktor, macht nichts besonderes.
	 */
	HamSanAlg(){
		init();
	}
	
	/** 
	 * setzt alle Variablen auf startzust�nde
	 */
	public void init() {
		lBlue = new ArrayList<Point>();
		lRed = new ArrayList<Point>();
		lBlueDel = new ArrayList<Point>();
		lRedDel = new ArrayList<Point>();
		leftborder = false;
		rightborder = false;
		leftb = 0;
		rightb = 0;
		firstRun = true;
		done = false;
		solution = null;
		verticalSol = false;
		borders = new double[64];
		crossings = new ArrayList<Crossing>();
	}
	
	/**
	 * Linien hinzuf�gen in Form zweier Koordinaten.
	 * nur m�glich, wenn der Algorithmus noch nicht angelaufen ist.
	 * @param x erste
	 * @param y zweite koordinate
	 * @param blue ist es eine blaus linie?
	 */
	public boolean addLine(double x, double y, boolean blue){
		if (!firstRun) {return false;}
		if (blue){
			lBlue.add(new Point(x, y));
		}
		else {
			lRed.add(new Point(x, y));
		}
		return true;
	}
	
	/**
	 * l�sche eine linie aus lBlue und lRed heraus. 
	 * nur m�glich, wenn der Algorithmus noch nicht angelaufen ist.
	 * @param l die zu l�schende linie
	 */
	public void removeLine(Point l) {
		if (!firstRun) {return;}
		lBlue.remove(l);
		lRed.remove(l);
	}
	
	/**
	 * verstecke eine Linie vor dem Algorithmus. sie wird dann gesondert gezeichnet.
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
	 * Funktion, die einen Punkt zur�ckgibt, der in der n�he der position (x,y) ist.
	 * @param tolerance wie weit entfernt (x,y) von dem Punkt sein darf;
	 * @return der Punkt
	 */
	public Point findPoint(double x, double y, double tolerance) {
		Point best = null;
		double bestdist = 9999;
		for (int i = 0; i < lBlue.size(); i++) {
			Point test = lBlue.get(i);
			double dist = Math.sqrt((test.a-x)*(test.a-x)+(test.b-y)*(test.b-y));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		for (int i = 0; i < lRed.size(); i++) {
			Point test = lRed.get(i);
			double dist = Math.sqrt((test.a-x)*(test.a-x)+(test.b-y)*(test.b-y));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		return best;
	}
	
	/**
	 * Funktion, die eine Gerade zur�ckgibt, der in der n�he der position (x,y) ist.
	 * @param tolerance wie weit entfernt (x,y) von dem Punkt sein darf;
	 * @return der Punkt
	 */
	public Point findLine(double x, double y, double tolerance) {
		Point best = null;
		double bestdist = 9999;
		for (int i = 0; i < lBlue.size(); i++) {
			Point test = lBlue.get(i);
			double dist = (Math.abs(y-test.eval(x)))*Math.cos(Math.atan(test.a));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		for (int i = 0; i < lRed.size(); i++) {
			Point test = lRed.get(i);
			double dist = (Math.abs(y-test.eval(x)))*Math.cos(Math.atan(test.a));
			if (dist < tolerance && dist < bestdist) {
				best = test;
				bestdist = dist;
			}
		}
		return best;
	}
	
	/**
	 * gib die l�sung aus. warscheinlich nicht so wichtig, da das sp�ter anders gemacht wird,
	 * aber ohne graphikinterface so in ordnung
	 */
	public void presentSolution() {
		if (!done) {
			System.out.println("algorithm not done yet :(");
		}
		if (verticalSol) {
			System.out.println("the solution is a vertical line with x = "+verticalSolPos);
		}
		else {
			System.out.print("the solution is the ");
			solution.repr_line();
		}
	}
	
	/**
	 * gibt die y-Koordinate der level'ten linie von Oben an der stelle x aus
	 * Dabei nimmt Level Werte zwischen 1 und lBlue.size()+1 bzw l.size()+1 an! 
	 * @param x die x-Koordinate
	 * @param blue von den Blauen oder Roten linien?
	 * @param level wievielte linie von oben?
	 * @return der y-Wert
	 */
	public boolean TestLineSort(double x, boolean blue){
		boolean r=true;
		LineComparator x_evaluation = new LineComparator(x);
		List<Point> locList;
		if(blue==true){
			locList = new ArrayList<Point>(lBlue);
		}
		else {
			locList= new ArrayList<Point>(lRed);
		}
		Collections.sort(locList, x_evaluation);
		for (int i = 0; i < locList.size(); i++) {
	        if (i+1!=locList.size()){
	        	if(  (locList.get(i).eval(x)==locList.get(i+1).eval(x)) && ( locList.get(i).i <=locList.get(i+1).i ) ){
	        		if (x<0){r=false;}
	        	}
	        	else if ( locList.get(i).i >locList.get(i+1).i ){
	        		if (x>=0){r=false;}
	        	}
	        }
	        		

        }
		return r;
	}
	
	public  List<Point> TestLineSort2(double x, boolean blue){
		LineComparator x_evaluation = new LineComparator(x);
		List<Point> locList;
		if(blue==true){
			locList = new ArrayList<Point>(lBlue);
		}
		else {
			locList= new ArrayList<Point>(lRed);
		}
		Collections.sort(locList, x_evaluation);

		return locList;
	}

	public double levelPos(double x, boolean blue, int level) {
		LineComparator x_evaluation = new LineComparator(x);
		List<Point> locList;
		if(blue==true){
			locList = new ArrayList<Point>(lBlue);
		}
		else {
			locList= new ArrayList<Point>(lRed);
		}
		Collections.sort(locList, x_evaluation);
		return locList.get(level-1).eval(x);
	}
	
	/**
	 * Ist an der stelle die Blaue Medianlinie h�her als die Rote?
	 * @param x die Stelle
	 * @return 1, falls blau oben, -1 falls rot, 0 falls wir einen Schnittpunkt haben.
	 */
	public int blueTop(double x) {
		//is the blue level higher than the red level at x?
		double bluePos = levelPos(x, true, levelBlue);
		double redPos = levelPos(x, false, levelRed);
		if (bluePos > redPos) {
			return 1;
		}	
		if (bluePos > redPos) {
			return -1;
		}
		return 0;
	}
	
	/**
	 * Hilfsfunktion, um herauszufinden, ob wir eine Kreuzung ber�cksichtigen m�ssen.
	 * Schaut nach, ob die Kreuzung innerhalb des momentanen Betrachtungsbereiches ist.
	 * @param c die betreffende Kreuzung
	 * @return true, falls wir die Kreuzung ber�cksichtigen m�ssen.
	 */
	public boolean inBorders(Crossing c) {
		if (c.atInf()) {
			if (c.atNegInf() && leftborder) {
				return false;
			}
			if (!c.atNegInf() && rightborder) {
				return false;
			}
		}
		if (leftborder && c.crAt() < leftb) { return false;}
		if (rightborder && c.crAt() >= rightb) { return false;}
		return true;
	}
	
	/**
	 * Funktion, die errechnet, ob im unbeschr�nkten bereich links die blaue medianlinie �ber der Roten ist
	 * @return true falls ja
	 */
	public boolean blueTopLeft() {
		LineComparator2 c = new LineComparator2();
		
		List<Point> blueLoc = new ArrayList<Point>(lBlue);
		List<Point> redLoc = new ArrayList<Point>(lRed);
		Collections.sort(blueLoc, c);
		Collections.sort(redLoc, c);
		return 1 == c.compare(blueLoc.get(levelBlue+1), redLoc.get(levelRed+1));
	}
	
	
	/**
	 * der eigentliche Algorithmus. ein ausf�hren dieses Algorithmus stellt einen
	 * Iterationsschritt dar. wir wollen das warscheinlich noch weiter in 
	 * kleinere Schritte aufteilen.
	 */
	public void doAlg() { //sets done to true iff it has found a solution
		if (firstRun) {
			//make sure that both sets are odd by deleting a point out of each set:
			if ((lBlue.size()%2) == 0) {
				hideLine(lBlue.get(0));
			}
			if ((lRed.size()%2) == 0) {
				hideLine(lRed.get(0));
			}
			//set the levelBlue and levelRed to the correct values:
			levelBlue = (lBlue.size()+1/2);
			levelRed = (lRed.size()+1/2);
			firstRun = false; //so we don't change the points, and only do this once
		}
		
		//check if trivial solution:
		if (lBlue.size()==1 && lRed.size()==1) {
			Point b = lBlue.get(0);
			Point r = lRed.get(0);
			//do we need a vertical line?
			if (b.a == r.a) {
				done = true;
				verticalSol = true;
				verticalSolPos = b.a;
				return;
			}
			done = true;
			//find intersection point and return that. done!
			//double sl = (b.b-r.b)/(b.a-r.a);
			//solution = new Point(sl,r.b-r.a*sl);
			//or should it be:
			Crossing c = new Crossing(r, b);
			solution = new Point(-c.crAt(),r.eval(c.crAt()));
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
		}
		
		//generate all the crossings:
		crossings = new ArrayList<Crossing>();
		for (int i = 0; i < lBlue.size();i++) {
			for (int j = i+1; j < lBlue.size();j++){
				Crossing c = new Crossing(lBlue.get(i),lBlue.get(j));
				if (inBorders(c)) {
					crossings.add(c);
				}
			}
		}
		for (int i = 0; i < lBlue.size();i++) {
			for (int j = 0; j < lRed.size();j++){
				Crossing c = new Crossing(lBlue.get(i),lRed.get(j));
				if (inBorders(c)) {
					crossings.add(c);
				}
			}
		}
		for (int i = 0; i < lRed.size();i++) {
			for (int j = i+1; j < lRed.size();j++){
				Crossing c = new Crossing(lRed.get(i),lRed.get(j));
				if (inBorders(c)) {
					crossings.add(c);
				}
			}
		}
		
		
		//sort them. crossings implements comparable.
		
		//make stripes with at most alpha*(n choose 2) crossings a piece.
		Collections.sort(crossings);
		Collections.reverse(crossings);
		
		int minband = 0;
		int maxband = 0; //wird �berschrieben.
		int band = 1;
		int bandsize = (int) (crossings.size()*alpha);
		bandsize = Math.max(1, bandsize); 
		System.out.println(crossings.size());
		System.out.println(bandsize);
		for (int i = bandsize; i < crossings.size();i+=bandsize){
			while (crossings.get(i).atInf() && crossings.get(i).atNegInf()) {i++;} // only need for ugly cases, test later
			if (crossings.get(i).atInf() && !crossings.get(i).atNegInf()) {
				while (crossings.get(i).atInf() && !crossings.get(i).atNegInf()) {
					i--;
				}
				borders[band] = crossings.get(i).crAt();
				maxband = band;
				break;
			}
			borders[band] = crossings.get(i).crAt();
			band++;
			maxband = band;
		}
		
		if (DEBUG) {
			for (int i = 0; i<= maxband; i++) {
				System.out.println(borders[i]);
			}
			return;
		}
		//find strip with odd number of intersections by binary search:		
		boolean bluetop = blueTopLeft();
		while ((maxband-minband) > 1) { //TODO i think this needs to be more robust for the non-bounded cases?
			int testband = (maxband-minband)/2;
			int bluetesttop = blueTop(borders[testband]);
			if (bluetop == (bluetesttop==1)) {
				minband = testband;
			}
			else if (bluetop == (bluetesttop==-1)) {
				maxband = testband;
			}
			else if (bluetesttop ==0) { //we have a winner!
				System.out.println("schnittpunkt gefunden!");
				done = true;
				solution = new Point(-borders[testband],levelPos(borders[testband], true, levelBlue));
				return;
			}
		}
		leftb = borders[minband];
		rightb = borders[maxband];
		
		//TODO handle non-bounded case
		int topLvl = levelBlue - (int) (eps*lBlue.size());
		int botLvl = levelBlue - (int) (eps*lBlue.size());
		double tl = levelPos(leftb,true,topLvl);
		double tr = levelPos(rightb,true,topLvl);
		double bl = levelPos(leftb,true,botLvl);
		double br = levelPos(rightb,true,botLvl);
		Trapeze t = new Trapeze(leftb, tl, bl, rightb, tr, br);
		
		//cut away lines, count and make sure levelB/R are correct:
		for (int i = 0; i < lBlue.size(); ++i) {
			int s = t.intersects(lBlue.get(i));
			if (s != 0) {
				if (s > 0) {
					levelBlue --;
				}
				hideLine(lBlue.get(i));
			}
		}
		for (int i = 0; i < lRed.size(); ++i) {
			int s = t.intersects(lRed.get(i));
			if (s != 0) {
				if (s > 0) {
					levelRed --;
				}
				hideLine(lRed.get(i));
			}
		}
		
	}	
	
	public List<VisualPoint> getVisualPoints() {
		List<VisualPoint> result = new ArrayList<VisualPoint>();
		for (Point p : lBlue) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, false);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, false);
				result.add(newPoint);
			}
		}
		
		for (Point p : lRed) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, false);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, false);
				result.add(newPoint);
			}
		}
		
		for (Point p : lBlueDel) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, true);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, true);
				result.add(newPoint);
			}
		}
		
		for (Point p : lRedDel) {
			if (colorSwap) {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.BLUE, true);
				result.add(newPoint);
			} else {
				VisualPoint newPoint = new VisualPoint(p.a, p.b, PointType.RED, true);
				result.add(newPoint);
			}
		}
		
		return result;
	}
}
