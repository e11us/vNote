package pin.memory;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import app.DeskTopNote;
import app.DeskTopNoteAr;
import app.PinArrFactory;
import app.popUp;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import machine.Helper;
import machine.p;
import machine.SQL.mySQLportal;
import machine.SQL.sqlResult;
import pin.Pin;



public class _PinMemFactory extends PinArrFactory {
	//
	public static final String		pinTypeName	= PinArrFactory.types[0];
	public static final double[]	memoryFrame	= { 0.3333, 1.0, 2.5, 4.5, 8.5, 20.0, 55.0 };
	//
	private Document				doc			= null;
	private org.w3c.dom.Node		elm			= null;
	private DeskTopNoteAr			board		= null;
	//
	private TreeSet <String>		notesID		= new TreeSet <>();
	private ArrayList <Pin>			due			= new ArrayList <Pin>();
	private ArrayList <Pin>			newCre		= new ArrayList <Pin>();
	private ArrayList <Pin>			dueFutr		= new ArrayList <Pin>();
	private ArrayList <Pin>			dueDone		= new ArrayList <Pin>();
	//
	// test function to add current time.
	public long						testTimeInc	= 0;

	public void incTime() {
		testTimeInc+= 3600 * 1000 * 6;
		reInitCont();
		board.reArrangeBoard();
	}

