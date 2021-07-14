/**
 * National Cancer Institute Center for Bioinformatics
 * <p>
 * OWLKb
 * gov.nih.nci.evs.owl.entity
 * RootConcept.java
 * May 5, 2009
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Association;
import gov.nih.nci.evs.owl.entity.Concept;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Relationship;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author safrant
 */
public class RootConcept extends Concept {

    /** The associations. */
    private final HashMap<URI, Integer> associations = new HashMap<URI, Integer>();
    private final Vector<URI> definedDescendants = new Vector<URI>();
    private final Vector<URI> primitiveDescendants = new Vector<URI>();
    /** The props. */
    private final HashMap<URI, Integer> props = new HashMap<URI, Integer>();
    /** The roles. */
    private final HashMap<URI, Integer> roles = new HashMap<URI, Integer>();
    /** The disjoints. */
    public Vector<String> disjoints;
    private Vector<URI> descendantMap = new Vector<URI>();
    private Integer descendantSize = 0;
    private Integer definedDescendantsSize = 0;
    private Integer primitiveDescedantsSize = 0;


    /**
     * @param rootCode
     * @param api
     */
    public RootConcept(URI rootCode, String name, OWLKb api) {
        super(rootCode, name, api);
        loadDescendantMap();
    }

    private void loadDescendantMap() {
        descendantMap = getAllDescendantCodes();
        descendantSize = descendantMap.size();
        loadDefinedDescendants();
    }

    /**
     * Gets the defined descendants.
     *
     * @return the defined descendants
     */
    private void loadDefinedDescendants() {
        // loop through and build

        for (URI key : descendantMap) {
            ConceptProxy cls = api.getConcept(key);
            if (cls.isDefined()) {
                definedDescendants.add(key);
            } else {
                primitiveDescendants.add(key);
            }
        }
        definedDescendantsSize = definedDescendants.size();
        primitiveDescedantsSize = primitiveDescendants.size();
    }

    public RootConcept(URI rootCode, OWLKb api) {
        super(rootCode, api);
        try {
            loadDescendantMap();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public Integer getDefinedDescendantSize() {
        return definedDescendantsSize;
    }

    public Vector<URI> getDefinedDescendants() {
//		if (definedDescendants != null && definedDescendants.size() > 0)
//			return definedDescendants;
//		loadDefinedDescendants();
        return definedDescendants;
    }

    public HashMap<URI, Integer> getDescendantAssociationsCount() {
        if (associations.size() > 0) {
            return associations;
        }
        loadDescendantAssociations();
        return associations;
    }

    private void loadDescendantAssociations() {
        // build role collection here.
        try {
            for (URI key : descendantMap) {
                // String cls = descendantMap.get(key);
                Concept concept = new Concept(key, this.getNamespace(), api);
                Vector<Association> conceptAssociations = concept
                        .getAssociations();
                for (Relationship assoc : conceptAssociations) {
                    URI assocCode = assoc.getRelation().getCode();
                    if (associations.containsKey(assocCode)) {
                        Integer count = associations.get(assocCode);
                        count++;
                        associations.put(assocCode, count);
                    } else {
                        associations.put(assocCode, 1);
                    }
                }

            }
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the descendant map.
     *
     * @return the descendant map
     */
    public final Vector<URI> getDescendantMap() {
        if (descendantMap != null && descendantMap.size() > 0) {
            return descendantMap;
        }
        loadDescendantMap();
        return descendantMap;
    }

    public Integer getDescendantMapSize() {
        return descendantSize;
    }

    public HashMap<URI, Integer> getDescendantPropertiesCount() {
        if (props.size() > 0) {
            return props;
        }
        loadDescendantProperties();
        return props;
    }

    /**
     * Load descendant roles and properties.
     */
    private void loadDescendantProperties() {
        // System.out.println("    Loading Descendant properties");
        // Set<String> keySet = descendantMap.keySet();
        try {
            for (URI key : descendantMap) {
                // String cls = descendantMap.get(key);
                Concept concept = new Concept(key, this.getNamespace(), api);
                Vector<Property> conceptProperties = concept.getProperties();
                for (Property prop : conceptProperties) {
                    URI propCode = prop.getURI();
                    if (props.containsKey(propCode)) {
                        Integer count = props.get(propCode);
                        count++;
                        props.put(propCode, count);
                    } else {
                        props.put(propCode, 1);
                    }
                }

            }

        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    public HashMap<URI, Integer> getDescendantRolesCount() {
        if (roles.size() > 0) {
            return roles;
        }
        loadDescendantRoles();
        return roles;
    }

    private void loadDescendantRoles() {
        // build role collection here.
        try {
            for (URI key : descendantMap) {
                // String cls = descendantMap.get(key);
                Concept concept = new Concept(key, this.getNamespace(), api);
                Vector<Role> conceptRoles = concept.getRoles();

                for (Relationship role : conceptRoles) {
                    URI roleCode = role.getRelation().getCode();
                    if (roles.containsKey(roleCode)) {
                        Integer count = roles.get(roleCode);
                        count++;
                        roles.put(roleCode, count);
                    } else {
                        roles.put(roleCode, 1);
                    }
                }
                Vector<Role> equivalentClasses = concept
                        .getEquivalentClasses();
                for (Relationship eq : equivalentClasses) {
                    URI eqCode = eq.getRelation().getCode();
                    if (roles.containsKey(eqCode)) {
                        Integer count = roles.get(eqCode);
                        count++;
                        roles.put(eqCode, count);
                    } else {
                        roles.put(eqCode, 1);
                    }
                }

            }

        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public Vector<URI> getPrimitiveDescendants() {
//		//If primitive descendants populated, then return int
//		if (primitiveDescendants != null && primitiveDescendants.size() > 0)
//			return primitiveDescendants;
//		//If it is not populated but defined is, then that means there are no primitive.
//		if(definedDescendants !=null && definedDescendants.size() >0)
//		return primitiveDescendants;
//		loadDefinedDescendants();
        return primitiveDescendants;

    }

    public Integer getPrimitiveDescendantsSize() {
        return primitiveDescedantsSize;
    }

    public boolean isDescendant(URI conceptCode) {
        return descendantMap.contains(conceptCode);
//		if(descendantMap.contains(conceptCode)) {
//			return true;
//		}
//		return false;
    }

    /**
     * Gets the primitive descendants.
     *
     * @return the primitive descendants
     */
    private void loadPrimitiveDescendants() {
        // loop through and build

        // Set<String> keySet = descendantMap.keySet();
        for (URI key : descendantMap) {
            ConceptProxy cls = api.getConcept(key);
            if (!cls.isDefined()) {
                primitiveDescendants.add(key);
            }
        }
    }
}
