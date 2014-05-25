package hamSanApp;

	/**
	 * diese Klasse stellt ein Trapez dar, in dem sich die blaue medianlinie befindet
	 * @author fabian
	 *
	 */
public class Trapeze { // TODO was tun, wenn das trapez in einem unbegrenzten intervall ist?
	/**
	 * Konstruktor, alles ganz selbsterkl�rend
	 */
	Trapeze(double x1, double y_topleft, double y_botleft,double x2, double y_topright, double y_botright) {
		left = x1;
		right = x2;
		topleft = y_topleft;
		topright = y_topright;
		botleft = y_botleft;
		botright = y_botright;
	}
	double left; 	//linker Rand
	double right;	//rechter Rand
	double topleft; //
	double topright;//
	double botleft; //
	double botright;// die vier y-Werte zur Beschr�nkung
	
	/**
	 * Testet, ob eine Linie das Trapez schneidet
	 * @param i die zu testende Linie
	 * @return true g.d.w schneidet
	 */
	public boolean intersects(Point i) { //TODO: testen
		//TODO: vielleicht auch hier testen und zur�ckgeben, ob obendr�ber oder untendrunter vorbei geht?
		double y1 = i.eval(left);
		double y2 = i.eval(right);
		if (((y1 < botleft) && ( y2 < botright)) || ((y1 > topleft) && (y2 > topright)))  {
			return false;
		}
		else {
			return true;
		}
	}
}
