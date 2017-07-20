package pin.memory;

import java.util.ArrayList;
import java.util.Scanner;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import app.DeskTopNote;
import app.DeskTopNoteAr;
import app.popUp;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import machine.Helper;
import machine.p;
import pin.Pin;
import pin.boarder.PinBorderInterface;
import pin.boarder._PinBorderFactory;
import pin.note._PinNoteFactory;



public class MemoryNote extends Pin {
	protected static final String	name	= "MemoryNote";
	private MemoryNote				handler	= this;
	//
	private HBox					hb		= null;
	private TextArea				ta		= null;
	private PinBorderInterface		bd		= null;
	//
	private Element					dat		= null;
	private _PinMemFactory			fac		= null;
	// loc tmp copy.
	private int						width, height;
	private String					Cbd		= null;
	//
	protected boolean				ready	= false;

	/*-----------------------------------------------------------------------------------------
	 * constructor without data.( need to use createXMLdataElm after )
	 */
	public MemoryNote( _PinMemFactory fc ) {
		this.fac= fc;
	}

	public void SetXMLData( Document doc, String k, String s, String e ) {
		dat= doc.createElement( name );
		dat.setAttribute( "ID", fac.getNewID() );
		dat.setAttribute( "BoarderType", "default" );
		dat.setAttribute( "Key", k );
		dat.setAttribute( "Sol", s );
		dat.setAttribute( "Extra", e );
		// review dates.
		dat.setAttribute( "DateRem", Helper.getCurrentTimeStamp().replace( '_', ' ' ) );
		// time separation.
		dat.setAttribute( "LastRememberFrame", _PinMemFactory.memoryFrame[0] + "" );
		dat.setAttribute( "LastRememberFrameInd", "0" );
	}

	/*-----------------------------------------------------------------------------------------
	 * constructor with data. 
	 */
	public MemoryNote( Element node, _PinMemFactory fc ) {
		this.dat= node;
		this.fac= fc;
	}

