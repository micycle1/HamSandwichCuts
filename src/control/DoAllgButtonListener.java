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

public class DoAllgButtonListener implements ActionListener {

	private HamSanAlg h;
	private PointPanel pp;
	private LinePanel lp;
	private JLabel l;
	private HamSanApplet applet;

	public DoAllgButtonListener(HamSanAlg hsa, PointPanel pp, LinePanel lp, JLabel label, HamSanApplet hamSanApplet) {
		this.h = hsa;
		this.pp = pp;
		this.lp = lp;
		this.l = label;
		this.applet = hamSanApplet;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		doStuff();
	}

	public void doStuff() {
		if (!h.done) {
			if (h.lBlue.size() == 0 && h.lRed.size() == 0) {
				return;
			} else {
				applet.setPlacingEnabled(false);
				applet.setStepsEnabled(false);
			}
			while (!h.done) {
				h.doAlg();
			}
			if (!h.validSol(false)) {
				l.setText("uh oh");
			} else {
				l.setText("found valid solution");
			}
			pp.setAddingAllowed(false);
			List<VisualPoint> vpoints = h.getVisualPoints();
			pp.setVisualPoints(vpoints);
			lp.setVisualPoints(vpoints);
			pp.revalidate();
			pp.repaint();
			lp.revalidate();
			lp.repaint();
		}
	}

}
