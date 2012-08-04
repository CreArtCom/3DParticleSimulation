package Simulation3D;

import BlobsSystem3D.Blob;
import MagnetsSystem3D.MagnetsSystem;
import ParticlesSystem3D.ParticlesSystem;
import Utils3D.Line3;
import Utils3D.Plane;
import Utils3D.Vector3;
import com.cycling74.jitter.JitterMatrix;
import com.cycling74.max.Atom;

/**
 * Particles is a max instatiable object that is responsible for the particles
 * system simulation.
 * This class initialise needed objects and route messages to determine what 
 * action to do for each known message.
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 */
public class Particles extends Simulation
{
	// Particles' system settings messages
    /** 
	 * Particle system's stiffness message : ~ stiffness
	 * @see ParticlesSystem.ParticlesSystem
	 */
    public static String MSG_STIFFNESS = "stiffness";
	
    /** 
	 * Particle system's momentum message : ~ momentum
	 * @see ParticlesSystem.ParticlesSystem
	 */
    public static String MSG_MOMENTUM = "momentum";
	
    /** 
	 * Particle system's threshold message : ~ min max
	 * @see ParticlesSystem.ParticlesSystem
	 */
	public static String MSG_PARTSEUIL = "seuil";
	
    /** 
	 * Particle system's friction message : ~ friction
	 * @see ParticlesSystem.ParticlesSystem
	 */
	public static String MSG_FRICTION = "friction";
	
    /** 
	 * Particle system's memory message : ~ memory
	 * @see ParticlesSystem.ParticlesSystem
	 */
	public static String MSG_MEMORY = "memory";
	
    /** 
	 * Particle system's maximum message : ~ max
	 * @see ParticlesSystem.ParticlesSystem
	 */
	public static String MSG_MAXPART = "max";
	
    /** 
	 * Particle system's edges message : ~ left bottom right top
	 * @see ParticlesSystem.ParticlesSystem
	 */
	public static String MSG_EDGES = "edges";
	
	
	// Magnets settings messages
    /** Magnet apply message : ~ apply */
	public static String MSG_MAGNET_APPLY = "magnet_apply";
	
	/** Magnet add vertical ligne message : ~ */
	public static String MSG_MAGNET_RESET = "magnet_reset";
	
	/** Magnet list indexes message : ~ */
	public static String MSG_MAGNET_LIST = "magnet_list";
	
	/** Magnet get info message : ~ index */
	public static String MSG_MAGNET_INFO = "magnet_info";
	
	/** 
	 * Magnet set a point's message : ~ index x y z force
	 * @see ParticlesSystem.PointMagnet
	 */
	public static String MSG_MAGNET_POINT = "magnet_point";
	
	/** 
	 * Magnet set a line message : ~ index x1 y1 z1 x2 y2 z2 force
	 * @see ParticlesSystem.LineMagnet
	 */
	public static String MSG_MAGNET_LINE = "magnet_line";
	
	/** 
	 * Magnet set a plane message : ~ index x y z rotX rotY rotZ force
	 * @see ParticlesSystem.PlaneMagnet
	 */
	public static String MSG_MAGNET_PLANE = "magnet_plane";
	
	/** Magnet delete message : ~ index */
	public static String MSG_MAGNET_DEL = "magnet_delete";
	
	/** Magnet set force message : ~ index force */
	public static String MSG_MAGNET_FORCE = "magnet_force";	
	
	/** Magnet plane rotate message : ~ index rotX rotY rotZ */
	public static String MSG_MAGNET_PLANE_ROTATE = "magnet_plane_rotate";
	
	/** Magnet plane position message : ~ index x y z */
	public static String MSG_MAGNET_PLANE_POSITION = "magnet_plane_position";
	
	// Outlets for output and init matrix
	private static final int	OUTLET_MOUT		= 0;
	private static final int	OUTLET_MINIT	= 1;
	private static final int	OUTLET_MAGNET	= 2;
	private static final int	OUTLET_BLOB		= 3;
	
	/** Particles System use to manage the particles system simulation */
    protected ParticlesSystem particlesSystem;
	
	/** Magnets System use to manage the magnets system simulation */
    protected MagnetsSystem magnetsSystem;
	
	/** Output matrix : matrix of current particles positions */
    protected JitterMatrix outMatrix;
	
	/** Init matrix : matrix of initial particles positions */
	protected JitterMatrix initMatrix;
	
