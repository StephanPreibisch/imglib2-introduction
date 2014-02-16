package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import mpicbg.models.InvertibleBoundable;
import mpicbg.models.NoninvertibleModelException;
import mpicbg.models.RigidModel2D;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 *  Transform an image in N dimensions using Linear interpolation
 *  and OutOfBoundsStrategies
 */
public class ImgLib2_Transform implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Transform().run( null );
	}

	public void run(String arg0)
	{
		// get the current ImageJ ImagePlus
		ImagePlus imp = WindowManager.getCurrentImage();
		
		Img<FloatType> img;
		
		// test if an image is open, otherwise load blobs
		if ( imp == null )
		{
			// create the ImgOpener
	        ImgOpener imgOpener = new ImgOpener();

	        // load the image as FloatType using the ArrayImg
	        try 
	        {
				img = imgOpener.openImg( getClass().getResource( "/Drosophila.tif.zip" ).getFile(), new ArrayImgFactory<FloatType>(), new FloatType() );
			} 
	        catch (ImgIOException e) 
	        {
				e.printStackTrace();
				return;
			}
	        
	        // display the image
	        ImageJFunctions.show( img );
		}
		else
		{
			// wrap it into an ImgLib2 Img (no copying)
			img = ImageJFunctions.wrapFloat( imp );	
		}

		// process wrapped image with ImgLib2
		process( img );
	}

	public <T extends RealType<T>> void process( Img<T> img )
	{
		// Define the transformation model
		RigidModel2D model = new RigidModel2D();
		model.set( (float)Math.toRadians( 15 ),  0, 0 );

		// Define the transformation model
		//TranslationModel2D model = new TranslationModel2D();
		//model.set( 10.1f, -12.34f );

		try
		{
			// compute the gradient on the image
			Img<T> transformed = transform( img, model );
	
			// show the new Img that contains the gradient
			ImageJFunctions.show( transformed );
		}
		catch ( NoninvertibleModelException e )
		{
			IJ.log( model + " cannot be inverted: " + e );
		}
	}

	public <T extends RealType<T>> Img<T> transform( Img< T > img, InvertibleBoundable transform ) throws NoninvertibleModelException
	{
		// create a new ImgLib2 image of same type & dimensions
		ImgFactory<T> imgFactory = img.factory();
		Img<T> transformedImg = imgFactory.create( img, img.firstElement() );
				
		// create a localizing cursor on the TransformedImg, it will iterate all pixels
		// and is able to efficiently return its position at each pixel. At each pixel
		// we will look up the intensity at the corresponding, transformed location in
		// the input image
		Cursor<T> cursor = transformedImg.localizingCursor();

		// We extend the input image by a value out of bounds strategy so
		// that we can access pixels outside of the image
		T background = img.firstElement().copy();
		background.setReal( 0 );
		RandomAccessible<T> view = Views.extendValue( img, background );

		// We create a RealRandomAccessible by linear interpolating the view, i.e. we can 
		// compute a pixel value at any floating point location (e.g. 4.5, 5.1, ...)
		NLinearInterpolatorFactory< T > factory = new NLinearInterpolatorFactory< T >();
		RealRandomAccessible< T > interpolant = Views.interpolate( view, factory );

		// instantiate a RealRandomAccess on the extended, interpolated view
		RealRandomAccess< T > realRandomAccess = interpolant.realRandomAccess();

		// a temporary array to compute the transformation
		float[] tmp = new float[ img.numDimensions() ];
	
		// iterate over all pixels
		while ( cursor.hasNext() )
		{
			// move the cursor to the next pixel
			cursor.fwd();

			// get the location of the cursor
			cursor.localize( tmp );

			// apply the transformation
			transform.applyInverseInPlace( tmp );
			
			// set the interpolator to the transformed location
			realRandomAccess.setPosition( tmp );
			
			// set the value to the cursor
			cursor.get().set( realRandomAccess.get() );
		}

		return transformedImg;
	}
}