package machine;

import java.util.ArrayList;



public class p {
	public static ArrayList <String>	MGD	= new ArrayList <String>();
	public static ArrayList <String>	SRC	= new ArrayList <String>();
	public static ArrayList <String>	CMB	= new ArrayList <String>();

	public static void p( String msg ) {
		System.out.println( msg );
	}

	public static void p( String src, String msg ) {
		SRC.add( src );
		MGD.add( msg );
		CMB.add( "@ " + src.replace( "class", "" ) + " || " + msg );
		System.out.println( "@ " + src.replace( "class", "" ) + " | " + msg );
	}
}
