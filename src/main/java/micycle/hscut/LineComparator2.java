package micycle.hscut;

import java.util.Comparator;

/**
 * comparator for blueTopleft
 * 
 * @author fabian
 *
 */
class LineComparator2 implements Comparator<PointLineDual> {

	@Override
	public int compare(PointLineDual x, PointLineDual y) {
		if (x.equals(y)) {
			return 0;
		}
		if (x.m == y.m) {
			if (x.i < y.i) {
				return -1;
			} else {
				return 1;
			}
		}
		if (x.m < y.m) {
			return 1;
		} else {
			return -1;
		}
	}

}
