package in.succinct.defs.extensions;

import com.venky.core.util.Bucket;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.ModelOperationExtension;
import com.venky.swf.exceptions.AccessDeniedException;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;
import in.succinct.defs.db.model.did.subject.VerificationMethod.Purpose;

public class SubjectExtension extends ModelOperationExtension<Subject> {
    static  {
        registerExtension(new SubjectExtension());
    }
    
    @Override
    protected void beforeValidate(Subject instance) {
        super.beforeValidate(instance);
        if (instance.getModCount() == null){
            instance.setModCount(new Bucket(0));
        }
        if (ObjectUtil.isVoid(instance.getDid())){
            
            instance.setDid(String.format("/subjects/%s".formatted(instance.getName())));
            Subject persisted = Database.getTable(Subject.class).getRefreshed(instance);
            instance.getRawRecord().load(persisted.getRawRecord());
            instance.getRawRecord().setNewRecord(persisted.getRawRecord().isNewRecord());
        }
        ensureControllerAccess(instance);
    
    }
    
    private void ensureControllerAccess(Subject instance) {
        Subject signer = Database.getInstance().getContext(Subject.class.getName());
        
        boolean beingCreated = instance.getRawRecord().isNewRecord();
        
        if (signer == null){
            if (beingCreated) {
                if (instance.getRawRecord().isFieldDirty("CONTROLLER_ID")) {
                    throw new AccessDeniedException(" Unsigned request");
                }
            }
            if (!isPublic(instance)){
                throw new AccessDeniedException("Unsigned request");
            }
            return;
        }
        //Signer is not null;
        
        if (beingCreated){
            if (instance.getControllerId() == null) {
                instance.setControllerId(signer.getId());
            }else if (!ObjectUtil.equals(instance.getControllerId(),signer.getId())){
                throw new UnsupportedOperationException("Controller other than signer!");
            }
        }else {
            if (instance.getRawRecord().isFieldDirty("CONTROLLER_ID")){
                Object oldControllerId = instance.getRawRecord().getOldValue("CONTROLLER_ID");
                
                if (oldControllerId != null ){
                    if (!ObjectUtil.equals(oldControllerId,signer.getId())) {
                        throw new AccessDeniedException("Controller other than signer");
                    }
                    
                    if (instance.getControllerId() != null && !isVerifiedController(getController(instance))){
                        throw new UnsupportedOperationException("Subject cannot be transferred to an unverified controller");
                    }
                }
                
            }else {
                if (signer.getId() != getController(instance).getId()){
                    throw new UnsupportedOperationException("Signer not controller");
                }
            }
        
        }
        
    }
    
    public Subject getController(Subject instance){
        Subject controller = instance;
        if (instance.getControllerId() != null){
            controller =instance.getController();
            if (controller == null){
                throw new RuntimeException("Invalid controller");
            }
        }
        return controller;
    }
    private boolean isPublic(Subject instance){
        return !isVerifiedController(getController(instance));
    }
    
    @SuppressWarnings("all")
    private boolean isVerifiedController(Subject controller){
        if (controller == null){
            return false;
        }
        boolean controlledVerified = false;
        for (VerificationMethod vm : controller.getVerificationMethods()){
            if (vm.isVerified() && VerificationMethod.Purpose.valueOf(vm.getPurpose()) == Purpose.Assertion && VerificationMethod.PublicKeyType.valueOf(vm.getType()) == PublicKeyType.Ed25519){
                controlledVerified = true;
                break;
            }
        }
        return controlledVerified;
    }
    
    @Override
    protected void afterSave(Subject instance) {
        super.afterSave(instance);
        incrementModCount(instance);
    }
    
    @Override
    protected void beforeDestroy(Subject instance) {
        super.beforeDestroy(instance);
        ensureControllerAccess(instance);
    }
    
    @Override
    protected void afterDestroy(Subject instance) {
        super.afterDestroy(instance);
        incrementModCount(instance);
    }
    private void incrementModCount(Subject instance){
        if (instance.getControllerId() != null && !ObjectUtil.equals(instance.getControllerId(),instance.getId()) ){
            Subject controller = instance.getController();
            controller.getModCount().increment();
            controller.save();
        }
    }
}
