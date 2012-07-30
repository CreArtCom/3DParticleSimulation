package Utils;

/**
 * Plane is a common implementation of a 3D infinite plane.
 * Every plane verify this equation for every (x, y, z) : (a * x) + (b * y) + (c * z) + d = 0
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 * @since	2.0
 */
public class Plane
{
	/** Coefficient "a" in the equation : (a * x) + (b * y) + (c * z) + d = 0 */
	protected double a;
	
	/** Coefficient "b" in the equation : (a * x) + (b * y) + (c * z) + d = 0 */
	protected double b;
	
	/** Coefficient "c" in the equation : (a * x) + (b * y) + (c * z) + d = 0 */
	protected double c;
	
	/** Coefficient "c" in the equation : (a * x) + (b * y) + (c * z) + d = 0 */
	protected double d;
	
	/** Precision use for computes. Values below this one are considered null. */
	protected double precision;
	
	/**
	 * Construct a line with its coeficients' equation : (a * x) + (b * y) + (c * z) + d = 0
	 * @param a The a value
	 * @param b The b value
	 * @param c The c value
	 * @param d The d value
	 * @param precision Precision value
	 */
	public Plane(double a, double b, double c, double d, double precision) {
		this.a			= a;
		this.b			= b;
		this.c			= c;
		this.d			= d;
		this.precision	= precision;
	}
	
	/**
	 * Construct a line with its coeficients' equation : (a * x) + (b * y) + (c * z) + d = 0
	 * @param a The a value
	 * @param b The b value
	 * @param c The c value
	 * @param d The d value
	 */
	public Plane(double a, double b, double c, double d) {
		this(a, b, c, d, 0.);
	}
	
	public Plane(double x, double y, double z, double rotX, double rotY, double rotZ) {
		this(0., 0., 1., 0., 0.);// Plane z = 0
//		rotate(rotX, rotY, rotZ);
//		setPosition(x, y, z);
	}
	
	/**
	 * Determine if the given point is on the line
	 * @param point The given point to test
	 * @return <code>true</code> if the point is on the line, <code>else</code> otherwise
	 */
	public boolean IsOn(Vector3 point) {
		return Math.abs((a * point.x) + (b * point.y) + (c * point.z) + d) <= precision;
	}

	public Vector3 getNormal() {
		return new Vector3(a, b, c).Normalise();
	}

	public double getD() {
		return d;
	}
	
	public Vector3 getAPoint()
	{
		if(a != 0)
			return new Vector3(-d/a, 0., 0.);
		else if(b != 0)
			return new Vector3(0., -d/b, 0.);
		else if(c != 0)
			return new Vector3(0., 0., -d/c);
		else
			return null;
	}
	
	public Vector3 getOrthographicProjection(Vector3 OP) {
		if(IsOn(OP))
			return OP;
		else
			return new Vector3(getNormal()).Scalar(getNormal().Dot(new Vector3(getAPoint()).Subtract(OP)) / getNormal().getNorm()).Add(OP);
	}

	public final void rotate(double rotX, double rotY, double rotZ)
	{
		// X Rotation
		if(rotX != 0) {
			double x = Math.PI * rotX / 180;
			double newB = (b * Math.cos(x)) - (c * Math.sin(x));
			c = (b * Math.sin(x)) + (c * Math.cos(x));
			b = newB;
		}
		
		// Y Rotation
		if(rotY != 0) {
			double y = Math.PI * rotY / 180;
			double newA = (a * Math.cos(y)) - (c * Math.sin(y));
			c = (a * Math.sin(y)) + (c * Math.cos(y));
			a = newA;
		}
		
		// Z Rotation
		if(rotZ != 0) {
			double z = Math.PI * rotZ / 180;
			double newA = (a * Math.cos(z)) - (b * Math.sin(z));
			b = (a * Math.sin(z)) + (b * Math.cos(z));
			a = newA;
		}
	}

	public final void setPosition(double tx, double ty, double tz)
	{
		if(Math.abs(a) > precision)
			d = tx / a;
		else if(Math.abs(b) > precision)
			d = tx / b;
		else if(Math.abs(c) > precision)
			d = tx / c;
	}
}
