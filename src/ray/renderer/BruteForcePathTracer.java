package ray.renderer;

import ray.brdf.BRDF;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;
import ray.material.Material;
import ray.misc.LuminaireSamplingRecord;

public class BruteForcePathTracer extends PathTracer {
    /**
     * @param scene
     * @param ray
     * @param sampler
     * @param sampleIndex
     * @param outColor
     */
    protected void rayRadianceRecursive(Scene scene, Ray ray, 
            SampleGenerator sampler, int sampleIndex, int level, Color outColor) {
    	// W4160 TODO (B)
    	//
        // Find the visible surface along the ray, then add emitted and reflected radiance
        // to get the resulting color.
    	//
    	// If the ray depth is less than the limit (depthLimit), you need
    	// 1) compute the emitted light radiance from the current surface if the surface is a light surface
    	// 2) reflected radiance from other lights and objects. You need recursively compute the radiance
    	//    hint: You need to call gatherIllumination(...) method.

        if(level == 0)
			outColor.set(0.0);
			
		boolean legal = (level < depthLimit ) ? true : false;
		IntersectionRecord iRec = new IntersectionRecord();
        Vector3 outdir = ray.direction;	
        if(legal){
			if(scene.getFirstIntersection(iRec,ray)){	
        	    Material mat = iRec.surface.getMaterial();
        	    Vector3 outDir = new Vector3(1.0,1.0,1.0);      	
        	    if( mat.isEmitter() ){
				    Color emitColor = new Color(0.0,0.0,0.0);
				    mat.emittedRadiance(null,emitColor);
				    outColor.add(emitColor);			
        	    }        	
    		    gatherIllumination(scene, outdir, iRec,sampler,sampleIndex,level,outColor);  		
    		    return;        	
            }
		}
    	scene.getBackground().evaluate(outdir,outColor);   	
    }
}