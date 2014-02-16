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
 * Compute the center of mass of the image in N dimensions
 */
public class ImgLib2_CenterOfMass2 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_CenterOfMass2().run( null );
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
		double[] center = centerOfMass( img );

		// print out center of mass into the ImageJ log window
		IJ.log( "Center of mass = " + Util.printCoordinates( center ) );
	}

	public <T extends RealType<T>> double[] centerOfMass( Img< T > img )
	{
		// the center of mass in n dimensions,
		// same amount of dimensions as the image
		double[] center = new double[ img.numDimensions() ];
		
		// create a localizing cursor on the Img, it will iterate all pixels
		// and is able to efficiently return its position at each pixel
		Cursor<T> cursor = img.localizingCursor();

		// the sum of all pixel intensities for n dimensions
		double[] sumDim = new double[ img.numDimensions() ];
		
		// the sum of all intensities
		double sumI = 0;

		// iterate over all pixels
		while ( cursor.hasNext() )
		{
			// move the cursor to the next pixel
			cursor.fwd();

			// intensity of the pixel
			double i = cursor.get().getRealDouble();
			
			// sum up the location weighted by the intensity for each dimension
			for ( int d = 0; d < img.numDimensions(); ++d )
				sumDim[ d ] += cursor.getLongPosition( d ) * i;

			// sum up the intensities
			sumI += i;
		}

		// compute center of mass for all dimensions
		for ( int d = 0; d < img.numDimensions(); ++d )
			center[ d ] = sumDim[ d ] / sumI;

		return center;
	}
}