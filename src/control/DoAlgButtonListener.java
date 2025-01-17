package control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JLabel;

import hamSanApp.HamSanAlg;
import view.HamSanApplet;
import view.LinePanel;
import view.PointPanel;
import view.VisualPoint;

public class DoAlgButtonListener implements ActionListener {

	private HamSanAlg hsa;
	private PointPanel pp;
	private LinePanel lp;
	private JLabel l;
	private HamSanApplet applet;

	public DoAlgButtonListener(HamSanAlg h, PointPanel pp, LinePanel lp, JLabel label, HamSanApplet hamSanApplet) {
		this.hsa = h;
		this.pp = pp;
		this.lp = lp;
		this.l = label;
		this.applet = hamSanApplet;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		doStuff();
	}

	public void doStuff() {
		if (!hsa.done) {
			hsa.doAlg();
			int step = hsa.step;
			switch (step) {
				case 1 :
					l.setText("step 1: divided in intervals");
					break;
				case 2 :
					l.setText("step 2: found interval with odd intersection property");
					break;
				case 3 :
					l.setText("step 3: constructed trapeze");
					break;
				case 4 :
					l.setText("zoomed in on trapeze");
					lp.followTrapeze();
				case 0 :
					l.setText("step 4: removed lines outside the trapeze");
					break;
			}
			if (hsa.getVisualPoints().size() == 0) {
				l.setText("step 0: place points");
			} else {
				applet.setPlacingEnabled(false);
			}
			if (hsa.done) {
				applet.setStepsEnabled(false);
				if (hsa.validSol(false)) {
					l.setText("found valid solution!");
				} else {
					l.setText("found invalid solution");
				}
			}
			pp.setAddingAllowed(false);
			List<VisualPoint> vpoints = hsa.getVisualPoints();
			pp.setVisualPoints(vpoints);
			lp.setVisualPoints(vpoints);
			pp.revalidate();
			pp.repaint();
			lp.revalidate();
			lp.repaint();
		}
	}

}
