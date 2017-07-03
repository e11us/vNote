package machine;

import app.DeskTopApp;
import robo.*;



public class DriverMain {
	public static void main( String[] Arg ) {
		//
		// init
		_SysConfig.init();
		//
		// start.
		DeskTopApp dt= new DeskTopApp();
		dt.run();
	}
}
