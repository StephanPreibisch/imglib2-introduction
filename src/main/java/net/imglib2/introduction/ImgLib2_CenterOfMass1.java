package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

/**
 * Compute the center of mass of the image in two dimensions (x,y)
 */
public class ImgLib2_CenterOfMass1 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_CenterOfMass1().run( null );
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
	}

	public <T extends RealType<T>> void process( Img<T> img )
	{
		// compute the threshold on the image in-place
		// (overwrite each pixel with the threshold value)
		double[] center = centerOfMass2d( img );

		// print out center of mass into the ImageJ log window
		IJ.log( "Center of mass = " + Util.printCoordinates( center ) );
	}

	public <T extends RealType<T>> double[] centerOfMass2d( Img< T > img )
	{
		// the center of mass (x,y)
		double[] center = new double[ 2 ];
		
		// create a localizing cursor on the Img, it will iterate all pixels
		// and is able to efficiently return its position at each pixel
		Cursor<T> cursor = img.localizingCursor();

		// the sum of all pixel intensities for x and y;
		double sumX = 0;
		double sumY = 0;

		// the sum of all intensities
		double sumI = 0;

		// iterate over all pixels
		while ( cursor.hasNext() )
		{
			// move the cursor to the next pixel
			cursor.fwd();

			// location of the pixel in x
			double x = cursor.getLongPosition( 0 );

			// location of the pixel in y
			double y = cursor.getLongPosition( 1 );

			// intensity of the pixel
			double i = cursor.get().getRealDouble();

			// sum up the location weighted by the intensity for x and y
			sumX += x * i;
			sumY += y * i;

			// sum up the intensities
			sumI += i;
		}

		// compute center of mass for x and y
		center[ 0 ] = sumX / sumI;
		center[ 1 ] = sumY / sumI;

		return center;
	}
}