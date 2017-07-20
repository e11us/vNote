package machine;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import robo.ClipBoard;
import robo.MagnetLinkChecker;
import robo._SystemClock;
import java.io.File;
import java.util.ArrayList;
import javafx.scene.Node;
import javax.xml.parsers.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;



public class _SysConfig {
	public final static String	sysFolderName		= "Sys";
	public final static String	backgroundFolderName= "Background";
	public final static String	cursorFoldername	= "cursor";
	public final static int		newBoardNameLength	= 16;
	//
	private static int			ScreenSizeX			= 0;
	private static int			ScreenSizeY			= 0;
	//
	private final static String	masterConfigFileName= "_ESMconfig.xml";
	private static boolean		_initDone			= false;
	private static File			masterConfig		= null;
	private static File			boardFolder			= null;
	private static Element		masterConfigElm		= null;
	private static String[]		SQL_LI				= new String[3];
	//
	private static boolean		maxWindowOnStart	= false;

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	public static boolean maxWindowOnStart() {
		return maxWindowOnStart;
	}

	public static String getSysFolderName() {
		return sysFolderName;
	}

	public static int getScreenSizeX() {
		return ScreenSizeX;
	}

	public static int getScreenSizeY() {
		return ScreenSizeY;
	}

	public static String[] getSQL_LI() {
		return SQL_LI;
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	public static int init() {
		//
		// check init first.
		if( _initDone ){
			p.p( "machine._SysConfig", "sys init completed already" );
			return 0;
		}
		//
		// init the clock for the clipboard monitor.
		_SystemClock.addClock( ClipBoard._init(), 500 );
		_SystemClock.addClock( MagnetLinkChecker._init(), 500 );
		//
		Dimension screenSize= Toolkit.getDefaultToolkit().getScreenSize();
		ScreenSizeX= (int)screenSize.getWidth();
		ScreenSizeY= (int)screenSize.getHeight();
		//
		masterConfig= new File( masterConfigFileName );
		if( !masterConfig.exists() ){
			if( !createMasterConfigFile() )
				return 101;
			if( !writeDefMasterConfigFile() )
				return 102;
		}
		if( !loadMasterConfigFile() )
			return 105;
		//
		// load config.
		maxWindowOnStart= Boolean.parseBoolean(
				masterConfigElm.getAttribute( "ESM_MaxWindowOnStart" ) );
		SQL_LI[0]= masterConfigElm.getAttribute( "ESM_SQL_address" );
		SQL_LI[1]= masterConfigElm.getAttribute( "ESM_SQL_user" );
		SQL_LI[2]= masterConfigElm.getAttribute( "ESM_SQL_password" );
		//
		p.p( "machine._SysCOnfig", "ESM init running, time: " +
				Helper.getCurrentTimeStampMS() + " Total board: " +
				getTotBoardinESMB() );
		//
		// set the flag.
		_initDone= true;
		p.p( "machine._SysConfig", "sys init completed." );
		return 0;
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	public static File getDeskTopFile() {
		File defDesktop= new File( masterConfigElm.getAttribute( "ESM_BoardFolderName" ) + File.separatorChar +
				masterConfigElm.getAttribute( "ESM_BoardDeskTopName" ) );
		return defDesktop;
	}

	public static File getESMBoardFile() {
		return new File( masterConfigElm.getAttribute( "ESM_BoardFolderName" ) );
	}

	public static int getTotBoardinESMB() {
		File esmb= getESMBoardFile();
		return Helper.getAllFile( esmb.toString(), "xml" ).size();
	}

	public static File getESMBoardBkFolder() {
		return new File( masterConfigElm.getAttribute( "ESM_BoardFolderName" ) + "_BK" );
	}

	public static String getNewRandPath() {
		File defDesktop= new File( masterConfigElm.getAttribute( "ESM_BoardFolderName" ) + File.separatorChar +
				Helper.randAN( newBoardNameLength ) + ".xml" );
		while( defDesktop.exists() ){
			defDesktop= new File( masterConfigElm.getAttribute( "ESM_BoardFolderName" ) + File.separatorChar +
					Helper.randAN( newBoardNameLength ) + ".xml" );
		}
		return defDesktop.getPath().replace( '\\', '/' );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private static boolean loadMasterConfigFile() {
		try{
			File msc= new File( "." + File.separatorChar + masterConfigFileName );
			if( msc.canRead() ){
				DocumentBuilderFactory dbFactory= DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder= dbFactory.newDocumentBuilder();
				Document doc= dBuilder.parse( msc );
				doc.getDocumentElement().normalize();
				masterConfigElm= doc.getDocumentElement();
				if( !createBoardFolder( masterConfigElm.getAttribute( "ESM_BoardFolderName" ) ) )
					return false;
				// all good return true.
				return true;
			}else return false;
		}catch ( Exception ee ){
			ee.printStackTrace();
		}
		return false;
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private static boolean createMasterConfigFile() {
		try{
			File msc= new File( "." + File.separatorChar + masterConfigFileName );
			msc.createNewFile();
			return true;
		}catch ( Exception ee ){
			ee.printStackTrace();
		}
		return false;
	}
	/*
	private static void completeMasterXML() {
		if( masterConfigElm.getAttribute( "ESM_BoardFolderName" ).equals( "" ) )
			masterConfigElm.setAttribute( "ESM_BoardFolderName", "ESMB" );
		if( masterConfigElm.getAttribute( "ESM_BoardDeskTopName" ).equals( "" ) )
			masterConfigElm.setAttribute( "ESM_BoardDeskTopName", "_DeskTop.xml" );
		if( masterConfigElm.getAttribute( "ESM_MaxWindowOnStart" ).equals( "" ) )
			masterConfigElm.setAttribute( "ESM_MaxWindowOnStart", "true" );
		if( masterConfigElm.getAttribute( "ESM_SQL_address" ).equals( "" ) )
			masterConfigElm.setAttribute( "ESM_SQL_address", "www.example.com" );
		if( masterConfigElm.getAttribute( "ESM_SQL_user" ).equals( "" ) )
			masterConfigElm.setAttribute( "ESM_SQL_user", "user" );
		if( masterConfigElm.getAttribute( "ESM_SQL_password" ).equals( "" ) )
			masterConfigElm.setAttribute( "ESM_SQL_password", "pass" );
	}
	*/

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private static boolean writeDefMasterConfigFile() {
		XMLOutputFactory xof= XMLOutputFactory.newInstance();
		XMLStreamWriter xtw= null;
		try{
			xtw= xof.createXMLStreamWriter( new FileWriter( "." + File.separatorChar + masterConfigFileName ) );
			//xtw.writeStartDocument("utf-8","1.0");
			xtw.writeStartDocument();
			xtw.writeStartElement( "config" );
			// properties.
			xtw.writeAttribute( "ESM_BoardFolderName", "ESMB" );
			xtw.writeAttribute( "ESM_BoardDeskTopName", "_DeskTop.xml" );
			xtw.writeAttribute( "ESM_MaxWindowOnStart", "true" );
			xtw.writeAttribute( "ESM_SQL_address", "www.example.com" );
			xtw.writeAttribute( "ESM_SQL_user", "user" );
			xtw.writeAttribute( "ESM_SQL_password", "pass" );
			// end.
			xtw.writeEndElement();
			xtw.writeEndDocument();
			xtw.flush();
			xtw.close();
		}catch ( XMLStreamException | IOException e ){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private static boolean createBoardFolder( String inp ) {
		try{
			File bdf= new File( "." + File.separatorChar + inp );
			bdf.mkdirs();
			return true;
		}catch ( Exception ee ){
			ee.printStackTrace();
		}
		return false;
	}
}