	public void init() {
		hb= new HBox();
		ta= new TextArea( dat.getAttribute( "Key" ) );
		ta.setEditable( false );
		//
		ta.setOnMouseEntered( e -> {
			ta.requestFocus();
			String ex= dat.getAttribute( "Extra" );
			if( ex.length() > 0 ){
				ta.setText( dat.getAttribute( "Key" ) + "\n---\n"
						+ dat.getAttribute( "Sol" ) + "\n---\n" +
						dat.getAttribute( "Extra" ) );
			}else{
				ta.setText( dat.getAttribute( "Key" ) + "\n---\n"
						+ dat.getAttribute( "Sol" ) );
			}
		} );
		ta.setOnMouseExited( e -> {
			ta.setText( dat.getAttribute( "Key" ) );
			fac.removeFocus();
		} );
		ta.setOnMouseClicked( e -> {
			switch( e.getButton() ){
				case PRIMARY :
					if( e.getClickCount() == 2 ){
						getNewCont();
					}
					break;
				case SECONDARY :
					break;
				default :
			}
		} );
		//
		ta.setOnKeyTyped( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				switch( (int)event.getCharacter().charAt( 0 ) ){
					case 27 :
						// esc
						incRemFrame();
						return;
					case 8 :
						// back space.
						deleteThis();
						return;
					default :
				}
				switch( event.getCharacter() ){
					case " " :
						decRemFrame();
						return;
					case "r" :
					case "R" :
						DateTime dt= new DateTime();
						dt= dt.plus( fac.testTimeInc );
						fac.popMsg(
								dt.toString() + "\n" +
										"Remember Time Frame: " +
										dat.getAttribute( "LastRememberFrame" ) +
										"\n---\nReview Dates: \n" +
										dat.getAttribute( "DateRem" ) );
					default :
				}
			}
		} );
	}

	/*-----------------------------------------------------------------------------------------
	 * getter. 
	 */
	@Override
	public String getID() {
		return dat.getAttribute( "ID" );
	}

	@Override
	public Element getXMLdataElm() {
		return dat;
	}

	/*-----------------------------------------------------------------------------------------
	 * set graphic. 
	 */
	protected void setNoteGraphic( int[] gc ) {
		// set the size for the note.
		width= gc[0];
		height= gc[1];
		this.setMinHeight( height );
		this.setMaxHeight( height );
		this.setMinWidth( width );
		this.setMaxWidth( width );
		if( bd == null )
			bd= _PinBorderFactory.getBoarder( dat.getAttribute( "BoarderType" ) );
		bd.set( width, height, 4, Cbd, Cbd );
		//
		ta.setWrapText( true );
		ta.setMaxWidth( width - bd.getLeftOS() - bd.getRightOS() );
		ta.setMaxHeight( height - bd.getTopOS() - bd.getButtomOS() );
		ta.setMinWidth( width - bd.getLeftOS() - bd.getRightOS() );
		ta.setMinHeight( height - bd.getTopOS() - bd.getButtomOS() );
		ta.setTranslateX( bd.getLeftOS() );
		ta.setTranslateY( bd.getTopOS() );
		//
		this.getChildren().add( ta );
		this.getChildren().add( bd.getNodes() );
	}

	protected boolean isDue() {
		ArrayList <String> stral= Helper.str2ALstr( dat.getAttribute( "DateRem" ) );
		String str= stral.get( stral.size() - 1 );
		Scanner rdr= new Scanner( str );
		DateTime dt= new DateTime( rdr.nextInt(), rdr.nextInt(), rdr.nextInt(),
				rdr.nextInt(), rdr.nextInt(), rdr.nextInt(), 0 );
		dt= dt.plus( (long) ( Double.parseDouble(
				dat.getAttribute( "LastRememberFrame" ) )
				* 24 * 3600 * 1000 ) );
		if( ( new DateTime() ).getMillis() + fac.testTimeInc >= dt.getMillis() )
			return true;
		else return false;
	}

	protected boolean isDone() {
		if( Integer.parseInt( dat.getAttribute( "LastRememberFrameInd" ) ) >= _PinMemFactory.memoryFrame.length )
			return true;
		else return false;
	}

	protected void incRemFrame() {
		dat.setAttribute( "DateRem", dat.getAttribute( "DateRem" ) + "\n" +
				Helper.getCurrentTimeStamp().replace( '_', ' ' ) );
		int ind= Integer.parseInt( dat.getAttribute( "LastRememberFrameInd" ) );
		ind++ ;
		//
		if( ind <= _PinMemFactory.memoryFrame.length ){
			dat.setAttribute( "LastRememberFrameInd", ind + "" );
		}
		if( ind < _PinMemFactory.memoryFrame.length ){
			dat.setAttribute( "LastRememberFrame", _PinMemFactory.memoryFrame[ind] + "" );
		}
		fac.reInitCont();
		fac.reArrange();
	}

	protected void decRemFrame() {
		dat.setAttribute( "DateRem", dat.getAttribute( "DateRem" ) + "\n" +
				Helper.getCurrentTimeStamp().replace( '_', ' ' ) );
		int ind= Integer.parseInt( dat.getAttribute( "LastRememberFrameInd" ) );
		ind-- ;
		//
		if( ind < 0 )
			ind= 0;
		dat.setAttribute( "LastRememberFrameInd", ind + "" );
		dat.setAttribute( "LastRememberFrame", _PinMemFactory.memoryFrame[ind] + "" );
		fac.reInitCont();
		fac.reArrange();
	}

	protected void setColor( String inp ) {
		Cbd= inp;
		//ta.setStyle( "-fx-control-inner-background: " + inp );
	}

	private void getNewCont() {
		int colW= 500;
		int colH= 150;
		popUp pu= new popUp( "New Text Memory Note." );
		VBox comp= new VBox();
		//
		Label lb= new Label( "Key." );
		TextArea ta1= new TextArea( dat.getAttribute( "Key" ) );
		ta1.setWrapText( true );
		ta1.setMaxWidth( colW );
		ta1.setMaxHeight( colH );
		ta1.setMinWidth( colW );
		ta1.setMinHeight( colH );
		Label lb2= new Label( "To Remember, Sol." );
		TextArea ta2= new TextArea( dat.getAttribute( "Sol" ) );
		ta1.setWrapText( true );
		ta1.setMaxWidth( colW );
		ta1.setMaxHeight( colH );
		ta1.setMinWidth( colW );
		ta1.setMinHeight( colH );
		Label lb3= new Label( "Extra Info." );
		TextArea ta3= new TextArea( dat.getAttribute( "Extra" ) );
		ta1.setWrapText( true );
		ta1.setMaxWidth( colW );
		ta1.setMaxHeight( colH );
		ta1.setMinWidth( colW );
		ta1.setMinHeight( colH );
		//
		Button bt= new Button( "Confirm" );
		bt.setOnAction( e -> {
			pu.close();
			changeText( ta1.getText(), ta2.getText(), ta3.getText() );
		} );
		//
		comp.getChildren().addAll( lb, ta1, lb2, ta2, lb3, ta3, bt );
		Scene stageScene= new Scene( comp, colW, colH * 4 );
		pu.set( stageScene, true );
	}

	private void changeText( String key, String sol, String ex ) {
		if( key.length() > 0 && sol.length() > 0 ){
			dat.setAttribute( "Key", key );
			dat.setAttribute( "Sol", sol );
			dat.setAttribute( "Extra", ex );
			ta.setText( key );
		}
	}

	private void deleteThis() {
		fac.remove( this );
		fac.reArrange();
	}
}