	/**
	 * Construct a max object for particles system simulation
	 * @param args Max Object's  arguments - not use but required
	 */
    public Particles(Atom[] args)
    {
		super(args);
		
        // 2 inputs - 4 outputs
        declareIO(2, 4);
        setInletAssist(0, "Bang and setting's messages");
		setInletAssist(1, "Matrix of initials particles positions");
        setOutletAssist(OUTLET_MOUT, "Matrix of current particles positions");
		setOutletAssist(OUTLET_MINIT, "Matrix of initial particles positions");
		setOutletAssist(OUTLET_MAGNET, "Output of current magnets' settings");
		setOutletAssist(OUTLET_BLOB, "Output of current blobs' settings");

		outMatrix			= new JitterMatrix(3, "float32", 0, 0);
		initMatrix			= new JitterMatrix(3, "float32", 0, 0);
		magnetsSystem		= new MagnetsSystem(this);
		particlesSystem		= new ParticlesSystem(this, magnetsSystem, blobsSystem);
    }
 
	/**
	 * Routine when a bang message occurs on first inlet.
	 * For each update, this method retrieve the fluidSolver attributes (if 
	 * applyFluidForce is enabled), update the particlesSystem and output the 
	 * particles positions matrix.
	 * @see ParticlesSystem.ParticlesSystem
	 */
    @Override
    protected void bang()
    {
        if(getInlet() == 0)
        {
			particlesSystem.update();
			
			if(particlesSystem.hasParticles())
				outlet(OUTLET_MOUT, MSG_MATRIX, outMatrix.getName());
        }
    }
	
	/**
	 * Routine when something other than a bang message occurs.
	 * Execute appropriates routines for known messages (on appropriate inlet).
	 * Call first Simulation.TreatMessage.
	 * Call Max.unknownMessage if the message is unknown.
	 * @param message Max's header message
	 * @param args Max's parameters message
	 */
    @Override
    protected void anything(String message, Atom[] args)
    {
		boolean unknownMessage = false;
		
		if(TreatMessage(message, args))
		{
			if(getInlet() == 0)
			{
				// Messages de paramétrage
				if(args.length == 0)
				{
					if(message.contentEquals(MSG_RESET))
					{
						particlesSystem.reset();

						if(particlesSystem.hasParticles())
							outlet(OUTLET_MINIT, MSG_MATRIX, initMatrix.getName());
					}

					else if(message.contentEquals(MSG_MAGNET_RESET))
						magnetsSystem.resetMagnets();

					else if(message.contentEquals(MSG_MAGNET_LIST))
						outlet(OUTLET_MAGNET, magnetsSystem.listIndexes());

					else
						unknownMessage = true;
				}
				
				// Messages de paramétrage
				else if(args.length == 1)
				{			
					if(message.contentEquals(MSG_STIFFNESS))
						particlesSystem.setStiffness(args[0].toDouble());

					else if(message.contentEquals(MSG_MOMENTUM))
						particlesSystem.setMomentum(args[0].toDouble());

					else if(message.contentEquals(MSG_FRICTION))
						particlesSystem.setFriction(args[0].toDouble());

					else if(message.contentEquals(MSG_MAGNET_APPLY))
						magnetsSystem.setEnable(args[0].toBoolean());

					else if(message.contentEquals(MSG_MEMORY))
						particlesSystem.setMemory(args[0].toInt());

					else if(message.contentEquals(MSG_MAXPART))
						particlesSystem.setMaxParticles(args[0].toInt());
					
					else if(message.contentEquals(MSG_MAGNET_DEL))
						magnetsSystem.deleteMagnet(args[0].toInt());
					
					else if(message.contentEquals(MSG_MAGNET_INFO))
						outlet(OUTLET_MAGNET, magnetsSystem.getInfo(args[0].toInt()));

					else
						unknownMessage = true;

				}

				else if(args.length == 2)
				{
					if(message.contentEquals(MSG_PARTSEUIL))
						particlesSystem.setThreshold(args[0].toDouble(), args[1].toDouble());

					else if(message.contentEquals(MSG_MAGNET_FORCE))
						magnetsSystem.setMagnetForce(args[0].toInt(), args[1].toFloat());

					else
						unknownMessage = true;
				}
				
				else if(args.length == 4)
				{
					if(message.contentEquals(MSG_MAGNET_PLANE_ROTATE))
						magnetsSystem.planeRotate(args[0].toInt(), args[1].toDouble(), args[2].toDouble(), args[3].toDouble());
					
					else if(message.contentEquals(MSG_MAGNET_PLANE_POSITION))
						magnetsSystem.planePosition(args[0].toInt(), args[1].toDouble(), args[2].toDouble(), args[3].toDouble());
					
					else
						unknownMessage = true;
				}
				
				else if(args.length == 5)
				{
					if(message.contentEquals(MSG_MAGNET_POINT))
						magnetsSystem.setPointMagnet(args[0].toInt(), new Vector3(args[1].toDouble(), args[2].toDouble(), args[3].toDouble()), args[4].toDouble());
					
					else
						unknownMessage = true;
				}
				
				else if(args.length == 6)
				{
					if(message.contentEquals(MSG_EDGES))
						particlesSystem.setEdges(args[0].toInt(), args[1].toInt(), args[2].toInt(), args[3].toInt(), args[4].toInt(), args[5].toInt());
					
					else
						unknownMessage = true;
				}
				
				else if(args.length == 8)
				{
					if(message.contentEquals(MSG_MAGNET_LINE))
						magnetsSystem.setLineMagnet(args[0].toInt(), new Line3(0., new Vector3(args[1].toDouble(), args[2].toDouble(), args[3].toDouble()), new Vector3(args[4].toDouble(), args[5].toDouble(), args[6].toDouble())), args[7].toDouble());
					
					else if(message.contentEquals(MSG_MAGNET_PLANE))
						magnetsSystem.setPlaneMagnet(args[0].toInt(), new Plane(args[1].toDouble(), args[2].toDouble(), args[3].toDouble(), args[4].toDouble(), args[5].toDouble(), args[6].toDouble()), args[7].toDouble());
					
					else
						unknownMessage = true;
				}

				else
					unknownMessage = true;
			}
			
			// Load init particles
			else if(getInlet() == 1)
			{
				if(args.length == 1 && message.contentEquals(MSG_MATRIX))
				{
					JitterMatrix jm = new JitterMatrix(args[0].toString());
					particlesSystem.loadParticles(jm);
				}
				
				else
					unknownMessage = true;
			}
		}
		
		if(unknownMessage)
			unknownMessage(message, args);
    }
    
