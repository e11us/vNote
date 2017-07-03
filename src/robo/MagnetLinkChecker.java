package robo;

import java.util.ArrayList;
import machine.Helper;



public class MagnetLinkChecker implements _SystemClockLis {
	private static String				pre		= "magnet:?xt=urn:btih:";
	private static boolean				running	= false;
	private static ArrayList <String>	pasturl	= new ArrayList <>();

	public static MagnetLinkChecker _init() {
		return new MagnetLinkChecker();
	}

	public static boolean getStatus() {
		return running;
	}

	public static void startChecker() {
		running= true;
	}

	public static void stopChecker() {
		running= false;
	}

	@Override
	public void doTask() {
		String inp= ClipBoard.getClipBoard();
		if( inp != null && inp.length() == 40 && Helper.isAlphNum( inp )
				&& !pasturl.contains( inp ) ){
			ClipBoard.setClipBoard( pre + inp );
			pasturl.add( inp );
		}
	}

	private MagnetLinkChecker() {}
}
