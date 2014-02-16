package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 *  Compute the gradient at each pixel location in the image in N dimensions
 *  using OutOfBoundsStrategies. Here we always return the gradient as Float,
 *  no matter what the input type is
 */
public class ImgLib2_Gradient3 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Gradient3().run( null );
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
		// compute the gradient on the image
		Img<FloatType> gradient = gradient( img );

		// show the new Img that contains the gradient
		ImageJFunctions.show( gradient );
	}

	public <T extends RealType<T>> Img<FloatType> gradient( Img< T > img )
	{
		// create a new ImgLib2 image of same dimensions, but FloatType
		ImgFactory<FloatType> imgFactory = new PlanarImgFactory<FloatType>();
		Img<FloatType> gradientImg = imgFactory.create( img, new FloatType() );
				
		// create a localizing cursor on the GradientImg, it will iterate all pixels
		// and is able to efficiently return its position at each pixel, at each
		// pixel we will compute the gradient
		Cursor<FloatType> cursor = gradientImg.localizingCursor();

		// We extend the input image by a mirroring out of bounds strategy so
		// that we can access pixels outside of the image
		RandomAccessible<T> view = Views.extendMirrorSingle( img );
		
		// instantiate a RandomAccess on the extended view, it will be used to 
		// compute the gradient locally at each pixel location
		RandomAccess<T> randomAccess = view.randomAccess();

		// iterate over all pixels
		while ( cursor.hasNext() )
		{
			// move the cursor to the next pixel
			cursor.fwd();
			
			// compute gradient in each dimension
			double gradient = 0;
			
			for ( int d = 0; d < img.numDimensions(); ++d )
			{
				// set the randomaccess to the location of the cursor
				randomAccess.setPosition( cursor );

				// move one pixel back in dimension d
				randomAccess.bck( d );

				// get the value 
				double v1 = randomAccess.get().getRealDouble();

				// move twice forward in dimension d, i.e.
				// one pixel above the location of the cursor
				randomAccess.fwd( d );
				randomAccess.fwd( d );

				// get the value
				double v2 = randomAccess.get().getRealDouble();

				// add the square of the magnitude of the gradient
				gradient += ((v2 - v1) * (v2 - v1))/4;
			}

			// the square root of all quadratic sums yields
			// the magnitude of the gradient at this location,
			// set the pixel value of the gradient image
			cursor.get().set( (float)Math.sqrt( gradient ) );
		}

		return gradientImg;
	}
}