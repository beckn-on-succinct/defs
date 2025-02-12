package in.succinct.defs.db.model.did.subject;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.identifier.Did;

public interface Service extends Model , Did {
    @UNIQUE_KEY("K2")
    Long getSubjectId();
    void setSubjectId(Long id);
    Subject getSubject();
    
    
    
    String getEndPoint();
    void setEndPoint(String endPoint);
    // Then end point may be provided by a 3rd party controller.
    
    
    
    // End point provides apis as listed in the open api spec attached to the schema.
    @UNIQUE_KEY("K2")
    Long getSpecificationId();
    void setSpecificationId(Long id);
    Document getSpecification();
    // Service may be invoked
    
    //If any other subject or controller invokes an end point with its auth, a signed document may be produced which can be strored ad a cred of the calling subject.
    
}
