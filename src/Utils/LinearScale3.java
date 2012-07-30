package Utils;

/**
 * LinearScale3 is a common math object that easily computes linear scales in 3D.
 * Computes Y from X with the following relation :
 * Y = (A * X) + B, where all symbols are Vector3.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 * @since	2.0
 */
public class LinearScale3
{
	// A * X + B where X(x,y)
	protected Vector3 A;
	protected Vector3 B;
	
	public LinearScale3(Vector2 FromX, Vector2 FromY, Vector2 FromZ, Vector2 ToX, Vector2 ToY, Vector2 ToZ)
	{
		double[] xCoefs = computeCoefs(FromX.x, FromX.y, ToX.x, ToX.y);
		double[] yCoefs = computeCoefs(FromY.x, FromY.y, ToY.x, ToY.y);
		double[] zCoefs = computeCoefs(FromZ.x, FromZ.y, ToZ.x, ToZ.y);
		
		A = new Vector3(xCoefs[0], yCoefs[0], zCoefs[0]);
		B = new Vector3(xCoefs[1], yCoefs[1], zCoefs[1]);
	}
	
	public LinearScale3(Vector3 FromMin, Vector3 FromMax, Vector3 ToMin, Vector3 ToMax)
	{
		double[] xCoefs = computeCoefs(FromMin.x, FromMax.x, ToMin.x, ToMax.x);
		double[] yCoefs = computeCoefs(FromMin.y, FromMax.y, ToMin.y, ToMax.y);
		double[] zCoefs = computeCoefs(FromMin.z, FromMax.z, ToMin.z, ToMax.z);
		
		A = new Vector3(xCoefs[0], yCoefs[0], zCoefs[0]);
		B = new Vector3(xCoefs[1], yCoefs[1], zCoefs[1]);
	}
	
	/**
	 * Computes A and B coefficients for a 1D linear scale (y = a * x + b)
	 * 
	 * @param minFrom Lower bound of original interval
	 * @param maxFrom Higher bound of original interval
	 * @param minTo Lower bound of destination interval
	 * @param maxTo Higher bound of destination interval
	 * @return A and B coefficients
	 */
	private double[] computeCoefs(double minFrom, double maxFrom, double minTo, double maxTo)
	{
		double a, b;
		if(minFrom == maxFrom || minTo == maxTo)
		{
			a = 0;
			b = minTo;
		}
		
		// minFrom != 0 && minFrom != maxFrom
		else if(minFrom != 0)
		{
			double r = maxFrom / minFrom;
			b = ((maxTo - (minTo*r))/(1 - r));
			a = ((minTo - b) / minFrom);
		}
		
		// minFrom == 0 && minFrom != maxFrom
		else
		{
			b = minTo;
			a = ((maxTo - b) / maxFrom);
		}
		
		return new double[]{a, b};
	}
	
	/**
	 * Alias : Scale
	 * @param x The abscissa to scale
	 * @param y The ordinate to scale
	 * @param z The elevation to scale
	 * @return A float array like {new_x, new_y}
	 */
	public float[] Scale(double x, double y, double z) {
		return Scale(new Vector3(x, y, z)).toFloatArray();
	}
	
	/**
	 * Computes and return the scaled vector :  Y = (A * X) + B
	 * @param X The vector to scale
	 * @return The scaled vector
	 */
	public Vector3 Scale(Vector3 X)
	{
		Vector3 result = new Vector3(X);
		return result.Mul(A).Add(B);
	}
	
	/**
	 * Return a readable string which contains A and B vectors.
	 * Usefull for debug.
	 * @return A readable string which describes this object
	 */
	@Override
	public String toString() {
		return "A = " + A.toString() + "\n B = " + B.toString();
	}
}