	public _PinMemFactory( Document doc2, Element elm2, DeskTopNoteAr deskTopNoteAr ) {
		this.doc= doc2;
		this.elm= elm2;
		this.board= deskTopNoteAr;
		init();
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	protected void removeFocus() {
		board.removeFocusOfMe();
	}

	protected void reArrange() {
		board.reArrangeBoard();
	}

	protected void popMsg( String inp ) {
		board.popMsg( inp );
	}

	protected void reInitCont() {
		init();
	}

	protected ArrayList <Pin> getAllChildren() {
		ArrayList <Pin> ret= new ArrayList <>();
		ret.addAll( newCre );
		if( newCre.size() > 0 )
			ret.add( new NewLineSpacer() );
		ret.addAll( due );
		if( due.size() > 0 )
			ret.add( new NewLineSpacer() );
		ret.addAll( dueFutr );
		if( dueFutr.size() > 0 )
			ret.add( new NewLineSpacer() );
		ret.addAll( dueDone );
		if( dueDone.size() > 0 )
			ret.add( new NewLineSpacer() );
		return ret;
	}

	protected ArrayList <MenuItem> getMenus() {
		ArrayList <MenuItem> ret= new ArrayList <MenuItem>();
		//
		MenuItem dicI= new MenuItem( "New Word" );
		dicI.setOnAction( e -> {
			getWordMemInfo();
		} );
		ret.add( dicI );
		//
		MenuItem new2reme= new MenuItem( "New Text Item" );
		new2reme.setOnAction( e -> {
			getTextMemInfo();
		} );
		ret.add( new2reme );
		//
		return ret;
	}

	protected String getNewID() {
		String ret= Helper.randAN( 16 );
		while( notesID.contains( ret ) ){
			ret= Helper.randAN( 16 );
		}
		return ret;
	}

	protected void remove( Pin inp ) {
		if( due.contains( inp ) ){
			due.remove( inp );
			notesID.remove( inp.getID() );
			elm.removeChild( inp.getXMLdataElm() );
			board.remove( inp );
			return;
		}
		if( newCre.contains( inp ) ){
			newCre.remove( inp );
			notesID.remove( inp.getID() );
			elm.removeChild( inp.getXMLdataElm() );
			board.remove( inp );
			return;
		}
		if( dueFutr.contains( inp ) ){
			dueFutr.remove( inp );
			notesID.remove( inp.getID() );
			elm.removeChild( inp.getXMLdataElm() );
			board.remove( inp );
			return;
		}
		if( dueDone.contains( inp ) ){
			dueDone.remove( inp );
			notesID.remove( inp.getID() );
			elm.removeChild( inp.getXMLdataElm() );
			board.remove( inp );
			return;
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void init() {
		due.removeAll( due );
		dueDone.removeAll( dueDone );
		dueFutr.removeAll( dueFutr );
		newCre.removeAll( newCre );
		//
		Node tmp;
		NodeList nl= elm.getChildNodes();
		for( int i= 0; i < nl.getLength(); i++ ){
			tmp= nl.item( i );
			switch( tmp.getNodeName() ){
				case "MemoryNote" :
					MemoryNote mn= new MemoryNote( (Element)tmp, this );
					mn.init();
					//
					if( mn.isDue() ){
						due.add( mn );
						mn.setColor( "ffaa80" );
					}else if( mn.isDone() ){
						dueDone.add( mn );
						mn.setColor( "ffffff" );
					}else{
						dueFutr.add( mn );
						mn.setColor( "80aaff" );
					}
					//
					mn.setNoteGraphic( board.getGridSizeConfig() );
					notesID.add( mn.getID() );
					break;
				default :
					break;
			}
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void getTextMemInfo() {
		int colW= 500;
		int colH= 150;
		popUp pu= new popUp( "New Text Memory Note." );
		VBox comp= new VBox();
		//
		Label lb= new Label( "Key." );
		TextArea ta1= new TextArea();
		ta1.setWrapText( true );
		ta1.setMaxWidth( colW );
		ta1.setMaxHeight( colH );
		ta1.setMinWidth( colW );
		ta1.setMinHeight( colH );
		Label lb2= new Label( "To Remember, Sol." );
		TextArea ta2= new TextArea();
		ta1.setWrapText( true );
		ta1.setMaxWidth( colW );
		ta1.setMaxHeight( colH );
		ta1.setMinWidth( colW );
		ta1.setMinHeight( colH );
		Label lb3= new Label( "Extra Info." );
		TextArea ta3= new TextArea();
		ta1.setWrapText( true );
		ta1.setMaxWidth( colW );
		ta1.setMaxHeight( colH );
		ta1.setMinWidth( colW );
		ta1.setMinHeight( colH );
		//
		Button bt= new Button( "Confirm" );
		bt.setOnAction( e -> {
			pu.close();
			newTextNote( ta1.getText(), ta2.getText(), ta3.getText() );
		} );
		//
		comp.getChildren().addAll( lb, ta1, lb2, ta2, lb3, ta3, bt );
		Scene stageScene= new Scene( comp, colW, colH * 4 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void getWordMemInfo() {
		int colW= 400;
		int colH= 30;
		popUp pu= new popUp( "New Word Memory Note." );
		VBox comp= new VBox();
		//
		Label lb= new Label( "please input a new Word. Press enter to create." );
		TextField ta1= new TextField();
		ta1.setOnKeyPressed( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent arg0 ) {
				if( arg0.getCode() == KeyCode.ENTER ){
					String res= getFromSQL( ta1.getText() );
					if( res != null ){
						newTextNote( ta1.getText(), res, "" );
						pu.close();
					}else{
						lb.setText( "invalid word, please try again" );
					}
				}
			}
		} );
		ta1.setMaxWidth( colW );
		ta1.setMaxHeight( colH );
		ta1.setMinWidth( colW );
		ta1.setMinHeight( colH );
		//
		comp.getChildren().addAll( lb, ta1 );
		Scene stageScene= new Scene( comp, colW, colH * 2.2 );
		pu.set( stageScene, true );
	}

	private String getFromSQL( String word ) {
		ArrayList <String> name= new ArrayList <>();
		ArrayList <String> val= new ArrayList <>();
		name.add( "keyword" );
		val.add( word.toLowerCase() );
		ArrayList <sqlResult> res= mySQLportal.getByFunc( "DictionaryEng", 1, name, val );
		if( res.size() > 0 ){
			ArrayList <String> def= Helper.str2ALstr( (String)res.get( 0 ).val.get( 4 ) );
			ArrayList <String> ret= new ArrayList <String>();
			for( String tmp : def ){
				if( !tmp.startsWith( "regin" ) && !tmp.startsWith( "http" )
						&& !tmp.startsWith( "Sp" ) )
					ret.add( tmp );
			}
			return Helper.ALstr2str( ret );
		}
		return null;
	}

	private void newTextNote( String key, String sol, String extra ) {
		if( key.length() > 0 && sol.length() > 0 ){
			MemoryNote mn= new MemoryNote( this );
			mn.SetXMLData( doc, key, sol, extra );
			mn.init();
			mn.setColor( "80ffaa" );
			mn.setNoteGraphic( board.getGridSizeConfig() );
			//
			newCre.add( mn );
			notesID.add( mn.getID() );
			elm.appendChild( mn.getXMLdataElm() );
			board.storeXMLfil();
			//
			board.reArrangeBoard();
		}
	}
}