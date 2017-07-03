package pin.note;

import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import app.DeskTopNote;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import machine.Helper;
import machine.p;
import pin.Pin;
import pin.PinInterface;
import pin.boarder.PinBorder;
import pin.boarder._PinBorderFactory;
import robo.ClipBoard;



/* -------------------------------------------------------------------------------------------
 * 
 * -------------------------------------------------------------------------------------------
 */
public class StickyNote extends Pin implements PinNoteInterface {
	private static final String	name			= "StickyNote";
	private static final String	defMsg			= "New Sticky Note.";
	//
	private StickyNote			handler			= this;
	private DeskTopNote			board			= null;
	private _PinNoteFactory		PNF				= null;
	private PinBorder			bd				= null;
	//
	private Element				dat				= null;
	//
	private Label				lb				= null;
	private TextArea			ta				= null;
	private HBox				hb				= null;
	private Group				bdNodes			= null;
	//
	private boolean				BasefocusOn		= false;
	private boolean				TAfocusOn		= false;
	private boolean				dragWasOn		= false;
	private boolean				cutNoteSignal	= false;
	// loc tmp copy.
	private int					gx, gy, width, height;

	/*-----------------------------------------------------------------------------------------
	 * constructor without data.( need to use createXMLdataElm after )
	 */
	public StickyNote( int x, int y, DeskTopNote bd, _PinNoteFactory fc ) {
		gx= x;
		gy= y;
		board= bd;
		this.PNF= fc;
	}

