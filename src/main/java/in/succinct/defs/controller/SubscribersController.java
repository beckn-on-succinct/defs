package in.succinct.defs.controller;

import com.venky.core.date.DateUtils;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.Controller;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.db.model.CryptoKey;
import com.venky.swf.integration.FormatHelper;
import com.venky.swf.integration.IntegrationAdaptor;
import com.venky.swf.path.Path;
import com.venky.swf.pm.DataSecurityFilter;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.beckn.Request;
import in.succinct.beckn.Subscriber;
import in.succinct.beckn.Subscribers;
import in.succinct.defs.db.model.did.documents.Attestation;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class SubscribersController extends Controller {
    public SubscribersController(Path path) {
        super(path);
    }


    @RequireLogin(false)
    public View lookup() throws Exception{
        
        Subscriber subscriber = new Subscriber((JSONObject) Subscriber.parse(getPath().getInputStream()));
        
        String format = getPath().getHeaders().get("pub_key_format");
        if (!ObjectUtil.isVoid(format)){
            if (!ObjectUtil.equals("PEM",format.toUpperCase())){
                throw new RuntimeException("Only allowed value to be passed is PEM");
            }
        }
        Subscribers records = lookup(subscriber,0,s->{
            if (!ObjectUtil.isVoid(format)){
                s.setSigningPublicKey(Request.getPemSigningKey(s.getSigningPublicKey()));
                s.setEncrPublicKey(Request.getPemEncryptionKey(s.getEncrPublicKey()));
            }
        });


        return new BytesView(getPath(),records.getInner().toString().getBytes(),MimeType.APPLICATION_JSON);

    }

    @RequireLogin(false)
    public View generateSignatureKeys(){
        CryptoKey key = Database.getTable(CryptoKey.class).newRecord();

        String[] pair = CryptoKey.generateKeyPair(Request.SIGNATURE_ALGO,Request.SIGNATURE_ALGO_KEY_LENGTH);
        key.setPrivateKey(pair[0]);
        key.setPublicKey(pair[1]);
        return IntegrationAdaptor.instance(CryptoKey.class,FormatHelper.getFormatClass(MimeType.APPLICATION_JSON)).
                createResponse(getPath(),key, Arrays.asList("PUBLIC_KEY","PRIVATE_KEY"));

    }


    @RequireLogin(false)
    public View generateEncryptionKeys(){
        CryptoKey key = Database.getTable(CryptoKey.class).newRecord();

        String[] pair = CryptoKey.generateKeyPair(Request.ENCRYPTION_ALGO,Request.ENCRYPTION_ALGO_KEY_LENGTH);
        key.setPrivateKey(pair[0]);
        key.setPublicKey(pair[1]);
        return IntegrationAdaptor.instance(CryptoKey.class,FormatHelper.getFormatClass(MimeType.APPLICATION_JSON)).
                createResponse(getPath(),key, Arrays.asList("PUBLIC_KEY","PRIVATE_KEY"));

    }


    public static Subscribers lookup(Subscriber criteria, int maxRecords, KeyFormatFixer fixer) {
        Document specification = null;
        if (!ObjectUtil.isVoid(criteria.getType())){
            specification = Database.getTable(Document.class).newRecord();
            specification.setDid("/subjects/org/fide.org/documents/%s.yaml".formatted(criteria.getType()));
            specification = Database.getTable(Document.class).getRefreshed(specification);
            if (specification.getRawRecord().isNewRecord()){
                return new Subscribers();
            }
        }
        if (!ObjectUtil.isVoid(criteria.getPubKeyId())){
            VerificationMethod key = null ;
            key = Database.getTable(VerificationMethod.class).newRecord();
            key.setDid(criteria.getPubKeyId());
            key = Database.getTable(VerificationMethod.class).getRefreshed(key);
            if (key.getRawRecord().isNewRecord() || !key.isVerified()) {
                return new Subscribers();
            }else {
                Subscribers subscribers  = new Subscribers();
                subscribers.add(getSubjectMeta(null,key,specification,fixer));
                return subscribers;
            }
        }
        if (!ObjectUtil.isVoid(criteria.getSubscriberId())){
            Subject subject = null;
            subject = Database.getTable(Subject.class).newRecord();
            subject.setDid(criteria.getSubscriberId());
            subject = Database.getTable(Subject.class).getRefreshed(subject);
            if ( subject.getRawRecord().isNewRecord()){
                return new Subscribers();
            }else {
                Subscribers subscribers  = new Subscribers();
                subscribers.add(getSubjectMeta(subject,null,specification,fixer));
                return subscribers;
            }
        }
        
        return getSubjectMeta(null,null,specification,fixer);
    }
    public interface KeyFormatFixer {
        public void fix(Subscriber subscriber);
    }
    static Subscribers getSubjectMeta(Subject subject, VerificationMethod verificationMethod , Document spec , KeyFormatFixer fixer) {
        Subscribers subscribers = new Subscribers();
        List<Subject> subjects = new ArrayList<>();
        if (verificationMethod != null){
            Subject controller = verificationMethod.getController();
            subjects.add(controller);
            subjects.addAll(controller.getControlledSubjects());
        }else if (subject != null){
            subjects.add(subject);
        }else if (spec != null){
            
            Select select  = new Select().from(Service.class);
            select.where(new Expression(select.getPool(),"SPECIFICATION_ID",Operator.EQ,spec.getId()));
            List<Service> services  = select.execute();
            Set<Long> subjectIds = new HashSet<>();
            for (Service service : services) {
                subjectIds.add(service.getSubjectId());
            }
            Select attestationSelect = new Select().from(Attestation.class);
            attestationSelect.where(new Expression(attestationSelect.getPool(),Conjunction.AND).
                    add(new Expression(attestationSelect.getPool(),"DOCUMENT_ID",Operator.EQ,spec.getId())).
                    add(new Expression(attestationSelect.getPool(),"VERIFIED",Operator.EQ,true)));
            
            Set<Long> subjectsWithAttestations = new HashSet<>();
            for (Attestation attestation : attestationSelect.execute(Attestation.class)) {
                VerificationMethod m = attestation.getVerificationMethod();
                subjectsWithAttestations.add(m.getControllerId());
                subjectsWithAttestations.addAll(DataSecurityFilter.getIds(m.getController().getControlledSubjects()));
            }
            subjectIds.retainAll(subjectsWithAttestations);
            
            Select subjectSelect = new Select().from(Subject.class);
            subjectSelect.where(new Expression(subjectSelect.getPool(),"ID",Operator.IN,subjectIds.toArray()));
            subjects.addAll(subjectSelect.execute(Subject.class));
        }
        for (Subject s : subjects) {
            for (Subscriber subscriber  : getSubjectMeta(s,verificationMethod,spec)){
                fixer.fix(subscriber);
                subscribers.add(subscriber);
            }
        }
        
        return subscribers;
    }
    private static Subscribers getSubjectMeta(Subject subject, VerificationMethod verificationMethod, Document spec) {
        List<Service> services  = subject.getServices();
        List<VerificationMethod> methods = new ArrayList<>();
        if (spec != null) {
            services.removeIf(service -> !ObjectUtil.equals(service.getSpecificationId(), spec.getId()));
        }
        if (verificationMethod != null){
            methods.add(verificationMethod);
        }else {
            if (subject.getControllerId() == null) {
                methods.addAll(subject.getVerificationMethods());
            }else {
                methods.addAll(subject.getController().getVerificationMethods());
            }
        }
        Subscribers subscribers = new Subscribers();
        for (Service service : services){
            for (VerificationMethod method : methods){
                subscribers.add(getSubscriber(subject,method,service));
            }
        }
        return subscribers;
    }
    @NotNull
    private static Subscriber getSubscriber(Subject subject, VerificationMethod verificationMethod, Service service) {
        Subscriber subscriber = new Subscriber();

        subscriber.setSubscriberId(subject.getDid());
        subscriber.setSubscriberUrl(service.getEndPoint());
        subscriber.setStatus(Subscriber.SUBSCRIBER_STATUS_SUBSCRIBED);
        subscriber.setType(service.getSpecification().getName());
        
        subscriber.setSigningPublicKey(verificationMethod.getPublicKey());

        subscriber.setPubKeyId(verificationMethod.getDid());
        subscriber.setValidFrom(verificationMethod.getCreatedAt()) ;
        subscriber.setValidTo(DateUtils.HIGH_DATE);
        subscriber.setCreated(subject.getCreatedAt());
        subscriber.setUpdated(subject.getUpdatedAt());
        return subscriber;
    }
}
