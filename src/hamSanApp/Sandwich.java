package hamSanApp;

//import javax.swing.*;

public class Sandwich {
	
	/*
	private static void makeGui() {
		JFrame frame = new JFrame("draw thingie");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("hiya!");
        frame.getContentPane().add(label);
        
        frame.pack();
        frame.setVisible(true);
        
	}*/

	public static void main(String[] args) {
		Point i = new Point(1.0d,-4.0d);
		Point j = new Point(0.0d,-4.0d);
		Point k = new Point(0.0d,0.0d);
		int res = Point.op1naive(i, j, k);
		System.out.println(res);
		System.out.println(i.ell);
	}

}
;