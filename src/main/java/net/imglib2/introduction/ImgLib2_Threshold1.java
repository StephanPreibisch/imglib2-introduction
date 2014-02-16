package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Perform a threshold on an img (32-bit only) using Collections
 */
public class ImgLib2_Threshold1 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Threshold1().run( null );
	}

	public void run(String arg0)
	{
		// get the current ImageJ ImagePlus
		ImagePlus imp = WindowManager.getCurrentImage();
		
		// test if an image is open, otherwise load blobs
		if ( imp == null )
		{
			imp = new ImagePlus( getClass().getResource( "/blobs.tif" ).getFile() );
			imp.show();
		}
		
		// wrap it into an ImgLib2 Img (no copying)
		Img<FloatType> img = ImageJFunctions.wrapFloat( imp );

		// test if it could be wrapped
		if ( img == null )
		{
			IJ.log( "Cannot wrap image" );
			return;
		}

		// process wrapped image with ImgLib2
		process( img );

		// re-draw the ImgPlus instance
		// works because we actually changed the img itself
		imp.updateAndDraw();
	}

	public void process( Img<FloatType> img )
	{
		// define threshold
		float threshold = 100;

		// compute the threshold on the image in-place
		// (overwrite each pixel with the threshold value)
		threshold( img, threshold );
	}

	public void threshold( Img< FloatType > img, float threshold )
	{
		// for every pixel do
		for ( FloatType pixelValue : img )
		{
			// set the 0 or 255 depending on the value
			if ( pixelValue.get() > threshold )
				pixelValue.set( 255 );
			else
				pixelValue.set( 0 );
		}
	}
}