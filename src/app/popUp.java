package app;

import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import machine.p;



public class popUp {
	private Stage		newStage= null;
	private String		name	= null;
	private Timeline	ct		= null;
	private int			autoC	= 0;

	public popUp( String name ) {
		this.name= name;
	}

	public void set( Scene cont, boolean closeOnloseF ) {
		newStage= new Stage();
		newStage.setTitle( name );
		newStage.setScene( cont );
		newStage.focusedProperty().addListener( new ChangeListener <Boolean>() {
			@Override
			public void changed( ObservableValue <? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue ) {
				if( newPropertyValue ){
					// on focus.
				}else{
					// lost focus.			
					if( closeOnloseF ){
						newStage.close();
						if( ct != null )
							ct.play();
					}
				}
			}
		} );
		newStage.show();
		if( autoC > 0 ){
			Timeline ac= new Timeline( new KeyFrame(
					Duration.millis( autoC ),
					ae -> {
						newStage.close();
					} ) );
			ac.play();
		}
	}

	public void close() {
		if( newStage != null ){
			newStage.close();
			if( ct != null )
				ct.play();
		}
	}

	public void setCloseTask( Timeline tt ) {
		ct= tt;
	}

	public void setAutoCloseMS( int t ) {
		autoC= t;
	}
}