package machine.SQL;

import java.util.ArrayList;
import machine.p;



public class sqlResult {
	public ArrayList <String>	colName	= null;
	public ArrayList <String>	type	= null;
	public ArrayList <Object>	val		= null;
	public String				ID		= machine.Helper.getCurrentTime() + "_"
			+ machine.Helper.rand32AN().substring( 0, 5 );

	public sqlResult() {
		colName= new ArrayList <>();
		type= new ArrayList <>();
		val= new ArrayList <>();
	}

	public void printAll() {
		p.p( this.getClass().toString(), "SQL resutl ID: " + ID );
		int i= 1;
		for( String str : colName ){
			p.p( this.getClass().toString(), "column: " + ( i ) + " name: "
					+ str + " type: " + type.get( i - 1 ) + " value: " + val.get( i - 1 ) );
			i++ ;
		}
	}
}
