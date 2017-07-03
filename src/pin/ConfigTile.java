package pin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import app.DeskTopNote;
import pin.note._PinNoteFactory;



public abstract class ConfigTile extends Pin {
	protected Element		dat		= null;
	protected String		name	= null;
	// 
	protected DeskTopNote	board	= null;
	// loc tmp copy.
	private int				gx, gy;

	/*-----------------------------------------------------------------------------------------
	 * constructor without data.( need to use createXMLdataElm after )
	 */
	public ConfigTile( int x, int y, DeskTopNote bd, String name ) {
		gx= x;
		gy= y;
		board= bd;
		this.name= name;
	}

	@Override
	public void createXMLdataElm( Document doc ) {
		dat= doc.createElement( name );
		dat.setAttribute( "GridLocationX", gx + "" );
		dat.setAttribute( "GridLocationY", gy + "" );
		dat.setAttribute( "NoteStyle", ( Math.random() * 10000 + "" ).substring( 0, 5 ) );
		dat.setAttribute( "ColorBackGround1", "aaaaaa" );
		init();
	}
}