	/**
	 * Define whatever to do when a blob occurs.
	 * In this case, blob forces and erase events are transmitted to the 
	 * particlesSystem.
	 * @param index Blob's index
	 * @param position Position on which blob occurs
	 */
	@Override
	protected void applyBlob(int index, Vector3 position)
	{
		Blob blob = blobsSystem.setPosition(index, scaleFrom.Scale(position));
		
		// On ajoute des particles
		if(blob != null && blob.getToAdd() > 0)
			particlesSystem.addParticles(blob.getPosition(), blob.getToAdd());
	}

	/**
	 * Routine when max object is deleted.
	 * Destroy the particlesSystem.
	 */
    @Override
    public void notifyDeleted()
    {
		particlesSystem.destroy();
		outMatrix.freePeer();
		initMatrix.freePeer();
    }

	/**
	 * Get the output matrix
	 * @return Current output matrix
     * @since 1.0
     */
	public JitterMatrix getOutMatrix() {
		return outMatrix;
	}
	
	/**
	 * Set a cell in output matrix
	 * @param i Index <code>i</code> on the matrix
	 * @param j Index <code>j</code> on the matrix
	 * @param value Value to set
	 */
	public void setOutMatrix(int i, int j, Vector3 value) {
		outMatrix.setcell2d(i, j, scaleTo.Scale(value.x, value.y, value.z));
	}
    
	/**
	 * Get the init matrix
	 * @return Current init matrix
     * @since 1.0
     */
	public JitterMatrix getInitMatrix() {
		return initMatrix;
	}

	/**
	 * Set a cell in init matrix
	 * @param i Index <code>i</code> on the matrix
	 * @param j Index <code>j</code> on the matrix
	 * @param value Value to set
	 */
	public void setInitMatrix(int i, int j, Vector3 value) {
		initMatrix.setcell2d(i, j, scaleTo.Scale(value.x, value.y, value.z));
	}
	
	/**
	 * Set the output matrix dimensions.
	 * If dimension are not strictly positive, nowhere matrix will be outputed.
	 * @param width New width of the output matrix
	 * @param height New height of the output matrix
	 */
	public void setOutMatrixDim(int width, int height)
	{
		if(width > 0 && height > 0)
		{
			outMatrix.setDim(new int[]{width, height});
			initMatrix.setDim(new int[]{width, height});
		}
		else
		{
			outMatrix.clear();
			initMatrix.clear();
			outMatrix.setDim(new int[]{0, 0});
			initMatrix.setDim(new int[]{0, 0});
			outlet(OUTLET_MOUT, MSG_MATRIX, nowhereMatrix.getName());
			outlet(OUTLET_MINIT, MSG_MATRIX, nowhereMatrix.getName());
		}
	}

	@Override
	protected void outBlob(String message) {
		outlet(OUTLET_BLOB, message);
	}
}
