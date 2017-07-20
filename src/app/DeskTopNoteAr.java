package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import machine.Helper;
import machine._SysConfig;
import machine.p;
import pin.Pin;
import pin.PinCopyable;
import pin.memory.NewLineSpacer;
import pin.memory._PinMemFactory;
import pin.note._PinNoteFactory;



/*
 * almost a copy of the desktopNote, with some added and some deleted.
 */
public class DeskTopNoteAr {
	//
	// focusholder at z=3, bgImg at z=4. liveBG at z= 2.
	protected DeskTopApp	app					= null;
	private File			file				= null;
	protected Document		doc					= null;
	protected Element		elm					= null;
	protected Scene			scn					= null;
	private Group			rootSys				= null;
	protected Group			root				= null;
	private Group			bgNode				= null;
	private Region			focusHolder			= null;
	private BorderPane		topMenuP			= null;
	private ContextMenu		optionMenu			= null;
	//
	private Point2D			camShift			= new Point2D( 0.0, 0.0 );
	private MouseEvent		lastMouseMoveEvent	= null;
	//
	private PinArrFactory	PAF					= null;
	private String			FACtype				= null;
	private int				ShiftKeyboardSpeedY	= 0;
	private int				lastYind			= 0;

	/*-----------------------------------------------------------------------------------------
	 * getters and setter and other public func.
	 */
	public int[] getGridSizeConfig() {
		int[] ret= { Integer.parseInt( elm.getAttribute( "GridSizeX" ) ),
				Integer.parseInt( elm.getAttribute( "GridSizeY" ) ),
				Integer.parseInt( elm.getAttribute( "GridSpaceX" ) ),
				Integer.parseInt( elm.getAttribute( "GridSpaceY" ) ) };
		return ret;
	}

	public Point2D getDim() {
		return new Point2D( Integer.parseInt( elm.getAttribute( "WindowSizeX" ) ),
				Integer.parseInt( elm.getAttribute( "WindowSizeY" ) ) );
	}

	public Point2D getCamShift() {
		return camShift;
	}

