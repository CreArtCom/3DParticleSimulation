package Simulation;

import Utils.LinearScale3;
import Utils.Vector3;
import com.cycling74.jitter.JitterMatrix;
import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;

/**
 * Max is a MaxObject's abstraction that provide basic stuffs for it's 
 * children.
 * Please note that Max is an abstract class and each children will be 
 * an instantiable Max Object.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 * @since 2.0
 */
public abstract class Max extends MaxObject
{
	// Bornes coordonnées GL
	public static Vector3 GL_MIN = new Vector3(-1., -1., -1.);
	public static Vector3 GL_MAX = new Vector3(1., 1., 1.);
	
	// Bornes coordonnées ENGINE
	public static Vector3 ENGINE_MIN = new Vector3(-1., -1., -1.);
	public static Vector3 ENGINE_MAX = new Vector3(1., 1., 1.);
	
	protected static String VERSION	= "1.0";
	
	// Messages
	/** Jitter message to determine a matrix */
    public static String MSG_MATRIX	= "jit_matrix";
	
	/** Common message for reset */
	public static String MSG_RESET	= "reset";
	
	/** Message used to set up input's positions scale on X axis : ~ min max */
	public static String MSG_XSCALE	= "x_scale";
	
	/** Message used to set up input's positions scale on Y axis : ~ min max */
	public static String MSG_YSCALE	= "y_scale";
	
	/** Message used to set up input's positions scale on Z axis : ~ min max */
	public static String MSG_ZSCALE	= "z_scale";
	
	// Attributes
	/** LinearScale used to scale output position (from engine to output) */
	protected LinearScale3 scaleTo;
	
	/** LinearScale used to scale input position (from outside to engine) */
	protected LinearScale3 scaleFrom;
	
	/** Outdoor minimum positions use for scaling : (xmin, ymin, zmin) */
	protected Vector3 outdoorMin;
	
	/** Outdoor maximum positions use for scaling : (xmax, ymax, zmax) */
	protected Vector3 outdoorMax;
	
	/** 
	 * Nowhere matrix : a two plane 2D matrix with one cell per plane.
	 * Values are both Float.MAX_VALUE
	 * This matrix is used because it's impossible to send a blank matrix in 
	 * max. Use it when you want to notify that nothing is outputed (that is 
	 * different to not output).
	 */
	public JitterMatrix nowhereMatrix;
	
	/**
	 * Abstract constructor.
	 * Create output info and initialise its members.
	 * @param args Max Object's  arguments - not use but required
	 */
	protected Max(Atom[] args)
	{
		createInfoOutlet(true);
		
		outdoorMin		= new Vector3(ENGINE_MIN);
		outdoorMax		= new Vector3(ENGINE_MAX);
		scaleTo			= new LinearScale3(ENGINE_MIN, ENGINE_MAX, GL_MIN, GL_MAX);
		scaleFrom		= new LinearScale3(outdoorMin, outdoorMax, ENGINE_MIN, ENGINE_MAX);
		nowhereMatrix	= new JitterMatrix("nowhere", 3, "float32", 1, 1);
		nowhereMatrix.setcell2d(0, 0, new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE});
	}
	
	/**
	 * Send a string to the info's output
	 * @param msg 
	 */
	public void printOut(String msg) {
		outlet(getInfoIdx(), msg);
	}
	
	/**
	 * Report a unknown message to the max console
	 * @param message Unknown message's header
	 * @param args Unknown message's parameters
	 */
	protected void unknownMessage(String message, Atom[] args)
	{
		String argsError = "";

		for(Atom atom : args)
			argsError += " " + atom.toString();

		error("Unknown message : " + message + argsError);
	}
	
	/**
	 * Execute appropriates routines for known messages
	 * @param message Max's header message
	 * @param args Max's parameters message
	 * @return <code>true</code> if the message in unknown, <code>false</code> otherwise
	 */
	protected boolean TreatMessage(String message, Atom[] args)
	{
		if(getInlet() == 0 && args.length == 2)
		{
			if(message.contentEquals(MSG_XSCALE))
			{
				outdoorMin.x	= args[0].toDouble();
				outdoorMax.x	= args[1].toDouble();
				scaleFrom		= new LinearScale3(outdoorMin, outdoorMax, ENGINE_MIN, ENGINE_MAX);
			}
			
			else if(message.contentEquals(MSG_YSCALE))
			{
				outdoorMin.y	= args[0].toDouble();
				outdoorMax.y	= args[1].toDouble();
				scaleFrom		= new LinearScale3(outdoorMin, outdoorMax, ENGINE_MIN, ENGINE_MAX);
			}
			
			else if(message.contentEquals(MSG_ZSCALE))
			{
				outdoorMin.z	= args[0].toDouble();
				outdoorMax.z	= args[1].toDouble();
				scaleFrom		= new LinearScale3(outdoorMin, outdoorMax, ENGINE_MIN, ENGINE_MAX);
			}
			
			else
				return false;
		}
		else
			return false;
		
		return true;
	}
}
