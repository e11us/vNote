package robo;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;



// use this may make sys init slow. 
public class SysInfo {
	static Sigar sig= null;

	public static double getUsageR() {
		if( sig == null )
			init();
		try{
			return sig.getCpuPerc().getCombined();
		}catch ( SigarException e ){
			e.printStackTrace();
		}
		return 0;
	}

	public static double getUsed() {
		if( sig == null )
			init();
		try{
			return sig.getMem().getUsedPercent();
		}catch ( SigarException e ){
			e.printStackTrace();
		}
		return 0;
	}

	public static long getTotal() {
		if( sig == null )
			init();
		try{
			return sig.getMem().getTotal();
		}catch ( SigarException e ){
			e.printStackTrace();
		}
		return 0;
	}

	private static void init() {
		sig= new Sigar();
	}
}
