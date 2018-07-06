package ray.renderer;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import ray.brdf.BRDF;
import ray.material.Material;
import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.math.Frame3;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;
import ray.math.Point3;
import ray.light.PointLight;

public abstract class PathTracer extends DirectOnlyRenderer {

    protected int depthLimit = 5;
    protected int backgroundIllumination = 1;

    public void setDepthLimit(int depthLimit) { this.depthLimit = depthLimit; }
    public void setBackgroundIllumination(int backgroundIllumination) { this.backgroundIllumination = backgroundIllumination; }

    @Override
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, Color outColor) {
    
        rayRadianceRecursive(scene, ray, sampler, sampleIndex, 0, outColor);
    }

    protected abstract void rayRadianceRecursive(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, int level, Color outColor);

    public void gatherIllumination(Scene scene, Vector3 outDir, 
            IntersectionRecord iRec, SampleGenerator sampler, 
            int sampleIndex, int level, Color outColor) {
    	// W4160 TODO (B)
    	//
        // This method computes a Monte Carlo estimate of reflected radiance due to direct and/or indirect 
        // illumination.  It generates samples uniformly wrt. the projected solid angle measure:
        //
        //    f = brdf * radiance
        //    p = 1 / pi
        //    g = f / p = brdf * radiance * pi
    	// You need: 
    	//   1. Generate a random incident direction according to proj solid angle
    	//      pdf is constant 1/pi
    	//   2. Recursively find incident radiance from that direction
    	//   3. Estimate the reflected radiance: brdf * radiance / pdf = pi * brdf * radiance
    	//
    	// Here you need to use Geometry.squareToPSAHemisphere that you implemented earlier in this function
   		

		//1) dealing with direct light   
		// setting up usefull variables   
    	Material mat = iRec.surface.getMaterial();
    	BRDF materialBRDF = mat.getBRDF(iRec);
    	Frame3 frame = iRec.frame;   	
    	ArrayList<PointLight> lights = scene.getPointLights();
   		Iterator<PointLight> iterLight = lights.iterator();  		
   		Point3 P = frame.o;   		
        Color ambiantColor	= new Color();
	   	scene.getBackground().evaluate(outDir,ambiantColor);	   	
	   	Point2 seed = new Point2(1.0,2.0);
	   	sampler.sample(sampleIndex,level,seed);
    	Color outWeight = new Color();
    	Vector3 bDir = new Vector3();
    	materialBRDF.generate(frame,outDir,bDir,seed,outWeight);
    	bDir.normalize();
    	Color materialColor	= new Color();
    	materialBRDF.evaluate( frame, bDir , outDir, materialColor );
    	double red = ambiantColor.r*materialColor.r;
    	double green = ambiantColor.g*materialColor.g;
    	double blue = ambiantColor.b*materialColor.b;    	        	

    	// iterating through present light
		// possibly multiple lights present

   		while(iterLight.hasNext()){
   			PointLight light = iterLight.next();
   			Color lightDiff = light.diffuse;
   
       		//calculating light vector components
			double xc = light.location.x-P.x;
			double yc = light.location.y-P.y;
			double zc = light.location.z-P.z;
   			Vector3 L = new Vector3(xc,yc,zc);
   			L.normalize();
   			
			IntersectionRecord iRecBounce = new IntersectionRecord();
   			Ray rBounce = new Ray(P,L);
   			rBounce.makeOffsetRay();
   			if(scene.getAnyIntersection(iRecBounce,rBounce)){
   				continue;
   			}  						
    		double materialPDF = materialBRDF.pdf( frame , L , outDir );		
    		red += lightDiff.r * materialPDF;
   			green += lightDiff.g * materialPDF;
   			blue += lightDiff.b * materialPDF;   			
   		}


        //2) we deal with indirect light  		
   		Color outRad = new Color(0.0,1.0,0.0);
   		scene.incidentRadiance( P, bDir, outRad);
    	double materialPDF = Math.PI * materialBRDF.pdf(frame,bDir,outDir);		
		IntersectionRecord iRecBounce = new IntersectionRecord();
   		Ray rBounce = new Ray(P,bDir);
   		rBounce.makeOffsetRay();
   		rayRadianceRecursive(scene,rBounce,sampler,sampleIndex,++level,outColor);

		//ouput color calculation
   		double newRed = outWeight.r*red/Math.PI + outRad.r*materialColor.r*materialPDF;
		double newGreen = outWeight.g*green/Math.PI + outRad.g*materialColor.g*materialPDF;
		double newBlue = outWeight.b*blue/Math.PI + outRad.b*materialColor.b*materialPDF;
		Color gathered = new Color(newRed,newGreen,newBlue);    					
    	outColor.add(gathered);
    	
    }
}