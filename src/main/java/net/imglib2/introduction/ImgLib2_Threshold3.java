package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Perform a threshold on an image (all RealTypes),
 * display the result in a new ImgLib2 Img.
 */
public class ImgLib2_Threshold3 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Threshold3().run( null );
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
		Img< FloatType > img = ImageJFunctions.wrapFloat( imp );

		if ( img == null )
		{
			IJ.log( "Cannot wrap image" );
			return;
		}
		
		// process wrapped image with ImgLib2
		process( img );
	}

	public <T extends RealType<T>> void process( Img<T> img )
	{
		// define threshold
		float threshold = 100;

		// compute the threshold on the image
		// and write the result into a new one
		Img<T> thresholdImg = threshold( img, threshold );

		// show the new Img that contains the threshold
		ImageJFunctions.show( thresholdImg );
	}

    public <T extends RealType<T>> Img<T> threshold( Img< T > img, float threshold )
	{
		// create a new ImgLib2 image of same type & dimensions
		ImgFactory<T> imgFactory = img.factory();
		Img<T> thresholdImg = imgFactory.create( img, img.firstElement() );

		// create a cursor on the Img and the destination, it will iterate all pixels
		Cursor<T> cursor = img.cursor();
		Cursor<T> cursorThresholdImg = thresholdImg.cursor();
		
		// iterate over all pixels
		while ( cursor.hasNext() )
		{
			// get the value of the next pixel in the input
			T pixelValue = cursor.next();

			// get the value of the next pixel in the output
			T thresholdImgValue = cursorThresholdImg.next();

			// set the 0 or 255 depending on the value
			if ( pixelValue.getRealFloat() > threshold )
				thresholdImgValue.setReal( 255 );
			else
				thresholdImgValue.setReal( 0 );	
		}

		return thresholdImg;
	}
	
}