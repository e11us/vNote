package machine;

public class p {
	public static void p( String msg ) {
		System.out.println( msg );
	}

	public static void p( String src, String msg ) {
		System.out.println( "@" + src.replace( "class", "" ) + " | " + msg );
	}
}
