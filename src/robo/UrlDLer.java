package robo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import machine.Helper;



/* -----------------------------------------------------------------------------
example:
	new DL( "./folder/folder", "abc.txt", "http://exp.com" );

example:
	DL dler= new DL( ".", "out.mp3", "http://abc.mp3" );
	while( dler.isAlive() ){}
	System.out.println(  dler.isSucc() );

will create new file if already exists.
* -----------------------------------------------------------------------------
*/
public class UrlDLer implements Runnable {
	private String	url	= null;
	private File	path= null;
	private Thread	runs= null;

	public UrlDLer( String filepathInp, String filenameInp, String urlInp, boolean threaded, boolean skipExisting ) {
		if( filepathInp == null || filenameInp == null || urlInp == null )
			return;
		// check file path.
		File filepath= new File( filepathInp );
		if( !filepath.exists() )
			filepath.mkdirs();
		// check file.
		path= new File( filepathInp + "/" + filenameInp );
		if( path.exists() && path.isFile() ){
			if( skipExisting ){
				return;
			}else{
				path= new File( Helper.existingFileName( filepathInp + "/"
						+ filenameInp ) );
			}
		}
		this.url= urlInp;
		// starts to run it.
		if( threaded ){
			runs= new Thread( this, "DLerThread_" + Helper.randAN( 3 ) + "_" );
			runs.start();
		}else DL();
	}

	@Override
	public void run() {
		DL();
	}

	private void DL() {
		if( url == null || path == null )
			return;
		try{
			URL website= null;
			ReadableByteChannel rbc= null;
			FileOutputStream fos= null;
			website= new URL( url );
			rbc= Channels.newChannel( website.openStream() );
			fos= new FileOutputStream( path.getAbsolutePath() );
			fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );
			fos.close();
			rbc.close();
		}catch ( IOException e ){
			e.printStackTrace();
		}
	}
}