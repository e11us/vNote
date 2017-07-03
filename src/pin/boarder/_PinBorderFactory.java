package pin.boarder;

public class _PinBorderFactory {
	public static final String[] BoarderType= {
			"sifi1", "sifi2", "default"
	};

	public static PinBorder getBoarder( String name ) {
		switch( name ){
			case "sifi1" :
				return new SifiBorder1();
			case "sifi2" :
				return new SifiBorder2();
			case "default" :
			default :
				return new SimpleBorder();
		}
	}
}
