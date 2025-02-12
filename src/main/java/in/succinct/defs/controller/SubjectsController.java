package in.succinct.defs.controller;

import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.documents.Attestation;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

import java.util.List;
import java.util.Map;

public class SubjectsController extends AbstractDirectoryController<Subject> {
    
    public SubjectsController(Path path) {
        super(path);
    }
    
    
    
    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map =  super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        
        List<String> excluded = ModelReflector.instance(Subject.class).getFields();
        excluded.remove("DID");
        addToIncludedModelFieldsMap(map,Subject.class,excluded);
        
        excluded = ModelReflector.instance(Attestation.class).getFields();
        excluded.removeAll(List.of("DID","VERIFIED"));
        addToIncludedModelFieldsMap(map, Attestation.class, excluded);
        
        excluded = ModelReflector.instance(Service.class).getFields();
        excluded.remove("DID");
        addToIncludedModelFieldsMap(map, Service.class, excluded);
        
        
        excluded = ModelReflector.instance(Document.class).getFields();
        excluded.remove("DID");
        addToIncludedModelFieldsMap(map, Document.class, excluded);
        
        excluded = ModelReflector.instance(VerificationMethod.class).getFields();
        excluded.removeAll(List.of("DID","VERIFIED"));
        addToIncludedModelFieldsMap(map, VerificationMethod.class, excluded);
        
        return map;
        
    }
    

}
