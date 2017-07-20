package pin.note;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import app.DeskTopNote;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import machine.Helper;
import machine.p;
import pin.Pin;
import pin.PinInterface;
import pin.boarder.PinBorderInterface;
import pin.boarder._PinBorderFactory;



public class PinNoteStyle extends Pin {
	private static final String	name		= "PinNoteStyle";
	Element						dat			= null;
	//
	private DeskTopNote			board		= null;
	private _PinNoteFactory		PNF			= null;
	private PinNoteStyle		handler		= this;
	private Group				bd			= null;
	//
	private VBox				vb			= null;
	private VBox				vb2			= null;
	private Label				lb			= null;
	// loc tmp copy.
	private boolean				dragWasOn	= false;
	private boolean				editModeOn	= false;
	private int					gx, gy;

	/*-----------------------------------------------------------------------------------------
	 * constructor without data.( need to use createXMLdataElm after )
	 */
	public PinNoteStyle( int x, int y, DeskTopNote bd, _PinNoteFactory fc ) {
		gx= x;
		gy= y;
		board= bd;
		this.PNF= fc;
	}

	@Override
	public void createXMLdataElm( Document doc ) {
		dat= doc.createElement( name );
		dat.setAttribute( "GridLocationX", gx + "" );
		dat.setAttribute( "GridLocationY", gy + "" );
		//
		dat.setAttribute( "ColorBackGround1", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorBackGround2", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorBoarder", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorTitle", Helper.rand2colorHighStr() );
		dat.setAttribute( "ColorText", Helper.rand2colorHighStr() );
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
		String ID= PNF.getNewID();
		dat.setAttribute( "ID", ID );
		dat.setAttribute( "NoteStyle", ID );
		init();
	}

	/*-----------------------------------------------------------------------------------------
	 * constructor with data.
	 */
	public PinNoteStyle( org.w3c.dom.Node node, DeskTopNote bd, _PinNoteFactory fc ) {
		dat= (Element)node;
		board= bd;
		this.PNF= fc;
		gx= Integer.parseInt( dat.getAttribute( "GridLocationX" ) );
		gy= Integer.parseInt( dat.getAttribute( "GridLocationY" ) );
		init();
	}

	public String getTagName() {
		return dat.getAttribute( "NoteStyle" );
	}

	@Override
	public Node getXMLdataElm() {
		return dat;
	}

	public String getID() {
		return dat.getAttribute( "ID" );
	}

	@Override
	public void init() {
		vb= new VBox();
		lb= new Label( "configTile\nPinNote Style\n" +
				dat.getAttribute( "NoteStyle" ).substring( 0,
						dat.getAttribute( "NoteStyle" ).length() / 2 )
				+ "..." );
		vb.getChildren().add( lb );
		vb2= new VBox();
		vb2.setStyle( "-fx-background-color: #aaaaaa;" );
		//
		this.setOnMouseClicked( e -> {
			if( board.isWallpaperMode() )
				return;
			// if this is end of drag.
			if( dragWasOn ){
				dragWasOn= false;
				board.removeFocusOfMe();
				return;
			}
			// if this is click.
			board.removeFocusOfMe();
			switch( e.getButton() ){
				case PRIMARY :
					if( e.getClickCount() == 1 ){
						this.requestFocus();
					}else if( e.getClickCount() == 2 ){
						editModeOn= true;
						changeMode();
					}
					break;
				case SECONDARY :
					if( e.getClickCount() == 1 ){
						AnimationTimer tm= new AnimationTimer() {
							private long	lastUpdate	= 0;
							private int		str			= 0;

							@Override
							public void handle( long now ) {
								if( now - lastUpdate >= 20_000_000 ){
									lb.setStyle( "-fx-background-color: #" +
											str + str + "3333;" );
									str++ ;
									if( str == 10 ){
										this.stop();
										lb.setStyle( "-fx-background-color: #" + "333333;"
												+ "-fx-text-fill: white;" +
												"-fx-font-size:15;" );
									}
									lastUpdate= now;
								}
							}
						};
						tm.start();
						PNF.setStyleApplyer( this );
						editModeOn= false;
						changeMode();
					}
					break;
			}
		} );
		this.setOnKeyTyped( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				switch( (int)event.getCharacter().charAt( 0 ) ){
					case 27 :
						// esc
						editModeOn= false;
						changeMode();
						return;
					case 8 :
						// back space.
						PNF.remove( handler );
						board.remove( handler );
						return;
				}
			}
		} );
		this.setOnMouseDragged( e -> {
			if( board.isWallpaperMode() )
				return;
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
		int height, width;
		width= gc[0];
		height= gc[1];
		this.setMinHeight( height );
		this.setMaxHeight( height );
		this.setMinWidth( width );
		this.setMaxWidth( width );
		// sheift the note. if the location is not managed.
		if( !super.locationManaged ){
			this.setTranslateX( ( gx - 1 ) * ( gc[0] + gc[2] ) +
					gc[2] / 2 );
			this.setTranslateY( ( gy - 1 ) * ( gc[1] + gc[3] ) +
					gc[3] / 2 );
		}
		//
		if( bd == null ){
			PinBorderInterface b= _PinBorderFactory.getBoarder( "default" );
			b.set( width, height, 2, "ffffff", "0000ff" );
			bd= b.getNodes();
		}
		//
		lb.setWrapText( true );
		lb.setAlignment( Pos.CENTER );
		lb.setMaxWidth( width );
		lb.setMaxHeight( height );
		lb.setMinWidth( width );
		lb.setMinHeight( height );
		//
		changeMode();
	}

	private void changeMode() {
		if( editModeOn ){
			showEdit();
		}else{
			useLabel();
			board.storeXMLfil();
			PNF.applyChangedStyle( this );
		}
	}

	private void useLabel() {
		handler.getChildren().removeAll( handler.getChildren() );
		handler.getChildren().add( vb );
		lb.setStyle( "-fx-background-color: #" + "333333;"
				+ "-fx-text-fill: white;" +
				"-fx-font-size:15;" );
		this.getChildren().add( bd );
	}

	private void showEdit() {
		handler.getChildren().removeAll( handler.getChildren() );
		vb2.getChildren().removeAll( vb2.getChildren() );
		handler.getChildren().add( vb2 );
		//	
		Label bg1= new Label( "background color 1" );
		ColorPicker cbg1= new ColorPicker();
		cbg1.setValue( Helper.str2color( dat.getAttribute( "ColorBackGround1" ) ) );
		cbg1.setOnAction( e -> {
			dat.setAttribute( "ColorBackGround1",
					cbg1.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg2= new Label( "background color 2" );
		ColorPicker cbg2= new ColorPicker();
		cbg2.setValue( Helper.str2color( dat.getAttribute( "ColorBackGround2" ) ) );
		cbg2.setOnAction( e -> {
			dat.setAttribute( "ColorBackGround2",
					cbg2.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg3= new Label( "boarder color" );
		ColorPicker cbg3= new ColorPicker();
		cbg3.setValue( Helper.str2color( dat.getAttribute( "ColorBoarder" ) ) );
		cbg3.setOnAction( e -> {
			dat.setAttribute( "ColorBoarder",
					cbg3.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg4= new Label( "title color" );
		ColorPicker cbg4= new ColorPicker();
		cbg4.setValue( Helper.str2color( dat.getAttribute( "ColorTitle" ) ) );
		cbg4.setOnAction( e -> {
			dat.setAttribute( "ColorTitle",
					cbg4.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg5= new Label( "text color" );
		ColorPicker cbg5= new ColorPicker();
		cbg5.setValue( Helper.str2color( dat.getAttribute( "ColorText" ) ) );
		cbg5.setOnAction( e -> {
			dat.setAttribute( "ColorText",
					cbg5.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		//
		Label bg1hl= new Label( "background HL color 1" );
		ColorPicker cbg1hl= new ColorPicker();
		cbg1hl.setValue( Helper.str2color( dat.getAttribute( "ColorHighLightBackGround1" ) ) );
		cbg1hl.setOnAction( e -> {
			dat.setAttribute( "ColorHighLightBackGround1",
					cbg1hl.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg2hl= new Label( "background HL color 2" );
		ColorPicker cbg2hl= new ColorPicker();
		cbg2hl.setValue( Helper.str2color( dat.getAttribute( "ColorHighLightBackGround2" ) ) );
		cbg2hl.setOnAction( e -> {
			dat.setAttribute( "ColorHighLightBackGround2",
					cbg2hl.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg3hl= new Label( "boarder HL color" );
		ColorPicker cbg3hl= new ColorPicker();
		cbg3hl.setValue( Helper.str2color( dat.getAttribute( "ColorHighLightBoarder" ) ) );
		cbg3hl.setOnAction( e -> {
			dat.setAttribute( "ColorHighLightBoarder",
					cbg3hl.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg4hl= new Label( "title HL color" );
		ColorPicker cbg4hl= new ColorPicker();
		cbg4hl.setValue( Helper.str2color( dat.getAttribute( "ColorHighLightTitle" ) ) );
		cbg4hl.setOnAction( e -> {
			dat.setAttribute( "ColorHighLightTitle",
					cbg4hl.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bg5hl= new Label( "text HL color" );
		ColorPicker cbg5hl= new ColorPicker();
		cbg5hl.setValue( Helper.str2color( dat.getAttribute( "ColorHighLightText" ) ) );
		cbg5hl.setOnAction( e -> {
			dat.setAttribute( "ColorHighLightText",
					cbg5hl.getValue().toString().substring( 2, 10 ) );
			PNF.applyChangedStyle( this );
		} );
		Label bd= new Label( "boarder type" );
		ComboBox <String> cb= new ComboBox();
		cb.setValue( dat.getAttribute( "BoarderType" ) );
		cb.getItems().addAll( _PinBorderFactory.BoarderType );
		cb.setOnAction( e -> {
			dat.setAttribute( "BoarderType", (String)cb.getValue() );
			PNF.applyChangedStyle( this );
		} );
		Label fs= new Label( "font size" );
		ComboBox <Integer> cbfs= new ComboBox();
		cbfs.setValue( Integer.parseInt( dat.getAttribute( "FontSize" ) ) );
		cbfs.getItems().addAll( _PinNoteFactory.NoteFontSize );
		cbfs.setOnAction( e -> {
			dat.setAttribute( "FontSize", cbfs.getValue() + "" );
			PNF.applyChangedStyle( this );
		} );
		Label ff= new Label( "font type" );
		ComboBox <String> cbff= new ComboBox();
		cbff.setValue( dat.getAttribute( "FontType" ) );
		cbff.getItems().addAll( _PinNoteFactory.NoteFontType );
		cbff.setOnAction( e -> {
			dat.setAttribute( "FontType", (String)cbff.getValue() );
			PNF.applyChangedStyle( this );
		} );
		//
		Button ext= new Button( "- exit config -" );
		ext.setOnMouseClicked( e -> {
			editModeOn= false;
			changeMode();
		} );
		//
		vb2.getChildren().add( ext );
		vb2.getChildren().add( new Label( " " ) );
		//
		vb2.getChildren().add( ff );
		vb2.getChildren().add( cbff );
		vb2.getChildren().add( fs );
		vb2.getChildren().add( cbfs );
		vb2.getChildren().add( bd );
		vb2.getChildren().add( cb );
		//
		vb2.getChildren().add( bg1 );
		vb2.getChildren().add( cbg1 );
		vb2.getChildren().add( bg2 );
		vb2.getChildren().add( cbg2 );
		vb2.getChildren().add( bg3 );
		vb2.getChildren().add( cbg3 );
		vb2.getChildren().add( bg4 );
		vb2.getChildren().add( cbg4 );
		vb2.getChildren().add( bg5 );
		vb2.getChildren().add( cbg5 );
		//
		vb2.getChildren().add( bg1hl );
		vb2.getChildren().add( cbg1hl );
		vb2.getChildren().add( bg2hl );
		vb2.getChildren().add( cbg2hl );
		vb2.getChildren().add( bg3hl );
		vb2.getChildren().add( cbg3hl );
		vb2.getChildren().add( bg4hl );
		vb2.getChildren().add( cbg4hl );
		vb2.getChildren().add( bg5hl );
		vb2.getChildren().add( cbg5hl );
		//
		vb2.setBorder( new Border( new BorderStroke( Color.BLACK,
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT ) ) );
		vb2.setPadding( new Insets( 5, 10, 5, 10 ) );
	}
}
