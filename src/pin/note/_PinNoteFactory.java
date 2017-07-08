package pin.note;

import java.util.ArrayList;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import app.DeskTopNote;
import javafx.scene.Node;
import machine.Helper;
import machine.p;
import pin.Pin;
import pin.PinCopyable;



public class _PinNoteFactory {
	//
	public static final String			pinTypeName		= "PinNote";
	public static final String[]		NoteTypes		= {
			"StickyNote", "WebNote", "EnhancedNote", "GraphicNote", "PinNoteStyle"
	};
	public static final String[]		NoteLinkType	= {
			"webLink", "fileLink", "BoardLink", "null"
	};
	public static String[]				NoteFontType	= null;
	public static Integer[]				NoteFontSize	= null;
	private static final int			NoteFontSizeMin	= 5;
	private static final int			NoteFontSizeMax	= 155;
	private Document					doc				= null;
	private org.w3c.dom.Node			elm				= null;
	private DeskTopNote					board			= null;
	private ArrayList <Pin>				notes			= new ArrayList <>();
	private TreeSet <String>			notesID			= new TreeSet <>();
	private ArrayList <PinNoteStyle>	sty				= new ArrayList <>();
	//
	private PinNoteStyle				styleApplyer	= null;

	/*-----------------------------------------------------------------------------------------
	 * create the pinNote elm to hold all pin notes and note styles.
	 */
	public static Element createElm( Document doc ) {
		// create elm to hold all.
		Element ret= doc.createElement( pinTypeName );
		return ret;
	}

	/*-----------------------------------------------------------------------------------------
	 * create node for the scene from the given elm.
	 */
	public _PinNoteFactory( Document doc, org.w3c.dom.Node node, DeskTopNote board ) {
		p.p( this.getClass().toString(), "Constructor running." );
		if( NoteFontSize == null ){
			NoteFontSize= new Integer[NoteFontSizeMax - NoteFontSizeMin + 1];
			for( int i= NoteFontSizeMin; i <= NoteFontSizeMax; i++ )
				NoteFontSize[i - NoteFontSizeMin]= i;
		}
		if( NoteFontType == null ){
			NoteFontType= (String[])javafx.scene.text.Font.getFamilies().toArray();
		}
		//
		this.elm= node;
		this.doc= doc;
		this.board= board;
		//
		NodeList nl= node.getChildNodes();
		int[] gc= board.getGridSizeConfig();
		for( int i= 0; i < nl.getLength(); i++ ){
			switch( nl.item( i ).getNodeName() ){
				case "StickyNote" :
					StickyNote sn= new StickyNote( nl.item( i ), board, this );
					sn.setNoteGraphic( board.getGridSizeConfig() );
					notesID.add( sn.getID() );
					notes.add( sn );
					break;
				case "PinNoteStyle" :
					PinNoteStyle pns= new PinNoteStyle( nl.item( i ), board, this );
					pns.setNoteGraphic( board.getGridSizeConfig() );
					notes.add( pns );
					break;
				case "WebNote" :
					WebNote wn= new WebNote( nl.item( i ), board, this );
					wn.setNoteGraphic( board.getGridSizeConfig() );
					notesID.add( wn.getID() );
					notes.add( wn );
					break;
			}
		}
		//
		p.p( this.getClass().toString(), "Constructor finished." );
	}

	/*-----------------------------------------------------------------------------------------
	 * create a new note with given type.
	 */
	public Pin createNewNote( String typ, int x, int y ) {
		int[] gc= board.getGridSizeConfig();
		switch( typ ){
			case "StickyNote" :
				StickyNote sn= new StickyNote( location2GridX( x ),
						location2GridY( y ), board, this );
				sn.createXMLdataElm( doc );
				sn.setNoteGraphic( board.getGridSizeConfig() );
				notesID.add( sn.getID() );
				notes.add( sn );
				elm.appendChild( sn.getXMLdataElm() );
				return sn;
			case "WebNote" :
				WebNote wn= new WebNote( location2GridX( x ),
						location2GridY( y ), board, this );
				wn.createXMLdataElm( doc );
				wn.setNoteGraphic( board.getGridSizeConfig() );
				notesID.add( wn.getID() );
				notes.add( wn );
				elm.appendChild( wn.getXMLdataElm() );
				return wn;
			case "EnhancedNote" :
				return null;
			case "GraphicNote" :
				return null;
			case "PinNoteStyle" :
				PinNoteStyle pns= new PinNoteStyle( location2GridX( x ),
						location2GridY( y ), board, this );
				pns.createXMLdataElm( doc );
				pns.setNoteGraphic( board.getGridSizeConfig() );
				notes.add( pns );
				elm.appendChild( pns.getXMLdataElm() );
				return pns;
		}
		return null;
	}

	/*-----------------------------------------------------------------------------------------
	 * create new notes with given notes ( duplicate operation )
	 */
	public Pin createNewNote( PinCopyable pin, int x, int y ) {
		if( ! ( pin instanceof PinNoteInterface ) )
			return null;
		// get the right type to create new note. then set the data by copy.
		Pin ret= createNewNote( ( (PinNoteInterface)pin ).getTypeName(), x, y );
		if( ! ( (PinCopyable)ret ).setXMLDatForDup( pin.getXMLDatForDup() ) ){
			// if its good copy.
			( (PinCopyable)ret ).deleteAfterCut();
			return null;
		}
		return ret;
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	public ArrayList <Pin> getAllNodes() {
		return notes;
	}

	public String getNewID() {
		String ret= Helper.randAN( 16 );
		while( notesID.contains( ret ) ){
			ret= Helper.randAN( 16 );
		}
		return ret;
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	protected void remove( Pin inp ) {
		if( notesID.contains( inp.getID() ) ){
			notesID.remove( inp.getID() );
		}
		if( notes.contains( inp ) ){
			notes.remove( inp );
			elm.removeChild( inp.getXMLdataElm() );
		}
	}

	protected void applyChangedStyle( PinNoteStyle sty ) {
		String tag= sty.getTagName();
		for( Pin tmp : notes ){
			if( tmp instanceof PinNoteInterface ){
				if( ( (Element)tmp.getXMLdataElm() ).getAttribute( "NoteStyle" )
						.equals( tag ) ){
					( (PinNoteInterface)tmp ).setStyle( (Element)sty.getXMLdataElm() );
				}
			}
		}
		//
		board.storeXMLfil();
	}

	protected void setStyleApplyer( PinNoteStyle sty ) {
		styleApplyer= sty;
	}

	protected PinNoteStyle getStyleApplyer() {
		return styleApplyer;
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	protected int location2GridX( int x ) {
		int[] gc= board.getGridSizeConfig();
		return (int)Math.ceil( (double)x / ( gc[0] + gc[2] ) );
	}

	protected int location2GridY( int y ) {
		int[] gc= board.getGridSizeConfig();
		return (int)Math.ceil( (double)y / ( gc[1] + gc[3] ) );
	}
}
