package ray.renderer;

import ray.material.Material;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.LuminaireSamplingRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;
/**
 * A renderer that computes radiance due to emitted and directly reflected light only.
 * 
 * @author cxz (at Columbia)
 */
public class DirectOnlyRenderer implements Renderer {
    
    /**
     * This is the object that is responsible for computing direct illumination.
     */
    DirectIlluminator direct = null;
    Point2 seed = new Point2();
	Vector3 L = new Vector3();
	Vector3 R = new Vector3();
	Color emittedRadiance = new Color();
	Color directRadiance = new Color();
	IntersectionRecord iRec = new IntersectionRecord();
	LuminaireSamplingRecord lRec = new LuminaireSamplingRecord();
    /**
     * The default is to compute using uninformed sampling wrt. projected solid angle over the hemisphere.
     */
    public DirectOnlyRenderer() {
        this.direct = new ProjSolidAngleIlluminator();
    }
    
    
    /**
     * This allows the rendering algorithm to be selected from the input file by substituting an instance
     * of a different class of DirectIlluminator.
     * @param direct  the object that will be used to compute direct illumination
     */
    public void setDirectIlluminator(DirectIlluminator direct) {
        this.direct = direct;
    }

    
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, Color outColor) {
        // W4160 TODO (A)
    	// In this function, you need to implement your direct illumination rendering algorithm
    	//
    	// you need:
    	// 1) compute the emitted light radiance from the current surface if the surface is a light surface
    	// 2) direct reflected radiance from other lights. This is by implementing the function
    	//    ProjSolidAngleIlluminator.directIlluminaiton(...), and call direct.directIllumination(...) in this
    	//    function here.
    

        if (scene.getFirstIntersection(iRec, ray)) {
			Vector3 out_direction = ray.direction;                
            emittedRadiance(iRec, out_direction, emittedRadiance);
			sampler.sample(3, sampleIndex, seed);
            outColor.set(emittedRadiance);
			outColor.add(directRadiance); 
			direct.directIllumination(scene, L, R, iRec, seed, directRadiance);
		} else {
			scene.getBackground().evaluate(ray.direction, outColor);
		}

    }

    
    /**
     * Compute the radiance emitted by a surface.
     * @param iRec      Information about the surface point being shaded
     * @param dir          The exitant direction (surface coordinates)
     * @param outColor  The emitted radiance is written to this color
     */
    protected void emittedRadiance(IntersectionRecord iRec, Vector3 dir, Color outColor) {
    	// W4160 TODO (A)
        // If material is emitting, query it for emission in the relevant direction.
        // If not, the emission is zero.
    	// This function should be called in the rayRadiance(...) method above
        Material mtr = iRec.surface.getMaterial();

		if (mtr.isEmitter()) {
			lRec.set(iRec);
			lRec.emitDir.set(dir);
			lRec.emitDir.scale(-1);
			iRec.surface.getMaterial().emittedRadiance(lRec, outColor);
		} else {
			/* emitted radiance is zero if the material is not an emitter */
			outColor.set(0.0);
		}
    }
}
