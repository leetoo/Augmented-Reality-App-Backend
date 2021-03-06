package controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import models.Fingerprint;
import models.APFingerprint;
import models.Session;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * FingerprintController.
 *
 *@desc
 * 	{"x": float, "y": float, "AccessPoints": [{"macAdress": "String", "intensity": int},{"macAdress": "String", "intensity": int},...] }
 *
 * @author: Jan Schmalfuß, Richard
 */
public class FingerprintController extends Controller {
	/**
	 * Send all objects as JSON. Get a list of all Fingerprints and convert them to JSON.
	 */
	public static Result indexJson() {
		return ok(Json.toJson(Fingerprint.all()));
	}

	/**
	 * Create a new Fingerprint Object. Expect macAdress and ssid.
	 */
	public static Result createJson() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			
    		// Authenticate.
    		String token = request().getHeader("token");
    		Session session = Session.authenticate(token);
	        if(session == null) {
	        	return unauthorized("Unauthorized");
	        }
	        
	        Double xD = json.findPath("x").getDoubleValue(); // No getFloatValue, getNumberValue doesnt work either.
	        Double yD = json.findPath("y").getDoubleValue();
	    	Float x = new Float(xD); // Use Java datatype for Nullcheck below.
	    	Float y = new Float(yD);
			Iterator<JsonNode> it = json.findPath("AccessPoints").getElements();
			if (x == null || y == null || it == null) {
				return badRequest("Missing parameter! x: " + x + " y: " + y + " Wifi: " + it);
			} else {
				Fingerprint fingerprint = new Fingerprint(x, y);
					while(it.hasNext()) {
						JsonNode j = it.next();
						String macAddress = j.findPath("macAddress").getTextValue();
						Integer intensity = j.findPath("intensity").getIntValue();
						if (macAddress == null || intensity == null)
							return badRequest("Missing parameter! MacAddress: " + macAddress + " Intensity: " + intensity);
						fingerprint.accesspoints.add(new APFingerprint(macAddress,intensity));
					}				
				Fingerprint.create(fingerprint);
				
				return ok(Json.toJson(fingerprint));
			}
		}
	}

	/**
	 * Get.
	 */
	public static Result getJson(Long id) {
		Fingerprint fingerprint = Fingerprint.get(id);
		if (fingerprint == null) {
			return badRequest("Fingerprint " + id + " missing.");
		} else {
			return ok(Json.toJson(fingerprint));
		}
	}

	/**
	 * Update Fingerprint
	 */
	public static Result updateJson(Long id) {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			
    		// Authenticate.
    		String token = request().getHeader("token");
    		Session session = Session.authenticate(token);
	        if(session == null) {
	        	return unauthorized("Unauthorized");
	        }
	        
	        Double xD = json.findPath("x").getDoubleValue(); // No getFloatValue, getNumberValue doesnt work either.
	        Double yD = json.findPath("y").getDoubleValue();
    		Float x = new Float(xD); // Use Java datatype for Nullcheck below.
    		Float y = new Float(yD);
			Iterator<JsonNode> it = json.findPath("AccessPoints").getElements();
			
			List<APFingerprint> aps = new ArrayList<APFingerprint>();
			while(it.hasNext()) {
				JsonNode j = it.next();
				String macAdress = j.findPath("macAddress").getTextValue();
				int intensity = j.findPath("intensity").getIntValue();
				aps.add(new APFingerprint(macAdress,intensity));
			}			
			
			if (x == null || y == null || it == null) {
				return badRequest("Missing parameter!");
			} else {
				Fingerprint fingerprint = Fingerprint.get(id);
				Fingerprint updatedFingerprint = Fingerprint.update(
						fingerprint, x, y, aps);
				return ok(Json.toJson(updatedFingerprint));
			}
		}
	}

	public static Result destroyJson(Long id) {
		// Authenticate.
		String token = request().getHeader("token");
		Session session = Session.authenticate(token);
		if(session == null) {
			return unauthorized("Unauthorized");
		}
		
		Fingerprint.delete(id);
		return ok();
	}

	public static Result destroyAllJson() {
		// Authenticate.
		String token = request().getHeader("token");
		Session session = Session.authenticate(token);
		if(session == null) {
			return unauthorized("Unauthorized");
		}
		
		Fingerprint.deleteAll();
		return ok();
	}
}