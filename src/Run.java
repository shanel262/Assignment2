import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class Run {
	static Timer timer;
	public static void main(String[] args){
		timer = new Timer(100, al);
		timer.start();
		MultiThreadedServerA2 server =  new MultiThreadedServerA2();
	}
	
	static ActionListener al = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Timer");
			ClientA2 client = new ClientA2();
			timer.stop();
		}
	};
}
