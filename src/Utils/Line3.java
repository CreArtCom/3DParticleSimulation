package Utils;

/**
 * Line3 is a common implementation of a 3D infinite line.
 * Every lines verify this equation for every X : X = director * t + point
 * Where X, director and point are 3D vectors, and t double
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 * @since	2.0
 */
public class Line3
{
	Vector3 director;
	Vector3 point;
	
	/** Precision use for computes. Values below this one are considered null. */
	protected double precision;

	/**
	 * Construct a line with two vectors
	 * @param director Line's vector director
	 * @param point Point which is on the line
	 * @param precision Precision value
	 */
	public Line3(Vector3 director, Vector3 point, double precision)
	{
		this.director	= new Vector3(director).Normalise();
		this.point		= new Vector3(point);
		this.precision	= precision;
	}
	
	public Line3(Vector3 director, Vector3 point) {
		this(director, point, 0.);
	}
	
	public Line3(Vector3 director) {
		this(director, new Vector3(), 0.);
	}
	
	public Line3(double precision, Vector3 A, Vector3 B) {
		this(new Vector3(B).Subtract(A), A, precision);
	}
	
	/**
	 * Determine if the given point is on the line
	 * @param point The given point to test
	 * @return <code>true</code> if the point is on the line, <code>else</code> otherwise
	 */
	public boolean IsOn(Vector3 point)
	{
		double t;
		
		if(Math.abs(director.x) > precision)
			t = (point.x - this.point.x) / director.x;
		else if(Math.abs(director.y) > precision)
			t = (point.y - this.point.y) / director.y;
		else
			t = (point.z - this.point.z) / director.z;
		
		return ((point.x - ((director.x * t) + this.point.x)) < precision) && ((point.y - ((director.y * t) + this.point.y)) < precision) && ((point.z - ((director.z * t) + this.point.z)) < precision);
	}
	
	/**
	 * Get the director normalised vector of this line
	 * @return The director normalized vector
	 */
	public Vector3 getDirector() {
		return new Vector3(director);
	}
	
	public Vector3 intersect(Plane plane)
	{
		Vector3 n = plane.getNormal();
		
		// There is one intersection point
		if(Math.abs(director.Dot(n)) > precision) {
			double k = -(plane.getD() + n.Dot(point)) / n.Dot(director);
			return new Vector3(director).Scalar(k).Add(point);
			
		}
		else {
			// There is infinite intersections points
			if(plane.IsOn(point)) {
				return null;
			}
			// There is no intersection point
			else
				return null;
		}
	}
	
	public Vector3 getOrthographicProjection(Vector3 OP) {
		return new Vector3(director).Scalar(director.Dot(new Vector3(OP).Subtract(point))).Add(point);
	}
}
