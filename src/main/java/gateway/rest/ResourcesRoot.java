package gateway.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

//This is a configuration class for Jersey
//http://localhost:8080/sdp_project_red_war_exploded/
//Defines the base URI for all resource URIs.
@ApplicationPath("/")
//The java class declares root resource and provider classes
public class ResourcesRoot extends Application{
    //The method returns a non-empty collection with classes, that must be included in the published JAX-RS application
    @Override
    public Set<Class<?>> getClasses() {
        HashSet h = new HashSet<Class<?>>();
        h.add( NodeResource.class);
        h.add( MeasurementDataResource.class);
        return h;
    }
}