	public void scrollBoard( int val ) {
		if( val > 0.0 ){
			root.setTranslateY( ShiftKeyboardSpeedY + root.getTranslateY() );
			camShift= camShift.add( new Point2D( 0.0, (double) -ShiftKeyboardSpeedY ) );
		}else{
			root.setTranslateY( -ShiftKeyboardSpeedY + root.getTranslateY() );
			camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedY ) );
		}
	}

	public void storeXMLfil() {
		store();
	}

	public void reArrangeBoard() {
		refreshRoot();
	}

	public void remove( Pin inp ) {
		root.getChildren().remove( inp );
		store();
	}

	public void removeFocusOfMe() {
		focusHolder.requestFocus();
	}

	public String getUniqNameInBoardFolder( String ext ) {
		String uniq= Helper.randAN( 12 );
		File ret= new File( Helper.getFileName( file.toString() ) + uniq + ext );
		while( ret.exists() ){
			uniq= Helper.randAN( 12 );
			ret= new File( Helper.getFileName( file.toString() ) + uniq + ext );
		}
		return uniq;
	}

	public void popMsg( String inp ) {
		popUp pu= new popUp( "Attention." );
		VBox comp= new VBox();
		TextArea lb= new TextArea( inp );
		lb.setEditable( false );
		lb.setWrapText( true );
		lb.setMaxWidth( 500 );
		lb.setMaxHeight( 300 );
		lb.setMinWidth( 500 );
		lb.setMinHeight( 300 );
		comp.getChildren().add( lb );
		Scene stageScene= new Scene( comp, 500, 300 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * constructor:
	 * create a board data with a given root elm. 
	 * then sort root child node to spec pin factory.
	 */
	protected DeskTopNoteAr( File inp, DeskTopApp app, String type ) {
		p.p( this.getClass().toString(), "Constructor running for: " +
				inp.getAbsolutePath() + " " );
		// load file. setup elm.
		this.FACtype= type;
		this.file= inp;
		this.app= app;
		//
		if( !load() )
			return;
		// load all Pin Factory.
		PAF= PinArrFactory.getFactory( doc, elm, this, FACtype );
		// set up scene.
		setScrollSpeed();
		sceneSetup();
		//
		this.removeFocusOfMe();
		p.p( this.getClass().toString(), "Constructor finished." );
	}

	protected void close() {
		store();
	}

	/*-----------------------------------------------------------------------------------------
	 * reload all the Pin for the board( the node for javaFX) in the root group.
	 * ( for this arranged board, also give the loaction for the node/pin.
	 */
	protected Scene refreshRoot() {
		if( root == null || PAF == null || PAF.getAllChildren() == null )
			return null;
		root.getChildren().removeAll( root.getChildren() );
		root.getChildren().addAll( PAF.getAllChildren() );
		reArrange();
		return scn;
	}

	protected void reloadFactory() {
		PAF.reInitCont();
	}

	protected File getBoardFile() {
		return file;
	}

	/*-----------------------------------------------------------------------------------------
	 * load the doc from the XML file.
	 */
	private boolean load() {
		if( !file.exists() ){
			if( !createDefaultFile( file ) )
				return false;
		}
		//
		if( !file.canRead() )
			return false;
		//
		this.doc= parseDoc( file );
		this.elm= doc.getDocumentElement();
		// make sure all attri is there.
		completeAllAttr();
		store();
		if( doc == null )
			return false;
		return true;
	}

	/*-----------------------------------------------------------------------------------------
	 * retore the root elm to the XML file.
	 */
	protected boolean store() {
		try{
			file.delete();
			Source source= new DOMSource( doc );
			Result result= new StreamResult( file );
			Transformer xformer= TransformerFactory.newInstance().newTransformer();
			xformer.transform( source, result );
			return true;
		}catch ( Exception e ){
			e.printStackTrace();
		}
		return false;
	}

	/*-----------------------------------------------------------------------------------------
	 * parse the XML file into doc.
	 */
	private Document parseDoc( File inp ) {
		try{
			DocumentBuilderFactory dbFactory= DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder= dbFactory.newDocumentBuilder();
			Document doc= dBuilder.parse( inp );
			doc.getDocumentElement().normalize();
			return doc;
		}catch ( ParserConfigurationException e ){
			e.printStackTrace();
		}catch ( SAXException e ){
			e.printStackTrace();
		}catch ( IOException e ){
			e.printStackTrace();
		}
		return null;
	}

	/*-----------------------------------------------------------------------------------------
	 * create a default empty XML file for this board.
	 */
	private boolean createDefaultFile( File inp ) {
		XMLOutputFactory xof= XMLOutputFactory.newInstance();
		XMLStreamWriter xtw= null;
		try{
			xtw= xof.createXMLStreamWriter( new FileWriter( inp ) );
			xtw.writeStartDocument();
			xtw.writeStartElement( "board" );
			xtw.writeEndElement();
			xtw.writeEndDocument();
			xtw.flush();
			xtw.close();
			return true;
		}catch ( XMLStreamException | IOException e ){
			e.printStackTrace();
			return false;
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * make sure that all attribute are there.
	 */
	private void completeAllAttr() {
		if( elm.getAttribute( "GridSizeX" ).equals( "" ) )
			elm.setAttribute( "GridSizeX", "100" );
		if( elm.getAttribute( "GridSizeXmin" ).equals( "" ) )
			elm.setAttribute( "GridSizeXmin", "50" );
		if( elm.getAttribute( "GridSizeXmax" ).equals( "" ) )
			elm.setAttribute( "GridSizeXmax", "600" );
		if( elm.getAttribute( "GridSizeXstep" ).equals( "" ) )
			elm.setAttribute( "GridSizeXstep", "5" );
		if( elm.getAttribute( "GridSizeY" ).equals( "" ) )
			elm.setAttribute( "GridSizeY", "100" );
		if( elm.getAttribute( "GridSizeYmin" ).equals( "" ) )
			elm.setAttribute( "GridSizeYmin", "50" );
		if( elm.getAttribute( "GridSizeYmax" ).equals( "" ) )
			elm.setAttribute( "GridSizeYmax", "600" );
		if( elm.getAttribute( "GridSizeYstep" ).equals( "" ) )
			elm.setAttribute( "GridSizeYstep", "5" );
		if( elm.getAttribute( "GridSpaceX" ).equals( "" ) )
			elm.setAttribute( "GridSpaceX", "15" );
		if( elm.getAttribute( "GridSpaceY" ).equals( "" ) )
			elm.setAttribute( "GridSpaceY", "10" );
		if( elm.getAttribute( "WindowSizeX" ).equals( "" ) )
			elm.setAttribute( "WindowSizeX", "1920" );
		if( elm.getAttribute( "WindowSizeY" ).equals( "" ) )
			elm.setAttribute( "WindowSizeY", "1080" );
		if( elm.getAttribute( "ViewPortChange" ).equals( "" ) )
			elm.setAttribute( "ViewPortChange", "true" );
		if( elm.getAttribute( "ColorBackGround1" ).equals( "" ) )
			elm.setAttribute( "ColorBackGround1", "333333" );
		if( elm.getAttribute( "ColorBackGround2" ).equals( "" ) )
			elm.setAttribute( "ColorBackGround2", "777777" );
		if( elm.getAttribute( "BackGroundImage" ).equals( "" ) )
			elm.setAttribute( "BackGroundImage", "null" );
		if( elm.getAttribute( "BgSelection" ).equals( "" ) )
			elm.setAttribute( "BgSelection", "color" );
		if( elm.getAttribute( "WindowMaxOnStart" ).equals( "" ) )
			elm.setAttribute( "WindowMaxOnStart", "true" );
		if( elm.getAttribute( "Name" ).equals( "" ) )
			elm.setAttribute( "Name", Helper.randAN( 10 ) );
		if( elm.getAttribute( "DateCreate" ).equals( "" ) )
			elm.setAttribute( "DateCreate", Helper.getCurrentDate() );
		if( elm.getAttribute( "ScrollGridSize" ).equals( "" ) )
			elm.setAttribute( "ScrollGridSize", "1" );
		/*
		if( elm.getAttribute( "ScrollPolicy" ).equals( "" ) )
			elm.setAttribute( "ScrollPolicy", "full" );
		if( elm.getAttribute( "TimeIdel2Lock" ).equals( "" ) )
			elm.setAttribute( "TimeIdel2Lock", "0" );
		// lockup mode enabling. ( wallpaper mode. )
		if( elm.getAttribute( "LockUpMode" ).equals( "" ) )
			elm.setAttribute( "LockUpMode", "false" );
			*/
		if( elm.getAttribute( "CursorName" ).equals( "" ) )
			elm.setAttribute( "CursorName", "C2.png" );
	}

	/*-----------------------------------------------------------------------------------------
	 * setup the scene and the root.
	 */
	private void sceneSetup() {
		root= new Group();
		focusHolder= new Region();
		focusHolder.setMinHeight( _SysConfig.getScreenSizeY() );
		focusHolder.setMinWidth( _SysConfig.getScreenSizeX() );
		focusHolder.setTranslateZ( 3 );
		focusHolder.setStyle( "-fx-background-color: rgba(1.0, 1.0, 1.0, 0.03);" );
		//
		bgNode= new Group();
		SetBg();
		//
		rootSys= new Group();
		rootSys.getChildren().add( bgNode );
		rootSys.getChildren().add( focusHolder );
		rootSys.getChildren().add( root );
		if( Boolean.parseBoolean( elm.getAttribute( "WindowMaxOnStart" ) ) ){
			scn= new Scene( rootSys, DeskTopApp.screenSize.getWidth(),
					DeskTopApp.screenSize.getHeight(), true, SceneAntialiasing.BALANCED );
		}else{
			scn= new Scene( rootSys, getDim().getX(), getDim().getY(),
					true, SceneAntialiasing.BALANCED );
		}
		//
		scn.setOnMouseMoved( e -> {
			lastMouseMoveEvent= e;
		} );
		scn.setOnMouseEntered( e -> {
			lastMouseMoveEvent= e;
		} );
		// focus holder. switch mouse the key.
		focusHolder.setOnMouseClicked( e -> {
			switch( e.getButton() ){
				case PRIMARY :
					focusHolder.requestFocus();
					break;
				case SECONDARY :
					createMenu( lastMouseMoveEvent.getSceneX(),
							lastMouseMoveEvent.getScreenY(),
							lastMouseMoveEvent.getSceneX(),
							lastMouseMoveEvent.getSceneY(),
							camShift.getX(), camShift.getY(),
							PAF.getMenus() );
					break;
				default :
					break;
			}
		} );
		focusHolder.setOnScroll( e -> {
			if( e.getDeltaY() > 0.0 ){
				if( camShift.getY() > 0 )
					camShift= camShift.add( new Point2D( 0.0, (double) -ShiftKeyboardSpeedY *
							Integer.parseInt( elm.getAttribute( "ScrollGridSize" ) ) ) );
			}else{
				if( camShift.getY() < lastYind )
					camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedY *
							Integer.parseInt( elm.getAttribute( "ScrollGridSize" ) ) ) );
			}
			root.setTranslateY( (int)camShift.getY() * -1 );
		} );
		focusHolder.setOnMouseDragged( e -> {
			switch( e.getButton() ){
				case PRIMARY :
					break;
				default :
					break;
			}
		} );
		focusHolder.setOnMousePressed( e -> {
			switch( e.getButton() ){
				case PRIMARY :
					break;
				default :
					break;
			}
		} );
		focusHolder.setOnKeyTyped( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				switch( (int)event.getCharacter().charAt( 0 ) ){
					case 27 :
						// esc
						store();
						app.exitFuncBoard();
						return;
					case 8 :
						// backspace.
						return;
				}
				switch( event.getCharacter() ){
					//
					//***
					// t is for test. to remove after done.
					case "t" :
						( (_PinMemFactory)PAF ).incTime();
						break;
					//***
					//
					case " " :
						createMenu( lastMouseMoveEvent.getSceneX(),
								lastMouseMoveEvent.getScreenY(),
								lastMouseMoveEvent.getSceneX(),
								lastMouseMoveEvent.getSceneY(),
								camShift.getX(), camShift.getY(),
								PAF.getMenus() );
						break;
					case "e" :
					case "E" :
						if( topMenuP == null ){
							createTopConfigMenu();
						}else{
							rootSys.getChildren().remove( topMenuP );
							topMenuP= null;
						}
						break;
					case "m" :
						popUpConsoleLog();
						break;
					case "r" :
						popMsg( "Total Notes: " + root.getChildren().size() + " \n" +
								"Board Name: " + elm.getAttribute( "Name" ) + " \n" +
								"Creation Date: " + elm.getAttribute( "DateCreate" ) );
						break;
				}
			}
		} );
		// set cursor.
		try{
			if( !elm.getAttribute( "CursorName" ).equals( "null" ) ){
				FileInputStream input;
				input= new FileInputStream( _SysConfig.sysFolderName + File.separatorChar +
						_SysConfig.cursorFoldername + File.separatorChar +
						elm.getAttribute( "CursorName" ) );
				Image image= new Image( input );
				scn.setCursor( new ImageCursor( image ) );
			}
		}catch ( Exception ee ){
			// silent fail.
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * create right menu.
	 */
	private void createMenu( double xs, double ys, double x, double y, double xx, double yy,
			ArrayList <MenuItem> menus ) {
		optionMenu= new ContextMenu();
		optionMenu.getItems().addAll( menus );
		//
		optionMenu.show( root, xs, ys );
	}

	/*-----------------------------------------------------------------------------------------
	 * top config Menu
	 */
	private void createTopConfigMenu() {
		if( topMenuP == null ){
			MenuBar topMenu= new MenuBar();
			topMenu.prefWidthProperty().bind( app.getPrimStage().widthProperty() );
			topMenuP= new BorderPane();
			topMenuP.setTop( topMenu );
			rootSys.getChildren().add( topMenuP );
			//
			Menu gw= new Menu( "Grid Width" );
			ToggleGroup gwG= new ToggleGroup();
			int xs= Integer.parseInt( elm.getAttribute( "GridSizeXmin" ) );
			int xe= Integer.parseInt( elm.getAttribute( "GridSizeXmax" ) );
			int xt= Integer.parseInt( elm.getAttribute( "GridSizeXstep" ) );
			for( int i= xs; i < xe; i+= xt ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( gwG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSizeX", Integer.parseInt( mi.getText() ) + "" );
					reloadFactory();
					reArrangeBoard();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSizeX" ) ) )
					mi.setSelected( true );
				gw.getItems().add( mi );
			}
			topMenu.getMenus().add( gw );
			//
			Menu gh= new Menu( "Grid Height" );
			ToggleGroup ghG= new ToggleGroup();
			int ys= Integer.parseInt( elm.getAttribute( "GridSizeYmin" ) );
			int ye= Integer.parseInt( elm.getAttribute( "GridSizeYmax" ) );
			int yt= Integer.parseInt( elm.getAttribute( "GridSizeYstep" ) );
			for( int i= ys; i < ye; i+= yt ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( ghG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSizeY", Integer.parseInt( mi.getText() ) + "" );
					setScrollSpeed();
					reloadFactory();
					reArrangeBoard();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSizeY" ) ) )
					mi.setSelected( true );
				gh.getItems().add( mi );
			}
			topMenu.getMenus().add( gh );
			//
			Menu gws= new Menu( "Grid S Width" );
			ToggleGroup gwsG= new ToggleGroup();
			for( int i= 1; i < 50; i+= 1 ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( gwsG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSpaceX", Integer.parseInt( mi.getText() ) + "" );
					reloadFactory();
					reArrangeBoard();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSpaceX" ) ) )
					mi.setSelected( true );
				gws.getItems().add( mi );
			}
			topMenu.getMenus().add( gws );
			//
			Menu ghs= new Menu( "Grid S Height" );
			ToggleGroup ghsG= new ToggleGroup();
			for( int i= 1; i < 50; i+= 1 ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( ghsG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSpaceY", Integer.parseInt( mi.getText() ) + "" );
					setScrollSpeed();
					reloadFactory();
					reArrangeBoard();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSpaceY" ) ) )
					mi.setSelected( true );
				ghs.getItems().add( mi );
			}
			topMenu.getMenus().add( ghs );
			//
			Menu ppt= new Menu( "Property" );
			topMenu.getMenus().add( ppt );
			//
			MenuItem chooseBG= new MenuItem( "choose background picture" );
			chooseBG.setOnAction( e -> {
				File bg= app.chooseFile( _SysConfig.sysFolderName + File.separatorChar
						+ _SysConfig.backgroundFolderName );
				if( bg != null ){
					elm.setAttribute( "BackGroundImage", bg.toString().replace( '\\', '/' ) );
					elm.setAttribute( "BgSelection", "img" );
					SetBg();
				}
			} );
			ppt.getItems().add( chooseBG );
			//
			ToggleGroup bgo= new ToggleGroup();
			RadioMenuItem bgimg= new RadioMenuItem( "bg use image" );
			bgimg.setToggleGroup( bgo );
			RadioMenuItem bgcol= new RadioMenuItem( "bg use color" );
			bgcol.setToggleGroup( bgo );
			ppt.getItems().add( bgimg );
			ppt.getItems().add( bgcol );
			if( elm.getAttribute( "BgSelection" ).equals( "img" ) )
				bgimg.setSelected( true );
			else bgcol.setSelected( true );
			bgimg.setOnAction( e -> {
				elm.setAttribute( "BgSelection", "img" );
				SetBg();
			} );
			bgcol.setOnAction( e -> {
				elm.setAttribute( "BgSelection", "color" );
				SetBg();
			} );
			ppt.getItems().add( new SeparatorMenuItem() );
			//
			//	CheckMenuItem htmlMenuItem = new CheckMenuItem("HTML");
		}
	}

	private void setScrollSpeed() {
		ShiftKeyboardSpeedY= (int) ( ( (double)Integer.parseInt( elm.getAttribute( "GridSpaceY" ) ) ) / 2.0 +
				Integer.parseInt( elm.getAttribute( "GridSizeY" ) ) );
	}

	/*-----------------------------------------------------------------------------------------
	 * create a pop up menu for inter the new board name.
	 */
	private void setBoardNamePopUp() {
		popUp pu= new popUp( "Set Board Name." );
		VBox comp= new VBox();
		Label sig= new Label( "Press Enter key to set and exit." );
		TextField nameField= new TextField( elm.getAttribute( "Name" ) );
		nameField.setOnKeyPressed( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				if( event.getCode() == KeyCode.ENTER ){
					pu.close();
					elm.setAttribute( "Name", nameField.getText() );
				}
			}
		} );
		comp.getChildren().addAll( sig, nameField );
		Scene stageScene= new Scene( comp, 300, 100 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * create a pop up menu for inter the new board name.
	 */
	private void setScrollGridSizePopUp() {
		popUp pu= new popUp( "Set Scroll Grid Size." );
		VBox comp= new VBox();
		Label sig= new Label( "Press Enter key to set and exit." );
		TextField nameField= new TextField( elm.getAttribute( "ScrollGridSize" ) );
		nameField.setOnKeyPressed( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				if( event.getCode() == KeyCode.ENTER ){
					pu.close();
					try{
						Integer.parseInt( nameField.getText() );
						elm.setAttribute( "ScrollGridSize", nameField.getText() );
					}catch ( Exception ee ){}
				}
			}
		} );
		comp.getChildren().addAll( sig, nameField );
		Scene stageScene= new Scene( comp, 300, 100 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void popUpConsoleLog() {
		popUp pu= new popUp( "Console Output." );
		VBox comp= new VBox();
		TextArea ta= new TextArea( Helper.ALstr2str( p.CMB ) );
		ta.setEditable( false );
		ta.setWrapText( true );
		int w= _SysConfig.getScreenSizeX();
		int h= _SysConfig.getScreenSizeY();
		ta.setMaxWidth( w / 2 );
		ta.setMaxHeight( h / 2 );
		ta.setMinWidth( w / 2 );
		ta.setMinHeight( h / 2 );
		comp.getChildren().add( ta );
		Scene stageScene= new Scene( comp, w / 2, h / 2 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void popUpUnlock() {
		popUp pu= new popUp( "Unlock" );
		VBox comp= new VBox();
		String unlock= Helper.randA( 3 ).toLowerCase();
		Label tx= new Label( "   -- " + unlock + " --   " );
		tx.setMinWidth( 297 );
		tx.setStyle( "-fx-font-size: 26 ; -fx-alignment: center; " );
		TextField tf= new TextField();
		tf.setOnKeyTyped( e -> {
			if( tf.getText().equals( unlock ) ){
				pu.close();
				app.wallpaperMode= false;
			}
		} );
		Label des= new Label( "( type the 3 letter above, then press enter to unlock. )" );
		comp.getChildren().addAll( tx, tf, des );
		Scene stageScene= new Scene( comp, 300, 100 );
		//
		Timeline timeline= new Timeline( new KeyFrame(
				Duration.millis( 1 ),
				ae -> {
					app.switchWallPaperMode();
				} ) );
		pu.setCloseTask( timeline );
		pu.setAutoCloseMS( 16000 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void SetBg() {
		bgNode.getChildren().removeAll( bgNode.getChildren() );
		//
		if( elm.getAttribute( "BgSelection" ).equals( "img" )
				&& !elm.getAttribute( "BackGroundImage" ).equals( "null" ) ){
			try{
				FileInputStream input;
				input= new FileInputStream( elm.getAttribute( "BackGroundImage" ) );
				Image img= new Image( input );
				ImageView imgV= new ImageView();
				imgV.setImage( img );
				imgV.setTranslateZ( 4 );
				//
				focusHolder.setStyle( "-fx-background-color: rgba(1.0, 1.0, 1.0, 0.03);" );
				bgNode.getChildren().add( imgV );
				return;
			}catch ( FileNotFoundException e ){
				// silent fail.
			}
		}
		focusHolder.setStyle( "-fx-background-color: " +
				"linear-gradient(#" + elm.getAttribute( "ColorBackGround1" ) +
				", #" + elm.getAttribute( "ColorBackGround2" ) + " );" );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private int		curLocX		= 100;
	private int		curLocY		= 100;
	private boolean	lastSpacer	= false;

	private void reArrange() {
		curLocX= curLocY= 100;
		int[] gridConfig= getGridSizeConfig();
		for( Node tmp : root.getChildren() ){
			if( tmp instanceof NewLineSpacer ){
				if( !lastSpacer )
					incNewLine( gridConfig );
				continue;
			}
			if( tmp instanceof Pin ){
				( (Pin)tmp ).setLocationManaged( true );
				( (Pin)tmp ).setTranslateX( curLocX );
				( (Pin)tmp ).setTranslateY( curLocY );
				lastYind= curLocY;
				incNextLoc( gridConfig );
			}
		}
	}

	private void incNextLoc( int[] gridConfig ) {
		curLocX+= gridConfig[0] + gridConfig[2];
		if( curLocX + gridConfig[0] > app.getStageWidth() ){
			curLocX= 100;
			curLocY+= gridConfig[1] + gridConfig[3];
			lastSpacer= true;
		}else lastSpacer= false;
	}

	private void incNewLine( int[] gridConfig ) {
		curLocX= 100;
		curLocY+= gridConfig[1] + gridConfig[3];
	}
}