	/*-----------------------------------------------------------------------------------------
	 * create a new blank sticky note.
	 */
	@Override
	public void createXMLdataElm( Document doc ) {
		dat= doc.createElement( name );
		dat.setAttribute( "GridLocationX", gx + "" );
		dat.setAttribute( "GridLocationY", gy + "" );
		dat.setAttribute( "GridSizeX", "1" );
		dat.setAttribute( "GridSizeY", "1" );
		dat.setAttribute( "NoteStyle", "default" );
		dat.setAttribute( "Note", defMsg );
		//
		dat.setAttribute( "ColorBackGround1", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorBackGround2", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorBoarder", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorTitle", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorText", "000000" );
		dat.setAttribute( "ColorShade", Helper.rand2colorHighStr() );
		//
		dat.setAttribute( "ColorHighLightBackGround1", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorHighLightBackGround2", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorHighLightBoarder", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorHighLightTitle", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorHighLightText", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorHighLightShade", Helper.rand2colorHighStr() );
		//
		dat.setAttribute( "BoarderType", "default" );
		dat.setAttribute( "BoarderThick", "2" );
		dat.setAttribute( "FontSize", "15" );
		dat.setAttribute( "FontType", "Arial" );
		dat.setAttribute( "Link", "null" );
		dat.setAttribute( "ID", PNF.getNewID() );
		init();
	}

	/*-----------------------------------------------------------------------------------------
	 * constructor with data.
	 */
	public StickyNote( org.w3c.dom.Node node, DeskTopNote bd, _PinNoteFactory fc ) {
		dat= (Element)node;
		board= bd;
		this.PNF= fc;
		gx= Integer.parseInt( dat.getAttribute( "GridLocationX" ) );
		gy= Integer.parseInt( dat.getAttribute( "GridLocationY" ) );
		init();
	}

	/*-----------------------------------------------------------------------------------------
	 * duplicate this note.
	 */
	public Pin duplicate( Document doc, int x, int y,
			DeskTopNote bd, _PinNoteFactory fc ) {
		StickyNote ret= new StickyNote( x, y, bd, fc );
		//
		ret.dat= doc.createElement( name );
		ret.dat.setAttribute( "GridLocationX", x + "" );
		ret.dat.setAttribute( "GridLocationY", y + "" );
		ret.dat.setAttribute( "GridSizeX", dat.getAttribute( "GridSizeX" ) );
		ret.dat.setAttribute( "GridSizeY", dat.getAttribute( "GridSizeY" ) );
		ret.dat.setAttribute( "NoteStyle", dat.getAttribute( "NoteStyle" ) );
		ret.dat.setAttribute( "Note", dat.getAttribute( "Note" ) );
		//
		ret.dat.setAttribute( "ColorBackGround1", dat.getAttribute( "ColorBackGround1" ) );
		ret.dat.setAttribute( "ColorBackGround2", dat.getAttribute( "ColorBackGround2" ) );
		ret.dat.setAttribute( "ColorBoarder", dat.getAttribute( "ColorBoarder" ) );
		ret.dat.setAttribute( "ColorTitle", dat.getAttribute( "ColorTitle" ) );
		ret.dat.setAttribute( "ColorText", dat.getAttribute( "ColorText" ) );
		ret.dat.setAttribute( "ColorShade", dat.getAttribute( "ColorShade" ) );
		//
		ret.dat.setAttribute( "ColorHighLightBackGround1", dat.getAttribute( "ColorHighLightBackGround1" ) );
		ret.dat.setAttribute( "ColorHighLightBackGround2", dat.getAttribute( "ColorHighLightBackGround2" ) );
		ret.dat.setAttribute( "ColorHighLightBoarder", dat.getAttribute( "ColorHighLightBoarder" ) );
		ret.dat.setAttribute( "ColorHighLightTitle", dat.getAttribute( "ColorHighLightTitle" ) );
		ret.dat.setAttribute( "ColorHighLightText", dat.getAttribute( "ColorHighLightText" ) );
		ret.dat.setAttribute( "ColorHighLightShade", dat.getAttribute( "ColorHighLightShade" ) );
		//
		ret.dat.setAttribute( "BoarderType", dat.getAttribute( "BoarderType" ) );
		ret.dat.setAttribute( "BoarderThick", dat.getAttribute( "BoarderThick" ) );
		ret.dat.setAttribute( "FontSize", dat.getAttribute( "FontSize" ) );
		ret.dat.setAttribute( "FontType", dat.getAttribute( "FontType" ) );
		ret.dat.setAttribute( "Link", dat.getAttribute( "Link" ) );
		ret.dat.setAttribute( "ID", fc.getNewID() );
		//
		ret.init();
		ret.setNoteGraphic( bd.getGridSizeConfig() );
		// 
		if( cutNoteSignal ){
			// remove if cut note. same as back space.
			PNF.remove( handler );
			board.remove( handler );
			if( dat.getAttribute( "Link" ).startsWith( "board" ) ){
				new File( dat.getAttribute( "Link" ).replaceFirst( "board ", "" ) ).delete();
			}
		}
		return ret;
	}

	/*-----------------------------------------------------------------------------------------
	 * return the data inside the note.
	 */
	@Override
	public Element getXMLdataElm() {
		return dat;
	}

	public String getID() {
		return dat.getAttribute( "ID" );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	@Override
	public void init() {
		hb= new HBox();
		lb= new Label( "" );
		lb.setAlignment( Pos.TOP_LEFT );
		ta= new TextArea();
		ta.focusedProperty().addListener( new ChangeListener <Boolean>() {
			@Override
			public void changed( ObservableValue <? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue ) {
				if( !newPropertyValue ){
					BasefocusOn= false;
					changeMode();
				}
			}
		} );
		ta.setOnKeyTyped( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				switch( (int)event.getCharacter().charAt( 0 ) ){
					case 27 :
						// esc
						BasefocusOn= false;
						changeMode();
						board.removeFocusOfMe();
						return;
				}
			}
		} );
		lb.setText( dat.getAttribute( "Note" ) );
		ta.setText( dat.getAttribute( "Note" ) );
		//
		this.setOnMouseClicked( e -> {
			// if this is end of drag.
			if( dragWasOn ){
				dragWasOn= false;
				board.removeFocusOfMe();
				return;
			}
			// if this is click.
			switch( e.getButton() ){
				case PRIMARY :
					if( e.getClickCount() == 1 ){
						handler.requestFocus();
						BasefocusOn= true;
						changeMode();
						return;
					}
					if( e.getClickCount() == 2 ){
						BasefocusOn= TAfocusOn= true;
						changeMode();
						//
						ta.requestFocus();
						return;
					}
				case SECONDARY :
					if( e.getClickCount() == 1 ){
						String lstr= dat.getAttribute( "Link" );
						if( lstr.equals( "null" ) ){
							return;
						}else if( lstr.startsWith( "http://" ) || lstr.startsWith( "https://" ) ){
							Helper.openWebWithWindows( lstr );
							return;
						}else if( lstr.startsWith( "board" ) ){
							board.removeFocusOfMe();
							board.openBoard( lstr.replaceFirst( "board ", "" ) );
							return;
						}else{
							Helper.openFileWithWindows( lstr );
							return;
						}
					}
			}
		} );
		this.focusedProperty().addListener( new ChangeListener <Boolean>() {
			@Override
			public void changed( ObservableValue <? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue ) {
				if( newPropertyValue ){
					// on focus.
				}else{
					// lost focus.			
					if( !TAfocusOn ){
						BasefocusOn= false;
						changeMode();
					}
				}
			}
		} );
		this.setOnKeyTyped( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				switch( (int)event.getCharacter().charAt( 0 ) ){
					case 27 :
						// esc
						TAfocusOn= BasefocusOn= false;
						changeMode();
						board.removeFocusOfMe();
						return;
					case 8 :
						// back space.
						PNF.remove( handler );
						board.remove( handler );
						if( dat.getAttribute( "Link" ).startsWith( "board" ) ){
							new File( dat.getAttribute( "Link" ).replaceFirst( "board ", "" ) ).delete();
						}
						return;
				}
				switch( event.getCharacter() ){
					case "w" :
						int h= Integer.parseInt( dat.getAttribute( "GridSizeY" ) );
						if( h > 1 ){
							dat.setAttribute( "GridSizeY", --h + "" );
							setNoteGraphic( board.getGridSizeConfig() );
						}
						break;
					case "s" :
						dat.setAttribute( "GridSizeY",
								( Integer.parseInt( dat.getAttribute( "GridSizeY" ) ) + 1 ) + "" );
						setNoteGraphic( board.getGridSizeConfig() );
						break;
					case "a" :
						int w= Integer.parseInt( dat.getAttribute( "GridSizeX" ) );
						if( w > 1 ){
							dat.setAttribute( "GridSizeX", --w + "" );
							setNoteGraphic( board.getGridSizeConfig() );
						}
						break;
					case "d" :
						dat.setAttribute( "GridSizeX",
								( Integer.parseInt( dat.getAttribute( "GridSizeX" ) ) + 1 ) + "" );
						setNoteGraphic( board.getGridSizeConfig() );
						break;
					case "q" :
						PinNoteStyle ts= PNF.getStyleApplyer();
						if( ts != null ){
							dat.setAttribute( "NoteStyle", ts.getTagName() );
							setStyle( (Element)ts.getXMLdataElm() );
						}
						break;
					case "x" :
						File fl= board.chooseDir();
						if( fl != null ){
							dat.setAttribute( "Link", fl.getAbsolutePath().replace( '\\', '/' ) );
						}
						break;
					case "c" :
						File f2= board.chooseFile();
						if( f2 != null ){
							dat.setAttribute( "Link", f2.getAbsolutePath().replace( '\\', '/' ) );
						}
						break;
					case "C" :
						board.copyNote( handler );
						board.removeFocusOfMe();
						return;
					case "b" :
						dat.setAttribute( "Link", "board " + board.createNewBoard() );
						break;
					case "v" :
						String msg= ClipBoard.getClipBoard();
						if( msg.startsWith( "http://" ) || msg.startsWith( "https://" ) ){
							dat.setAttribute( "Link", msg );
						}else{
							dat.setAttribute( "Note", msg );
							lb.setText( msg );
							ta.setText( msg );
						}
						break;
					case "X" :
						cutNoteSignal= true;
						board.cutNote( handler );
						board.removeFocusOfMe();
						return;
					case "z" :
						dat.setAttribute( "Link", "null" );
					default :
						break;
				}
				board.storeXMLfil();
				BasefocusOn= true;
				changeMode();
			}
		} );
		this.setOnMouseDragged( e -> {
			// reset focus of all.
			board.removeFocusOfMe();
			//
			gx= PNF.location2GridX( (int) ( e.getSceneX() + board.getCamShift().getX() ) );
			gy= PNF.location2GridY( (int) ( e.getSceneY() + board.getCamShift().getY() ) );
			if( gx != Integer.parseInt( dat.getAttribute( "GridLocationX" ) ) ||
					gy != Integer.parseInt( dat.getAttribute( "GridLocationY" ) ) ){
				dat.setAttribute( "GridLocationX", gx + "" );
				dat.setAttribute( "GridLocationY", gy + "" );
				board.storeXMLfil();
				setNoteGraphic( board.getGridSizeConfig() );
				dragWasOn= true;
			}
		} );
	}

	/*-----------------------------------------------------------------------------------------
	 * config all the graphical part of this node.
	 */
	protected void setNoteGraphic( int[] gc ) {
		// set the size for the note.
		int sx= Integer.parseInt( dat.getAttribute( "GridSizeX" ) );
		if( sx == 1 )
			width= sx * gc[0];
		else width= sx * gc[0] + ( sx - 1 ) * gc[2];
		int sy= Integer.parseInt( dat.getAttribute( "GridSizeY" ) );
		if( sy == 1 )
			height= sy * gc[1];
		else height= sy * gc[1] + ( sy - 1 ) * gc[3];;
		this.setMinHeight( height );
		this.setMaxHeight( height );
		this.setMinWidth( width );
		this.setMaxWidth( width );
		// set boarder .
		if( bd == null )
			bd= _PinBorderFactory.getBoarder( dat.getAttribute( "BoarderType" ) );
		// sheift the note.
		this.setTranslateX( ( gx - 1 ) * ( gc[0] + gc[2] ) +
				gc[2] / 2 );
		this.setTranslateY( ( gy - 1 ) * ( gc[1] + gc[3] ) +
				gc[3] / 2 );
		//
		lb.setWrapText( true );
		lb.setMaxWidth( width - bd.getLeftOS() - bd.getRightOS() );
		lb.setMaxHeight( height - bd.getTopOS() - bd.getButtomOS() );
		lb.setMinWidth( width - bd.getLeftOS() - bd.getRightOS() );
		lb.setMinHeight( height - bd.getTopOS() - bd.getButtomOS() );
		lb.setTranslateX( bd.getLeftOS() );
		lb.setTranslateY( bd.getTopOS() );
		ta.setWrapText( true );
		ta.setMaxWidth( width - bd.getLeftOS() - bd.getRightOS() );
		ta.setMaxHeight( height - bd.getTopOS() - bd.getButtomOS() );
		ta.setMinWidth( width - bd.getLeftOS() - bd.getRightOS() );
		ta.setMinHeight( height - bd.getTopOS() - bd.getButtomOS() );
		ta.setTranslateX( bd.getLeftOS() );
		ta.setTranslateY( bd.getTopOS() );
		//
		changeMode();
	}

	public void setStyle( Element sty ) {
		dat.setAttribute( "ColorBackGround1", sty.getAttribute( "ColorBackGround1" ) );
		dat.setAttribute( "ColorBackGround2", sty.getAttribute( "ColorBackGround2" ) );
		dat.setAttribute( "ColorBoarder", sty.getAttribute( "ColorBoarder" ) );
		dat.setAttribute( "ColorTitle", sty.getAttribute( "ColorTitle" ) );
		dat.setAttribute( "ColorText", sty.getAttribute( "ColorText" ) );
		dat.setAttribute( "ColorShade", sty.getAttribute( "ColorShade" ) );
		//
		dat.setAttribute( "BoarderType", sty.getAttribute( "BoarderType" ) );
		dat.setAttribute( "BoarderThick", sty.getAttribute( "BoarderThick" ) );
		bd= _PinBorderFactory.getBoarder( dat.getAttribute( "BoarderType" ) );
		//
		dat.setAttribute( "FontSize", sty.getAttribute( "FontSize" ) );
		dat.setAttribute( "FontType", sty.getAttribute( "FontType" ) );
		//
		dat.setAttribute( "ColorHighLightBackGround1", sty.getAttribute( "ColorHighLightBackGround1" ) );
		dat.setAttribute( "ColorHighLightBackGround2", sty.getAttribute( "ColorHighLightBackGround2" ) );
		dat.setAttribute( "ColorHighLightBoarder", sty.getAttribute( "ColorHighLightBoarder" ) );
		dat.setAttribute( "ColorHighLightTitle", sty.getAttribute( "ColorHighLightTitle" ) );
		dat.setAttribute( "ColorHighLightText", sty.getAttribute( "ColorHighLightText" ) );
		dat.setAttribute( "ColorHighLightShade", sty.getAttribute( "ColorHighLightShade" ) );
		//
		setNoteGraphic( board.getGridSizeConfig() );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void changeMode() {
		// remove all.
		this.getChildren().removeAll( this.getChildren() );
		// change color.
		if( BasefocusOn && !TAfocusOn ){
			handler.setStyle( "-fx-background-color: #" +
					dat.getAttribute( "ColorHighLightBackGround1" ) );
			lb.setStyle( "-fx-text-fill: #" + dat.getAttribute( "ColorHighLightText" )
					+ "; -fx-font-size:" + dat.getAttribute( "FontSize" )
					+ "; -fx-font-family: " + dat.getAttribute( "FontType" ) );
			bd.set( width, height, Integer.parseInt( dat.getAttribute( "BoarderThick" ) ),
					dat.getAttribute( "ColorHighLightBoarder" ),
					dat.getAttribute( "ColorHighLightTitle" ) );
		}else if( !BasefocusOn && !TAfocusOn ){
			useLable();
		}else if( BasefocusOn && TAfocusOn ){
			useTextArea();
		}else if( !BasefocusOn && TAfocusOn ){
			TAfocusOn= false;
			useLable();
			dat.setAttribute( "Note", ta.getText() );
			board.storeXMLfil();
		}
		// add back box and border.
		this.getChildren().add( bd.getNodes() );
		this.getChildren().add( hb );
		// change txt.
		String lstr= dat.getAttribute( "Link" );
		if( lstr.equals( "null" ) ){
			lb.setText( dat.getAttribute( "Note" ) );
		}else if( lstr.startsWith( "http://" ) || lstr.startsWith( "https://" ) ){
			lb.setText( "<WebL+>\n" + dat.getAttribute( "Note" ) );
		}else if( lstr.startsWith( "board" ) ){
			lb.setText( "<B+>\n" + dat.getAttribute( "Note" ) );
		}else{
			lb.setText( "<F+>\n" + dat.getAttribute( "Note" ) );
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void useLable() {
		/*
		 * drop shadow to be added.
		 */
		handler.setStyle( "-fx-background-color: #" +
				dat.getAttribute( "ColorBackGround1" ) );
		lb.setStyle( "-fx-text-fill: #" + dat.getAttribute( "ColorText" )
				+ "; -fx-font-size:" + dat.getAttribute( "FontSize" )
				+ "; -fx-font-family: " + dat.getAttribute( "FontType" ) );
		bd.set( width, height, Integer.parseInt( dat.getAttribute( "BoarderThick" ) ),
				dat.getAttribute( "ColorBoarder" ),
				dat.getAttribute( "ColorTitle" ) );
		hb.getChildren().removeAll( hb.getChildren() );
		hb.getChildren().add( lb );
	}

	private void useTextArea() {
		handler.setStyle( "-fx-background-color: #" +
				dat.getAttribute( "ColorBackGround1" ) );
		ta.setStyle(
				"-fx-background-color: #" +
						dat.getAttribute( "ColorBackGround1" ) + ";" +
						"-fx-border-color: #" +
						dat.getAttribute( "ColorBackGround1" ) + ";" +
						"-fx-text-fill: black; " +
						"-fx-font-size: " + dat.getAttribute( "FontSize" ) + ";" );
		bd.set( width, height, Integer.parseInt( dat.getAttribute( "BoarderThick" ) ),
				dat.getAttribute( "ColorBoarder" ),
				dat.getAttribute( "ColorTitle" ) );
		hb.getChildren().removeAll( hb.getChildren() );
		hb.getChildren().add( ta );
		ta.requestFocus();
	}
	/* gradient bg.
	#ipad-dark-grey {
	    -fx-background-color:
	        linear-gradient(#686868 0%, #232723 25%, #373837 75%, #757575 100%),
	        linear-gradient(#020b02, #3a3a3a),
	        linear-gradient(#9d9e9d 0%, #6b6a6b 20%, #343534 80%, #242424 100%),
	        linear-gradient(#8a8a8a 0%, #6b6a6b 20%, #343534 80%, #262626 100%),
	        linear-gradient(#777777 0%, #606060 50%, #505250 51%, #2a2b2a 100%);
	    -fx-background-insets: 0,1,4,5,6;
	    -fx-background-radius: 9,8,5,4,3;
	    -fx-padding: 15 30 15 30;
	    -fx-font-family: "Helvetica";
	    -fx-font-size: 18px;
	    -fx-font-weight: bold;
	    -fx-text-fill: white;
	    -fx-effect: dropshadow( three-pass-box , rgba(255,255,255,0.2) ,
	                1, 0.0 , 0 , 1);
	}
	*/
}
