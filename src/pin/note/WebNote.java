package pin.note;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import machine.Helper;
import machine.p;
import pin.Pin;
import pin.PinCopyable;
import pin.boarder.PinBorder;
import pin.boarder._PinBorderFactory;
import robo.ClipBoard;



/* -------------------------------------------------------------------------------------------
 * 
 * -------------------------------------------------------------------------------------------
 */
public class WebNote extends Pin implements PinNoteInterface, PinCopyable {
	private static final String	name		= "WebNote";
	private static final String	defMsg		= "New Web Note.";
	//
	private WebNote				handler		= this;
	private DeskTopNote			board		= null;
	private _PinNoteFactory		PNF			= null;
	private PinBorder			bd			= null;
	//
	private Element				dat			= null;
	//
	private Label				lb			= null;
	private TextArea			ta			= null;
	private HBox				hb			= null;
	private Group				bdNodes		= null;
	//
	private boolean				BasefocusOn	= false;
	private boolean				TAfocusOn	= false;
	private boolean				dragWasOn	= false;
	// loc tmp copy.
	private int					gx, gy, width, height;
	private int					totLink		= 0;

	/*-----------------------------------------------------------------------------------------
	 * constructor without data.( need to use createXMLdataElm after )
	 */
	public WebNote( int x, int y, DeskTopNote bd, _PinNoteFactory fc ) {
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
	public WebNote( org.w3c.dom.Node node, DeskTopNote bd, _PinNoteFactory fc ) {
		dat= (Element)node;
		board= bd;
		this.PNF= fc;
		gx= Integer.parseInt( dat.getAttribute( "GridLocationX" ) );
		gy= Integer.parseInt( dat.getAttribute( "GridLocationY" ) );
		init();
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
	 *  for dup op.
	 */
	@Override
	public String getTypeName() {
		return WebNote.name;
	}

	@Override
	public String getFactyName() {
		return _PinNoteFactory.pinTypeName;
	}

	@Override
	public Element getXMLDatForDup() {
		return getXMLdataElm();
	}

	@Override
	public boolean setXMLDatForDup( Element dat ) {
		this.dat.setAttribute( "GridSizeX", dat.getAttribute( "GridSizeX" ) );
		this.dat.setAttribute( "GridSizeY", dat.getAttribute( "GridSizeY" ) );
		this.dat.setAttribute( "NoteStyle", dat.getAttribute( "NoteStyle" ) );
		this.dat.setAttribute( "Note", dat.getAttribute( "Note" ) );
		this.dat.setAttribute( "Link", dat.getAttribute( "Link" ) );
		//
		lb.setText( dat.getAttribute( "Note" ) );
		ta.setText( dat.getAttribute( "Note" ) );
		//
		totLink= link2link( dat.getAttribute( "Link" ) ).size();
		setStyle( dat );
		return true;
	}

	public void selectHL() {
		BasefocusOn= true;
		changeMode();
		return;
	}

	public void selectDeHL() {
		BasefocusOn= false;
		changeMode();
		return;
	}

	public Point2D getGridloc() {
		return new Point2D( Integer.parseInt( dat.getAttribute( "GridSizeX" ) ),
				Integer.parseInt( dat.getAttribute( "GridSizeY" ) ) );
	}

	@Override
	public void deleteAfterCut() {
		deleteThis();
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	@Override
	public void deleteThis() {
		PNF.remove( handler );
		board.remove( handler );
		if( dat.getAttribute( "Link" ).startsWith( "board" ) ){
			new File( dat.getAttribute( "Link" ).replaceFirst( "board ", "" ) ).delete();
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	@Override
	public void init() {
		totLink= link2link( dat.getAttribute( "Link" ) ).size();
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
						if( totLink > 0 ){
							String lstr= dat.getAttribute( "Link" );
							for( String tmp : Helper.str2ALstr( lstr ) ){
								try{
									Helper.openWebWithWindows( tmp );
								}catch ( Exception ee ){
									ee.printStackTrace();
								}
							}
						}
					}
					return;
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
						deleteThis();
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
					case "v" :
						String msg= ClipBoard.getClipBoard();
						ArrayList <String> links= link2link( msg );
						if( links.size() > 0 ){
							if( totLink == 0 ){
								if( links.size() == 1 ){
									dat.setAttribute( "Note", "getting title..." );
									getTitle( links.get( 0 ) );
								}
								dat.setAttribute( "Link", Helper.ALstr2str( links ) );
								totLink= links.size();
							}else{
								totLink+= links.size();
								dat.setAttribute( "Link", dat.getAttribute( "Link" ) + "\n" +
										Helper.ALstr2str( links ) );
							}
						}
						break;
					case "z" :
						totLink= 0;
						dat.setAttribute( "Link", "null" );
					default :
						break;
					case "C" :
						board.copyNote( handler );
						board.removeFocusOfMe();
						return;
					case "X" :
						board.cutNote( handler );
						board.removeFocusOfMe();
						return;
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
			lb.setText( "No Link\n" + dat.getAttribute( "Note" ) );
		}else{
			lb.setText( totLink + " Links\n" + dat.getAttribute( "Note" ) );
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

	private ArrayList <String> link2link( String inp ) {
		ArrayList <String> ret= new ArrayList <String>();
		ArrayList <String> tmp= Helper.str2ALstr( inp );
		for( String msg : tmp ){
			if( msg.startsWith( "http://" ) || msg.startsWith( "https://" ) ){
				ret.add( msg );
			}
		}
		return ret;
	}

	private void getTitle( String inp ) {
		Thread tt= new Thread() {
			public void run() {
				org.jsoup.nodes.Document doc;
				try{
					doc= Jsoup.connect( inp ).get();
					String tit= doc.title();
					dat.setAttribute( "Note", tit );
					lb.setText( tit );
					ta.setText( tit );
					//
					board.storeXMLfil();
				}catch ( IOException e ){
					e.printStackTrace();
				}
			}
		};
		tt.start();
	}
}
