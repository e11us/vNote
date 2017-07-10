package robo;

import java.util.ArrayList;
import machine.p;



public class _SystemClock {
	//
	// add a clock.
	public static void addClock( _SystemClockLis obj, int ms ) {
		objs.add( new _SystemClock( obj, ms ) );
	}

	// stop a clock.
	public static void stopClock( _SystemClockLis obj ) {
		_SystemClock found= null;
		for( _SystemClock tmp : objs ){
			if( tmp.objI == obj ){
				tmp._run= false;
				found= tmp;
				break;
			}
		}
		if( found != null ){
			objs.remove( found );
			found= null;
		}
		return;
	}
	//
	//----------------------------------------------------------------------------------------------------

	//static manager.
	private static ArrayList <_SystemClock>	objs= new ArrayList <>();
	// individual clocker.
	private _SystemClockLis					objI= null;
	private int								ms	= 1;
	private boolean							_run= false;
	private Thread							td	= new Thread() {
													@Override
													public void run() {
														while( objI != null && _run ){
															objI.doTask();
															try{
																this.sleep( ms );
															}catch ( InterruptedException e ){
																e.printStackTrace();
															}
														}
														//
														end();
													}
												};

	private _SystemClock( _SystemClockLis obj, int ms ) {
		this.objI= obj;
		this.ms= ms;
		this._run= true;
		td.start();
	}

	private void end() {
		td= null;
		ms= 1;
		objI= null;
	}
}
