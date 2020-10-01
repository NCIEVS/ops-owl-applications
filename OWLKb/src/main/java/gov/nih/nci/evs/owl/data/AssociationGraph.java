package gov.nih.nci.evs.owl.data;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.io.RDFNode;
import org.semanticweb.owlapi.io.RDFResource;
import org.semanticweb.owlapi.io.RDFTriple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.rdf.model.RDFGraph;

public class AssociationGraph extends RDFGraph {
	
	@Nonnull private final Map<IRI, Set<RDFTriple>> triplesByObject = new HashMap<>();
	
    public void addTriple(@Nonnull RDFTriple triple) {
        super.addTriple(triple);
        

        //check to see if there are any other triples for these objects.
        //If there are, append to that collection
        //If not, do a put.
        Set<RDFTriple> tripleSet = triplesByObject.get(triple.getObject().getIRI());
        if (tripleSet == null) {
            tripleSet = new LinkedHashSet<>();
            triplesByObject.put(triple.getObject().getIRI(), tripleSet);
        } 
        tripleSet.add(triple);
    }
    
    public Collection<RDFTriple> getTriplesForObject(RDFNode object) {
        Set<RDFTriple> set = triplesByObject.get(object);
        if (set == null) {
//            // check if the node is remapped
//            RDFNode rdfNode = remappedNodes.get(subject);
//            if (rdfNode == null) {
                return Collections.emptyList();
//            }
//            // else return the triples for the remapped node
//            return getTriplesForSubject(rdfNode);
        }
        return set;
    }
    
    public Collection<RDFTriple> getTriplesForObject(IRI object) {
        Set<RDFTriple> set = triplesByObject.get(object);
        if (set == null) {
                return Collections.emptyList();
        }
        return set;
    }

}
