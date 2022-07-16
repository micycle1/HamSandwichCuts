package micycle.hscut;

import static org.junit.jupiter.api.Assertions.*;

import java.util.SplittableRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class HSCutTests {

	@BeforeEach
	void before(TestInfo testInfo) {
		System.out.println(testInfo.getDisplayName() + " log:");
	}

	@AfterEach
	void after() {
		System.out.println("");
	}

	@Test
	void testSimpleLineEquation() {
		HamSandwichCutter h = new HamSandwichCutter();
		h.addPoint(5, 1, false);
		h.addPoint(-5, 1, true);

		h.process();

		assertTrue(h.isValid());
		assertEquals(0.0, h.solution.m + 0.0); // add 0.0 to handle signed zero
		assertEquals(1, h.solution.b);

		h = new HamSandwichCutter();
		h.addPoint(1, 1, false);
		h.addPoint(-1, -1, true);

		h.process();

		assertTrue(h.isValid());
		assertEquals(1, h.solution.m);
		assertEquals(0, h.solution.b);
	}

	@Test
	void testVerticalSolution() {
		double x = 7;
		HamSandwichCutter h = new HamSandwichCutter();
		h.addPoint(x, 5, false);
		h.addPoint(x, 6, false);
		h.addPoint(x, -5, true);
		h.addPoint(x, -6, true);

		h.addPoint(5, x, false);
		h.addPoint(-5, x, true);

		h.process();

		assertTrue(h.isValid());
		assertTrue(h.verticalSol);
		assertEquals(x, h.verticalSolPos); // line x = x'
	}

	/**
	 * The solution to this point set looks wrong on first glance because there are
	 * no points on one side of the solution at all! It doesn’t quite look like what
	 * we would expect of m “halving”. But it is still correct. Points on the line
	 * are counted to neither side, and not more than half the points of any color
	 * are on m side of the line.
	 */
	@Test
	void testOneSided() {
		HamSandwichCutter h = new HamSandwichCutter();

		for (int i = -9; i < -4; i += 2) {
			h.addPoint(i, i, true);
		}
		h.addPoint(-4, -4, false);
		for (int i = -3; i < 1; i += 2) {
			h.addPoint(i, i, true);
		}
		for (int i = 1; i < 5; i += 2) {
			h.addPoint(i, i, false);
		}
		h.addPoint(5, 5, true);
		for (int i = 6; i < 10; i += 2) {
			h.addPoint(i, i, false);
		}
		h.addPoint(6, 7, false);
		h.addPoint(4, 8, false);
		h.addPoint(3, 5, false);
		h.addPoint(-3, -1, true);
		h.addPoint(-8, -4, true);

		h.process();

		assertTrue(h.isValid());
	}

	@Test
	void testManyRandom() {
		SplittableRandom r = new SplittableRandom(1337);
		HamSandwichCutter h = new HamSandwichCutter();

		for (int i = 0; i < 500; i++) {
			h.addPoint(r.nextDouble(-1, 1), r.nextDouble(-1, 1), false);
			h.addPoint(r.nextDouble(-1, 1), r.nextDouble(-1, 1), true);
		}

		h.process();

		assertTrue(h.isValid());
	}

	@Test
	void testUnevenRandom() {
		SplittableRandom r = new SplittableRandom(13337);
		HamSandwichCutter h = new HamSandwichCutter();

		for (int i = 0; i < 500; i++) { // 500 red
			h.addPoint(r.nextDouble(-1, 1), r.nextDouble(-1, 1), false);
		}

		for (int i = 0; i < 50; i++) { // 50 blue
			h.addPoint(r.nextDouble(-1, 1), r.nextDouble(-1, 1), true);
		}

		h.process();

		assertTrue(h.isValid());
	}

	@Test
	void testOneColor() {
		HamSandwichCutter h = new HamSandwichCutter();

		for (int i = 0; i < 500; i++) { // red
			h.addPoint(Math.random(), Math.random(), false);
		}

		h.process();

		assertTrue(h.isValid());

		h = new HamSandwichCutter();

		for (int i = 0; i < 500; i++) { // blue
			h.addPoint(Math.random(), Math.random(), true);
		}

		h.process();

		assertTrue(h.isValid());
	}

}
