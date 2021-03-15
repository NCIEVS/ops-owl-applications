import java.io.IOException;
import java.util.List;

import org.protege.editor.owl.server.versioning.ChangeHistoryImpl;
import org.protege.editor.owl.server.versioning.ChangeHistoryUtils;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.semanticweb.binaryowl.BinaryOWLMetadata;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class RepairHistory {

	public static void main(String[] args) {
		
		try {
			HistoryFile bad = HistoryFile.createNew(args[0]);
			
			HistoryFile repaired = HistoryFile.createNew(args[1]);
			
			
			System.out.println(bad.toPath());
			
			ChangeHistory badhist = ChangeHistoryUtils.readChanges(bad);
			
			System.out.println(badhist.getBaseRevision());
			
			ChangeHistory goodhist = ChangeHistoryImpl.createEmptyChangeHistory();
			
			
			DocumentRevision base = badhist.getBaseRevision();
            DocumentRevision head = badhist.getHeadRevision();
            for (DocumentRevision current = base.next(); current.behindOrSameAs(head); current = current.next()) {
                List<OWLOntologyChange> changeSet = badhist.getChangesForRevision(current);
                RevisionMetadata metadata = badhist.getMetadataForRevision(current);
                BinaryOWLMetadata changeMetadata = ChangeHistoryUtils.getBinaryOWLMetadata(metadata);
                
                goodhist.addRevision(metadata, changeSet);
            }
            
            System.out.println(goodhist.getBaseRevision());
            
            ChangeHistoryUtils.appendChanges(goodhist, repaired);
            
            System.out.println("done");
            
            
            
            
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

}
