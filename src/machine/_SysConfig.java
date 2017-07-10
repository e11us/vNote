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
	//
	private static int			ScreenSizeX			= 0;
	private static int			ScreenSizeY			= 0;
	//
	private final static String	masterConfigFileName= "_ESMconfig.xml";
	private static boolean		_initDone			= false;
	private static File			masterConfig		= null;
	private static File			boardFolder			= null;
	private static Element		masterConfigElm		= null;
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
		maxWindowOnStart= Boolean.parseBoolean(
				masterConfigElm.getAttribute( "ESM_MaxWindowOnStart" ) );
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
				Helper.randAN( 16 ) + ".xml" );
		while( defDesktop.exists() ){
			defDesktop= new File( masterConfigElm.getAttribute( "ESM_BoardFolderName" ) + File.separatorChar +
					Helper.randAN( 16 ) + ".xml" );
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
