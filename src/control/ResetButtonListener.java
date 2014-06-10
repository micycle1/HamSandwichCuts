package control;

import hamSanApp.HamSanAlg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import view.HamSanApplet;
import view.LinePanel;
import view.PointPanel;
import view.PointType;

public class ResetButtonListener implements ActionListener {

	private HamSanAlg hsa;
	private PointPanel pp;
	private LinePanel lp;
	private JLabel l;
	private HamSanApplet applet;
	
	public ResetButtonListener(HamSanAlg hsa, PointPanel pp, LinePanel lp, JLabel label, HamSanApplet hamSanApplet) {
		this.hsa = hsa;
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
		applet.setPlacingEnabled(true);
		applet.setStepsEnabled(true);
		hsa.init();
		l.setText("step 0: place points");
		pp.setAddingAllowed(true);
		pp.setVisualPoints(hsa.getVisualPoints());
		lp.setMinAndMax(-10, -10, 10, 10);
		pp.refreshAll();
		pp.setCurrentType(PointType.BLUE);
	}
}
